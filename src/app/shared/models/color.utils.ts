export interface Rgb { r: number; g: number; b: number; }

export function toHex({ r, g, b }: Rgb): string {
  return '#' + [r, g, b].map(v => v.toString(16).padStart(2, '0')).join('').toUpperCase();
}

export function fromHex(hex: string): Rgb {
  const h = hex.replace('#', '');
  return {
    r: parseInt(h.slice(0, 2), 16),
    g: parseInt(h.slice(2, 4), 16),
    b: parseInt(h.slice(4, 6), 16),
  };
}

/** Aplica o brilho (0–100) na cor, como o firmware espera receber. */
export function scaleBrightness({ r, g, b }: Rgb, brightness: number): Rgb {
  const k = brightness / 100;
  return { r: Math.round(r * k), g: Math.round(g * k), b: Math.round(b * k) };
}

export interface Hsv { h: number; s: number; v: number; }

/** h em graus (0–359), s e v de 0 a 1. */
export function hsvToRgb({ h, s, v }: Hsv): Rgb {
  const f = (n: number) => {
    const k = (n + h / 60) % 6;
    return Math.round((v - v * s * Math.max(0, Math.min(k, 4 - k, 1))) * 255);
  };
  return { r: f(5), g: f(3), b: f(1) };
}

export function rgbToHsv({ r, g, b }: Rgb): Hsv {
  const rn = r / 255, gn = g / 255, bn = b / 255;
  const max = Math.max(rn, gn, bn), min = Math.min(rn, gn, bn), d = max - min;
  let h = 0;
  if (d !== 0) {
    if (max === rn) h = ((gn - bn) / d) % 6;
    else if (max === gn) h = (bn - rn) / d + 2;
    else h = (rn - gn) / d + 4;
    h = Math.round(h * 60);
    if (h < 0) h += 360;
  }
  return { h, s: max === 0 ? 0 : d / max, v: max };
}

export function rgbCss({ r, g, b }: Rgb, alpha = 1): string {
  return alpha >= 1 ? `rgb(${r}, ${g}, ${b})` : `rgba(${r}, ${g}, ${b}, ${alpha})`;
}

/** Cor de texto legível sobre um fundo dessa cor. */
export function readableInk({ r, g, b }: Rgb): string {
  return (0.299 * r + 0.587 * g + 0.114 * b) / 255 > 0.62 ? '#0A0B0E' : '#FFFFFF';
}
