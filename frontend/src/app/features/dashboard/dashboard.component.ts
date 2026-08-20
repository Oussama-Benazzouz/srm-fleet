import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { ROLE_LABELS } from '../../core/models/auth.models';
import { BarChartComponent } from './bar-chart.component';
import { CompteParLibelle, DashboardData, Segment } from './dashboard.models';
import { DashboardService } from './dashboard.service';
import { DonutChartComponent } from './donut-chart.component';

const COULEURS_STATUT = ['#1a7f4b', '#3f7fc4', '#c77700', '#8b909a'];
const COULEURS_ACQUISITION = ['#3f7fc4', '#1a7f4b'];

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, DonutChartComponent, BarChartComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page">
      <header class="welcome">
        <div>
          <h2>Bonjour, {{ login() }}</h2>
          <p class="text-muted text-sm">{{ sousTitre() }} · {{ dateDuJour }}</p>
        </div>
        @if (data(); as d) {
          <a class="synthese" routerLink="/alertes" [class.synthese--urgent]="d.alertesActives > 0">
            @if (d.alertesActives > 0) {
              {{ d.alertesActives }} alerte(s) à traiter →
            } @else {
              Aucune alerte active ✓
            }
          </a>
        }
      </header>

      @if (error()) {
        <div class="alert alert--danger" role="alert">{{ error() }}</div>
      }

      @if (loading()) {
        <div class="card panel__state">
          <span class="spinner" aria-hidden="true"></span>
          <span class="text-muted text-sm">Chargement du tableau de bord…</span>
        </div>
      } @else if (data(); as d) {
        <!-- Indicateurs clés -->
        <div class="kpis">
          <div class="stat card">
            <span class="stat__value">{{ d.vehicules }}</span>
            <span class="stat__label">Véhicules</span>
            <span class="stat__sub">{{ d.vehiculesDisponibles }} dispo · {{ d.vehiculesAffectes }} affectés</span>
          </div>
          <div class="stat card">
            <span class="stat__value">{{ d.conducteurs }}</span>
            <span class="stat__label">Conducteurs</span>
          </div>
          <div class="stat card">
            <span class="stat__value">{{ d.affectationsActives }}</span>
            <span class="stat__label">Affectations actives</span>
          </div>
          <div class="stat card">
            <span class="stat__value">{{ d.cartesActives }}</span>
            <span class="stat__label">Cartes actives</span>
          </div>
          <div class="stat card">
            <span class="stat__value">{{ d.demandesEnAttente }}</span>
            <span class="stat__label">Demandes en attente</span>
          </div>
          <div class="stat card" [class.stat--alert]="d.alertesActives > 0">
            <span class="stat__value">{{ d.alertesActives }}</span>
            <span class="stat__label">Alertes actives</span>
          </div>
          <div class="stat card" [class.stat--alert]="d.sinistresOuverts > 0">
            <span class="stat__value">{{ d.sinistresOuverts }}</span>
            <span class="stat__label">Sinistres ouverts</span>
          </div>
        </div>

        <!-- Graphes -->
        <div class="charts">
          <div class="card chart">
            <h3 class="chart__title">Véhicules par statut</h3>
            <app-donut-chart [segments]="statutSegments()" unite="véhicules" titre="Véhicules par statut" />
          </div>
          <div class="card chart">
            <h3 class="chart__title">Mode d'acquisition</h3>
            <app-donut-chart [segments]="acquisitionSegments()" unite="véhicules" titre="Mode d'acquisition" />
          </div>
          @if (d.vehiculesParProvince.length > 1) {
            <div class="card chart">
              <h3 class="chart__title">Véhicules par province</h3>
              <app-bar-chart [items]="d.vehiculesParProvince" />
            </div>
          }
          <div class="card chart">
            <h3 class="chart__title">Sinistres par statut</h3>
            <app-bar-chart [items]="d.sinistresParStatut" couleur="#c77700" />
          </div>
          <div class="card chart">
            <h3 class="chart__title">Alertes par type</h3>
            <app-bar-chart [items]="d.alertesParType" />
          </div>
        </div>
      }
    </section>
  `,
  styles: [
    `
      .page {
        display: flex;
        flex-direction: column;
        gap: var(--s-5);
        max-width: 1180px;
      }
      .welcome {
        display: flex;
        align-items: flex-start;
        justify-content: space-between;
        gap: var(--s-4);
        flex-wrap: wrap;
      }
      .welcome p {
        margin-top: var(--s-1);
      }
      .synthese {
        flex: none;
        padding: var(--s-2) var(--s-4);
        font-size: var(--fs-md);
        font-weight: 500;
        color: var(--c-text-muted);
        background: var(--c-surface);
        border: 1px solid var(--c-border);
        border-radius: var(--r-full);
        text-decoration: none;
        transition:
          background var(--transition),
          color var(--transition);
      }
      .synthese:hover {
        background: var(--c-surface-alt);
      }
      .synthese--urgent {
        color: var(--c-danger);
        background: var(--c-danger-soft);
        border-color: var(--c-danger-border);
      }
      .panel__state {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: var(--s-3);
        padding: var(--s-7) var(--s-4);
      }
      /* --- Indicateurs --- */
      .kpis {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
        gap: var(--s-4);
      }
      .stat {
        display: flex;
        flex-direction: column;
        gap: var(--s-1);
        padding: var(--s-4) var(--s-5);
      }
      .stat__value {
        font-size: 1.75rem;
        font-weight: 700;
        line-height: 1.1;
        font-variant-numeric: tabular-nums;
        color: var(--c-text);
      }
      .stat__label {
        font-size: var(--fs-md);
        color: var(--c-text-muted);
      }
      .stat__sub {
        font-size: var(--fs-xs);
        color: var(--c-text-subtle);
      }
      .stat--alert .stat__value {
        color: var(--c-danger);
      }
      /* --- Graphes --- */
      .charts {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
        gap: var(--s-4);
        align-items: start;
      }
      .chart {
        padding: var(--s-5);
        display: flex;
        flex-direction: column;
        gap: var(--s-4);
      }
      .chart__title {
        font-size: var(--fs-md);
        color: var(--c-text);
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
    const perimetre = user.role === 'ADMINISTRATEUR_CENTRAL' ? 'périmètre national' : 'votre province';
    return `${ROLE_LABELS[user.role]} · ${perimetre}`;
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
