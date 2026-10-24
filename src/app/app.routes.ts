import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'splash', pathMatch: 'full' },
  {
    path: 'splash',
    loadComponent: () => import('./features/splash/splash.component').then(m => m.SplashComponent),
  },

  // Telas principais dentro do shell (abas no celular, sidebar no desktop).
  // Cada filho direto é uma aba; o primeiro segmento da URL define a aba ativa.
  {
    path: '',
    loadComponent: () => import('./layout/shell/shell.component').then(m => m.ShellComponent),
    children: [
      {
        path: 'home',
        loadComponent: () => import('./features/home/home.component').then(m => m.HomeComponent),
      },
      {
        path: 'effects',
        loadComponent: () => import('./features/colors/colors.component').then(m => m.ColorsComponent),
      },
      {
        path: 'favorites',
        loadComponent: () => import('./features/history/history.component').then(m => m.HistoryComponent),
      },
      {
        path: 'devices',
        loadComponent: () => import('./features/devices/devices.component').then(m => m.DevicesComponent),
      },
      // A busca na rede agora fica dentro da aba Dispositivos.
      { path: 'devices/discovery', redirectTo: '/devices' },
    ],
  },

  // Endereços antigos
  { path: 'colors', redirectTo: 'effects' },
  { path: 'history', redirectTo: 'favorites' },
  { path: 'discovery', redirectTo: 'devices' },
  { path: '**', redirectTo: 'home' },
];
