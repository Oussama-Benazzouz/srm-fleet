import { Routes } from '@angular/router';

export const ASSURANCES_ROUTES: Routes = [
  {
    path: '',
    title: 'Assurances',
    loadComponent: () =>
      import('./assurance-list.component').then((m) => m.AssuranceListComponent),
  },
  {
    path: 'nouvelle',
    title: 'Nouvelle assurance',
    loadComponent: () =>
      import('./assurance-form.component').then((m) => m.AssuranceFormComponent),
  },
];

export const VISITES_ROUTES: Routes = [
  {
    path: '',
    title: 'Visites techniques',
    loadComponent: () => import('./visite-list.component').then((m) => m.VisiteListComponent),
  },
  {
    path: 'nouvelle',
    title: 'Nouvelle visite technique',
    loadComponent: () => import('./visite-form.component').then((m) => m.VisiteFormComponent),
  },
];
