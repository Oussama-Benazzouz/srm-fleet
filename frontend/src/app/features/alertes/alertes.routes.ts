import { Routes } from '@angular/router';

export const ALERTES_ROUTES: Routes = [
  {
    path: '',
    title: 'Alertes',
    loadComponent: () =>
      import('./alerte-dashboard.component').then((m) => m.AlerteDashboardComponent),
  },
];
