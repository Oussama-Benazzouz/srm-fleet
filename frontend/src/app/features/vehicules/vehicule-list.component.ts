import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { ExportButtonComponent } from '../exports/export-button.component';
import { FlotteService } from './flotte.service';
import { MODE_LABELS, STATUT_LABELS, StatutVehicule, TYPES_VEHICULE, Vehicule } from './flotte.models';

@Component({
  selector: 'app-vehicule-list',
  imports: [RouterLink, FormsModule, ExportButtonComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page">
      <header class="page__head">
        <div>
          <div class="page__title">
            <h2>Véhicules</h2>
            @if (!loading()) {
              <span class="count" aria-live="polite">{{ vehicules().length }}</span>
            }
          </div>
          <p class="text-muted text-sm">Parc de la flotte, rattachement et contrats de location.</p>
        </div>
        <div class="head-actions" style="display:flex;gap:var(--s-2);align-items:center">
          <app-export-button resource="vehicules" [params]="{ immatriculation, type, statut }" />
          @if (isAdmin()) {
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
              Nouveau véhicule
            </a>
          }
        </div>
      </header>

      @if (error()) {
        <div class="alert alert--danger" role="alert">{{ error() }}</div>
      }

      <div class="toolbar card">
        <div class="toolbar__search">
          <svg class="icon toolbar__search-icon" viewBox="0 0 20 20" aria-hidden="true">
            <path
              d="M9 15a6 6 0 1 0 0-12 6 6 0 0 0 0 12Zm4.5-1.5L17 17"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
              stroke-linecap="round"
            />
          </svg>
          <input
            class="input toolbar__input"
            [(ngModel)]="immatriculation"
            name="immatriculation"
            placeholder="Rechercher par immatriculation…"
            (keyup.enter)="charger()"
            aria-label="Recherche par immatriculation"
          />
        </div>
        <select
          class="input select toolbar__select"
          [(ngModel)]="type"
          name="type"
          aria-label="Filtrer par catégorie"
          (ngModelChange)="charger()"
        >
          <option [ngValue]="''">Toutes les catégories</option>
          @for (t of types; track t) {
            <option [ngValue]="t">{{ t }}</option>
          }
        </select>
        <select
          class="input select toolbar__select"
          [(ngModel)]="statut"
          name="statut"
          aria-label="Filtrer par statut"
          (ngModelChange)="charger()"
        >
          <option [ngValue]="''">Tous les statuts</option>
          @for (s of statuts; track s) {
            <option [ngValue]="s">{{ label(s) }}</option>
          }
        </select>
        <button type="button" class="btn" (click)="charger()">Rechercher</button>
        @if (filtresActifs()) {
          <button type="button" class="btn btn--ghost" (click)="reinitialiser()">Réinitialiser</button>
        }
      </div>

      <div class="card panel">
        @if (loading()) {
          <div class="panel__state">
            <span class="spinner" aria-hidden="true"></span>
            <span class="text-muted text-sm">Chargement…</span>
          </div>
        } @else if (vehicules().length === 0) {
          <div class="panel__state panel__state--empty">
            <svg class="empty-icon" viewBox="0 0 20 20" aria-hidden="true">
              <path
                d="M2.5 13V7.5h9V13M11.5 9.5h3.2L17 12.3V13h-5.5"
                fill="none"
                stroke="currentColor"
                stroke-width="1.3"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
              <circle cx="6" cy="14.5" r="1.4" fill="none" stroke="currentColor" stroke-width="1.3" />
              <circle cx="13.5" cy="14.5" r="1.4" fill="none" stroke="currentColor" stroke-width="1.3" />
            </svg>
            <p class="text-muted text-sm">
              {{ filtresActifs() ? 'Aucun véhicule ne correspond aux filtres.' : 'Aucun véhicule.' }}
            </p>
          </div>
        } @else {
          <div class="panel__scroll">
            <table class="table ctable">
              <thead>
                <tr>
                  <th>Immatriculation</th>
                  <th>Type</th>
                  <th>Statut</th>
                  <th>Province</th>
                  <th>Acquisition</th>
                  <th>Fournisseur</th>
                  <th class="ctable__actions-head"><span class="sr-only">Actions</span></th>
                </tr>
              </thead>
              <tbody>
                @for (v of vehicules(); track v.id) {
                  <tr>
                    <td class="vehicle-plate">{{ v.immatriculation }}</td>
                    <td class="text-muted">{{ v.typeVehicule }}</td>
                    <td>
                      <span
                        class="badge"
                        [class.badge--success]="v.statut === 'DISPONIBLE'"
                        [class.badge--danger]="v.statut === 'IMMOBILISE' || v.statut === 'SORTI_DU_PARC'"
                      >
                        {{ label(v.statut) }}
                      </span>
                    </td>
                    <td>
                      @if (v.provinceLibelle) {
                        <span class="pill">{{ v.provinceLibelle }}</span>
                      } @else {
                        <span class="text-subtle">—</span>
                      }
                    </td>
                    <td class="text-muted">{{ modeLabel(v.modeAcquisition) }}</td>
                    <td>
                      @if (v.fournisseurRaisonSociale) {
                        <span class="pill">{{ v.fournisseurRaisonSociale }}</span>
                      } @else {
                        <span class="text-subtle">—</span>
                      }
                    </td>
                    <td>
                      <div class="ctable__actions">
                        <a class="btn btn--sm" [routerLink]="[v.id]">Consulter</a>
                        <a class="btn btn--sm" [routerLink]="['/affectations/historique/vehicule', v.id]">
                          Historique
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

      /* --- Barre de filtres --- */
      .toolbar {
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        gap: var(--s-3);
        padding: var(--s-3);
      }

      .toolbar__search {
        position: relative;
        flex: 1 1 260px;
        min-width: 200px;
      }

      .toolbar__search-icon {
        position: absolute;
        left: var(--s-3);
        top: 50%;
        transform: translateY(-50%);
        color: var(--c-text-subtle);
        pointer-events: none;
      }

      .toolbar__input {
        padding-left: 2.25rem;
      }

      .toolbar__select {
        flex: 0 1 190px;
        min-width: 150px;
      }

      /* --- Panneau / tableau --- */
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

      .empty-icon {
        width: 2.25rem;
        height: 2.25rem;
        color: var(--c-text-subtle);
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

      .ctable tbody tr {
        transition: background var(--transition);
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

      .vehicle-plate {
        font-weight: 600;
        color: var(--c-text);
        font-variant-numeric: tabular-nums;
        letter-spacing: 0.02em;
      }

      /* --- Pastilles neutres réutilisant les tokens --- */
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
export class VehiculeListComponent implements OnInit {
  private readonly service = inject(FlotteService);
  private readonly auth = inject(AuthService);

  protected readonly isAdmin = this.auth.isAdmin;

  protected readonly vehicules = signal<Vehicule[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly statuts: StatutVehicule[] = ['DISPONIBLE', 'AFFECTE', 'IMMOBILISE', 'SORTI_DU_PARC'];
  protected readonly types = TYPES_VEHICULE;

  protected immatriculation = '';
  protected type = '';
  protected statut: StatutVehicule | '' = '';

  /** Méthode (et non `computed`) : elle lit des champs liés par `ngModel`, pas des signaux. */
  protected filtresActifs(): boolean {
    return !!this.immatriculation || !!this.type || this.statut !== '';
  }

  ngOnInit(): void {
    this.charger();
  }

  protected label(s: StatutVehicule): string {
    return STATUT_LABELS[s];
  }

  protected modeLabel(m: Vehicule['modeAcquisition']): string {
    return MODE_LABELS[m];
  }

  /** Recharge la liste en déléguant le filtrage (et le cloisonnement province) au backend. */
  protected charger(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service
      .rechercherVehicules({
        immatriculation: this.immatriculation,
        type: this.type,
        statut: this.statut,
      })
      .subscribe({
        next: (list) => {
          this.vehicules.set(list);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('Impossible de charger les véhicules.');
          this.loading.set(false);
        },
      });
  }

  protected reinitialiser(): void {
    this.immatriculation = '';
    this.type = '';
    this.statut = '';
    this.charger();
  }
}