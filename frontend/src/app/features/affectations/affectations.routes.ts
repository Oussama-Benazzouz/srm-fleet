import { Routes } from '@angular/router';

export const AFFECTATIONS_ROUTES: Routes = [
  { path: '', title: 'Affectations',
    loadComponent: () => import('./affectation-list.component').then((m) => m.AffectationListComponent) },
  { path: 'nouvelle', title: 'Nouvelle affectation',
    loadComponent: () => import('./affectation-form.component').then((m) => m.AffectationFormComponent) },
  { path: 'historique/:type/:id', title: 'Historique des affectations',
    loadComponent: () => import('./affectation-historique.component').then((m) => m.AffectationHistoriqueComponent) },
];