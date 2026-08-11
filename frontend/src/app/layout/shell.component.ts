import { Component, ChangeDetectionStrategy, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { AuthService } from '../core/auth/auth.service';
import { Role, ROLE_LABELS } from '../core/models/auth.models';

/** Entrée de navigation du menu latéral. */
interface NavItem {
  label: string;
  route: string;
  icon: 'dashboard' | 'users' | 'map' | 'building' | 'drivers' | 'fleet' | 'suppliers';
  /** Restreint l'entrée à ces rôles ; visible par tous si omis. */
  roles?: Role[];
}

const NAV_ITEMS: readonly NavItem[] = [
  { label: 'Tableau de bord', route: '/', icon: 'dashboard' },
  {
    label: 'Comptes utilisateurs',
    route: '/administration/utilisateurs',
    icon: 'users',
    roles: ['ADMINISTRATEUR_CENTRAL'],
  },
  {
    label: 'Provinces',
    route: '/administration/provinces',
    icon: 'map',
    roles: ['ADMINISTRATEUR_CENTRAL'],
  },
  {
    label: 'Départements',
    route: '/administration/departements',
    icon: 'building',
    roles: ['ADMINISTRATEUR_CENTRAL'],
  },
  { label: 'Conducteurs', route: '/conducteurs', icon: 'drivers' },
  { label: 'Véhicules', route: '/vehicules', icon: 'fleet' },
  { label: 'Fournisseurs', route: '/fournisseurs', icon: 'suppliers' },
];

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { '(document:keydown.escape)': 'closeNav()' },
  template: `
    <div class="shell" [class.shell--nav-open]="navOpen()">
      <!-- Voile affiché derrière le menu sur mobile. -->
      <div class="shell__scrim" (click)="closeNav()" aria-hidden="true"></div>

      <aside class="sidebar">
        <div class="sidebar__brand">
          <span class="sidebar__mark" aria-hidden="true">SRM</span>
          <span class="sidebar__name">Gestion de flotte</span>
        </div>

        <nav class="sidebar__nav" aria-label="Navigation principale">
          @for (item of visibleNav(); track item.route) {
            <a
              class="nav-link"
              [routerLink]="item.route"
              routerLinkActive="nav-link--active"
              [routerLinkActiveOptions]="{ exact: item.route === '/' }"
              #link="routerLinkActive"
              [attr.aria-current]="link.isActive ? 'page' : null"
            >
              <svg class="nav-link__icon" viewBox="0 0 20 20" aria-hidden="true">
                @switch (item.icon) {
                  @case ('dashboard') {
                    <path
                      d="M3.5 3.5h5.5v5.5H3.5V3.5Zm7.5 0h5.5v3.5H11V3.5ZM3.5 11h5.5v5.5H3.5V11Zm7.5-2h5.5v7.5H11V9Z"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="1.4"
                      stroke-linejoin="round"
                    />
                  }
                  @case ('users') {
                    <path
                      d="M7.5 9.5a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5Zm6 0a2 2 0 1 0 0-4M3 15.5c0-2.2 2-3.5 4.5-3.5s4.5 1.3 4.5 3.5M13 12.2c2 .2 3.5 1.4 3.5 3.3"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="1.4"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />
                  }
                  @case ('map') {
                    <path
                      d="M7.5 4 3 5.8v10L7.5 14l5 2 4.5-1.8v-10L12.5 6l-5-2Zm0 0v10m5-8v10"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="1.4"
                      stroke-linejoin="round"
                      stroke-linecap="round"
                    />
                  }
                  @case ('building') {
                    <path
                      d="M4 17.5V4.5h8v13M12 8.5h4.5v9M3 17.5h14M6.5 7.5h1.5M6.5 10.5h1.5M6.5 13.5h1.5"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="1.4"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />
                  }
                  @case ('drivers') {
                    <svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.4"
                        stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                      <circle cx="10" cy="6.5" r="3" />
                      <path d="M4.5 16.5c0-3 2.5-4.5 5.5-4.5s5.5 1.5 5.5 4.5" />
                    </svg>
                  }
                  @case ('fleet') {
                    <path
                      d="M2 13V7h9v6M11 9h3.5L17 12v1h-6"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="1.4"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />
                    <circle cx="5.5" cy="14.5" r="1.4" fill="none" stroke="currentColor" stroke-width="1.4" />
                    <circle cx="13.5" cy="14.5" r="1.4" fill="none" stroke="currentColor" stroke-width="1.4" />
                  }
                  @case ('suppliers') {
                    <path
                      d="M3 7l7-3 7 3-7 3-7-3z M3 7v6l7 3 7-3V7 M10 10v6"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="1.4"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />
                  }
                }
              </svg>
              <span>{{ item.label }}</span>
            </a>
          }
        </nav>

        @if (auth.user(); as user) {
          <div class="sidebar__user">
            <span class="avatar" aria-hidden="true">{{ initials() }}</span>
            <span class="sidebar__identity">
              <span class="sidebar__login truncate">{{ user.login }}</span>
              <span class="sidebar__role truncate">{{ roleLabel() }}</span>
            </span>
            <button
              type="button"
              class="btn btn--ghost sidebar__logout"
              (click)="logout()"
              aria-label="Se déconnecter"
            >
              <svg viewBox="0 0 20 20" aria-hidden="true">
                <path
                  d="M12.5 6V4.5h-8v11h8V14M8.5 10h9m0 0-2.5-2.5M17.5 10 15 12.5"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.4"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </button>
          </div>
        }
      </aside>

      <div class="shell__main">
        <header class="topbar">
          <button
            type="button"
            class="btn btn--ghost topbar__toggle"
            (click)="toggleNav()"
            [attr.aria-expanded]="navOpen()"
            aria-label="Ouvrir la navigation"
          >
            <svg viewBox="0 0 20 20" aria-hidden="true">
              <path
                d="M3 5.5h14M3 10h14M3 14.5h14"
                fill="none"
                stroke="currentColor"
                stroke-width="1.5"
                stroke-linecap="round"
              />
            </svg>
          </button>
          <h1 class="topbar__title">{{ currentLabel() }}</h1>
        </header>

        <main class="content">
          <router-outlet />
        </main>
      </div>
    </div>
  `,
  styles: [
    `
      /* --- Ossature --- */

      .shell {
        display: flex;
        min-height: 100vh;
        min-height: 100dvh;
      }

      .shell__main {
        display: flex;
        flex-direction: column;
        flex: 1;
        min-width: 0;
      }

      .content {
        flex: 1;
        padding: var(--s-6);
      }

      /* --- Menu latéral --- */

      .sidebar {
        display: flex;
        flex-direction: column;
        flex: none;
        width: var(--sidebar-w);
        background: var(--c-surface);
        border-right: 1px solid var(--c-border);
      }

      .sidebar__brand {
        display: flex;
        align-items: center;
        gap: var(--s-3);
        height: var(--header-h);
        padding: 0 var(--s-4);
        border-bottom: 1px solid var(--c-border);
      }

      .sidebar__mark {
        display: grid;
        place-items: center;
        flex: none;
        width: 26px;
        height: 26px;
        font-size: 0.625rem;
        font-weight: 600;
        color: var(--c-text-invert);
        background: var(--c-accent);
        border-radius: var(--r-sm);
      }

      .sidebar__name {
        font-size: var(--fs-md);
        font-weight: 600;
      }

      .sidebar__nav {
        display: flex;
        flex-direction: column;
        gap: 2px;
        flex: 1;
        padding: var(--s-3);
        overflow-y: auto;
      }

      /* --- Liens de navigation --- */

      .nav-link {
        display: flex;
        align-items: center;
        gap: var(--s-3);
        padding: var(--s-2) var(--s-3);
        font-size: var(--fs-md);
        font-weight: 500;
        color: var(--c-text-muted);
        text-decoration: none;
        border-radius: var(--r-md);
        transition:
          background var(--transition),
          color var(--transition);
      }

      .nav-link:hover {
        color: var(--c-text);
        background: var(--c-surface-alt);
      }

      /* L'état actif est le seul usage de la couleur d'accent dans le menu. */
      .nav-link--active,
      .nav-link--active:hover {
        color: var(--c-accent);
        background: var(--c-accent-soft);
      }

      .nav-link__icon {
        flex: none;
        width: 18px;
        height: 18px;
      }

      /* --- Bloc utilisateur --- */

      .sidebar__user {
        display: flex;
        align-items: center;
        gap: var(--s-3);
        padding: var(--s-3);
        border-top: 1px solid var(--c-border);
      }

      .avatar {
        display: grid;
        place-items: center;
        flex: none;
        width: 30px;
        height: 30px;
        font-size: var(--fs-xs);
        font-weight: 600;
        color: var(--c-text-muted);
        background: var(--c-surface-alt);
        border-radius: var(--r-full);
      }

      .sidebar__identity {
        display: flex;
        flex-direction: column;
        min-width: 0;
        flex: 1;
      }

      .sidebar__login {
        font-size: var(--fs-md);
        font-weight: 500;
      }

      .sidebar__role {
        font-size: var(--fs-xs);
        color: var(--c-text-subtle);
      }

      .sidebar__logout {
        flex: none;
        padding: var(--s-2);
      }

      .sidebar__logout svg {
        width: 18px;
        height: 18px;
      }

      /* --- Barre supérieure --- */

      .topbar {
        display: flex;
        align-items: center;
        gap: var(--s-2);
        height: var(--header-h);
        flex: none;
        padding: 0 var(--s-6);
        background: var(--c-surface);
        border-bottom: 1px solid var(--c-border);
      }

      .topbar__title {
        font-size: var(--fs-base);
        font-weight: 600;
      }

      /* Le déclencheur du menu n'existe qu'en dessous du point de rupture. */
      .topbar__toggle {
        display: none;
        padding: var(--s-2);
      }

      .topbar__toggle svg {
        width: 18px;
        height: 18px;
      }

      .shell__scrim {
        display: none;
      }

      /* --- Adaptatif --- */

      /* Sous 900px, le menu devient un tiroir superposé au contenu. */
      @media (max-width: 900px) {
        .sidebar {
          position: fixed;
          top: 0;
          bottom: 0;
          left: 0;
          z-index: 20;
          transform: translateX(-100%);
          transition: transform var(--transition);
          box-shadow: var(--sh-lg);
        }

        .shell--nav-open .sidebar {
          transform: translateX(0);
        }

        .shell--nav-open .shell__scrim {
          display: block;
          position: fixed;
          inset: 0;
          z-index: 10;
          background: rgba(16, 24, 40, 0.4);
        }

        .topbar__toggle {
          display: inline-flex;
        }

        .topbar {
          padding: 0 var(--s-4);
        }

        .content {
          padding: var(--s-4);
        }
      }
    `,
  ],
})
export class ShellComponent {
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly navOpen = signal(false);

