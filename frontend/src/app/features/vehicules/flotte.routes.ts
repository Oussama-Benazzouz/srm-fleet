import { Routes } from '@angular/router';
import { adminGuard } from '../../core/auth/auth.guard';

export const FLOTTE_ROUTES: Routes = [
  {
    path: '',
    title: 'Véhicules',
    loadComponent: () => import('./vehicule-list.component').then((m) => m.VehiculeListComponent),
  },
  {
    path: 'nouveau',
    title: 'Nouveau véhicule',
    canActivate: [adminGuard],
    loadComponent: () => import('./vehicule-form.component').then((m) => m.VehiculeFormComponent),
  },
  {
    path: ':id',
    title: 'Fiche véhicule',
    loadComponent: () =>
      import('./vehicule-detail.component').then((m) => m.VehiculeDetailComponent),
  },
  {
    path: ':id/modifier',
    title: 'Modifier le véhicule',
    canActivate: [adminGuard],
    loadComponent: () => import('./vehicule-form.component').then((m) => m.VehiculeFormComponent),
  },
];
