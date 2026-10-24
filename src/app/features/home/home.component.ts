import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PageShellComponent } from '../../shared/components/page-shell/page-shell.component';
import { IconComponent } from '../../shared/ui/icon.component';
import { LedStripComponent } from '../../shared/ui/led-strip.component';
import { DeviceStore } from '../../core/state/device.store';
import { StorageService } from '../../core/services/storage.service';
import { EFFECTS, EffectCmd, effectLabel } from '../../shared/models/effects';
import { ColorFavorite } from '../../shared/models/color.model';
import { hsvToRgb, readableInk, rgbCss, scaleBrightness } from '../../shared/models/color.utils';

interface Preset { h: number; s: number; label: string; }

const PRESETS: Preset[] = [
  { h: 220, s: 0.95, label: 'Azul' },
  { h: 0,   s: 0.88, label: 'Vermelho' },
  { h: 26,  s: 0.95, label: 'Laranja' },
  { h: 48,  s: 0.85, label: 'Amarelo' },
  { h: 145, s: 0.80, label: 'Verde' },
  { h: 278, s: 0.70, label: 'Roxo' },
  { h: 34,  s: 0.22, label: 'Branco quente' },
];

const HOME_EFFECTS: EffectCmd[] = ['L', 'R', 'OC', 'FE', 'SP', 'SS'];

