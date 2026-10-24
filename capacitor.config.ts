import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  // Mesmo applicationId do app nativo antigo (iluminus/app/build.gradle),
  // para publicar como atualização na Play Store (exige a mesma chave de assinatura).
  appId: 'com.rad.iluminus',
  appName: 'iLuminus',
  webDir: 'www',
  plugins: {
    // Android 15+ desenha o app atrás das barras do sistema (edge-to-edge).
    // 'css' injeta --safe-area-inset-* no WebView; o tema escuro pede ícones claros.
    SystemBars: {
      insetsHandling: 'css',
      style: 'DARK',
    },
  },
};

export default config;
