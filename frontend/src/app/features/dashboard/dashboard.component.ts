import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { ROLE_LABELS } from '../../core/models/auth.models';
import { BarChartComponent } from './bar-chart.component';
import { CompteParLibelle, DashboardData, Segment } from './dashboard.models';
import { DashboardService } from './dashboard.service';
import { DonutChartComponent } from './donut-chart.component';

/** Couleurs sémantiques (jetons thème) pour les répartitions du parc. */
const COULEURS_STATUT = ['var(--success)', 'var(--chart-1)', 'var(--warning)', 'var(--chart-5)'];
const COULEURS_ACQUISITION = ['var(--chart-1)', 'var(--chart-3)'];

interface Attention {
  label: string;
  value: number;
  route: string;
  tone: 'danger' | 'warning' | 'info';
  icon: 'alert' | 'crash' | 'fuel';
}

interface ParcPart {
  label: string;
  value: number;
  color: string;
}

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, DonutChartComponent, BarChartComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page dash">
      <header class="dash__welcome">
        <div>
          <h1>Bonjour, {{ login() }}</h1>
          <p class="page__subtitle">{{ sousTitre() }} · {{ dateDuJour }}</p>
        </div>
      </header>

      @if (error()) {
        <div class="alert alert--danger" role="alert">{{ error() }}</div>
      }

      @if (loading()) {
        <div class="card state">
          <span class="spinner" aria-hidden="true"></span>
          <span class="text-muted text-sm">Chargement du tableau de bord…</span>
        </div>
      } @else if (data(); as d) {
        <!-- Bande principale : état du parc + éléments à traiter. -->
        <div class="dash__hero">
          <article class="card hero-parc">
            <header class="hero-parc__head">
              <div>
                <h2 class="hero-parc__value">{{ d.vehicules }}</h2>
                <p class="hero-parc__label">Véhicules au parc</p>
              </div>
              <a routerLink="/vehicules" class="btn btn--subtle btn--sm">Voir le parc</a>
            </header>

            @if (d.vehicules > 0) {
              <div class="segbar" role="img" aria-label="Répartition du parc par disponibilité">
                @for (p of parcParts(); track p.label) {
                  @if (p.value > 0) {
                    <span
                      class="segbar__seg"
                      [style.flex-grow]="p.value"
                      [style.background]="p.color"
                      [title]="p.label + ' : ' + p.value"
                    ></span>
                  }
                }
              </div>
              <ul class="hero-parc__legend">
                @for (p of parcParts(); track p.label) {
                  <li>
                    <span class="hero-parc__dot" [style.background]="p.color" aria-hidden="true"></span>
                    <span class="hero-parc__leg-label">{{ p.label }}</span>
                    <span class="hero-parc__leg-value">{{ p.value }}</span>
                  </li>
                }
              </ul>
            } @else {
              <p class="text-muted text-sm">Aucun véhicule enregistré.</p>
            }
          </article>

          <article class="card attention">
            <header class="attention__head">
              <h3>À traiter</h3>
              @if (totalAttention() === 0) {
                <span class="badge badge--success">À jour</span>
              }
            </header>
            <ul class="attention__list">
              @for (a of attentions(); track a.label) {
                <a class="attention__item" [class]="'attention__item--' + a.tone" [routerLink]="a.route">
                  <span class="attention__icon" aria-hidden="true">
                    @switch (a.icon) {
                      @case ('alert') {
                        <svg viewBox="0 0 20 20"><path d="M10 3.5 2.5 16.5h15L10 3.5Zm0 5v4m0 2.5h.01" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
                      }
                      @case ('crash') {
                        <svg viewBox="0 0 20 20"><path d="M2 13V8.5h9V13M11 10h3.5L17 12.5V13h-6" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/><path d="m6 3 1 2.5M9 3 8.5 5.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/></svg>
                      }
                      @default {
                        <svg viewBox="0 0 20 20"><path d="M5 4.5h6v11H5v-11Zm6 3h2l1.5 1.5v5a1.2 1.2 0 0 1-2.4 0V11H11M5 8h6" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
                      }
                    }
                  </span>
                  <span class="attention__label">{{ a.label }}</span>
                  <span class="attention__value">{{ a.value }}</span>
                  <svg class="attention__chevron" viewBox="0 0 20 20" aria-hidden="true"><path d="m8 6 4 4-4 4" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
                </a>
              }
            </ul>
          </article>
        </div>

        <!-- Indicateurs secondaires. -->
        <div class="dash__kpis">
          <a class="stat card" routerLink="/conducteurs">
            <span class="stat__value">{{ d.conducteurs }}</span>
            <span class="stat__label">Conducteurs</span>
          </a>
          <a class="stat card" routerLink="/affectations">
            <span class="stat__value">{{ d.affectationsActives }}</span>
            <span class="stat__label">Affectations actives</span>
          </a>
          <a class="stat card" routerLink="/cartes">
            <span class="stat__value">{{ d.cartesActives }}</span>
            <span class="stat__label">Cartes carburant actives</span>
          </a>
        </div>

        <!-- Analyses. -->
        <div class="dash__charts">
          @if (d.vehiculesParProvince.length > 1) {
            <div class="card chart chart--wide">
              <h3 class="chart__title">Véhicules par province</h3>
              <app-bar-chart [items]="d.vehiculesParProvince" />
            </div>
          }
          <div class="card chart">
            <h3 class="chart__title">Véhicules par statut</h3>
            <app-donut-chart [segments]="statutSegments()" unite="véhic." titre="Véhicules par statut" />
          </div>
          <div class="card chart">
            <h3 class="chart__title">Mode d'acquisition</h3>
            <app-donut-chart [segments]="acquisitionSegments()" unite="véhic." titre="Mode d'acquisition" />
          </div>
          <div class="card chart">
            <h3 class="chart__title">Alertes par type</h3>
            <app-bar-chart [items]="d.alertesParType" couleur="var(--warning)" />
          </div>
          <div class="card chart">
            <h3 class="chart__title">Sinistres par statut</h3>
            <app-bar-chart [items]="d.sinistresParStatut" couleur="var(--chart-6)" />
          </div>
        </div>
      }
    </section>
  `,
  styles: [
    `
      .dash {
        gap: var(--s-5);
      }
      .dash__welcome h1 {
        font-size: var(--fs-2xl);
      }

      /* --- Bande principale --- */
      .dash__hero {
        display: grid;
        grid-template-columns: 1.7fr 1fr;
        gap: var(--s-5);
        align-items: stretch;
      }

      .hero-parc {
        padding: var(--s-5);
        display: flex;
        flex-direction: column;
        gap: var(--s-4);
        background:
          radial-gradient(120% 140% at 100% 0%, var(--brand-soft) 0%, transparent 55%),
          var(--surface-1);
      }
      .hero-parc__head {
        display: flex;
        align-items: flex-start;
        justify-content: space-between;
        gap: var(--s-4);
      }
      .hero-parc__value {
        font-size: var(--fs-3xl);
        font-weight: 900;
        line-height: 1;
        font-variant-numeric: tabular-nums;
        color: var(--brand-text);
      }
      .hero-parc__label {
        margin-top: var(--s-1);
        font-size: var(--fs-md);
        color: var(--text-2);
      }
      .segbar {
        display: flex;
        gap: 3px;
        height: 14px;
        margin-top: auto;
      }
      .segbar__seg {
        border-radius: var(--r-sm);
        min-width: 6px;
        transition: flex-grow var(--transition-slow);
      }
      .hero-parc__legend {
        list-style: none;
        margin: 0;
        padding: 0;
        display: flex;
        flex-wrap: wrap;
        gap: var(--s-3) var(--s-5);
      }
      .hero-parc__legend li {
        display: inline-flex;
        align-items: center;
        gap: var(--s-2);
        font-size: var(--fs-md);
      }
      .hero-parc__dot {
        width: 10px;
        height: 10px;
        border-radius: 3px;
        flex: none;
      }
      .hero-parc__leg-label {
        color: var(--text-2);
      }
      .hero-parc__leg-value {
        font-weight: 700;
        color: var(--text-1);
        font-variant-numeric: tabular-nums;
      }

      /* --- À traiter --- */
      .attention {
        padding: var(--s-4) var(--s-4) var(--s-3);
        display: flex;
        flex-direction: column;
        gap: var(--s-3);
      }
      .attention__head {
        display: flex;
        align-items: center;
        justify-content: space-between;
      }
      .attention__list {
        list-style: none;
        margin: 0;
        padding: 0;
        display: flex;
        flex-direction: column;
        gap: 2px;
        flex: 1;
      }
      .attention__item {
        display: flex;
        align-items: center;
        gap: var(--s-3);
        padding: var(--s-2) var(--s-2);
        border-radius: var(--r-md);
        text-decoration: none;
        color: var(--text-1);
        transition: background var(--transition);
      }
      .attention__item:hover {
        background: var(--surface-2);
      }
      .attention__icon {
        display: grid;
        place-items: center;
        width: 2rem;
        height: 2rem;
        flex: none;
        border-radius: var(--r-sm);
        color: var(--text-3);
        background: var(--surface-3);
      }
      .attention__icon svg {
        width: 1.125rem;
        height: 1.125rem;
      }
      .attention__item--danger .attention__icon {
        color: var(--danger-text);
        background: var(--danger-soft);
      }
      .attention__item--warning .attention__icon {
        color: var(--warning-text);
        background: var(--warning-soft);
      }
      .attention__item--info .attention__icon {
        color: var(--info-text);
        background: var(--info-soft);
      }
      .attention__label {
        flex: 1;
        font-size: var(--fs-md);
        color: var(--text-2);
      }
      .attention__value {
        font-size: var(--fs-lg);
        font-weight: 700;
        font-variant-numeric: tabular-nums;
      }
      .attention__item--danger .attention__value {
        color: var(--danger-text);
      }
      .attention__item--warning .attention__value {
        color: var(--warning-text);
      }
      .attention__chevron {
        width: 1rem;
        height: 1rem;
        color: var(--text-3);
        flex: none;
      }

      /* --- KPIs secondaires --- */
      .dash__kpis {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: var(--s-4);
      }
      .stat {
        display: flex;
        flex-direction: column;
        gap: var(--s-1);
        padding: var(--s-4) var(--s-5);
        text-decoration: none;
        transition:
          border-color var(--transition),
          box-shadow var(--transition),
          transform var(--transition);
      }
      a.stat:hover {
        border-color: var(--border-brand);
        box-shadow: var(--sh-md);
        transform: translateY(-1px);
      }
      .stat__value {
        font-size: var(--fs-2xl);
        font-weight: 800;
        line-height: 1.1;
        font-variant-numeric: tabular-nums;
        color: var(--text-1);
      }
      .stat__label {
        font-size: var(--fs-md);
        color: var(--text-2);
      }

      /* --- Analyses --- */
      .dash__charts {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: var(--s-4);
        /* Cartes d'une même rangée à hauteur égale (bas alignés). */
        align-items: stretch;
      }
      .chart {
        padding: var(--s-5);
        display: flex;
        flex-direction: column;
        gap: var(--s-4);
      }
      .chart--wide {
        grid-column: 1 / -1;
      }
      .chart__title {
        font-size: var(--fs-md);
        font-weight: 600;
        color: var(--text-1);
      }

      @media (max-width: 960px) {
        .dash__hero {
          grid-template-columns: 1fr;
        }
      }
      @media (max-width: 720px) {
        .dash__kpis {
          grid-template-columns: 1fr;
        }
        .dash__charts {
          grid-template-columns: 1fr;
        }
      }
    `,
  ],
})
export class DashboardComponent implements OnInit {
  private readonly service = inject(DashboardService);
  private readonly auth = inject(AuthService);

  protected readonly data = signal<DashboardData | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  protected readonly dateDuJour = new Date().toLocaleDateString('fr-FR', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  });

  protected readonly login = computed(() => this.auth.user()?.login ?? '');
  protected readonly sousTitre = computed(() => {
    const user = this.auth.user();
    if (!user) return '';
    const perimetre =
      user.role === 'ADMINISTRATEUR_CENTRAL' ? 'périmètre national' : 'votre province';
    return `${ROLE_LABELS[user.role]} · ${perimetre}`;
  });

  protected readonly parcParts = computed<ParcPart[]>(() => {
    const d = this.data();
    if (!d) return [];
    const autres = Math.max(
      0,
      d.vehicules - d.vehiculesDisponibles - d.vehiculesAffectes - d.vehiculesImmobilises,
    );
    return [
      { label: 'Disponibles', value: d.vehiculesDisponibles, color: 'var(--success)' },
      { label: 'Affectés', value: d.vehiculesAffectes, color: 'var(--chart-1)' },
      { label: 'Immobilisés', value: d.vehiculesImmobilises, color: 'var(--warning)' },
      { label: 'Sortis du parc', value: autres, color: 'var(--chart-5)' },
    ];
  });

  protected readonly attentions = computed<Attention[]>(() => {
    const d = this.data();
    if (!d) return [];
    const items: Attention[] = [
      { label: 'Alertes actives', value: d.alertesActives, route: '/alertes', tone: 'danger', icon: 'alert' },
      { label: 'Sinistres ouverts', value: d.sinistresOuverts, route: '/sinistres', tone: 'warning', icon: 'crash' },
      { label: 'Demandes en attente', value: d.demandesEnAttente, route: '/demandes', tone: 'info', icon: 'fuel' },
    ];
    // Un compteur à zéro n'a rien d'alarmant : on retombe sur un ton neutre.
    return items.map((a) => (a.value === 0 ? { ...a, tone: 'info' as const } : a));
  });

  protected readonly totalAttention = computed(() => {
    const d = this.data();
    return d ? d.alertesActives + d.sinistresOuverts + d.demandesEnAttente : 0;
  });

  protected readonly statutSegments = computed(() =>
    this.colorise(this.data()?.vehiculesParStatut ?? [], COULEURS_STATUT),
  );
  protected readonly acquisitionSegments = computed(() =>
    this.colorise(this.data()?.vehiculesParAcquisition ?? [], COULEURS_ACQUISITION),
  );

  ngOnInit(): void {
    this.service.consulter().subscribe({
      next: (d) => {
        this.data.set(d);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Impossible de charger le tableau de bord.');
        this.loading.set(false);
      },
    });
  }

  private colorise(items: CompteParLibelle[], couleurs: string[]): Segment[] {
    return items.map((it, i) => ({
      libelle: it.libelle,
      valeur: it.valeur,
      couleur: couleurs[i % couleurs.length],
    }));
  }
}
