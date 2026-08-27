import { Component, ChangeDetectionStrategy, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { AuthService } from '../core/auth/auth.service';
import { Role, ROLE_LABELS } from '../core/models/auth.models';
import { ThemeService } from '../core/theme/theme.service';
import { BrandLogoComponent } from '../core/ui/brand-logo.component';

type NavIcon =
  | 'dashboard'
  | 'bell'
  | 'fleet'
  | 'drivers'
  | 'link'
  | 'fuel'
  | 'card'
  | 'shield'
  | 'inspect'
  | 'alert'
  | 'suppliers'
  | 'users'
  | 'map'
  | 'building'
  | 'sliders';

interface NavItem {
  label: string;
  route: string;
  icon: NavIcon;
  roles?: Role[];
}

interface NavGroup {
  label: string;
  items: NavItem[];
}

/** Navigation regroupée par domaine métier (RF : lisibilité de l'IA). */
const NAV_GROUPS: readonly NavGroup[] = [
  {
    label: 'Pilotage',
    items: [
      { label: 'Tableau de bord', route: '/', icon: 'dashboard' },
      { label: 'Alertes', route: '/alertes', icon: 'bell' },
    ],
  },
  {
    label: 'Flotte',
    items: [
      { label: 'Véhicules', route: '/vehicules', icon: 'fleet' },
      { label: 'Conducteurs', route: '/conducteurs', icon: 'drivers' },
      { label: 'Affectations', route: '/affectations', icon: 'link' },
    ],
  },
  {
    label: 'Carburant',
    items: [
      { label: 'Demandes', route: '/demandes', icon: 'fuel' },
      { label: 'Cartes', route: '/cartes', icon: 'card' },
    ],
  },
  {
    label: 'Conformité',
    items: [
      { label: 'Assurances', route: '/assurances', icon: 'shield' },
      { label: 'Visites techniques', route: '/visites-techniques', icon: 'inspect' },
      { label: 'Sinistres', route: '/sinistres', icon: 'alert' },
    ],
  },
  {
    label: 'Administration',
    items: [
      { label: 'Fournisseurs', route: '/fournisseurs', icon: 'suppliers' },
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
      {
        label: "Seuils d'alerte",
        route: '/seuils-alerte',
        icon: 'sliders',
        roles: ['ADMINISTRATEUR_CENTRAL'],
      },
    ],
  },
];

const ALL_ITEMS = NAV_GROUPS.flatMap((g) => g.items);
const COLLAPSE_KEY = 'srm-nav-collapsed';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, BrandLogoComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { '(document:keydown.escape)': 'closeNav()' },
  template: `
    <div class="shell" [class.shell--nav-open]="navOpen()" [class.shell--rail]="collapsed()">
      <div class="shell__scrim" (click)="closeNav()" aria-hidden="true"></div>

      <aside class="sidebar">
        <div class="sidebar__brand">
          <a routerLink="/" class="sidebar__brand-link" aria-label="Accueil — SRM Gestion de flotte">
            <app-brand-logo [showLabel]="!collapsed()" />
          </a>
        </div>

        <nav class="sidebar__nav" aria-label="Navigation principale">
          @for (group of visibleNav(); track group.label) {
            <div class="nav-group">
              <p class="nav-group__label">{{ group.label }}</p>
              @for (item of group.items; track item.route) {
                <a
                  class="nav-link"
                  [routerLink]="item.route"
                  routerLinkActive="nav-link--active"
                  [routerLinkActiveOptions]="{ exact: item.route === '/' }"
                  #link="routerLinkActive"
                  [attr.aria-current]="link.isActive ? 'page' : null"
                  [attr.title]="collapsed() ? item.label : null"
                >
                  <svg class="nav-link__icon" viewBox="0 0 20 20" fill="none" aria-hidden="true">
                    @switch (item.icon) {
                      @case ('dashboard') {
                        <path d="M3.5 3.5h5.5v5.5H3.5V3.5Zm7.5 0h5.5v3.5H11V3.5ZM3.5 11h5.5v5.5H3.5V11Zm7.5-2h5.5v7.5H11V9Z" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round" />
                      }
                      @case ('bell') {
                        <path d="M10 3.5a4.5 4.5 0 0 0-4.5 4.5c0 3.5-1.5 4.5-1.5 5.5h12c0-1-1.5-2-1.5-5.5A4.5 4.5 0 0 0 10 3.5ZM8.5 16a1.5 1.5 0 0 0 3 0" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round" />
                      }
                      @case ('fleet') {
                        <path d="M2 13V7h9v6M11 9h3.5L17 12v1h-6" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round" />
                        <circle cx="5.5" cy="14.5" r="1.4" stroke="currentColor" stroke-width="1.4" />
                        <circle cx="13.5" cy="14.5" r="1.4" stroke="currentColor" stroke-width="1.4" />
                      }
                      @case ('drivers') {
                        <circle cx="10" cy="6.5" r="3" stroke="currentColor" stroke-width="1.4" />
                        <path d="M4.5 16.5c0-3 2.5-4.5 5.5-4.5s5.5 1.5 5.5 4.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round" />
                      }
                      @case ('link') {
                        <path d="M8 12a3 3 0 0 0 4 0l2-2a3 3 0 0 0-4-4l-1 1" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round" />
                        <path d="M12 8a3 3 0 0 0-4 0l-2 2a3 3 0 0 0 4 4l1-1" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round" />
                      }
                      @case ('fuel') {
                        <path d="M5 4.5h6v11H5v-11Zm6 3h2l1.5 1.5v5a1.2 1.2 0 0 1-2.4 0V11H11M5 8h6" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round" />
                      }
                      @case ('card') {
                        <path d="M3 6.5h14v7H3v-7Zm0 2.5h14M6 12h3" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round" />
                      }
                      @case ('shield') {
                        <path d="M10 3l6 2v4c0 4-2.7 6.3-6 7.5C6.7 15.3 4 13 4 9V5l6-2Z" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round" />
                      }
                      @case ('inspect') {
                        <path d="M10 17a7 7 0 1 0 0-14 7 7 0 0 0 0 14Zm-3-7 2 2 4-4" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round" />
                      }
                      @case ('alert') {
                        <path d="M10 3.5 2.5 16.5h15L10 3.5Zm0 5v4m0 2.5h.01" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round" />
                      }
                      @case ('suppliers') {
                        <path d="M3 7l7-3 7 3-7 3-7-3z M3 7v6l7 3 7-3V7 M10 10v6" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round" />
                      }
                      @case ('users') {
                        <path d="M7.5 9.5a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5Zm6 0a2 2 0 1 0 0-4M3 15.5c0-2.2 2-3.5 4.5-3.5s4.5 1.3 4.5 3.5M13 12.2c2 .2 3.5 1.4 3.5 3.3" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round" />
                      }
                      @case ('map') {
                        <path d="M7.5 4 3 5.8v10L7.5 14l5 2 4.5-1.8v-10L12.5 6l-5-2Zm0 0v10m5-8v10" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round" stroke-linecap="round" />
                      }
                      @case ('building') {
                        <path d="M4 17.5V4.5h8v13M12 8.5h4.5v9M3 17.5h14M6.5 7.5h1.5M6.5 10.5h1.5M6.5 13.5h1.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round" />
                      }
                      @case ('sliders') {
                        <path d="M4 6.5h7m3 0h2M4 13.5h2m3 0h7M11 4.5v4M6 11.5v4" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round" />
                      }
                    }
                  </svg>
                  <span class="nav-link__label">{{ item.label }}</span>
                </a>
              }
            </div>
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
              [attr.title]="collapsed() ? 'Se déconnecter' : null"
              aria-label="Se déconnecter"
            >
              <svg viewBox="0 0 20 20" aria-hidden="true">
                <path d="M12.5 6V4.5h-8v11h8V14M8.5 10h9m0 0-2.5-2.5M17.5 10 15 12.5" fill="none" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round" />
              </svg>
            </button>
          </div>
        }
      </aside>

      <div class="shell__main">
        <header class="topbar">
          <button
            type="button"
            class="btn btn--ghost topbar__icon-btn topbar__toggle"
            (click)="toggleNav()"
            [attr.aria-expanded]="navOpen()"
            aria-label="Ouvrir la navigation"
          >
            <svg viewBox="0 0 20 20" aria-hidden="true">
              <path d="M3 5.5h14M3 10h14M3 14.5h14" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
            </svg>
          </button>

          <button
            type="button"
            class="btn btn--ghost topbar__icon-btn topbar__rail"
            (click)="toggleCollapsed()"
            [attr.aria-pressed]="collapsed()"
            aria-label="Réduire ou déployer le menu"
          >
            <svg viewBox="0 0 20 20" aria-hidden="true">
              <path d="M4 4.5h12v11H4z M8 4.5v11" fill="none" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round" />
            </svg>
          </button>

          <nav class="breadcrumb" aria-label="Fil d'ariane">
            <span class="breadcrumb__root">SRM Flotte</span>
            <span class="breadcrumb__sep" aria-hidden="true">/</span>
            <span class="breadcrumb__current">{{ currentLabel() }}</span>
          </nav>

          <div class="topbar__spacer"></div>

          <button
            type="button"
            class="btn btn--ghost topbar__icon-btn"
            (click)="theme.toggle()"
            [attr.aria-label]="isDark() ? 'Passer en thème clair' : 'Passer en thème sombre'"
            [attr.title]="isDark() ? 'Thème clair' : 'Thème sombre'"
          >
            @if (isDark()) {
              <svg viewBox="0 0 20 20" aria-hidden="true">
                <circle cx="10" cy="10" r="3.75" fill="none" stroke="currentColor" stroke-width="1.4" />
                <path d="M10 2.5v2M10 15.5v2M2.5 10h2M15.5 10h2M4.7 4.7l1.4 1.4M13.9 13.9l1.4 1.4M15.3 4.7l-1.4 1.4M6.1 13.9l-1.4 1.4" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" />
              </svg>
            } @else {
              <svg viewBox="0 0 20 20" aria-hidden="true">
                <path d="M16 11.2A6.5 6.5 0 0 1 8.8 4a6.5 6.5 0 1 0 7.2 7.2Z" fill="none" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round" />
              </svg>
            }
          </button>
        </header>

        <main class="content">
          <router-outlet />
        </main>
      </div>
    </div>
  `,
  styles: [
    `
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
        background: var(--surface-1);
        border-right: 1px solid var(--border);
        transition: width var(--transition-slow);
        /* Épinglé au viewport : le menu ne défile pas avec le contenu. */
        position: sticky;
        top: 0;
        align-self: flex-start;
        height: 100vh;
        height: 100dvh;
      }
      .shell--rail .sidebar {
        width: 74px;
      }

      .sidebar__brand {
        display: flex;
        align-items: center;
        height: var(--header-h);
        padding: 0 var(--s-4);
        border-bottom: 1px solid var(--border);
      }
      .sidebar__brand-link {
        display: flex;
        align-items: center;
        min-width: 0;
        text-decoration: none;
        border-radius: var(--r-sm);
      }

      .sidebar__nav {
        display: flex;
        flex-direction: column;
        gap: var(--s-4);
        flex: 1;
        padding: var(--s-4) var(--s-3);
        overflow-y: auto;
      }

      .nav-group {
        display: flex;
        flex-direction: column;
        gap: 2px;
      }
      .nav-group__label {
        padding: 0 var(--s-3);
        margin-bottom: var(--s-1);
        font-size: 0.6875rem;
        font-weight: 700;
        letter-spacing: 0.08em;
        text-transform: uppercase;
        color: var(--text-3);
      }
      .shell--rail .nav-group__label {
        opacity: 0;
        height: 1px;
        margin: var(--s-1) 0;
        overflow: hidden;
      }

      .nav-link {
        position: relative;
        display: flex;
        align-items: center;
        gap: var(--s-3);
        padding: 0.5rem var(--s-3);
        font-size: var(--fs-md);
        font-weight: 500;
        color: var(--text-2);
        text-decoration: none;
        border-radius: var(--r-md);
        transition:
          background var(--transition),
          color var(--transition);
      }
      .nav-link:hover {
        color: var(--text-1);
        background: var(--surface-3);
      }
      .nav-link--active,
      .nav-link--active:hover {
        color: var(--brand-text);
        background: var(--brand-soft);
      }
      /* Repère d'onglet actif. */
      .nav-link--active::before {
        content: '';
        position: absolute;
        left: -3px;
        top: 20%;
        bottom: 20%;
        width: 3px;
        border-radius: var(--r-full);
        background: var(--brand);
      }
      .nav-link__icon {
        flex: none;
        width: 19px;
        height: 19px;
      }
      .shell--rail .nav-link {
        justify-content: center;
        padding: 0.5rem;
      }
      .shell--rail .nav-link__label {
        display: none;
      }

      /* --- Bloc utilisateur --- */
      .sidebar__user {
        display: flex;
        align-items: center;
        gap: var(--s-3);
        padding: var(--s-3);
        border-top: 1px solid var(--border);
      }
      .avatar {
        display: grid;
        place-items: center;
        flex: none;
        width: 32px;
        height: 32px;
        font-size: var(--fs-xs);
        font-weight: 700;
        color: var(--text-inv);
        background: var(--brand);
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
        font-weight: 600;
      }
      .sidebar__role {
        font-size: var(--fs-xs);
        color: var(--text-3);
      }
      .sidebar__logout {
        flex: none;
        padding: var(--s-2);
      }
      .sidebar__logout svg {
        width: 18px;
        height: 18px;
      }
      .shell--rail .sidebar__user {
        flex-direction: column;
      }
      .shell--rail .sidebar__identity {
        display: none;
      }

      /* --- Barre supérieure --- */
      .topbar {
        display: flex;
        align-items: center;
        gap: var(--s-2);
        height: var(--header-h);
        flex: none;
        padding: 0 var(--s-5);
        background: color-mix(in srgb, var(--surface-1) 88%, transparent);
        backdrop-filter: blur(8px);
        border-bottom: 1px solid var(--border);
        position: sticky;
        top: 0;
        z-index: 5;
      }
      .topbar__spacer {
        flex: 1;
      }
      .topbar__icon-btn {
        padding: var(--s-2);
      }
      .topbar__icon-btn svg {
        width: 19px;
        height: 19px;
      }
      .topbar__toggle {
        display: none;
      }

      .breadcrumb {
        display: flex;
        align-items: center;
        gap: var(--s-2);
        font-size: var(--fs-md);
        min-width: 0;
      }
      .breadcrumb__root {
        color: var(--text-3);
        font-weight: 500;
      }
      .breadcrumb__sep {
        color: var(--text-3);
      }
      .breadcrumb__current {
        color: var(--text-1);
        font-weight: 600;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .shell__scrim {
        display: none;
      }

      @media (max-width: 900px) {
        .sidebar {
          position: fixed;
          top: 0;
          bottom: 0;
          left: 0;
          z-index: 40;
          width: var(--sidebar-w) !important;
          transform: translateX(-100%);
          transition: transform var(--transition-slow);
          box-shadow: var(--sh-lg);
        }
        .shell--nav-open .sidebar {
          transform: translateX(0);
        }
        .shell--nav-open .shell__scrim {
          display: block;
          position: fixed;
          inset: 0;
          z-index: 30;
          background: var(--overlay);
        }
        .shell--rail .nav-link__label,
        .shell--rail .nav-group__label,
        .shell--rail .sidebar__identity {
          display: flex;
        }
        .topbar__toggle {
          display: inline-flex;
        }
        .topbar__rail {
          display: none;
        }
        .breadcrumb__root,
        .breadcrumb__sep {
          display: none;
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
  protected readonly theme = inject(ThemeService);
  private readonly router = inject(Router);

  protected readonly navOpen = signal(false);
  protected readonly collapsed = signal(this.readCollapsed());
  protected readonly isDark = computed(() => this.theme.resolved() === 'dark');

  protected readonly visibleNav = computed<NavGroup[]>(() => {
    const role = this.auth.user()?.role;
    return NAV_GROUPS.map((g) => ({
      label: g.label,
      items: g.items.filter((it) => !it.roles || (role && it.roles.includes(role))),
    })).filter((g) => g.items.length > 0);
  });

  protected readonly initials = computed(() =>
    (this.auth.user()?.login ?? '?').slice(0, 2).toUpperCase(),
  );

  protected readonly roleLabel = computed(() => {
    const role = this.auth.user()?.role;
    return role ? ROLE_LABELS[role] : '';
  });

  private readonly url = signal(this.router.url);
  protected readonly currentLabel = computed(() => {
    const url = this.url();
    const match = ALL_ITEMS.filter((item) =>
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
        this.closeNav();
      });
  }

  protected toggleNav(): void {
    this.navOpen.update((open) => !open);
  }

  protected closeNav(): void {
    this.navOpen.set(false);
  }

  protected toggleCollapsed(): void {
    this.collapsed.update((c) => {
      const next = !c;
      try {
        localStorage.setItem(COLLAPSE_KEY, next ? '1' : '0');
      } catch {
        /* ignore */
      }
      return next;
    });
  }

  protected logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }

  private readCollapsed(): boolean {
    try {
      return localStorage.getItem(COLLAPSE_KEY) === '1';
    } catch {
      return false;
    }
  }
}
