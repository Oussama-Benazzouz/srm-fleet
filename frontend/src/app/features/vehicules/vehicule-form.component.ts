import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AdministrationService } from '../administration/administration.service';
import { Departement, Province } from '../administration/utilisateur.models';
import { ToastService } from '../../core/ui/toast.service';
import { FlotteService } from './flotte.service';
import { Fournisseur, ModeAcquisition, StatutVehicule, TYPES_VEHICULE } from './flotte.models';

@Component({
  selector: 'app-vehicule-form',
  imports: [ReactiveFormsModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page page--form">
      <a [routerLink]="retour()" class="back-link">
        <svg viewBox="0 0 20 20" width="16" height="16" aria-hidden="true">
          <path d="M12 5l-5 5 5 5" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
        </svg>
        {{ id() ? 'Fiche véhicule' : 'Véhicules' }}
      </a>

      <header class="page__head">
        <div>
          <h1>{{ id() ? 'Modifier le véhicule' : 'Nouveau véhicule' }}</h1>
          <p class="page__subtitle">Identité, rattachement géographique et contrat.</p>
        </div>
      </header>

      @if (error()) {
        <div class="alert alert--danger" role="alert">{{ error() }}</div>
      }

      <form class="card page__panel" [formGroup]="form" (ngSubmit)="enregistrer()">
        <div class="form-grid">
          <div class="field">
            <label class="field__label" for="imm" data-required>Immatriculation</label>
            <input id="imm" class="input" formControlName="immatriculation" [attr.aria-invalid]="isInvalid('immatriculation')" />
            @if (isInvalid('immatriculation')) {
              <p class="field__error">L'immatriculation est obligatoire.</p>
            }
          </div>
          <div class="field">
            <label class="field__label" for="type" data-required>Catégorie</label>
            <select id="type" class="input select" formControlName="typeVehicule" [attr.aria-invalid]="isInvalid('typeVehicule')">
              <option value="" disabled>Sélectionner une catégorie</option>
              @for (t of typesVehicule; track t) {
                <option [value]="t">{{ t }}</option>
              }
            </select>
            @if (isInvalid('typeVehicule')) {
              <p class="field__error">La catégorie est obligatoire.</p>
            }
          </div>
          <div class="field">
            <label class="field__label" for="mode" data-required>Mode d'acquisition</label>
            <select id="mode" class="input select" formControlName="modeAcquisition">
              <option value="PROPRIETE">Propriété</option>
              <option value="LOCATION">Location</option>
            </select>
          </div>
          <div class="field">
            <label class="field__label" for="statut" data-required>Statut</label>
            <select id="statut" class="input select" formControlName="statut">
              <option value="DISPONIBLE">Disponible</option>
              <option value="AFFECTE">Affecté</option>
              <option value="IMMOBILISE">Immobilisé</option>
              <option value="SORTI_DU_PARC">Sorti du parc</option>
            </select>
          </div>
          <div class="field">
            <label class="field__label" for="province" data-required>Province</label>
            <select id="province" class="input select" formControlName="provinceId" [attr.aria-invalid]="isInvalid('provinceId')">
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
          <fieldset class="fieldset">
            <legend>Contrat de location</legend>
            <p class="field__hint">Un fournisseur est obligatoire pour un véhicule en location (RG2).</p>
            <div class="form-grid">
              <div class="field">
                <label class="field__label" for="fournisseur" data-required>Fournisseur</label>
                <select id="fournisseur" class="input select" formControlName="fournisseurId" [attr.aria-invalid]="fournisseurManquant()">
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

        <div class="form-actions">
          <a class="btn" [routerLink]="retour()">Annuler</a>
          <button type="submit" class="btn btn--primary" [disabled]="saving()">
            @if (saving()) {
              <span class="spinner" aria-hidden="true"></span>
            }
            Enregistrer
          </button>
        </div>
      </form>
    </section>
  `,
  styles: [
    `
      .page__panel {
        padding: var(--s-5);
        display: flex;
        flex-direction: column;
        gap: var(--s-5);
      }
    `,
  ],
})
export class VehiculeFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(FlotteService);
  private readonly referentiel = inject(AdministrationService);
  private readonly toast = inject(ToastService);
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

  protected readonly mode = toSignal(this.form.controls.modeAcquisition.valueChanges, {
    initialValue: this.form.controls.modeAcquisition.value,
  });

  /** Cible du bouton retour : la fiche si l'on édite, la liste sinon. */
  protected retour(): string {
    return this.id() ? `/vehicules/${this.id()}` : '/vehicules';
  }

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
  }

  protected isInvalid(name: string): boolean {
    const c = this.form.get(name);
    return !!c && c.invalid && (c.touched || this.submitted());
  }

  protected fournisseurManquant(): boolean {
    return (
      this.submitted() && this.mode() === 'LOCATION' && this.form.controls.fournisseurId.value == null
    );
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
    const edition = this.id() != null;
    const obs = edition ? this.service.modifierVehicule(this.id()!, req) : this.service.creerVehicule(req);
    obs.subscribe({
      next: (v) => {
        this.toast.success(
          edition ? 'Véhicule mis à jour' : 'Véhicule créé',
          v.immatriculation,
        );
        this.router.navigate(['/vehicules', v.id]);
      },
      error: (e) => {
        this.toast.error('Enregistrement impossible', e?.error?.detail);
        this.saving.set(false);
      },
    });
  }
}
