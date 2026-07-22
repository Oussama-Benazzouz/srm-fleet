import { Component, ChangeDetectionStrategy, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { AuthService } from '../core/auth/auth.service';
import { Role } from '../core/models/auth.models';

/** Entrée de navigation du menu latéral. */
interface NavItem {
  label: string;
  route: string;
  icon: 'dashboard' | 'fleet' | 'settings';
  /** Restreint l'entrée à ces rôles ; visible par tous si omis. */
  roles?: Role[];
}

const NAV_ITEMS: readonly NavItem[] = [{ label: 'Tableau de bord', route: '/', icon: 'dashboard' }];

const ROLE_LABELS: Record<Role, string> = {
  ADMINISTRATEUR_CENTRAL: 'Administrateur central',
  RESPONSABLE_PROVINCIAL: 'Responsable provincial',
};

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
                  @case ('fleet') {
                    <path
                      d="M2.5 12.5h15M4.5 12.5V9l2-3.5h7L15.5 9v3.5M6 15v-2.5M14 15v-2.5"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="1.4"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    />
                  }
                  @case ('settings') {
                    <path
                      d="M10 12.5a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5Zm7-2.5-1.8-.6-.5-1.2.8-1.7-1.5-1.5-1.7.8-1.2-.5L10.4 3H9.6l-.7 1.8-1.2.5-1.7-.8-1.5 1.5.8 1.7-.5 1.2L3 10v.8l1.8.7.5 1.2-.8 1.7 1.5 1.5 1.7-.8 1.2.5.7 1.4h.8l.7-1.4 1.2-.5 1.7.8 1.5-1.5-.8-1.7.5-1.2 1.8-.7V10Z"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="1.3"
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

  /** Titre affiché dans la barre supérieure, dérivé de la route active. */
  private readonly url = signal(this.router.url);
  protected readonly currentLabel = computed(
    () => NAV_ITEMS.find((item) => item.route === this.url())?.label ?? 'Tableau de bord',
  );

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
