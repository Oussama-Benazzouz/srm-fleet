import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { ToastService } from '../../core/ui/toast.service';
import { CarburantService } from './carburant.service';
import { Demande, OptionFournisseur, STATUT_DEMANDE_LABELS } from './carburant.models';

@Component({
  selector: 'app-demande-detail',
  imports: [ReactiveFormsModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page page--form">
      <a routerLink="/demandes" class="back-link">
        <svg viewBox="0 0 20 20" width="16" height="16" aria-hidden="true">
          <path d="M12 5l-5 5 5 5" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
        </svg>
        Demandes
      </a>

      <header class="page__head">
        <div>
          <h1>Demande de carte</h1>
          <p class="page__subtitle">Détail de la demande et traitement (central).</p>
        </div>
      </header>

      @if (error()) {
        <div class="alert alert--danger" role="alert">{{ error() }}</div>
      }

      @if (demande(); as d) {
        <div class="card page__panel infos">
          <dl class="grid">
            <div>
              <dt>Véhicule</dt>
              <dd class="plate">{{ d.immatriculation }}</dd>
            </div>
            <div>
              <dt>Province</dt>
              <dd>{{ d.provinceLibelle ?? '—' }}</dd>
            </div>
            <div>
              <dt>Demandeur</dt>
              <dd>
                {{ d.demandeurNomComplet ?? '—' }} <span class="sub">{{ d.demandeurLogin }}</span>
              </dd>
            </div>
            <div>
              <dt>Date de demande</dt>
              <dd>{{ d.dateDemande }}</dd>
            </div>
            <div>
              <dt>Statut</dt>
              <dd>
                <span
                  class="badge"
                  [class.badge--success]="d.statut === 'VALIDEE'"
                  [class.badge--danger]="d.statut === 'REFUSEE'"
                  >{{ statutLabel(d.statut) }}</span
                >
              </dd>
            </div>
            <div>
              <dt>Traité le</dt>
              <dd>
                {{ d.dateTraitement ?? '—' }} <span class="sub">{{ d.validateurLogin }}</span>
              </dd>
            </div>
            <div style="grid-column:1 / -1;">
              <dt>Motif</dt>
              <dd>{{ d.motif || '—' }}</dd>
            </div>
          </dl>
        </div>

        @if (isAdmin() && d.statut === 'EN_ATTENTE') {
          <form class="card page__panel" [formGroup]="form" (ngSubmit)="valider()">
            <h3>Valider et émettre la carte</h3>
            <p class="text-muted text-sm">
              Le central fixe le plafond et le fournisseur ; la carte est créée (ACTIVE).
            </p>
            <div class="form__grid">
              <div class="field">
                <label class="field__label" for="numero">Numéro de carte</label>
                <input
                  id="numero"
                  class="input"
                  formControlName="numeroCarte"
                  [attr.aria-invalid]="isInvalid('numeroCarte')"
                />
                @if (isInvalid('numeroCarte')) {
                  <p class="field__error">Numéro obligatoire.</p>
                }
              </div>
              <div class="field">
                <label class="field__label" for="plafond">Plafond (MAD)</label>
                <input
                  id="plafond"
                  class="input"
                  type="number"
                  min="1"
                  step="0.01"
                  formControlName="plafondMontant"
                  [attr.aria-invalid]="isInvalid('plafondMontant')"
                />
                @if (isInvalid('plafondMontant')) {
                  <p class="field__error">Plafond positif obligatoire.</p>
                }
              </div>
              <div class="field">
                <label class="field__label" for="fournisseur">Fournisseur (carburant)</label>
                <select
                  id="fournisseur"
                  class="input select"
                  formControlName="fournisseurId"
                  [attr.aria-invalid]="isInvalid('fournisseurId')"
                >
                  <option [ngValue]="null" disabled>Sélectionner un fournisseur</option>
                  @for (f of fournisseurs(); track f.id) {
                    <option [ngValue]="f.id">{{ f.raisonSociale }}</option>
                  }
                </select>
                @if (isInvalid('fournisseurId')) {
                  <p class="field__error">Fournisseur obligatoire.</p>
                }
                @if (fournisseurs().length === 0) {
                  <p class="field__error">
                    Aucun fournisseur de type carburant. Créez-en un d'abord.
                  </p>
                }
              </div>
            </div>
            <div class="form__actions">
              <button type="button" class="btn btn--ghost" [disabled]="busy()" (click)="refuser()">
                Refuser
              </button>
              <button type="submit" class="btn btn--primary" [disabled]="busy()">
                @if (busy()) {
                  <span class="spinner" aria-hidden="true"></span>
                }
                Valider et émettre
              </button>
            </div>
          </form>
        }
      }
    </section>
  `,
  styles: [
    `
      .grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: var(--s-4);
        margin: 0;
      }
      dt {
        font-size: var(--fs-xs);
        text-transform: uppercase;
        letter-spacing: 0.04em;
        color: var(--c-text-subtle);
      }
      dd {
        margin: var(--s-1) 0 0;
        color: var(--c-text);
      }
      .plate {
        font-weight: 600;
        font-variant-numeric: tabular-nums;
        letter-spacing: 0.02em;
      }
      .sub {
        font-size: var(--fs-xs);
        color: var(--c-text-subtle);
      }
      .form__grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: var(--s-4);
      }
      .form__actions {
        display: flex;
        justify-content: flex-end;
        gap: var(--s-3);
      }
      @media (max-width: 560px) {
        .grid,
        .form__grid {
          grid-template-columns: 1fr;
        }
      }
    `,
  ],
})
export class DemandeDetailComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(CarburantService);
  private readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  protected readonly isAdmin = this.auth.isAdmin;
  protected readonly demande = signal<Demande | null>(null);
  protected readonly fournisseurs = signal<OptionFournisseur[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly busy = signal(false);
  protected readonly submitted = signal(false);
  private id = 0;

  protected readonly form = this.fb.nonNullable.group({
    numeroCarte: ['', Validators.required],
    plafondMontant: [null as number | null, [Validators.required, Validators.min(1)]],
    fournisseurId: [null as number | null, Validators.required],
  });

  ngOnInit(): void {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.service.demande(this.id).subscribe({
      next: (d) => this.demande.set(d),
      error: (e) =>
        this.error.set(
          e?.status === 404
            ? 'Demande introuvable dans votre périmètre.'
            : 'Impossible de charger la demande.',
        ),
    });
    if (this.isAdmin()) {
      this.service.fournisseursCarburant().subscribe((f) => this.fournisseurs.set(f));
    }
  }

  protected statutLabel(s: Demande['statut']): string {
    return STATUT_DEMANDE_LABELS[s];
  }

  protected isInvalid(name: string): boolean {
    const c = this.form.get(name);
    return !!c && c.invalid && (c.touched || this.submitted());
  }

  protected valider(): void {
    this.submitted.set(true);
    if (this.form.invalid) return;
    this.busy.set(true);
    this.error.set(null);
    this.service.validerDemande(this.id, this.form.getRawValue()).subscribe({
      // La carte émise apparaît dans la liste des cartes.
      next: () => {
        this.toast.success('Demande approuvée', 'La carte a été émise.');
        this.router.navigate(['/cartes']);
      },
      error: (e) => {
        this.toast.error('Validation impossible', e?.error?.detail);
        this.busy.set(false);
      },
    });
  }

  protected refuser(): void {
    this.busy.set(true);
    this.error.set(null);
    this.service.refuserDemande(this.id).subscribe({
      next: () => {
        this.toast.success('Demande refusée');
        this.router.navigate(['/demandes']);
      },
      error: (e) => {
        this.toast.error('Refus impossible', e?.error?.detail);
        this.busy.set(false);
      },
    });
  }
}
