import { EffectCmd } from './effects';
import { Rgb, hsvToRgb } from './color.utils';

/** Quantidade de LEDs da fita (LED_COUNT no firmware). */
export const LED_COUNT = 9;

/**
 * Prévia estática de como cada efeito aparece nos LEDs da fita.
 * `base` é a cor escolhida, usada pelos efeitos que dependem dela.
 */
export function previewLeds(cmd: EffectCmd, base: Rgb, count = LED_COUNT): Rgb[] {
  const mid = (count - 1) / 2;
  const fire = [0, 18, 34, 8, 44, 24, 4, 38, 14];
  const dim = (k: number): Rgb => ({ r: Math.round(base.r * k), g: Math.round(base.g * k), b: Math.round(base.b * k) });
  const off: Rgb = { r: 28, g: 31, b: 38 };
  const white: Rgb = { r: 255, g: 255, b: 255 };

  return Array.from({ length: count }, (_, i) => {
    const d = Math.abs(i - mid);
    switch (cmd) {
      case 'L':  return base;
      case 'R':  return hsvToRgb({ h: (i * 360) / count, s: 1, v: 1 });
      case 'T':  return i % 3 === 0 ? hsvToRgb({ h: i * 40, s: 1, v: 1 }) : off;
      case 'SP': return i === 2 || i === 6 ? white : dim(0.4);
      case 'SS': return hsvToRgb({ h: (330 + i * 9) % 360, s: 0.85, v: 1 - i * 0.04 });
      case 'OC': return hsvToRgb({ h: 180 + i * 6, s: 0.85, v: i % 2 ? 0.55 : 1 });
      case 'FR': return hsvToRgb({ h: 95 + i * 6, s: 0.8, v: i % 2 ? 0.5 : 0.85 });
      case 'N':  return i % 4 === 1 ? white : dim(0.55);
      case 'C':  return dim(Math.max(0.1, 1 - d * 0.45));
      case 'SL': return dim(0.22);
      case 'O':  return dim(0.15 + (i / (count - 1)) * 0.85);
      case 'FE': return hsvToRgb({ h: fire[i % fire.length], s: 1, v: 1 - (i % 3) * 0.15 });
      default:   return off;
    }
  });
}
