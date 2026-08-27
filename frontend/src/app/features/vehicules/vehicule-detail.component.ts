import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { FlotteService } from './flotte.service';
import { MODE_LABELS, STATUT_LABELS, StatutVehicule, Vehicule } from './flotte.models';

/**
 * Fiche véhicule en lecture. Page d'information structurée (et non un
 * formulaire désactivé) : en-tête avec identité + statut + actions, aperçu
 * des attributs clés, puis section contrat affichée seulement en location.
 */
@Component({
  selector: 'app-vehicule-detail',
  imports: [RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page detail">
      <a routerLink="/vehicules" class="back-link">
        <svg viewBox="0 0 20 20" width="16" height="16" aria-hidden="true">
          <path d="M12 5l-5 5 5 5" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
        </svg>
        Véhicules
      </a>

      @if (error()) {
        <div class="alert alert--danger" role="alert">{{ error() }}</div>
      }

      @if (vehicule(); as v) {
        <header class="page__head">
          <div class="stack" style="gap: var(--s-2)">
            <div class="page__title">
              <h1 class="cell-strong">{{ v.immatriculation }}</h1>
              <span class="badge" [class]="statutClass(v.statut)">{{ statutLabel(v.statut) }}</span>
            </div>
            <p class="page__subtitle">
              {{ v.typeVehicule }} · {{ modeLabel(v.modeAcquisition) }}
              @if (v.provinceLibelle) { · {{ v.provinceLibelle }} }
            </p>
          </div>
          <div class="page__actions">
            <a class="btn" [routerLink]="['/affectations/historique/vehicule', v.id]">Historique</a>
            @if (isAdmin()) {
              <a class="btn btn--primary" routerLink="modifier">
                <svg class="icon" viewBox="0 0 20 20" aria-hidden="true"><path d="M13.5 4.5l2 2L7 15l-2.5.5L5 13l8.5-8.5Z" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/></svg>
                Modifier
              </a>
            }
          </div>
        </header>

        <section class="card detail__card">
          <h2 class="detail__section-title">Informations générales</h2>
          <dl class="detail-grid">
            <div class="data">
              <dt class="data__label">Statut</dt>
              <dd class="data__value">
                <span class="badge" [class]="statutClass(v.statut)">{{ statutLabel(v.statut) }}</span>
              </dd>
            </div>
            <div class="data">
              <dt class="data__label">Catégorie</dt>
              <dd class="data__value">{{ v.typeVehicule }}</dd>
            </div>
            <div class="data">
              <dt class="data__label">Mode d'acquisition</dt>
              <dd class="data__value">{{ modeLabel(v.modeAcquisition) }}</dd>
            </div>
            <div class="data">
              <dt class="data__label">Mise en service</dt>
              <dd class="data__value" [class.data__value--empty]="!v.dateMiseEnService">
                {{ formatDate(v.dateMiseEnService) ?? 'Non renseignée' }}
              </dd>
            </div>
            <div class="data">
              <dt class="data__label">Province</dt>
              <dd class="data__value" [class.data__value--empty]="!v.provinceLibelle">
                {{ v.provinceLibelle ?? 'Non rattaché' }}
              </dd>
            </div>
            <div class="data">
              <dt class="data__label">Département</dt>
              <dd class="data__value" [class.data__value--empty]="!v.departementLibelle">
                {{ v.departementLibelle ?? '—' }}
              </dd>
            </div>
          </dl>
        </section>

        @if (v.modeAcquisition === 'LOCATION') {
          <section class="card detail__card">
            <h2 class="detail__section-title">
              Contrat de location
              @if (v.fournisseurRaisonSociale) {
                <span class="pill">{{ v.fournisseurRaisonSociale }}</span>
              }
            </h2>
            <dl class="detail-grid">
              <div class="data">
                <dt class="data__label">Fournisseur</dt>
                <dd class="data__value" [class.data__value--empty]="!v.fournisseurRaisonSociale">
                  {{ v.fournisseurRaisonSociale ?? 'Non renseigné' }}
                </dd>
              </div>
              <div class="data">
                <dt class="data__label">N° de contrat</dt>
                <dd class="data__value" [class.data__value--empty]="!v.numeroContrat">
                  {{ v.numeroContrat ?? '—' }}
                </dd>
              </div>
              <div class="data">
                <dt class="data__label">Début de contrat</dt>
                <dd class="data__value" [class.data__value--empty]="!v.dateDebutContrat">
                  {{ formatDate(v.dateDebutContrat) ?? '—' }}
                </dd>
              </div>
              <div class="data">
                <dt class="data__label">Fin de contrat</dt>
                <dd class="data__value" [class.data__value--empty]="!v.dateFinContrat">
                  {{ formatDate(v.dateFinContrat) ?? '—' }}
                </dd>
              </div>
              <div class="data">
                <dt class="data__label">Loyer mensuel</dt>
                <dd class="data__value mono" [class.data__value--empty]="v.loyerMensuel === null">
                  {{ formatMoney(v.loyerMensuel) ?? '—' }}
                </dd>
              </div>
              <div class="data">
                <dt class="data__label">Plafond kilométrique</dt>
                <dd class="data__value mono" [class.data__value--empty]="v.plafondKm === null">
                  {{ formatKm(v.plafondKm) ?? '—' }}
                </dd>
              </div>
            </dl>
          </section>
        }
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
    `,
  ],
})
export class VehiculeDetailComponent implements OnInit {
  private readonly service = inject(FlotteService);
  private readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);

  protected readonly isAdmin = this.auth.isAdmin;
  protected readonly vehicule = signal<Vehicule | null>(null);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.service.vehicule(id).subscribe({
      next: (v) => this.vehicule.set(v),
      error: (e) =>
        this.error.set(
          e?.status === 404 ? 'Véhicule introuvable dans votre périmètre.' : 'Impossible de charger la fiche.',
        ),
    });
  }

  protected statutLabel(s: StatutVehicule): string {
    return STATUT_LABELS[s];
  }
  protected modeLabel = (m: Vehicule['modeAcquisition']): string => MODE_LABELS[m];

  protected statutClass(s: StatutVehicule): string {
    if (s === 'DISPONIBLE') return 'badge--success';
    if (s === 'AFFECTE') return 'badge--info';
    if (s === 'IMMOBILISE') return 'badge--warning';
    return 'badge--danger';
  }

  protected formatDate(s: string | null): string | null {
    if (!s) return null;
    const d = new Date(s);
    return isNaN(d.getTime()) ? s : d.toLocaleDateString('fr-FR', { day: '2-digit', month: 'long', year: 'numeric' });
  }

  protected formatMoney(n: number | null): string | null {
    if (n == null) return null;
    return n.toLocaleString('fr-FR', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + ' MAD';
  }

  protected formatKm(n: number | null): string | null {
    if (n == null) return null;
    return n.toLocaleString('fr-FR') + ' km';
  }
}
