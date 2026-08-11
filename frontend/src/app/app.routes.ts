import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    title: 'Connexion — SRM Flotte',
    loadComponent: () => import('./features/auth/login.component').then((m) => m.LoginComponent),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/shell.component').then((m) => m.ShellComponent),
    children: [
      {
        path: '',
        title: 'Tableau de bord — SRM Flotte',
        loadComponent: () => import('./features/home/home.component').then((m) => m.HomeComponent),
      },
      {
        path: 'administration',
        loadChildren: () =>
          import('./features/administration/administration.routes').then(
            (m) => m.ADMINISTRATION_ROUTES,
          ),
      },
      {
        path: 'conducteurs',
        loadChildren: () =>
          import('./features/conducteurs/conducteurs.routes').then((m) => m.CONDUCTEURS_ROUTES),
      },
      {
        path: 'vehicules',
        loadChildren: () => import('./features/vehicules/flotte.routes').then((m) => m.FLOTTE_ROUTES),
      },
      {
        path: 'fournisseurs',
        title: 'Fournisseurs',
        loadComponent: () =>
          import('./features/fournisseurs/fournisseur-list.component').then(
            (m) => m.FournisseurListComponent,
          ),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
