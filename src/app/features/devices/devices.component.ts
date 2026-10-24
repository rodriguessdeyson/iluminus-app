import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { PageShellComponent } from '../../shared/components/page-shell/page-shell.component';
import { IconComponent } from '../../shared/ui/icon.component';
import { DeviceStore } from '../../core/state/device.store';
import { DiscoveryService } from '../../core/services/discovery.service';
import { rgbCss } from '../../shared/models/color.utils';

/** Aba Dispositivos: fita atual, busca na rede e conexão pelo IP. */
@Component({
  selector: 'app-devices',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, PageShellComponent, IconComponent],
  styles: [`
    .ping { position: absolute; inset: 0; border-radius: 50%; border: 2px solid currentColor; animation: app-ping 1.2s ease-out infinite; }
  `],
  template: `
<app-page-shell title="Dispositivos" subtitle="Fitas iLuminus na sua rede Wi-Fi">

  <div class="grid grid-cols-1 lg:grid-cols-2 gap-4 md:gap-6 items-start">
    <div class="flex flex-col gap-4">

      <!-- Fita atual -->
      @if (store.ip()) {
        <section class="card flex flex-col gap-[14px]" aria-label="Fita atual">
          <div class="flex items-center gap-3">
            <span class="w-12 h-12 rounded-[14px] grid place-items-center flex-shrink-0" [style.background]="ledTint()">
              <span class="block w-[18px] h-[18px] rounded-full" [style.background]="store.hex()" [style.box-shadow]="ledGlow()"></span>
            </span>
            <div class="flex-1 min-w-0 flex flex-col gap-0.5">
              @if (renaming()) {
                <form class="flex gap-2" (ngSubmit)="saveName()">
                  <label for="device-name" class="sr-only">Nome da fita</label>
                  <input id="device-name" name="name" class="text-field h-10 flex-1" [(ngModel)]="nameDraft" maxlength="40" autocomplete="off">
                  <button type="submit" class="btn btn-primary min-h-10 h-10 px-3">Salvar</button>
                </form>
              } @else {
                <span class="text-[16px] font-extrabold truncate">{{ store.deviceName() }}</span>
              }
              <span class="font-mono text-[12px] text-muted truncate">{{ store.ip() }}</span>
            </div>
            @if (!renaming()) {
              <span class="status-pill" role="status"
                    [class.is-online]="store.status() === 'online'"
                    [class.is-offline]="store.status() === 'offline'"
                    [class.is-checking]="store.status() === 'checking' || store.status() === 'unknown'">
                {{ statusLabel() }}
              </span>
            }
          </div>
          <div class="grid grid-cols-3 gap-2">
            <button type="button" class="btn btn-secondary px-2" (click)="startRename()">
              <app-icon name="edit" [size]="16" /> Renomear
            </button>
            <button type="button" class="btn btn-secondary px-2" [disabled]="store.status() === 'checking'" (click)="store.checkConnection()">
              <app-icon name="refresh" [size]="16" /> Testar
            </button>
            <button type="button" class="btn btn-danger px-2" (click)="store.forget()">Esquecer</button>
          </div>
        </section>
      } @else {
        <section class="card flex flex-col items-center gap-2 text-center py-8">
          <p class="m-0 text-[15px] font-bold">Nenhuma fita conectada</p>
          <p class="m-0 text-[13px] text-muted">Procure na rede ou informe o IP do ESP8266.</p>
        </section>
      }

      <!-- Adicionar pelo IP -->
      <section class="card flex flex-col gap-[10px]">
        <label for="device-ip" class="section-title">Adicionar pelo IP</label>
        <form class="flex gap-2" (ngSubmit)="connectManual()">
          <input id="device-ip" name="ip" type="text" inputmode="decimal" autocomplete="off"
                 class="text-field flex-1 font-mono" placeholder="192.168.0.50" [(ngModel)]="manualIp">
          <button type="submit" class="btn btn-secondary h-12" [disabled]="testingIp()">
            {{ testingIp() ? 'Testando…' : 'Testar' }}
          </button>
        </form>
        @if (ipError()) {
          <p class="m-0 text-[12px] text-danger" role="alert">{{ ipError() }}</p>
        } @else {
          <p class="m-0 text-[12px] leading-normal text-muted">O IP aparece no monitor serial do ESP8266 ou na lista de clientes do roteador.</p>
        }
      </section>
    </div>

    <!-- Busca na rede -->
    <section class="flex flex-col gap-[10px]" aria-labelledby="scan-title">
      <div class="flex justify-between items-center">
        <h2 id="scan-title" class="section-title">Na rede</h2>
        <span class="text-[12px] text-muted" aria-live="polite">{{ scanStatus() }}</span>
      </div>

      <button type="button" class="btn h-14 rounded-2xl text-[15px] font-extrabold"
              [style.background]="scanning() ? 'var(--app-surface-2)' : 'var(--app-ink)'"
              [style.color]="scanning() ? 'var(--app-ink)' : 'var(--app-bg)'"
              [disabled]="scanning()" (click)="scan()">
        <span class="relative w-5 h-5 grid place-items-center">
          @if (scanning()) { <span class="ping"></span> }
          <span class="block w-2 h-2 rounded-full bg-current"></span>
        </span>
        {{ scanning() ? 'Procurando…' : 'Procurar na rede' }}
      </button>

      @for (ip of found(); track ip) {
        <div class="flex items-center gap-3 p-3 rounded-2xl bg-surface">
          <span class="w-10 h-10 rounded-xl bg-surface-2 grid place-items-center text-soft flex-shrink-0">
            <app-icon name="devices" [size]="20" />
          </span>
          <span class="flex-1 min-w-0 flex flex-col gap-0.5">
            <span class="text-[14px] font-bold">iLuminus</span>
            <span class="font-mono text-[12px] text-muted">{{ ip }}</span>
          </span>
          @if (ip === store.ip() && store.connected()) {
            <span class="status-pill is-online">Em uso</span>
          } @else {
            <button type="button" class="btn btn-primary px-4 text-[13px] font-extrabold" (click)="connect(ip)">Conectar</button>
          }
        </div>
      }

      @if (scanError()) {
        <p class="m-0 text-[13px] text-danger" role="alert">{{ scanError() }}</p>
      }
      <p class="m-0 text-[12px] leading-normal text-muted">
        No celular a busca usa UDP (porta 4210). No navegador, testa os endereços mais comuns da rede local, o que pode levar alguns segundos.
      </p>
    </section>
  </div>
</app-page-shell>
  `
})
export class DevicesComponent {
  protected readonly store = inject(DeviceStore);
  private readonly discovery = inject(DiscoveryService);
  private readonly router = inject(Router);

