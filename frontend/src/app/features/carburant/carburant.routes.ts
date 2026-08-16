import { Routes } from '@angular/router';

export const DEMANDES_ROUTES: Routes = [
  {
    path: '',
    title: 'Demandes de carte',
    loadComponent: () => import('./demande-list.component').then((m) => m.DemandeListComponent),
  },
  {
    path: 'nouvelle',
    title: 'Nouvelle demande',
    loadComponent: () => import('./demande-form.component').then((m) => m.DemandeFormComponent),
  },
  {
    path: ':id',
    title: 'Demande de carte',
    loadComponent: () => import('./demande-detail.component').then((m) => m.DemandeDetailComponent),
  },
];

export const CARTES_ROUTES: Routes = [
  {
    path: '',
    title: 'Cartes carburant',
    loadComponent: () => import('./carte-list.component').then((m) => m.CarteListComponent),
  },
];
