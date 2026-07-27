import { Routes } from '@angular/router';
import { adminGuard } from '../../core/auth/auth.guard';

/**
 * Routes du module administration (RF-27/28), réservées à l'administrateur
 * central. `adminGuard` protège l'ensemble ; le menu latéral n'affiche déjà ces
 * entrées qu'aux administrateurs, mais la garde reste la barrière de sécurité.
 */
export const ADMINISTRATION_ROUTES: Routes = [
  {
    path: '',
    canActivate: [adminGuard],
    children: [
      { path: '', redirectTo: 'utilisateurs', pathMatch: 'full' },
      {
        path: 'utilisateurs',
        title: 'Comptes utilisateurs — SRM Flotte',
        loadComponent: () =>
          import('./utilisateurs/utilisateur-list.component').then((m) => m.UtilisateurListComponent),
      },
      {
        path: 'utilisateurs/:id',
        title: 'Compte utilisateur — SRM Flotte',
        loadComponent: () =>
          import('./utilisateurs/utilisateur-form.component').then((m) => m.UtilisateurFormComponent),
      },
      {
        path: 'provinces',
        title: 'Provinces — SRM Flotte',
        loadComponent: () =>
          import('./provinces/province-list.component').then((m) => m.ProvinceListComponent),
      },
    ],
  },
];
