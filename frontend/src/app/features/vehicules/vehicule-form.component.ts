import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { AdministrationService } from '../administration/administration.service';
import { Departement, Province } from '../administration/utilisateur.models';
import { FlotteService } from './flotte.service';
import { Fournisseur, ModeAcquisition, StatutVehicule, TYPES_VEHICULE } from './flotte.models';

@Component({
  selector: 'app-vehicule-form',
  imports: [ReactiveFormsModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page">
      <header class="page__head">
        <div>
          <a routerLink="/vehicules" class="back">← Véhicules</a>
          <h2>{{ id() ? 'Véhicule' : 'Nouveau véhicule' }}</h2>
          <p class="text-muted text-sm">
            {{ readonly() ? 'Consultation (lecture seule).' : 'Fiche véhicule, rattachement et contrat.' }}
          </p>
        </div>
      </header>

      @if (error()) {
        <div class="alert alert--danger" role="alert">{{ error() }}</div>
      }

      <form class="card page__panel" [formGroup]="form" (ngSubmit)="enregistrer()">
        <div class="form__grid">
          <div class="field">
            <label class="field__label" for="imm">Immatriculation</label>
            <input
              id="imm"
              class="input"
              formControlName="immatriculation"
              [attr.aria-invalid]="isInvalid('immatriculation')"
            />
            @if (isInvalid('immatriculation')) {
              <p class="field__error">L'immatriculation est obligatoire.</p>
            }
          </div>
          <div class="field">
            <label class="field__label" for="type">Type</label>
            <select
              id="type"
              class="input select"
              formControlName="typeVehicule"
              [attr.aria-invalid]="isInvalid('typeVehicule')"
            >
              <option value="" disabled>Catégorie</option>
              @for (t of typesVehicule; track t) {
                <option [value]="t">{{ t }}</option>
              }
            </select>
            @if (isInvalid('typeVehicule')) {
              <p class="field__error">Le type est obligatoire.</p>
            }
          </div>
          <div class="field">
            <label class="field__label" for="mode">Mode d'acquisition</label>
            <select id="mode" class="input select" formControlName="modeAcquisition">
              <option value="PROPRIETE">Propriété</option>
              <option value="LOCATION">Location</option>
            </select>
          </div>
          <div class="field">
            <label class="field__label" for="statut">Statut</label>
            <select id="statut" class="input select" formControlName="statut">
              <option value="DISPONIBLE">Disponible</option>
              <option value="AFFECTE">Affecté</option>
              <option value="IMMOBILISE">Immobilisé</option>
              <option value="SORTI_DU_PARC">Sorti du parc</option>
            </select>
          </div>
          <div class="field">
            <label class="field__label" for="province">Province</label>
            <select
              id="province"
              class="input select"
              formControlName="provinceId"
              [attr.aria-invalid]="isInvalid('provinceId')"
            >
              <option [ngValue]="null" disabled>Sélectionner une province</option>
              @for (p of provinces(); track p.id) {
                <option [ngValue]="p.id">{{ p.libelle }}</option>
              }
            </select>
            @if (isInvalid('provinceId')) {
              <p class="field__error">La province est obligatoire (RG1).</p>
            }
          </div>
          <div class="field">
            <label class="field__label" for="departement">Département</label>
            <select id="departement" class="input select" formControlName="departementId">
              <option [ngValue]="null">—</option>
              @for (d of departements(); track d.id) {
                <option [ngValue]="d.id">{{ d.libelle }}</option>
              }
            </select>
          </div>
          <div class="field">
            <label class="field__label" for="miseService">Mise en service</label>
            <input id="miseService" class="input" type="date" formControlName="dateMiseEnService" />
          </div>
        </div>

        @if (mode() === 'LOCATION') {
          <fieldset class="contrat">
            <legend>Contrat de location</legend>
            <p class="text-muted text-sm">Un fournisseur est obligatoire pour un véhicule en location (RG2).</p>
            <div class="form__grid">
              <div class="field">
                <label class="field__label" for="fournisseur">Fournisseur</label>
                <select
                  id="fournisseur"
                  class="input select"
                  formControlName="fournisseurId"
                  [attr.aria-invalid]="fournisseurManquant()"
                >
                  <option [ngValue]="null">—</option>
                  @for (f of fournisseurs(); track f.id) {
                    <option [ngValue]="f.id">{{ f.raisonSociale }}</option>
                  }
                </select>
                @if (fournisseurManquant()) {
                  <p class="field__error">Le fournisseur est obligatoire en location (RG2).</p>
                }
              </div>
              <div class="field">
                <label class="field__label" for="numeroContrat">Numéro de contrat</label>
                <input id="numeroContrat" class="input" formControlName="numeroContrat" />
              </div>
              <div class="field">
                <label class="field__label" for="debutContrat">Début de contrat</label>
                <input id="debutContrat" class="input" type="date" formControlName="dateDebutContrat" />
              </div>
              <div class="field">
                <label class="field__label" for="finContrat">Fin de contrat</label>
                <input id="finContrat" class="input" type="date" formControlName="dateFinContrat" />
              </div>
              <div class="field">
                <label class="field__label" for="loyer">Loyer mensuel (MAD)</label>
                <input id="loyer" class="input" type="number" min="0" step="0.01" formControlName="loyerMensuel" />
              </div>
              <div class="field">
                <label class="field__label" for="plafondKm">Plafond km</label>
                <input id="plafondKm" class="input" type="number" min="0" formControlName="plafondKm" />
              </div>
            </div>
          </fieldset>
        }

        @if (!readonly()) {
          <div class="form__actions">
            <button type="submit" class="btn btn--primary" [disabled]="saving()">
              @if (saving()) {
                <span class="spinner" aria-hidden="true"></span>
              }
              Enregistrer
            </button>
          </div>
        }
      </form>
    </section>
  `,
  styles: [
    `
      .page {
        display: flex;
        flex-direction: column;
        gap: var(--s-5);
        max-width: 720px;
      }

      .back {
        display: inline-block;
        margin-bottom: var(--s-2);
        font-size: var(--fs-sm);
        color: var(--c-text-muted);
        text-decoration: none;
      }

      .back:hover {
        color: var(--c-text);
      }

      .page__head p {
        margin-top: var(--s-1);
      }

      .page__panel {
        padding: var(--s-5);
        display: flex;
        flex-direction: column;
        gap: var(--s-4);
      }

      .form__grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: var(--s-4);
      }

      .contrat {
        margin: 0;
        padding: var(--s-4);
        border: 1px solid var(--c-border);
        border-radius: var(--r-md);
        display: flex;
        flex-direction: column;
        gap: var(--s-4);
      }

      .contrat legend {
        padding: 0 var(--s-2);
        font-size: var(--fs-sm);
        font-weight: 600;
        color: var(--c-text);
      }

      .form__actions {
        display: flex;
        justify-content: flex-end;
        gap: var(--s-3);
      }

      @media (max-width: 560px) {
        .form__grid {
          grid-template-columns: 1fr;
        }
      }
    `,
  ],
})
export class VehiculeFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(FlotteService);
  private readonly referentiel = inject(AdministrationService);
  private readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly typesVehicule = TYPES_VEHICULE;

  protected readonly id = signal<number | null>(null);
  protected readonly provinces = signal<Province[]>([]);
  protected readonly departements = signal<Departement[]>([]);
  protected readonly fournisseurs = signal<Fournisseur[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly saving = signal(false);
  protected readonly submitted = signal(false);

  /** Le Responsable Provincial consulte sans modifier (contrôle réel côté backend — RG12). */
  protected readonly readonly = signal(!this.auth.isAdmin());

  protected readonly form = this.fb.nonNullable.group({
    immatriculation: ['', Validators.required],
    typeVehicule: ['', Validators.required],
    modeAcquisition: ['PROPRIETE' as ModeAcquisition, Validators.required],
    dateMiseEnService: [null as string | null],
    statut: ['DISPONIBLE' as StatutVehicule, Validators.required],
    numeroContrat: [null as string | null],
    dateDebutContrat: [null as string | null],
    dateFinContrat: [null as string | null],
    loyerMensuel: [null as number | null],
    plafondKm: [null as number | null],
    provinceId: [null as number | null, Validators.required],
    departementId: [null as number | null],
    fournisseurId: [null as number | null],
  });

  /** Suit le mode d'acquisition pour afficher/masquer la section contrat (RG2). */
  protected readonly mode = toSignal(this.form.controls.modeAcquisition.valueChanges, {
    initialValue: this.form.controls.modeAcquisition.value,
  });

  ngOnInit(): void {
    this.referentiel.listerProvinces().subscribe((list) => this.provinces.set(list));
    this.referentiel.listerDepartements().subscribe((list) => this.departements.set(list));
    this.service.listerFournisseurs().subscribe((list) => this.fournisseurs.set(list));

    const param = this.route.snapshot.paramMap.get('id');
    if (param && param !== 'nouveau') {
      this.id.set(Number(param));
      this.service.vehicule(this.id()!).subscribe((v) => {
        this.form.patchValue({
          immatriculation: v.immatriculation,
          typeVehicule: v.typeVehicule,
          modeAcquisition: v.modeAcquisition,
          dateMiseEnService: v.dateMiseEnService,
          statut: v.statut,
          numeroContrat: v.numeroContrat,
          dateDebutContrat: v.dateDebutContrat,
          dateFinContrat: v.dateFinContrat,
          loyerMensuel: v.loyerMensuel,
          plafondKm: v.plafondKm,
          provinceId: v.provinceId,
          departementId: v.departementId,
          fournisseurId: v.fournisseurId,
        });
      });
    }

    // Lecture seule : on neutralise l'édition côté UI (le backend reste la barrière).
    if (this.readonly()) {
      this.form.disable();
    }
  }

  protected isInvalid(name: string): boolean {
    const c = this.form.get(name);
    return !!c && c.invalid && (c.touched || this.submitted());
  }

  /** RG2 côté client : signale un fournisseur manquant après tentative d'envoi en location. */
  protected fournisseurManquant(): boolean {
    return this.submitted() && this.mode() === 'LOCATION' && this.form.controls.fournisseurId.value == null;
  }

  protected enregistrer(): void {
    this.submitted.set(true);
    if (this.form.invalid) return;
    if (this.mode() === 'LOCATION' && this.form.controls.fournisseurId.value == null) {
      this.error.set('Un véhicule en location doit avoir un fournisseur (RG2).');
      return;
    }
    this.saving.set(true);
    this.error.set(null);
    const req = this.form.getRawValue();
    const obs = this.id() ? this.service.modifierVehicule(this.id()!, req) : this.service.creerVehicule(req);
    obs.subscribe({
      next: () => this.router.navigate(['/vehicules']),
      error: (e) => {
        this.error.set(e?.error?.detail ?? "Erreur lors de l'enregistrement.");
        this.saving.set(false);
      },
    });
  }
}
