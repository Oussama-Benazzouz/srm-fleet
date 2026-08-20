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
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent),
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
        loadChildren: () =>
          import('./features/vehicules/flotte.routes').then((m) => m.FLOTTE_ROUTES),
      },
      {
        path: 'fournisseurs',
        title: 'Fournisseurs',
        loadComponent: () =>
          import('./features/fournisseurs/fournisseur-list.component').then(
            (m) => m.FournisseurListComponent,
          ),
      },
      {
        path: 'affectations',
        loadChildren: () =>
          import('./features/affectations/affectations.routes').then((m) => m.AFFECTATIONS_ROUTES),
      },
      {
        path: 'demandes',
        loadChildren: () =>
          import('./features/carburant/carburant.routes').then((m) => m.DEMANDES_ROUTES),
      },
      {
        path: 'cartes',
        loadChildren: () =>
          import('./features/carburant/carburant.routes').then((m) => m.CARTES_ROUTES),
      },
      {
        path: 'assurances',
        loadChildren: () =>
          import('./features/conformite/conformite.routes').then((m) => m.ASSURANCES_ROUTES),
      },
      {
        path: 'visites-techniques',
        loadChildren: () =>
          import('./features/conformite/conformite.routes').then((m) => m.VISITES_ROUTES),
      },
      {
        path: 'sinistres',
        loadChildren: () =>
          import('./features/conformite/conformite.routes').then((m) => m.SINISTRES_ROUTES),
      },
      {
        path: 'alertes',
        loadChildren: () =>
          import('./features/alertes/alertes.routes').then((m) => m.ALERTES_ROUTES),
      },
      {
        path: 'seuils-alerte',
        title: "Seuils d'alerte",
        loadComponent: () =>
          import('./features/alertes/seuils-alerte.component').then((m) => m.SeuilsAlerteComponent),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
