import { inject } from '@angular/core';
import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { AuthService } from './auth.service';

/** Route par défaut de l'application authentifiée. */
export const HOME_URL = '/';

/**
 * Réserve une route aux utilisateurs authentifiés. L'URL demandée est
 * conservée dans `returnUrl` afin que la connexion y ramène l'utilisateur.
 */
export const authGuard: CanActivateFn = (_route, state): true | UrlTree => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.isAuthenticated()) return true;
  return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};

/**
 * Réserve une route aux visiteurs anonymes (page de connexion). Un utilisateur
 * déjà authentifié est renvoyé vers l'application : `/login` n'est jamais rendu
 * pour lui, quel que soit le mode de navigation (URL saisie, retour arrière,
 * lien direct).
 */
export const guestGuard: CanActivateFn = (): true | UrlTree => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.isAuthenticated() ? router.createUrlTree([HOME_URL]) : true;
};

/** Réserve une route aux administrateurs centraux. */
export const adminGuard: CanActivateFn = (): true | UrlTree => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.isAdmin() ? true : router.createUrlTree([HOME_URL]);
};
