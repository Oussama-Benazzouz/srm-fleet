import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ConducteurService } from './conducteur.service';
import { Conducteur } from './conducteur.models';
import { AuthService } from '../../core/auth/auth.service';
import { AdministrationService } from '../administration/administration.service';
import { Departement, Province } from '../administration/utilisateur.models';

@Component({
  selector: 'app-conducteur-list',
  imports: [RouterLink, FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page">
      <header class="page__head">
        <div>
          <div class="page__title">
            <h2>Conducteurs</h2>
            @if (!loading()) {
              <span class="count" aria-live="polite">{{ conducteurs().length }}</span>
            }
          </div>
          <p class="text-muted text-sm">Fiches conducteurs, rattachement géographique et permis.</p>
        </div>
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
            Nouveau conducteur
          </a>
        }
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
            [(ngModel)]="recherche"
            name="recherche"
            placeholder="Rechercher par matricule, nom ou prénom…"
            (keyup.enter)="charger()"
            aria-label="Recherche"
          />
        </div>
        <select
          class="input select toolbar__select"
          [(ngModel)]="provinceId"
          name="provinceId"
          aria-label="Filtrer par province"
          (ngModelChange)="charger()"
        >
          <option [ngValue]="null">Toutes les provinces</option>
          @for (p of provinces(); track p.id) {
            <option [ngValue]="p.id">{{ p.libelle }}</option>
          }
        </select>
        <select
          class="input select toolbar__select"
          [(ngModel)]="departementId"
          name="departementId"
          aria-label="Filtrer par département"
          (ngModelChange)="charger()"
        >
          <option [ngValue]="null">Tous les départements</option>
          @for (d of departements(); track d.id) {
            <option [ngValue]="d.id">{{ d.libelle }}</option>
          }
        </select>
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
        } @else if (conducteurs().length === 0) {
          <div class="panel__state panel__state--empty">
            <svg class="empty-icon" viewBox="0 0 20 20" aria-hidden="true">
              <circle cx="10" cy="7" r="3" fill="none" stroke="currentColor" stroke-width="1.3" />
              <path
                d="M4.5 16c0-3 2.5-4.5 5.5-4.5s5.5 1.5 5.5 4.5"
                fill="none"
                stroke="currentColor"
                stroke-width="1.3"
                stroke-linecap="round"
              />
            </svg>
            <p class="text-muted text-sm">
              {{ filtresActifs() ? 'Aucun conducteur ne correspond aux filtres.' : 'Aucun conducteur.' }}
            </p>
          </div>
        } @else {
          <div class="panel__scroll">
            <table class="table ctable">
              <thead>
                <tr>
                  <th>Conducteur</th>
                  <th>Province</th>
                  <th>Département</th>
                  <th>Fonction</th>
                  <th>Groupe</th>
                  <th>Permis</th>
                  <th class="ctable__actions-head"><span class="sr-only">Actions</span></th>
                </tr>
              </thead>
              <tbody>
                @for (c of conducteurs(); track c.id) {
                  <tr>
                    <td>
                      <div class="driver">
                        <span class="avatar-sm" aria-hidden="true">{{ initiales(c) }}</span>
                        <span class="driver__id">
                          <span class="driver__name">{{ c.prenom }} {{ c.nom }}</span>
                          <span class="driver__mat">{{ c.matricule }}</span>
                        </span>
                      </div>
                    </td>
                    <td><span class="pill">{{ c.provinceLibelle }}</span></td>
                    <td><span class="pill">{{ c.departementLibelle }}</span></td>
                    <td class="text-muted">{{ c.fonction || '—' }}</td>
                    <td>
                      @if (c.groupeSanguin) {
                        <span class="pill pill--blood">{{ c.groupeSanguin }}</span>
                      } @else {
                        <span class="text-subtle">—</span>
                      }
                    </td>
                    <td>
                      <span class="pill pill--count">{{ c.permis.length }}</span>
                    </td>
                    <td>
                      <div class="ctable__actions">
                        <a class="btn btn--sm" [routerLink]="[c.id]">
                          {{ isAdmin() ? 'Modifier' : 'Consulter' }}
                        </a>
                        @if (isAdmin()) {
                          <button
                            type="button"
                            class="btn btn--sm btn--icon-danger"
                            [disabled]="busyId() === c.id"
                            (click)="supprimer(c)"
                            [attr.aria-label]="'Supprimer ' + c.prenom + ' ' + c.nom"
                          >
                            <svg class="icon" viewBox="0 0 20 20" aria-hidden="true">
                              <path
                                d="M6 6.5 6.6 15a1 1 0 0 0 1 .9h4.8a1 1 0 0 0 1-.9L14 6.5M4.5 6.5h11M8 6.5V5a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1v1.5M8.7 9v4M11.3 9v4"
                                fill="none"
                                stroke="currentColor"
                                stroke-width="1.4"
                                stroke-linecap="round"
                                stroke-linejoin="round"
                              />
                            </svg>
                          </button>
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

      /* --- En-tête --- */
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
        flex: 0 1 210px;
        min-width: 170px;
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

      /* Densité et hiérarchie inspirées des tables SaaS (Linear/Stripe). */
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

      /* --- Cellule conducteur (avatar + identité) --- */
      .driver {
        display: flex;
        align-items: center;
        gap: var(--s-3);
      }

      .avatar-sm {
        display: grid;
        place-items: center;
        flex: none;
        width: 32px;
        height: 32px;
        font-size: var(--fs-xs);
        font-weight: 600;
        color: var(--c-accent);
        background: var(--c-accent-soft);
        border-radius: var(--r-full);
      }

      .driver__id {
        display: flex;
        flex-direction: column;
        line-height: 1.3;
      }

      .driver__name {
        font-weight: 600;
        color: var(--c-text);
      }

      .driver__mat {
        font-size: var(--fs-xs);
        color: var(--c-text-subtle);
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

      .pill--blood {
        color: var(--c-danger);
        background: var(--c-danger-soft);
        border-color: var(--c-danger-border);
        font-variant-numeric: tabular-nums;
      }

      .pill--count {
        min-width: 1.5rem;
        justify-content: center;
        font-variant-numeric: tabular-nums;
      }

      .text-subtle {
        color: var(--c-text-subtle);
      }

      /* Suppression : icône discrète qui vire au rouge au survol. */
      .btn--icon-danger {
        padding: 0.25rem;
        color: var(--c-text-subtle);
      }

      .btn--icon-danger:hover:not(:disabled) {
        color: var(--c-danger);
        background: var(--c-danger-soft);
        border-color: var(--c-danger-border);
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
export class ConducteurListComponent implements OnInit {
  private readonly service = inject(ConducteurService);
  private readonly referentiel = inject(AdministrationService);
  private readonly auth = inject(AuthService);

  protected readonly isAdmin = this.auth.isAdmin;

  protected readonly conducteurs = signal<Conducteur[]>([]);
  protected readonly provinces = signal<Province[]>([]);
  protected readonly departements = signal<Departement[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  /** Ligne dont une action est en cours, pour désactiver son bouton. */
  protected readonly busyId = signal<number | null>(null);

  protected recherche = '';
  protected provinceId: number | null = null;
  protected departementId: number | null = null;

  /**
   * Méthode (et non `computed`) car elle lit des champs liés par `ngModel`, pas
   * des signaux : la détection de changement la ré-évalue à chaque saisie/filtre.
   */
  protected filtresActifs(): boolean {
    return !!this.recherche || this.provinceId !== null || this.departementId !== null;
  }

  ngOnInit(): void {
    this.charger();
    this.chargerReferentiels();
  }

  /**
   * Recharge la liste en déléguant le filtrage au backend (province,
   * département, recherche). Les critères sont indépendants : n'importe quelle
   * combinaison est transmise telle quelle, sans filtrage local.
   */
  protected charger(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service
      .lister({
        recherche: this.recherche,
        provinceId: this.provinceId,
        departementId: this.departementId,
      })
      .subscribe({
        next: (list) => {
          this.conducteurs.set(list);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('Impossible de charger les conducteurs.');
          this.loading.set(false);
        },
      });
  }

  /** Alimente les deux listes déroulantes (endpoints ouverts en lecture). */
  private chargerReferentiels(): void {
    this.referentiel.listerProvinces().subscribe((list) => this.provinces.set(list));
    this.referentiel.listerDepartements().subscribe((list) => this.departements.set(list));
  }

  protected reinitialiser(): void {
    this.recherche = '';
    this.provinceId = null;
    this.departementId = null;
    this.charger();
  }

  protected initiales(c: Conducteur): string {
    return `${c.prenom.charAt(0)}${c.nom.charAt(0)}`.toUpperCase();
  }

  protected supprimer(c: Conducteur): void {
    if (!confirm(`Supprimer définitivement ${c.prenom} ${c.nom} et ses permis ?`)) return;
    this.busyId.set(c.id);
    this.error.set(null);
    this.service.supprimer(c.id).subscribe({
      next: () => {
        this.conducteurs.update((list) => list.filter((x) => x.id !== c.id));
        this.busyId.set(null);
      },
      error: () => {
        this.error.set('La suppression a échoué.');
        this.busyId.set(null);
      },
    });
  }
}
