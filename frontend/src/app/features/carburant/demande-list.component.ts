import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CarburantService } from './carburant.service';
import { Demande, STATUT_DEMANDE_LABELS, StatutDemande } from './carburant.models';

@Component({
  selector: 'app-demande-list',
  imports: [RouterLink, FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page">
      <header class="page__head">
        <div>
          <div class="page__title">
            <h2>Demandes de carte</h2>
            @if (!loading()) {
              <span class="count" aria-live="polite">{{ demandes().length }}</span>
            }
          </div>
          <p class="text-muted text-sm">
            Demandes de carte carburant, validées ou refusées par le central.
          </p>
        </div>
        <a routerLink="nouvelle" class="btn btn--primary">
          <svg class="icon" viewBox="0 0 20 20" aria-hidden="true">
            <path
              d="M10 4.5v11M4.5 10h11"
              fill="none"
              stroke="currentColor"
              stroke-width="1.6"
              stroke-linecap="round"
            />
          </svg>
          Nouvelle demande
        </a>
      </header>

      @if (error()) {
        <div class="alert alert--danger" role="alert">{{ error() }}</div>
      }

      <div class="toolbar card">
        <select
          class="input select toolbar__select"
          [(ngModel)]="statut"
          name="statut"
          (ngModelChange)="charger()"
          aria-label="Filtrer par statut"
        >
          <option value="">Tous les statuts</option>
          <option value="EN_ATTENTE">En attente</option>
          <option value="VALIDEE">Validées</option>
          <option value="REFUSEE">Refusées</option>
        </select>
        @if (statut) {
          <button type="button" class="btn btn--ghost" (click)="reinitialiser()">
            Réinitialiser
          </button>
        }
      </div>

      <div class="card panel">
        @if (loading()) {
          <div class="panel__state">
            <span class="spinner" aria-hidden="true"></span>
            <span class="text-muted text-sm">Chargement…</span>
          </div>
        } @else if (demandes().length === 0) {
          <div class="panel__state panel__state--empty">
            <p class="text-muted text-sm">Aucune demande.</p>
          </div>
        } @else {
          <div class="panel__scroll">
            <table class="table ctable">
              <thead>
                <tr>
                  <th>Véhicule</th>
                  <th>Demandeur</th>
                  <th>Date</th>
                  <th>Statut</th>
                  <th>Traité le</th>
                  <th class="ctable__actions-head"><span class="sr-only">Actions</span></th>
                </tr>
              </thead>
              <tbody>
                @for (d of demandes(); track d.id) {
                  <tr>
                    <td class="plate">{{ d.immatriculation }}</td>
                    <td>
                      {{ d.demandeurNomComplet ?? '—' }}
                      @if (d.demandeurLogin) {
                        <span class="sub">{{ d.demandeurLogin }}</span>
                      }
                    </td>
                    <td class="text-muted">{{ d.dateDemande }}</td>
                    <td>
                      <span
                        class="badge"
                        [class.badge--success]="d.statut === 'VALIDEE'"
                        [class.badge--danger]="d.statut === 'REFUSEE'"
                        >{{ label(d.statut) }}</span
                      >
                    </td>
                    <td class="text-muted">{{ d.dateTraitement ?? '—' }}</td>
                    <td>
                      <div class="ctable__actions">
                        <a class="btn btn--sm" [routerLink]="[d.id]">
                          {{ d.statut === 'EN_ATTENTE' ? 'Traiter' : 'Détail' }}
                        </a>
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
        max-width: 1180px;
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
      .icon {
        width: 1rem;
        height: 1rem;
      }
      .toolbar {
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        gap: var(--s-3);
        padding: var(--s-3);
      }
      .toolbar__select {
        flex: 0 1 220px;
        min-width: 170px;
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
      .sub {
        display: block;
        font-size: var(--fs-xs);
        color: var(--c-text-subtle);
      }
      @media (max-width: 640px) {
        .page__head {
          flex-direction: column;
        }
        .toolbar__select {
          flex: 1 1 100%;
        }
      }
    `,
  ],
})
export class DemandeListComponent implements OnInit {
  private readonly service = inject(CarburantService);

  protected readonly demandes = signal<Demande[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected statut = '';

  ngOnInit(): void {
    this.charger();
  }

  protected label(s: StatutDemande): string {
    return STATUT_DEMANDE_LABELS[s];
  }

  protected charger(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.listerDemandes(this.statut).subscribe({
      next: (list) => {
        this.demandes.set(list);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Impossible de charger les demandes.');
        this.loading.set(false);
      },
    });
  }

  protected reinitialiser(): void {
    this.statut = '';
    this.charger();
  }
}
