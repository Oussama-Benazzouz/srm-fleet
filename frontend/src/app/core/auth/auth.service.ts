import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { CurrentUser, LoginRequest, LoginResponse } from '../models/auth.models';

const TOKEN_KEY = 'srm_token';
const USER_KEY = 'srm_user';

/**
 * Source de vérité de l'état d'authentification.
 *
 * L'état est restauré de façon *synchrone* à la construction du service. Comme
 * les gardes injectent ce service, la session est donc toujours rétablie avant
 * la première décision de routage : aucune course entre le démarrage de
 * l'application et les gardes n'est possible, et aucun APP_INITIALIZER n'est
 * nécessaire.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly _user = signal<CurrentUser | null>(this.restoreSession());
  readonly user = this._user.asReadonly();
  readonly isAuthenticated = computed(() => this._user() !== null);
  readonly isAdmin = computed(() => this._user()?.role === 'ADMINISTRATEUR_CENTRAL');

  constructor() {
    // Maintient l'état aligné sur le LocalStorage lorsqu'un autre onglet se
    // connecte ou se déconnecte.
    window.addEventListener('storage', (event) => {
      if (event.key === TOKEN_KEY || event.key === USER_KEY || event.key === null) {
        this._user.set(this.restoreSession());
      }
    });
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/v1/auth/login', credentials).pipe(
      tap((res) => {
        const user: CurrentUser = { login: res.login, role: res.role, provinceId: res.provinceId };
        localStorage.setItem(TOKEN_KEY, res.token);
        localStorage.setItem(USER_KEY, JSON.stringify(user));
        this._user.set(user);
      }),
    );
  }

  logout(): void {
    this.clearSession();
    this._user.set(null);
  }

  get token(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  /**
   * Une session n'est valide que si le jeton *et* le profil sont présents et
   * que le jeton n'est pas expiré. Toute session partielle ou périmée est
   * purgée pour éviter que l'application se croie authentifiée avec un jeton
   * que le backend rejettera.
   */
  private restoreSession(): CurrentUser | null {
    const token = localStorage.getItem(TOKEN_KEY);
    const raw = localStorage.getItem(USER_KEY);
    if (!token || !raw || this.isExpired(token)) {
      this.clearSession();
      return null;
    }

    try {
      return JSON.parse(raw) as CurrentUser;
    } catch {
      this.clearSession();
      return null;
    }
  }

  private clearSession(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  }

  /**
   * Lit la revendication `exp` du JWT. Il s'agit d'un contrôle de confort côté
   * client : la validation faisant foi reste celle du backend.
   */
  private isExpired(token: string): boolean {
    const payload = token.split('.')[1];
    if (!payload) return true;

    try {
      const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
      const exp = (JSON.parse(json) as { exp?: number }).exp;
      return typeof exp === 'number' ? exp * 1000 <= Date.now() : false;
    } catch {
      return true;
    }
  }
}
