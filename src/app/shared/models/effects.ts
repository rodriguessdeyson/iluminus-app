/**
 * Fonte única dos efeitos do firmware (iluminus.ino → handleCommand).
 * `cmd` é o valor enviado em /set?c=<cmd>.
 */
export type EffectCmd =
  | 'L' | 'R' | 'T' | 'SP' | 'SS' | 'OC' | 'FR' | 'N' | 'C' | 'SL' | 'O' | 'FE' | 'D';

export interface LightEffect {
  cmd: EffectCmd;
  label: string;
  /** true = o efeito usa a cor escolhida; false = tem paleta própria. */
  usesColor: boolean;
}

export const EFFECTS: readonly LightEffect[] = [
  { cmd: 'L',  label: 'Cor sólida',  usesColor: true  },
  { cmd: 'R',  label: 'Arco-íris',   usesColor: false },
  { cmd: 'T',  label: 'Perseguição', usesColor: false },
  { cmd: 'SP', label: 'Faíscas',     usesColor: true  },
  { cmd: 'SS', label: 'Pôr do sol',  usesColor: false },
  { cmd: 'OC', label: 'Oceano',      usesColor: false },
  { cmd: 'FR', label: 'Floresta',    usesColor: false },
  { cmd: 'N',  label: 'Neve',        usesColor: true  },
  { cmd: 'C',  label: 'Scanner',     usesColor: true  },
  { cmd: 'SL', label: 'Dormir',      usesColor: true  },
  { cmd: 'O',  label: 'Pulsar',      usesColor: true  },
  { cmd: 'FE', label: 'Fogo',        usesColor: false },
];

/** Efeitos que aparecem como atalho na tela inicial. */
export const QUICK_EFFECTS: readonly LightEffect[] =
  (['L', 'R', 'OC', 'FE'] as const).map(cmd => EFFECTS.find(e => e.cmd === cmd)!);

export function effectLabel(cmd: string): string {
  if (cmd === 'D') return 'Desligado';
  return EFFECTS.find(e => e.cmd === cmd)?.label ?? cmd;
}
