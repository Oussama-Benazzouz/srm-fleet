import { Component, ChangeDetectionStrategy, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AdministrationService } from '../administration.service';
import { Utilisateur } from '../utilisateur.models';
import { ROLE_LABELS } from '../../../core/models/auth.models';

@Component({
  selector: 'app-utilisateur-list',
  imports: [RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page">
      <header class="page__head">
        <div>
          <h2>Comptes utilisateurs</h2>
          <p class="text-muted text-sm">Créez et gérez les accès à l'application.</p>
        </div>
        <a routerLink="nouveau" class="btn btn--primary">Nouveau compte</a>
      </header>

      @if (error()) {
        <div class="alert alert--danger" role="alert">{{ error() }}</div>
      }

      <div class="card page__panel">
        @if (loading()) {
          <div class="page__state">
            <span class="spinner" aria-hidden="true"></span>
            <span class="text-muted text-sm">Chargement…</span>
          </div>
        } @else if (utilisateurs().length === 0) {
          <p class="page__state text-muted text-sm">Aucun compte pour le moment.</p>
        } @else {
          <div class="page__scroll">
            <table class="table">
              <thead>
                <tr>
                  <th>Login</th>
                  <th>Nom</th>
                  <th>Email</th>
                  <th>Rôle</th>
                  <th>Statut</th>
                  <th class="table__actions-head"><span class="sr-only">Actions</span></th>
                </tr>
              </thead>
              <tbody>
                @for (u of utilisateurs(); track u.id) {
                  <tr>
                    <td>{{ u.login }}</td>
                    <td>{{ u.prenom }} {{ u.nom }}</td>
                    <td class="text-muted">{{ u.email }}</td>
                    <td>{{ roleLabel(u.role) }}</td>
                    <td>
                      <span
                        class="badge"
                        [class.badge--success]="u.statutCompte === 'ACTIF'"
                        [class.badge--danger]="u.statutCompte === 'DESACTIVE'"
                      >
                        {{ u.statutCompte === 'ACTIF' ? 'Actif' : 'Désactivé' }}
                      </span>
                    </td>
                    <td>
                      <div class="table__actions">
                        <a class="btn btn--sm" [routerLink]="[u.id]">Modifier</a>
                        @if (u.statutCompte === 'ACTIF') {
                          <button
                            type="button"
                            class="btn btn--sm"
                            [disabled]="busyId() === u.id"
                            (click)="desactiver(u)"
                          >
                            Désactiver
                          </button>
                        } @else {
                          <button
                            type="button"
                            class="btn btn--sm"
                            [disabled]="busyId() === u.id"
                            (click)="activer(u)"
                          >
                            Activer
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
        max-width: 1100px;
      }

      .page__head {
        display: flex;
        align-items: flex-start;
        justify-content: space-between;
        gap: var(--s-4);
      }

      .page__head p {
        margin-top: var(--s-1);
      }

      .page__panel {
        padding: 0;
        overflow: hidden;
      }

      /* Le tableau défile horizontalement sur écran étroit sans casser la page. */
      .page__scroll {
        overflow-x: auto;
      }

      .page__state {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: var(--s-3);
        padding: var(--s-7) var(--s-4);
      }

      .table__actions-head {
        text-align: right;
      }
    `,
  ],
})
export class UtilisateurListComponent implements OnInit {
  private readonly service = inject(AdministrationService);

  protected readonly utilisateurs = signal<Utilisateur[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  /** Ligne dont une action est en cours, pour désactiver ses boutons. */
  protected readonly busyId = signal<number | null>(null);

  ngOnInit(): void {
    this.charger();
  }

  protected roleLabel(role: Utilisateur['role']): string {
    return ROLE_LABELS[role];
  }

  private charger(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.listerUtilisateurs().subscribe({
      next: (list) => {
        this.utilisateurs.set(list);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Impossible de charger les comptes.');
        this.loading.set(false);
      },
    });
  }

  protected desactiver(u: Utilisateur): void {
    this.changerStatut(u.id, this.service.desactiver(u.id));
  }

  protected activer(u: Utilisateur): void {
    this.changerStatut(u.id, this.service.activer(u.id));
  }

  private changerStatut(id: number, action: ReturnType<AdministrationService['activer']>): void {
    this.busyId.set(id);
    this.error.set(null);
    action.subscribe({
      next: (updated) => {
        this.utilisateurs.update((list) => list.map((u) => (u.id === id ? updated : u)));
        this.busyId.set(null);
      },
      error: () => {
        this.error.set('Le changement de statut a échoué.');
        this.busyId.set(null);
      },
    });
  }
}
