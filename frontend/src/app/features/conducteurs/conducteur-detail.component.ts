import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { ConfirmService } from '../../core/ui/confirm.service';
import { ToastService } from '../../core/ui/toast.service';
import { Conducteur, Permis } from './conducteur.models';
import { ConducteurService } from './conducteur.service';

/**
 * Fiche conducteur en lecture : identité, rattachement, permis avec état de
 * validité, et actions (modifier / supprimer) réservées à l'administrateur.
 */
@Component({
  selector: 'app-conducteur-detail',
  imports: [RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page detail">
      <a routerLink="/conducteurs" class="back-link">
        <svg viewBox="0 0 20 20" width="16" height="16" aria-hidden="true">
          <path d="M12 5l-5 5 5 5" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
        </svg>
        Conducteurs
      </a>

      @if (error()) {
        <div class="alert alert--danger" role="alert">{{ error() }}</div>
      }

      @if (conducteur(); as c) {
        <header class="page__head">
          <div class="detail__ident">
            <span class="detail__avatar" aria-hidden="true">{{ initials(c) }}</span>
            <div class="stack" style="gap: var(--s-1)">
              <h1>{{ c.prenom }} {{ c.nom }}</h1>
              <p class="page__subtitle">
                <span class="mono">{{ c.matricule }}</span>
                @if (c.fonction) { · {{ c.fonction }} }
              </p>
            </div>
          </div>
          @if (isAdmin()) {
            <div class="page__actions">
              <button type="button" class="btn btn--danger" (click)="supprimer(c)" [disabled]="busy()">
                Supprimer
              </button>
              <a class="btn btn--primary" routerLink="modifier">
                <svg class="icon" viewBox="0 0 20 20" aria-hidden="true"><path d="M13.5 4.5l2 2L7 15l-2.5.5L5 13l8.5-8.5Z" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/></svg>
                Modifier
              </a>
            </div>
          }
        </header>

        <section class="card detail__card">
          <h2 class="detail__section-title">Identité &amp; rattachement</h2>
          <dl class="detail-grid">
            <div class="data">
              <dt class="data__label">Matricule</dt>
              <dd class="data__value mono">{{ c.matricule }}</dd>
            </div>
            <div class="data">
              <dt class="data__label">Fonction</dt>
              <dd class="data__value" [class.data__value--empty]="!c.fonction">{{ c.fonction ?? 'Non renseignée' }}</dd>
            </div>
            <div class="data">
              <dt class="data__label">Groupe sanguin</dt>
              <dd class="data__value" [class.data__value--empty]="!c.groupeSanguin">{{ c.groupeSanguin ?? '—' }}</dd>
            </div>
            <div class="data">
              <dt class="data__label">Province</dt>
              <dd class="data__value">{{ c.provinceLibelle }}</dd>
            </div>
            <div class="data">
              <dt class="data__label">Département</dt>
              <dd class="data__value">{{ c.departementLibelle }}</dd>
            </div>
          </dl>
        </section>

        <section class="card detail__card">
          <h2 class="detail__section-title">
            Permis
            <span class="count">{{ c.permis.length }}</span>
          </h2>
          @if (c.permis.length === 0) {
            <p class="text-muted text-sm">Aucun permis enregistré pour ce conducteur.</p>
          } @else {
            <ul class="permis">
              @for (p of c.permis; track p.id) {
                <li class="permis__item">
                  <span class="permis__type">{{ p.typePermis }}</span>
                  <span class="permis__date">
                    Valide jusqu'au <strong>{{ formatDate(p.dateFinValidite) }}</strong>
                  </span>
                  <span class="badge" [class]="validiteClass(p)">{{ validiteLabel(p) }}</span>
                </li>
              }
            </ul>
          }
        </section>
      } @else if (!error()) {
        <div class="card state">
          <span class="spinner" aria-hidden="true"></span>
          <span class="text-muted text-sm">Chargement de la fiche…</span>
        </div>
      }
    </section>
  `,
  styles: [
    `
      .detail__ident {
        display: flex;
        align-items: center;
        gap: var(--s-4);
      }
      .detail__avatar {
        display: grid;
        place-items: center;
        width: 3rem;
        height: 3rem;
        flex: none;
        font-size: var(--fs-lg);
        font-weight: 700;
        color: var(--brand-text);
        background: var(--brand-soft);
        border-radius: var(--r-full);
      }
      .detail__card {
        padding: var(--s-5);
        display: flex;
        flex-direction: column;
        gap: var(--s-5);
      }
      .detail__section-title {
        display: flex;
        align-items: center;
        gap: var(--s-3);
        font-size: var(--fs-base);
        font-weight: 600;
        color: var(--text-1);
        padding-bottom: var(--s-3);
        border-bottom: 1px solid var(--border);
      }
      .permis {
        list-style: none;
        margin: 0;
        padding: 0;
        display: flex;
        flex-direction: column;
        gap: var(--s-2);
      }
      .permis__item {
        display: flex;
        align-items: center;
        gap: var(--s-4);
        padding: var(--s-3) var(--s-4);
        border: 1px solid var(--border);
        border-radius: var(--r-md);
        background: var(--surface-2);
      }
      .permis__type {
        display: grid;
        place-items: center;
        min-width: 2.75rem;
        height: 2rem;
        padding: 0 var(--s-2);
        font-weight: 700;
        font-size: var(--fs-md);
        color: var(--brand-text);
        background: var(--surface-1);
        border: 1px solid var(--border);
        border-radius: var(--r-sm);
      }
      .permis__date {
        flex: 1;
        font-size: var(--fs-md);
        color: var(--text-2);
      }
      @media (max-width: 520px) {
        .permis__item {
          flex-wrap: wrap;
          gap: var(--s-2);
        }
        .permis__date {
          flex-basis: 100%;
          order: 3;
        }
      }
    `,
  ],
})
export class ConducteurDetailComponent implements OnInit {
  private readonly service = inject(ConducteurService);
  private readonly auth = inject(AuthService);
  private readonly confirm = inject(ConfirmService);
  private readonly toast = inject(ToastService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly isAdmin = this.auth.isAdmin;
  protected readonly conducteur = signal<Conducteur | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly busy = signal(false);
  private id = 0;

  ngOnInit(): void {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.service.parId(this.id).subscribe({
      next: (c) => this.conducteur.set(c),
      error: (e) =>
        this.error.set(
          e?.status === 404 ? 'Conducteur introuvable dans votre périmètre.' : 'Impossible de charger la fiche.',
        ),
    });
  }

  protected initials(c: Conducteur): string {
    return `${c.prenom.charAt(0)}${c.nom.charAt(0)}`.toUpperCase();
  }

  protected formatDate(s: string): string {
    const d = new Date(s);
    return isNaN(d.getTime()) ? s : d.toLocaleDateString('fr-FR', { day: '2-digit', month: 'long', year: 'numeric' });
  }

  /** Jours restants avant expiration du permis. */
  private joursRestants(p: Permis): number {
    const fin = new Date(p.dateFinValidite).getTime();
    return Math.ceil((fin - Date.now()) / 86_400_000);
  }

  protected validiteLabel(p: Permis): string {
    const j = this.joursRestants(p);
    if (j < 0) return 'Expiré';
    if (j <= 30) return `Expire dans ${j} j`;
    return 'Valide';
  }

  protected validiteClass(p: Permis): string {
    const j = this.joursRestants(p);
    if (j < 0) return 'badge--danger';
    if (j <= 30) return 'badge--warning';
    return 'badge--success';
  }

  protected async supprimer(c: Conducteur): Promise<void> {
    const ok = await this.confirm.askDelete(`le conducteur ${c.prenom} ${c.nom}`);
    if (!ok) return;
    this.busy.set(true);
    this.service.supprimer(this.id).subscribe({
      next: () => {
        this.toast.success('Conducteur supprimé', `${c.prenom} ${c.nom}`);
        this.router.navigate(['/conducteurs']);
      },
      error: (e) => {
        this.toast.error('Suppression impossible', e?.error?.detail);
        this.busy.set(false);
      },
    });
  }
}
