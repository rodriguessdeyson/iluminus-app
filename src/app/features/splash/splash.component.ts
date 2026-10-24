import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { DeviceStore } from '../../core/state/device.store';
import { rgbCss } from '../../shared/models/color.utils';

/** Abertura: testa a conexão com a fita e segue para Luz (online) ou Dispositivos. */
@Component({
  selector: 'app-splash',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: [`
    :host {
      display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 40px;
      height: 100%;
      padding: calc(32px + var(--app-safe-top)) 24px calc(32px + var(--app-safe-bottom));
      background: var(--app-bg); color: var(--app-ink); box-sizing: border-box;
    }
    .orb {
      width: 112px; height: 112px; border-radius: 50%;
      animation: breathe 2.4s ease-in-out infinite;
    }
    @keyframes breathe { 0%, 100% { transform: scale(0.94); } 50% { transform: scale(1); } }
    .bar { width: 180px; height: 4px; border-radius: 2px; background: var(--app-surface-2); overflow: hidden; }
    .bar > span { display: block; height: 100%; border-radius: 2px; background: var(--app-ink); transition: width 250ms ease-out; }
  `],
  template: `
    <span class="orb" [style.background]="led()" [style.box-shadow]="glow()" aria-hidden="true"></span>
    <div class="flex flex-col items-center gap-2 text-center">
      <h1 class="m-0 text-[28px] font-extrabold tracking-tight">iLuminus</h1>
      <p class="m-0 text-[14px] text-muted" role="status">{{ statusText() }}</p>
    </div>
    <div class="bar" role="progressbar" aria-label="Carregando" [attr.aria-valuenow]="progress()" aria-valuemin="0" aria-valuemax="100">
      <span [style.width.%]="progress()"></span>
    </div>
  `,
})
export class SplashComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly store = inject(DeviceStore);

  // Signals: o app roda sem zone.js.
  protected readonly statusText = signal('Procurando sua fita de LED…');
  protected readonly progress = signal(10);
  protected readonly led = computed(() => rgbCss(this.store.color()));
  protected readonly glow = computed(() =>
    `0 0 0 14px ${rgbCss(this.store.color(), 0.08)}, 0 0 80px 18px ${rgbCss(this.store.color(), 0.45)}`);

  async ngOnInit() {
    const minDelay = new Promise(r => setTimeout(r, 1200));
    const tick = setInterval(() => this.progress.update(p => Math.min(p + 15, 90)), 250);

    await Promise.all([this.store.init(), minDelay]);
    clearInterval(tick);

    const online = this.store.connected();
    this.statusText.set(online ? 'Conectado.' : 'Fita não encontrada.');
    this.progress.set(100);
    setTimeout(() => {
      this.router.navigateByUrl(online ? '/home' : '/devices', { replaceUrl: true });
    }, 400);
  }
}
