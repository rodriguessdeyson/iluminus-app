import {
  ChangeDetectionStrategy, Component, DestroyRef, ElementRef, afterNextRender, inject, input, signal, viewChild,
} from '@angular/core';
import { DeviceStore } from '../../../core/state/device.store';

/**
 * Cabeçalho + área rolável de uma tela.
 * A navegação (abas no celular, sidebar no desktop) fica no ShellComponent.
 *
 * O cabeçalho flutua sobre o conteúdo: a área rolável ocupa a tela inteira e
 * começa logo abaixo dele (padding = altura do cabeçalho). Assim o brilho da
 * luz na tela Luz "sangra" para trás do título e da status bar, sem corte.
 * Ao rolar, o cabeçalho ganha fundo e desfoque para o título continuar legível.
 */
@Component({
  selector: 'app-page-shell',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: [`
    :host {
      position: relative;
      display: flex; flex-direction: column; flex: 1; min-height: 0;
      background: var(--app-bg);
      overflow: hidden;
    }

    .top-bar {
      position: absolute; top: 0; left: 0; right: 0; z-index: 2;
      border-bottom: 1px solid transparent;
      transition: background-color 200ms, border-color 200ms, backdrop-filter 200ms;
    }
    .top-bar.scrolled {
      background: color-mix(in srgb, var(--app-bg) 82%, transparent);
      -webkit-backdrop-filter: blur(16px);
      backdrop-filter: blur(16px);
      border-bottom-color: var(--app-line);
    }
    .top-bar-inner {
      display: flex; align-items: center; gap: 12px;
      box-sizing: border-box;
      padding: calc(20px + var(--app-safe-top)) calc(20px + var(--app-safe-right)) 8px calc(20px + var(--app-safe-left));
    }
    h1 { margin: 0; font-size: 22px; font-weight: 800; letter-spacing: -0.02em; line-height: 1.2; color: var(--app-ink); }
    .subtitle { margin: 4px 0 0; font-size: 13px; color: var(--app-ink-muted); }

    .content-scroll { flex: 1; overflow-y: auto; overflow-x: hidden; overscroll-behavior: contain; }
    .content-inner {
      box-sizing: border-box;
      padding: calc(var(--header-h, 76px) + 8px) calc(20px + var(--app-safe-right)) 24px calc(20px + var(--app-safe-left));
    }

    @media (min-width: 768px) {
      .top-bar-inner { padding: calc(32px + var(--app-safe-top)) calc(40px + var(--app-safe-right)) 12px 40px; max-width: 1200px; margin: 0 auto; }
      h1 { font-size: 28px; }
      .subtitle { font-size: 14px; }
      .content-inner {
        padding: calc(var(--header-h, 110px) + 16px) calc(40px + var(--app-safe-right)) calc(48px + var(--app-safe-bottom)) 40px;
        max-width: 1200px;
        margin: 0 auto;
      }
    }
  `],
  template: `
    <header #header class="top-bar" [class.scrolled]="scrolled()">
      <div class="top-bar-inner">
        <div class="flex-1 min-w-0">
          <h1 class="truncate">{{ title() }}</h1>
          @if (subtitle()) {
            <p class="subtitle truncate">{{ subtitle() }}</p>
          }
        </div>
        <ng-content select="[slot=actions]"></ng-content>
      </div>
    </header>

    <div class="content-scroll" (scroll)="onScroll($event)">
      <div class="content-inner">
        <ng-content></ng-content>
      </div>
    </div>
  `,
})
export class PageShellComponent {
  readonly title = input<string>('iLuminus');
  readonly subtitle = input<string>('');

  protected readonly store = inject(DeviceStore);
  protected readonly scrolled = signal(false);

  private readonly header = viewChild.required<ElementRef<HTMLElement>>('header');

  constructor() {
    const host = inject(ElementRef<HTMLElement>).nativeElement as HTMLElement;
    const destroyRef = inject(DestroyRef);

    // A altura do cabeçalho muda com a safe area, o subtítulo e a largura da tela.
    afterNextRender(() => {
      const el = this.header().nativeElement;
      const sync = () => host.style.setProperty('--header-h', `${el.offsetHeight}px`);
      sync();
      const ro = new ResizeObserver(sync);
      ro.observe(el);
      destroyRef.onDestroy(() => ro.disconnect());
    });
  }

  protected onScroll(e: Event) {
    const scrolled = (e.target as HTMLElement).scrollTop > 4;
    if (scrolled !== this.scrolled()) this.scrolled.set(scrolled);
  }
}
