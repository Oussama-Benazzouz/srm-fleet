import { Routes } from '@angular/router';
import { adminGuard } from '../../core/auth/auth.guard';

export const CONDUCTEURS_ROUTES: Routes = [
  {
    path: '',
    title: 'Conducteurs',
    loadComponent: () =>
      import('./conducteur-list.component').then((m) => m.ConducteurListComponent),
  },
  {
    path: 'nouveau',
    title: 'Nouveau conducteur',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./conducteur-form.component').then((m) => m.ConducteurFormComponent),
  },
  {
    path: ':id',
    title: 'Fiche conducteur',
    loadComponent: () =>
      import('./conducteur-detail.component').then((m) => m.ConducteurDetailComponent),
  },
  {
    path: ':id/modifier',
    title: 'Modifier le conducteur',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./conducteur-form.component').then((m) => m.ConducteurFormComponent),
  },
];