@Component({
  selector: 'app-home',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, PageShellComponent, IconComponent, LedStripComponent],
  styles: [`
    .orb {
      width: 188px; height: 188px; border-radius: 50%;
      border: none; cursor: pointer;
      display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 6px;
      font: inherit;
      transition: background 160ms, box-shadow 220ms, transform 120ms;
    }
    .orb:active { transform: scale(0.98); }
    @media (min-width: 768px) { .orb { width: 240px; height: 240px; } }
  `],
  template: `
<app-page-shell [title]="store.deviceName()" [subtitle]="statusLine()">
  <a slot="actions" routerLink="/devices" class="icon-btn" aria-label="Trocar dispositivo">
    <app-icon name="swap" [size]="20" />
  </a>

  @if (store.status() === 'offline') {
    <div class="card mb-4 flex flex-wrap items-center gap-3">
      <span class="w-11 h-11 rounded-xl bg-surface-2 grid place-items-center text-danger">
        <app-icon name="wifi-off" [size]="20" />
      </span>
      <div class="flex-1 min-w-[11rem]">
        <p class="m-0 text-[14px] font-bold">Sem conexão com a fita</p>
        <p class="m-0 text-[13px] text-muted">Confira se o celular está no mesmo Wi-Fi do ESP8266.</p>
      </div>
      <a routerLink="/devices" class="btn btn-secondary">Procurar</a>
    </div>
  }

  <div class="grid grid-cols-1 md:grid-cols-2 gap-[14px] md:gap-6 items-start">

    <!-- Luz + cor + brilho -->
    <div class="flex flex-col gap-[14px] md:gap-6 md:bg-surface md:rounded-[28px] md:p-8">
      <section class="flex flex-col items-center gap-3 pt-6 pb-1 md:pt-2" aria-label="Luz">
        <button type="button" class="orb"
                [style.background]="orbBg()" [style.box-shadow]="orbShadow()" [style.color]="orbInk()"
                [attr.aria-pressed]="store.power()"
                [attr.aria-label]="store.power() ? 'Desligar a luz' : 'Ligar a luz'"
                (click)="store.togglePower()">
          <app-icon name="power" [size]="28" [stroke]="2" />
          <span class="font-mono text-[15px] font-semibold">{{ store.power() ? store.litHex() : 'Desligada' }}</span>
        </button>
        <p class="m-0 text-[13px] text-faint">Toque na luz para {{ store.power() ? 'desligar' : 'ligar' }}</p>
      </section>

      <section class="card md:p-0 md:bg-transparent flex flex-col gap-[14px]" aria-labelledby="home-color-title">
        <div class="flex justify-between items-baseline">
          <h2 id="home-color-title" class="section-title">Cor</h2>
          <span class="font-mono text-[12px] text-muted">{{ store.hex() }}</span>
        </div>

        <div class="grid grid-cols-7">
          @for (p of presets; track p.label) {
            <button type="button" class="h-11 grid place-items-center bg-transparent border-0 p-0 cursor-pointer"
                    [attr.aria-label]="p.label" [attr.aria-pressed]="isPreset(p)"
                    (click)="pickPreset(p)">
              <span class="block w-8 h-8 rounded-full"
                    [style.background]="presetCss(p)"
                    [style.box-shadow]="isPreset(p) ? '0 0 0 3px var(--app-surface), 0 0 0 5px var(--app-ink)' : 'none'"></span>
            </button>
          }
        </div>

        <div class="flex flex-col gap-2">
          <label for="home-hue" class="field-label">Tom</label>
          <input id="home-hue" type="range" class="app-range" min="0" max="359"
                 [value]="hue()" (input)="onHue($event)"
                 style="background: linear-gradient(90deg,#ff0000,#ffff00,#00ff00,#00ffff,#0000ff,#ff00ff,#ff0000)">
        </div>
        <div class="flex flex-col gap-2">
          <label for="home-sat" class="field-label">Saturação</label>
          <input id="home-sat" type="range" class="app-range" min="0" max="100"
                 [value]="saturation()" (input)="onSaturation($event)"
                 [style.background]="satTrack()">
        </div>
      </section>

      <section class="card md:p-0 md:bg-transparent flex flex-col gap-[10px]">
        <div class="flex justify-between items-baseline">
          <label for="home-brightness" class="section-title">Brilho</label>
          <span class="font-mono text-[13px]">{{ store.brightness() }}%</span>
        </div>
        <input id="home-brightness" type="range" class="app-range" min="1" max="100"
               [value]="store.brightness()" (input)="onBrightness($event)"
               [style.background]="briTrack()">
      </section>
    </div>

    <!-- Efeitos + favoritos -->
    <div class="flex flex-col gap-[14px] md:gap-6">
      <section class="flex flex-col gap-[10px] md:bg-surface md:rounded-[28px] md:p-6" aria-labelledby="home-fx-title">
        <div class="flex justify-between items-center">
          <h2 id="home-fx-title" class="section-title">Efeitos rápidos</h2>
          <a routerLink="/effects" class="btn btn-text text-[13px]">Ver todos</a>
        </div>
        <div class="grid grid-cols-2 md:grid-cols-3 gap-[10px]">
          @for (e of quickEffects; track e.cmd; let i = $index) {
            <button type="button" class="effect-card md:bg-raised md:flex" [class.hidden]="i >= 4"
                    [attr.aria-pressed]="store.power() && store.effect() === e.cmd"
                    (click)="store.applyEffect(e.cmd)">
              <app-led-strip [effect]="e.cmd" [color]="store.color()" />
              <span class="text-[14px] font-bold">{{ e.label }}</span>
            </button>
          }
        </div>
      </section>

      <section class="card md:rounded-[28px] md:p-6 flex flex-col gap-4" aria-labelledby="home-fav-title">
        <div class="flex justify-between items-center gap-3">
          <h2 id="home-fav-title" class="section-title">Favoritos</h2>
          <button type="button" class="btn btn-secondary min-h-9 h-9 px-3 text-[13px]" (click)="saveFavorite()">
            <app-icon [name]="justSaved() ? 'check' : 'plus'" [size]="16" />
            {{ justSaved() ? 'Salva' : 'Salvar cor atual' }}
          </button>
        </div>
        @if (favorites().length === 0) {
          <p class="m-0 text-[13px] text-muted">Salve combinações de cor, brilho e efeito para voltar a elas com um toque.</p>
        } @else {
          <div class="flex flex-wrap gap-3">
            @for (f of favorites(); track f.id) {
              <button type="button" class="w-16 flex flex-col items-center gap-1.5 bg-transparent border-0 p-0 cursor-pointer text-soft text-[12px] font-bold"
                      [attr.aria-label]="'Aplicar ' + f.name" (click)="store.applyPreset(f)">
                <span class="block w-[52px] h-[52px] rounded-2xl" [style.background]="favCss(f)"></span>
                <span class="w-full truncate text-center">{{ f.name }}</span>
              </button>
            }
          </div>
        }
      </section>
    </div>
  </div>
</app-page-shell>
  `
})
export class HomeComponent {
  protected readonly store = inject(DeviceStore);
  private readonly storage = inject(StorageService);

