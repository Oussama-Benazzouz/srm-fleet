import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ExportButtonComponent } from '../exports/export-button.component';
import { ConformiteService } from './conformite.service';
import { OptionVehicule, Sinistre, STATUT_SINISTRE_LABELS, StatutSinistre } from './conformite.models';

@Component({
  selector: 'app-sinistre-list',
  imports: [RouterLink, FormsModule, ExportButtonComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page">
      <header class="page__head">
        <div>
          <div class="page__title">
            <h2>Sinistres</h2>
            @if (!loading()) {
              <span class="count" aria-live="polite">{{ sinistres().length }}</span>
            }
          </div>
          <p class="text-muted text-sm">
            Déclarations rattachées au véhicule et à l'affectation active à la date des faits (RF-23).
          </p>
        </div>
        <div class="head-actions" style="display:flex;gap:var(--s-2);align-items:center">
          <app-export-button resource="sinistres" [params]="{ statut, vehiculeId }" />
          <a routerLink="nouveau" class="btn btn--primary">
            <svg class="icon" viewBox="0 0 20 20" aria-hidden="true">
              <path
                d="M10 4.5v11M4.5 10h11"
                fill="none"
                stroke="currentColor"
                stroke-width="1.6"
                stroke-linecap="round"
              />
            </svg>
            Déclarer un sinistre
          </a>
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
          <option value="OUVERT">Ouvert</option>
          <option value="EN_COURS">En cours</option>
          <option value="CLOTURE">Clôturé</option>
        </select>
        <select
          class="input select toolbar__select"
          [(ngModel)]="vehiculeId"
          name="vehicule"
          (ngModelChange)="charger()"
          aria-label="Filtrer par véhicule"
        >
          <option [ngValue]="null">Tous les véhicules</option>
          @for (v of vehicules(); track v.id) {
            <option [ngValue]="v.id">{{ v.libelle }}</option>
          }
        </select>
        @if (statut || vehiculeId) {
          <button type="button" class="btn btn--ghost" (click)="reinitialiser()">Réinitialiser</button>
        }
      </div>

      <div class="card panel">
        @if (loading()) {
          <div class="panel__state">
            <span class="spinner" aria-hidden="true"></span>
            <span class="text-muted text-sm">Chargement…</span>
          </div>
        } @else if (sinistres().length === 0) {
          <div class="panel__state panel__state--empty">
            <p class="text-muted text-sm">Aucun sinistre.</p>
          </div>
        } @else {
          <div class="panel__scroll">
            <table class="table ctable">
              <thead>
                <tr>
                  <th>Véhicule</th>
                  <th>Date</th>
                  <th>Statut</th>
                  <th>Conducteur responsable</th>
                  <th class="ctable__actions-head"><span class="sr-only">Actions</span></th>
                </tr>
              </thead>
              <tbody>
                @for (s of sinistres(); track s.id) {
                  <tr>
                    <td class="plate">{{ s.immatriculation }}</td>
                    <td class="text-muted">{{ s.dateSinistre }}</td>
                    <td>
                      <span
                        class="badge"
                        [class.badge--success]="s.statut === 'CLOTURE'"
                        [class.badge--danger]="s.statut === 'OUVERT'"
                        >{{ label(s.statut) }}</span
                      >
                    </td>
                    <td>{{ s.conducteurNomComplet ?? '—' }}</td>
                    <td>
                      <div class="ctable__actions">
                        <a class="btn btn--sm" [routerLink]="[s.id]">Détail</a>
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
export class SinistreListComponent implements OnInit {
  private readonly service = inject(ConformiteService);

  protected readonly sinistres = signal<Sinistre[]>([]);
  protected readonly vehicules = signal<OptionVehicule[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected statut: StatutSinistre | '' = '';
  protected vehiculeId: number | null = null;

  ngOnInit(): void {
    this.service.vehicules().subscribe((v) => this.vehicules.set(v));
    this.charger();
  }

  protected label(s: StatutSinistre): string {
    return STATUT_SINISTRE_LABELS[s];
  }

  protected charger(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.listerSinistres(this.statut, this.vehiculeId).subscribe({
      next: (list) => {
        this.sinistres.set(list);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Impossible de charger les sinistres.');
        this.loading.set(false);
      },
    });
  }

  protected reinitialiser(): void {
    this.statut = '';
    this.vehiculeId = null;
    this.charger();
  }
}
