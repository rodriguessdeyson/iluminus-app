import { ChangeDetectionStrategy, Component, DOCUMENT, effect, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { IonTabs } from '@ionic/angular/ion-tabs';
import { IonTabBar } from '@ionic/angular/ion-tab-bar';
import { IonTabButton } from '@ionic/angular/ion-tab-button';
import { IonLabel } from '@ionic/angular/ion-label';
import { NAV_ITEMS } from '../../core/navigation';
import { DeviceStore } from '../../core/state/device.store';
import { IconComponent } from '../../shared/ui/icon.component';

/**
 * Layout das telas principais.
 *
 * Celular (< 768px): abas embaixo (ion-tabs), cada aba com sua própria pilha
 * de navegação e o botão voltar do Android tratado pelo Ionic.
 * Desktop (≥ 768px): sidebar fixa à esquerda; a barra de abas some.
 * As duas navegações usam a mesma lista (core/navigation.ts).
 */
@Component({
  selector: 'app-shell',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, RouterLinkActive, IonTabs, IonTabBar, IonTabButton, IonLabel, IconComponent],
  styles: [`
    /* O outlet adiciona .ion-page (flex em coluna); aqui é linha: sidebar + conteúdo. */
    :host { display: flex; flex-direction: row; justify-content: flex-start; height: 100%; background: var(--app-bg); }

    .sidebar {
      display: none;
      flex-direction: column;
      gap: 1.5rem;
      width: 240px;
      flex-shrink: 0;
      box-sizing: border-box;
      padding: calc(1.5rem + var(--app-safe-top)) 1rem calc(1.5rem + var(--app-safe-bottom)) calc(1rem + var(--app-safe-left));
      background: var(--app-bg-raised);
      border-right: 1px solid var(--app-line);
      overflow-y: auto;
    }
    .brand { display: flex; align-items: center; gap: 0.625rem; padding: 0 0.5rem; }
    .brand-mark {
      width: 32px; height: 32px; border-radius: 10px;
      display: grid; place-items: center;
      background: color-mix(in srgb, var(--app-led) 22%, transparent);
    }
    .brand-dot { width: 12px; height: 12px; border-radius: 50%; background: var(--app-led); }
    .brand-name { font-size: 17px; font-weight: 800; letter-spacing: -0.02em; color: var(--app-ink); }

    .nav { display: flex; flex-direction: column; gap: 4px; }
    .nav-link {
      display: flex; align-items: center; gap: 0.75rem;
      height: 44px; padding: 0 0.75rem; border-radius: 12px;
      color: var(--app-ink-muted); font-size: 14px; font-weight: 600; text-decoration: none;
      transition: background 0.15s, color 0.15s;
    }
    .nav-link:hover { background: rgba(255, 255, 255, 0.05); color: var(--app-ink); }
    .nav-link.active {
      color: var(--app-ink); font-weight: 700;
      background: color-mix(in srgb, var(--app-led) 20%, transparent);
    }
    .nav-link:focus-visible { outline: 2px solid var(--app-ink); outline-offset: 2px; }

    .device {
      margin-top: auto; padding: 0.75rem; border-radius: 14px;
      background: var(--app-surface); color: inherit; text-decoration: none;
      display: flex; flex-direction: column; gap: 4px;
    }
    .device-name { display: flex; align-items: center; gap: 0.5rem; font-size: 13px; font-weight: 700; color: var(--app-ink); }
    .device-ip { font-family: var(--font-mono); font-size: 12px; color: var(--app-ink-muted); }
    .dot { width: 8px; height: 8px; border-radius: 50%; background: var(--app-ink-faint); }
    .dot.online { background: var(--app-ok); }

    .main { position: relative; flex: 1; min-width: 0; }

    ion-tab-bar {
      --background: var(--app-bg-raised);
      --border: 1px solid var(--app-line);
      --color: var(--app-ink-faint);
      --color-selected: var(--app-ink);
      height: 68px;
      padding-top: 6px;
    }
    ion-tab-button { --padding-top: 2px; --padding-bottom: 8px; --ripple-color: transparent; font-size: 12px; }
    .tab-pill {
      width: 56px; height: 30px; border-radius: 15px;
      display: grid; place-items: center; margin-bottom: 4px;
      transition: background 0.15s;
    }
    ion-tab-button.tab-selected .tab-pill { background: color-mix(in srgb, var(--app-led) 22%, transparent); }
    ion-label { font-weight: 600; margin: 0; }
    ion-tab-button.tab-selected ion-label { font-weight: 700; }

    @media (min-width: 768px) {
      .sidebar { display: flex; }
      ion-tab-bar { display: none; }
    }
  `],
  template: `
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-mark"><span class="brand-dot"></span></span>
        <span class="brand-name">iLuminus</span>
      </div>

      <nav class="nav" aria-label="Navegação principal">
        @for (item of nav; track item.tab) {
          <a class="nav-link" [routerLink]="'/' + item.tab"
             routerLinkActive="active" ariaCurrentWhenActive="page">
            <app-icon [name]="item.icon" [size]="20" />
            {{ item.label }}
          </a>
        }
      </nav>

      <a class="device" routerLink="/devices">
        <span class="device-name">
          <span class="dot" [class.online]="store.connected()"></span>
          {{ store.deviceName() }}
        </span>
        <span class="device-ip">{{ store.connected() ? store.ip() : 'Desconectado' }}</span>
      </a>
    </aside>

    <div class="main">
      <ion-tabs>
        <ion-tab-bar slot="bottom">
          @for (item of nav; track item.tab) {
            <ion-tab-button [tab]="item.tab">
              <span class="tab-pill"><app-icon [name]="item.icon" /></span>
              <ion-label>{{ item.label }}</ion-label>
            </ion-tab-button>
          }
        </ion-tab-bar>
      </ion-tabs>
    </div>
  `,
})
export class ShellComponent {
  protected readonly store = inject(DeviceStore);
  protected readonly nav = NAV_ITEMS;

  constructor() {
    void this.store.init();

    // A cor atual do LED vira um token de tema (--app-led): destaques da
    // navegação acompanham a luz escolhida.
    const root = inject(DOCUMENT).documentElement;
    effect(() => root.style.setProperty('--app-led', this.store.hex()));
  }
}