  // Renomear
  protected readonly renaming = signal(false);
  protected nameDraft = '';

  // IP manual
  protected manualIp = '';
  protected readonly testingIp = signal(false);
  protected readonly ipError = signal('');

  // Busca
  protected readonly scanning = signal(false);
  protected readonly found = signal<string[]>([]);
  protected readonly scanMsg = signal('');
  protected readonly scanError = signal('');
  protected readonly scanStatus = computed(() =>
    this.scanning() ? this.scanMsg() : this.found().length ? `${this.found().length} encontrado(s)` : '');

  protected readonly ledTint = computed(() => `color-mix(in srgb, ${this.store.hex()} 18%, transparent)`);
  protected readonly ledGlow = computed(() => `0 0 16px 4px ${rgbCss(this.store.color(), 0.6)}`);

  protected statusLabel() {
    switch (this.store.status()) {
      case 'online': return 'Conectado';
      case 'offline': return 'Sem resposta';
      default: return 'Testando…';
    }
  }

  protected startRename() {
    this.nameDraft = this.store.deviceName();
    this.renaming.set(true);
  }

  protected async saveName() {
    await this.store.rename(this.nameDraft);
    this.renaming.set(false);
  }

  protected async connectManual() {
    const ip = this.manualIp.trim();
    if (!ip) return;
    this.testingIp.set(true);
    this.ipError.set('');
    const ok = await this.store.connect(ip);
    this.testingIp.set(false);
    if (ok) {
      this.manualIp = '';
      this.router.navigateByUrl('/home');
    } else {
      this.ipError.set(`Nenhum iLuminus respondeu em ${ip}.`);
    }
  }

  protected async scan() {
    this.scanning.set(true);
    this.found.set([]);
    this.scanError.set('');
    this.scanMsg.set('Procurando…');
    await this.discovery.scanNetwork(
      ip => this.found.update(list => list.includes(ip) ? list : [...list, ip]),
      msg => this.scanMsg.set(msg),
    );
    this.scanning.set(false);
    if (this.found().length === 0) {
      this.scanError.set('Nenhuma fita encontrada. Confira se o ESP8266 está ligado e no mesmo Wi-Fi.');
    }
  }

  protected async connect(ip: string) {
    this.scanError.set('');
    if (await this.store.connect(ip)) this.router.navigateByUrl('/home');
    else this.scanError.set(`${ip} parou de responder.`);
  }
}
