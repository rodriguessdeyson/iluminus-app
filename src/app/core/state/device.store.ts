import { Injectable, computed, inject, signal } from '@angular/core';
import { Preferences } from '@capacitor/preferences';
import { IluminusApiService } from '../services/iluminus-api.service';
import { DiscoveryService } from '../services/discovery.service';
import { StorageService } from '../services/storage.service';
import { EFFECTS, EffectCmd } from '../../shared/models/effects';
import { Rgb, rgbToHsv, scaleBrightness, toHex } from '../../shared/models/color.utils';
import { ColorFavorite, ColorHistory } from '../../shared/models/color.model';

export type ConnectionStatus = 'unknown' | 'checking' | 'online' | 'offline';

/**
 * Estado único do dispositivo e da luz.
 *
 * As telas leem os signals e chamam os métodos; só esta classe fala com o ESP.
 * O envio é "último valor vence": no máximo uma requisição em voo, e mudanças
 * feitas enquanto ela não volta são juntadas num único envio seguinte. Assim
 * arrastar um slider não enfileira dezenas de requisições no ESP8266.
 */
@Injectable({ providedIn: 'root' })
export class DeviceStore {
  private readonly api = inject(IluminusApiService);
  private readonly discovery = inject(DiscoveryService);
  private readonly storage = inject(StorageService);

  // ── Conexão ────────────────────────────────────────────────────────────
  readonly ip = signal('');
  /** Nome dado pelo usuário à fita (o firmware não tem nome próprio). */
  readonly deviceName = signal('Minha fita');
  readonly status = signal<ConnectionStatus>('unknown');
  readonly connected = computed(() => this.status() === 'online');

  // ── Luz ────────────────────────────────────────────────────────────────
  readonly color = signal<Rgb>({ r: 13, g: 89, b: 242 });
  readonly brightness = signal(85);
  readonly power = signal(true);
  readonly effect = signal<EffectCmd>('L');

  /** Cor escolhida, sem brilho aplicado. */
  readonly hex = computed(() => toHex(this.color()));
  /** Cor como sai no LED, com o brilho aplicado. */
  readonly litHex = computed(() => toHex(scaleBrightness(this.color(), this.brightness())));
  /** Cor como sai no LED, para pintar a tela (com piso de 25% para não sumir no fundo escuro). */
  readonly litRgb = computed(() => scaleBrightness(this.color(), Math.max(this.brightness(), 25)));
  /** Tom (0–359) e saturação (0–1) da cor escolhida. */
  readonly hsv = computed(() => rgbToHsv(this.color()));

  private readonly NAME_KEY = 'iluminus_device_name';
  private initPromise?: Promise<void>;
  private sending = false;
  private dirty = false;
  private historyTimer?: ReturnType<typeof setTimeout>;

  /** Carrega o IP salvo e testa a conexão. Chamadas repetidas reaproveitam a primeira. */
  init(): Promise<void> {
    return (this.initPromise ??= (async () => {
      const [ip, name] = await Promise.all([
        this.discovery.getIp(),
        Preferences.get({ key: this.NAME_KEY }),
      ]);
      this.ip.set(ip);
      if (name.value) this.deviceName.set(name.value);
      await this.checkConnection();
    })());
  }

  async checkConnection(): Promise<boolean> {
    const ip = this.ip();
    if (!ip) {
      this.status.set('offline');
      return false;
    }
    this.status.set('checking');
    const ok = await this.api.checkConnection(ip);
    this.status.set(ok ? 'online' : 'offline');
    return ok;
  }

  /** Testa o IP e, se responder, passa a usá-lo e o salva. */
  async connect(ip: string): Promise<boolean> {
    const target = ip.trim();
    if (!target) return false;
    this.status.set('checking');
    const ok = await this.api.checkConnection(target, 3000);
    if (ok) {
      await this.discovery.setIp(target);
      this.ip.set(target);
      this.status.set('online');
    } else {
      this.status.set(this.ip() ? 'offline' : 'unknown');
    }
    return ok;
  }

  async rename(name: string): Promise<void> {
    const value = name.trim();
    if (!value) return;
    this.deviceName.set(value);
    await Preferences.set({ key: this.NAME_KEY, value });
  }

  async forget(): Promise<void> {
    await this.discovery.clearIp();
    this.ip.set('');
    this.status.set('offline');
  }

  // ── Comandos ───────────────────────────────────────────────────────────

  setColor(color: Rgb): void {
    this.color.set(color);
    if (!this.effectUsesColor()) this.effect.set('L');
    this.power.set(true);
    this.push(true);
  }

  setBrightness(value: number): void {
    this.brightness.set(Math.max(0, Math.min(100, Math.round(value))));
    if (this.power()) this.push(true);
  }

  setPower(on: boolean): void {
    this.power.set(on);
    this.push(false);
  }

  togglePower(): void {
    this.setPower(!this.power());
  }

  applyEffect(cmd: EffectCmd): void {
    if (cmd === 'D') {
      this.setPower(false);
      return;
    }
    this.effect.set(cmd);
    this.power.set(true);
    this.push(true);
  }

  /** Reaplica um favorito ou item do histórico. */
  applyPreset(p: ColorFavorite | ColorHistory): void {
    this.color.set({ r: p.r, g: p.g, b: p.b });
    this.brightness.set(p.brightness);
    this.effect.set((p.effect === 'D' ? 'L' : p.effect) as EffectCmd);
    this.power.set(true);
    this.push(false);
  }

  async saveFavorite(name?: string): Promise<ColorFavorite> {
    const c = this.color();
    const fav: ColorFavorite = {
      id: Date.now().toString(),
      name: name?.trim() || 'Cor ' + this.litHex(),
      r: c.r, g: c.g, b: c.b,
      brightness: this.brightness(),
      effect: this.effect(),
    };
    await this.storage.saveFavorite(fav);
    return fav;
  }

  // ── Envio ──────────────────────────────────────────────────────────────

  private effectUsesColor(): boolean {
    return EFFECTS.find(e => e.cmd === this.effect())?.usesColor ?? false;
  }

  private push(recordHistory: boolean): void {
    this.dirty = true;
    void this.pump();
    if (recordHistory) this.scheduleHistory();
  }

  private async pump(): Promise<void> {
    if (this.sending) return;
    this.sending = true;
    try {
      while (this.dirty) {
        this.dirty = false;
        const ok = await this.sendCurrent();
        this.status.set(ok ? 'online' : 'offline');
        // Pequeno respiro para o ESP8266 entre comandos.
        await new Promise(r => setTimeout(r, 60));
      }
    } finally {
      this.sending = false;
    }
  }

  private async sendCurrent(): Promise<boolean> {
    if (!this.ip()) await this.init();
    const ip = this.ip();
    if (!this.power()) return this.api.sendCommand(ip, 'D');
    const { r, g, b } = scaleBrightness(this.color(), this.brightness());
    return this.api.sendCommand(ip, this.effect(), r, g, b, this.brightness());
  }

  /** Grava no histórico só quando o usuário para de mexer (antes gravava a cada 60 ms). */
  private scheduleHistory(): void {
    clearTimeout(this.historyTimer);
    this.historyTimer = setTimeout(() => {
      const c = this.color();
      void this.storage.addHistory({
        id: Date.now().toString(),
        timestamp: Date.now(),
        r: c.r, g: c.g, b: c.b,
        brightness: this.brightness(),
        effect: this.effect(),
      });
    }, 1500);
  }
}
