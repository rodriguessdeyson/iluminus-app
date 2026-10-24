import { IconName } from '../shared/ui/icon.component';

export interface NavItem {
  /** Rota filha do shell e `tab` do ion-tab-button. */
  tab: string;
  label: string;
  icon: IconName;
}

/** Fonte única da navegação: abas no celular e sidebar no desktop. */
export const NAV_ITEMS: readonly NavItem[] = [
  { tab: 'home',      label: 'Luz',          icon: 'light' },
  { tab: 'effects',   label: 'Efeitos',      icon: 'sparkle' },
  { tab: 'favorites', label: 'Favoritos',    icon: 'heart' },
  { tab: 'devices',   label: 'Dispositivos', icon: 'devices' },
];
