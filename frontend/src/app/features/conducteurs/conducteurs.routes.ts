import { Routes } from '@angular/router';

export const CONDUCTEURS_ROUTES: Routes = [
  {
    path: '',
    title: 'Conducteurs',
    loadComponent: () => import('./conducteur-list.component').then((m) => m.ConducteurListComponent),
  },
  {
    path: 'nouveau',
    title: 'Nouveau conducteur',
    loadComponent: () => import('./conducteur-form.component').then((m) => m.ConducteurFormComponent),
  },
  {
    path: ':id',
    title: 'Fiche conducteur',
    loadComponent: () => import('./conducteur-form.component').then((m) => m.ConducteurFormComponent),
  },
];