  /** Entrées visibles selon le rôle de l'utilisateur courant. */
  protected readonly visibleNav = computed(() => {
    const role = this.auth.user()?.role;
    return NAV_ITEMS.filter((item) => !item.roles || (role && item.roles.includes(role)));
  });

  protected readonly initials = computed(() =>
    (this.auth.user()?.login ?? '?').slice(0, 2).toUpperCase(),
  );

  protected readonly roleLabel = computed(() => {
    const role = this.auth.user()?.role;
    return role ? ROLE_LABELS[role] : '';
  });

  /**
   * Titre affiché dans la barre supérieure, dérivé de la route active. On
   * retient l'entrée dont la route est le plus long préfixe de l'URL courante,
   * afin que les sous-écrans (formulaires, détails) héritent du titre parent.
   */
  private readonly url = signal(this.router.url);
  protected readonly currentLabel = computed(() => {
    const url = this.url();
    const match = NAV_ITEMS.filter((item) =>
      item.route === '/' ? url === '/' : url.startsWith(item.route),
    ).sort((a, b) => b.route.length - a.route.length)[0];
    return match?.label ?? 'Tableau de bord';
  });

  constructor() {
    this.router.events
      .pipe(
        filter((event): event is NavigationEnd => event instanceof NavigationEnd),
        takeUntilDestroyed(),
      )
      .subscribe((event) => {
        this.url.set(event.urlAfterRedirects);
        this.closeNav(); // Le tiroir mobile se referme après chaque navigation.
      });
  }

  protected toggleNav(): void {
    this.navOpen.update((open) => !open);
  }

  protected closeNav(): void {
    this.navOpen.set(false);
  }

  protected logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
