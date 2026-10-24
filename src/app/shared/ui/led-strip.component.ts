import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { EffectCmd } from '../models/effects';
import { LED_COUNT, previewLeds } from '../models/effect-preview';
import { Rgb, rgbCss } from '../models/color.utils';

/** Prévia de um efeito nos LEDs da fita (decorativa: o nome do efeito vem ao lado). */
@Component({
  selector: 'app-led-strip',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { 'aria-hidden': 'true' },
  styles: [`
    :host { display: flex; gap: var(--led-gap, 4px); }
    span { display: block; border-radius: 50%; flex-shrink: 0; }
  `],
  template: `
    @for (led of leds(); track $index) {
      <span [style.width.px]="size()" [style.height.px]="size()"
            [style.background]="led.fill" [style.box-shadow]="led.glow"></span>
    }
  `,
})
export class LedStripComponent {
  readonly effect = input.required<EffectCmd>();
  readonly color = input.required<Rgb>();
  readonly size = input(9);
  readonly count = input(LED_COUNT);
  /** Brilho em volta de cada LED (para prévias grandes). */
  readonly glow = input(false);

  protected readonly leds = computed(() =>
    previewLeds(this.effect(), this.color(), this.count()).map(c => ({
      fill: rgbCss(c),
      glow: this.glow() ? `0 0 14px 2px ${rgbCss(c, 0.55)}` : 'none',
    })));
}
