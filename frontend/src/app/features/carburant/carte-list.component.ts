import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/auth/auth.service';
import { CarburantService } from './carburant.service';
import { Carte, STATUT_CARTE_LABELS, StatutCarte } from './carburant.models';

@Component({
  selector: 'app-carte-list',
  imports: [FormsModule, DecimalPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page">
      <header class="page__head">
        <div>
          <div class="page__title">
            <h2>Cartes carburant</h2>
            @if (!loading()) {
              <span class="count" aria-live="polite">{{ cartes().length }}</span>
            }
          </div>
          <p class="text-muted text-sm">
            Cartes émises (plafond fixé à la validation). Historique conservé par la clôture.
          </p>
        </div>
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
          <option value="ACTIVE">Actives</option>
          <option value="CLOTUREE">Clôturées</option>
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
        } @else if (cartes().length === 0) {
          <div class="panel__state panel__state--empty">
            <p class="text-muted text-sm">Aucune carte.</p>
          </div>
        } @else {
          <div class="panel__scroll">
            <table class="table ctable">
              <thead>
                <tr>
                  <th>N° carte</th>
                  <th>Plafond</th>
                  <th>Statut</th>
                  <th>Véhicule</th>
                  <th>Fournisseur</th>
                  <th>Province</th>
                  <th>Émise le</th>
                  <th class="ctable__actions-head"><span class="sr-only">Actions</span></th>
                </tr>
              </thead>
              <tbody>
                @for (c of cartes(); track c.id) {
                  <tr>
                    <td class="plate">{{ c.numeroCarte }}</td>
                    <td class="text-muted">{{ c.plafondMontant | number: '1.2-2' }} MAD</td>
                    <td>
                      <span
                        class="badge"
                        [class.badge--success]="c.statut === 'ACTIVE'"
                        [class.badge--danger]="c.statut === 'CLOTUREE'"
                        >{{ label(c.statut) }}</span
                      >
                    </td>
                    <td>
                      <span class="pill">{{ c.immatriculation }}</span>
                    </td>
                    <td>
                      <span class="pill">{{ c.fournisseurRaisonSociale }}</span>
                    </td>
                    <td class="text-muted">{{ c.provinceLibelle ?? '—' }}</td>
                    <td class="text-muted">{{ c.dateCreation }}</td>
                    <td>
                      <div class="ctable__actions">
                        @if (isAdmin() && c.statut === 'ACTIVE') {
                          <button
                            type="button"
                            class="btn btn--sm btn--ghost"
                            [disabled]="busyId() === c.id"
                            (click)="cloturer(c)"
                          >
                            Clôturer
                          </button>
                        } @else {
                          <span class="text-subtle">—</span>
                        }
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
      .page__head p {
        margin-top: var(--s-1);
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
      .pill {
        display: inline-flex;
        align-items: center;
        padding: 0.125rem var(--s-2);
        font-size: var(--fs-xs);
        font-weight: 500;
        color: var(--c-text-muted);
        background: var(--c-surface-alt);
        border: 1px solid var(--c-border);
        border-radius: var(--r-sm);
        white-space: nowrap;
      }
      .text-subtle {
        color: var(--c-text-subtle);
      }
    `,
  ],
})
export class CarteListComponent implements OnInit {
  private readonly service = inject(CarburantService);
  private readonly auth = inject(AuthService);
  protected readonly isAdmin = this.auth.isAdmin;

  protected readonly cartes = signal<Carte[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly busyId = signal<number | null>(null);
  protected statut = '';

  ngOnInit(): void {
    this.charger();
  }

  protected label(s: StatutCarte): string {
    return STATUT_CARTE_LABELS[s];
  }

  protected charger(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.listerCartes(this.statut).subscribe({
      next: (list) => {
        this.cartes.set(list);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Impossible de charger les cartes.');
        this.loading.set(false);
      },
    });
  }

  protected reinitialiser(): void {
    this.statut = '';
    this.charger();
  }

  protected cloturer(c: Carte): void {
    if (!confirm(`Clôturer définitivement la carte ${c.numeroCarte} ?`)) return;
    this.busyId.set(c.id);
    this.error.set(null);
    this.service.cloturerCarte(c.id).subscribe({
      next: () => {
        this.busyId.set(null);
        this.charger();
      },
      error: (e) => {
        this.error.set(e?.error?.detail ?? 'Clôture impossible.');
        this.busyId.set(null);
      },
    });
  }
}