  protected readonly presets = PRESETS;
  protected readonly quickEffects = HOME_EFFECTS.map(cmd => EFFECTS.find(e => e.cmd === cmd)!);
  protected readonly favorites = signal<ColorFavorite[]>([]);
  protected readonly justSaved = signal(false);

  /** Último tom escolhido: com saturação 0 o tom some da cor, mas o slider não deve pular. */
  private readonly lastHue = signal(220);
  protected readonly hue = computed(() => this.store.hsv().s > 0.02 ? this.store.hsv().h : this.lastHue());
  protected readonly saturation = computed(() => Math.round(this.store.hsv().s * 100));

  protected readonly statusLine = computed(() => {
    switch (this.store.status()) {
      case 'online':   return (this.store.power() ? effectLabel(this.store.effect()) : 'Desligada') + ' · ' + this.store.ip();
      case 'checking': return 'Conectando…';
      case 'offline':  return 'Desconectado';
      default:         return '';
    }
  });

  protected readonly orbBg = computed(() => this.store.power() ? rgbCss(this.store.litRgb()) : 'var(--app-surface)');
  protected readonly orbInk = computed(() => this.store.power() ? readableInk(this.store.litRgb()) : 'var(--app-ink-faint)');
  protected readonly orbShadow = computed(() => {
    if (!this.store.power()) return 'inset 0 0 0 1px rgba(255,255,255,0.08)';
    const c = this.store.color();
    const glow = 0.25 + this.store.brightness() / 250;
    return `0 0 0 10px ${rgbCss(c, 0.08)}, 0 0 0 22px ${rgbCss(c, 0.05)}, 0 0 90px 20px ${rgbCss(c, glow)}, 0 0 180px 60px ${rgbCss(c, glow * 0.35)}`;
  });
  protected readonly satTrack = computed(() =>
    `linear-gradient(90deg, #FFFFFF, ${rgbCss(hsvToRgb({ h: this.hue(), s: 1, v: 1 }))})`);
  protected readonly briTrack = computed(() =>
    `linear-gradient(90deg, var(--app-surface-2), ${rgbCss(this.store.color())})`);

  async ionViewWillEnter() {
    this.favorites.set((await this.storage.getFavorites()).reverse().slice(0, 6));
  }

  protected presetCss(p: Preset) { return rgbCss(hsvToRgb({ h: p.h, s: p.s, v: 1 })); }

  protected isPreset(p: Preset) {
    const { h, s } = this.store.hsv();
    return Math.abs(s - p.s) < 0.04 && (Math.abs(h - p.h) < 4 || s < 0.02);
  }

  protected pickPreset(p: Preset) {
    this.lastHue.set(p.h);
    this.store.setColor(hsvToRgb({ h: p.h, s: p.s, v: 1 }));
  }

  protected onHue(e: Event) {
    const h = +(e.target as HTMLInputElement).value;
    this.lastHue.set(h);
    this.store.setColor(hsvToRgb({ h, s: Math.max(this.store.hsv().s, 0.02), v: 1 }));
  }

  protected onSaturation(e: Event) {
    const s = +(e.target as HTMLInputElement).value / 100;
    this.store.setColor(hsvToRgb({ h: this.hue(), s, v: 1 }));
  }

  protected onBrightness(e: Event) {
    this.store.setBrightness(+(e.target as HTMLInputElement).value);
  }

  protected favCss(f: ColorFavorite) {
    return rgbCss(scaleBrightness(f, Math.max(f.brightness, 25)));
  }

  protected async saveFavorite() {
    const fav = await this.store.saveFavorite();
    this.favorites.update(list => [fav, ...list].slice(0, 6));
    this.justSaved.set(true);
    setTimeout(() => this.justSaved.set(false), 1500);
  }
}
