import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

const LOGIN_ENDPOINT = '/api/v1/auth/login';

/**
 * Traduit un 401 en fin de session : purge l'état et renvoie vers la page de
 * connexion. L'appel de connexion lui-même est exclu — un 401 y signifie
 * « identifiants invalides », pas « session expirée », et c'est le composant
 * de connexion qui l'affiche.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((err) => {
      if (err.status === 401 && !req.url.startsWith(LOGIN_ENDPOINT)) {
        auth.logout();
        router.navigate(['/login'], { queryParams: { returnUrl: router.url } });
      }
      return throwError(() => err);
    }),
  );
};
