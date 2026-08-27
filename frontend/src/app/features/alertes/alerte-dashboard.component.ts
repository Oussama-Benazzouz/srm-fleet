import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { AuthService } from '../../core/auth/auth.service';
import { Alerte, TYPE_SOURCE_LABELS, TypeSourceAlerte } from './alertes.models';
import { AlertesService } from './alertes.service';

@Component({
  selector: 'app-alerte-dashboard',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page">
      <header class="page__head">
        <div>
          <div class="page__title">
            <h2>Alertes</h2>
            @if (!loading()) {
              <span class="count" aria-live="polite">{{ alertes().length }}</span>
            }
          </div>
          <p class="text-muted text-sm">
            Échéances d'assurance et de contrôle technique qui approchent, les plus urgentes d'abord.
          </p>
        </div>
        @if (isAdmin()) {
          <button type="button" class="btn" [disabled]="detecting()" (click)="lancerDetection()">
            @if (detecting()) {
              <span class="spinner" aria-hidden="true"></span>
            }
            Lancer la détection
          </button>
        }
      </header>

      @if (error()) {
        <div class="alert alert--danger" role="alert">{{ error() }}</div>
      }
      @if (info()) {
        <div class="alert alert--info" role="status">{{ info() }}</div>
      }

      <div class="card panel">
        @if (loading()) {
          <div class="panel__state">
            <span class="spinner" aria-hidden="true"></span>
            <span class="text-muted text-sm">Chargement…</span>
          </div>
        } @else if (alertes().length === 0) {
          <div class="panel__state panel__state--empty">
            <p class="text-muted text-sm">Aucune alerte active. Tout est à jour.</p>
          </div>
        } @else {
          <div class="panel__scroll">
            <table class="table ctable">
              <thead>
                <tr>
                  <th>Urgence</th>
                  <th>Type</th>
                  <th>Véhicule</th>
                  <th>Échéance</th>
                  <th>Conducteur / contact</th>
                  <th class="ctable__actions-head"><span class="sr-only">Actions</span></th>
                </tr>
              </thead>
              <tbody>
                @for (a of alertes(); track a.id) {
                  <tr>
                    <td>
                      <span class="badge" [class.badge--danger]="a.joursRestants < 0">
                        {{ urgence(a) }}
                      </span>
                    </td>
                    <td>{{ typeLabel(a.typeSource) }}</td>
                    <td class="plate">{{ a.immatriculation }}</td>
                    <td class="text-muted">{{ a.dateEcheance }}</td>
                    <td>{{ a.conducteurNomComplet ?? '—' }}</td>
                    <td>
                      <div class="ctable__actions">
                        <button type="button" class="btn btn--sm" (click)="marquerLue(a)">
                          Marquer lue
                        </button>
                      </div>
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }
      </div>
    </section>
  `,
  styles: [
    `
      .page {
        display: flex;
        flex-direction: column;
        gap: var(--s-5);
      }
      .page__head {
        display: flex;
        align-items: flex-start;
        justify-content: space-between;
        gap: var(--s-4);
      }
      .page__title {
        display: flex;
        align-items: center;
        gap: var(--s-3);
      }
      .count {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        min-width: 1.5rem;
        height: 1.5rem;
        padding: 0 var(--s-2);
        font-size: var(--fs-xs);
        font-weight: 600;
        color: var(--c-text-muted);
        background: var(--c-surface-alt);
        border-radius: var(--r-full);
      }
      .page__head p {
        margin-top: var(--s-1);
      }
      .alert--info {
        color: var(--c-accent);
        background: var(--c-accent-soft);
        border-color: var(--c-accent-ring);
      }
      .panel {
        padding: 0;
        overflow: hidden;
      }
      .panel__scroll {
        overflow-x: auto;
      }
      .panel__state {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: var(--s-3);
        padding: var(--s-7) var(--s-4);
      }
      .panel__state--empty {
        flex-direction: column;
      }
      .ctable th {
        font-size: var(--fs-xs);
        font-weight: 600;
        letter-spacing: 0.04em;
        text-transform: uppercase;
        color: var(--c-text-subtle);
      }
      .ctable td {
        padding-top: var(--s-3);
        padding-bottom: var(--s-3);
        vertical-align: middle;
      }
      .ctable__actions-head {
        text-align: right;
      }
      .ctable__actions {
        display: flex;
        gap: var(--s-2);
        justify-content: flex-end;
        align-items: center;
      }
      .plate {
        font-weight: 600;
        color: var(--c-text);
        font-variant-numeric: tabular-nums;
        letter-spacing: 0.02em;
      }
      @media (max-width: 640px) {
        .page__head {
          flex-direction: column;
        }
      }
    `,
  ],
})
export class AlerteDashboardComponent implements OnInit {
  private readonly service = inject(AlertesService);
  private readonly auth = inject(AuthService);

  protected readonly isAdmin = this.auth.isAdmin;
  protected readonly alertes = signal<Alerte[]>([]);
  protected readonly loading = signal(false);
  protected readonly detecting = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly info = signal<string | null>(null);

  ngOnInit(): void {
    this.charger();
  }

  protected typeLabel(t: TypeSourceAlerte): string {
    return TYPE_SOURCE_LABELS[t];
  }

  /** Libellé d'urgence : en retard, aujourd'hui, ou nombre de jours restants. */
  protected urgence(a: Alerte): string {
    if (a.joursRestants < 0) return `En retard de ${-a.joursRestants} j`;
    if (a.joursRestants === 0) return "Échéance aujourd'hui";
    return `Dans ${a.joursRestants} j`;
  }

  protected charger(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.listerAlertes().subscribe({
      next: (list) => {
        this.alertes.set(list);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Impossible de charger les alertes.');
        this.loading.set(false);
      },
    });
  }

  protected marquerLue(a: Alerte): void {
    this.service.marquerLue(a.id).subscribe({
      next: () => this.alertes.update((list) => list.filter((x) => x.id !== a.id)),
      error: () => this.error.set('Impossible de marquer cette alerte comme lue.'),
    });
  }

  protected lancerDetection(): void {
    this.detecting.set(true);
    this.error.set(null);
    this.info.set(null);
    this.service.generer().subscribe({
      next: (r) => {
        this.info.set(`Détection terminée : ${r.creees} nouvelle(s) alerte(s).`);
        this.detecting.set(false);
        this.charger();
      },
      error: (e) => {
        this.error.set(e?.error?.detail ?? 'Échec de la détection.');
        this.detecting.set(false);
      },
    });
  }
}
