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
      // Les routes d'administration seront ajoutées ici
    ],
  },
  { path: '**', redirectTo: '' },
];
