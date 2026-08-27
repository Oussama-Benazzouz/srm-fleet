import { ChangeDetectionStrategy, Component, OnInit, computed, effect, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ToastService } from '../../core/ui/toast.service';
import { AffectationService } from './affectation.service';
import { OptionConducteur, OptionVehicule } from './affectation.models';

@Component({
  selector: 'app-affectation-form',
  imports: [ReactiveFormsModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page page--form">
      <a routerLink="/affectations" class="back-link">
        <svg viewBox="0 0 20 20" width="16" height="16" aria-hidden="true">
          <path d="M12 5l-5 5 5 5" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
        </svg>
        Affectations
      </a>

      <header class="page__head">
        <div>
          <h1>Nouvelle affectation</h1>
          <p class="page__subtitle">Associer un véhicule disponible à un conducteur de la même province.</p>
        </div>
      </header>

      @if (error()) { <div class="alert alert--danger" role="alert">{{ error() }}</div> }

      <form class="card page__panel" [formGroup]="form" (ngSubmit)="enregistrer()">
        <div class="form__grid">
          <div class="field">
            <label class="field__label" for="vehicule">Véhicule (disponible)</label>
            <select id="vehicule" class="input select" formControlName="vehiculeId"
                    [attr.aria-invalid]="isInvalid('vehiculeId')">
              <option [ngValue]="null">—</option>
              @for (v of vehicules(); track v.id) { <option [ngValue]="v.id">{{ v.immatriculation }}</option> }
            </select>
            @if (isInvalid('vehiculeId')) { <p class="field__error">Choisissez un véhicule.</p> }
            @if (vehicules().length === 0) {
              <p class="field__error">Aucun véhicule disponible dans votre périmètre.</p>
            }
          </div>
          <div class="field">
            <label class="field__label" for="conducteur">Conducteur</label>
            <select id="conducteur" class="input select" formControlName="conducteurId"
                    [attr.aria-invalid]="isInvalid('conducteurId')">
              <option [ngValue]="null">—</option>
              @for (c of conducteursFiltres(); track c.id) { <option [ngValue]="c.id">{{ c.libelle }}</option> }
            </select>
            @if (isInvalid('conducteurId')) { <p class="field__error">Choisissez un conducteur.</p> }
            @if (form.controls.vehiculeId.value !== null && conducteursFiltres().length === 0) {
              <p class="field__error">Aucun conducteur dans la province de ce véhicule.</p>
            }
          </div>
          <div class="field">
            <label class="field__label" for="dateDebut">Date de début</label>
            <input id="dateDebut" class="input" type="date" formControlName="dateDebut" />
          </div>
          <div class="field" style="grid-column:1 / -1;">
            <label class="field__label" for="motif">Motif</label>
            <input id="motif" class="input" formControlName="motif" maxlength="255"
                   placeholder="Mission, remplacement…" />
          </div>
        </div>

        <div class="form__actions">
          <button type="submit" class="btn btn--primary" [disabled]="saving()">
            @if (saving()) { <span class="spinner" aria-hidden="true"></span> }
            Créer l'affectation
          </button>
        </div>
      </form>
    </section>
  `,
  styles: [`
    .form__grid { display:grid; grid-template-columns:repeat(2,1fr); gap:var(--s-4); }
    .form__actions { display:flex; justify-content:flex-end; }
    @media (max-width:560px){ .form__grid { grid-template-columns:1fr; } }
  `],
})
export class AffectationFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(AffectationService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  protected readonly vehicules = signal<OptionVehicule[]>([]);
  protected readonly conducteurs = signal<OptionConducteur[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly saving = signal(false);
  protected readonly submitted = signal(false);

  protected readonly form = this.fb.nonNullable.group({
    vehiculeId: [null as number | null, Validators.required],
    conducteurId: [null as number | null, Validators.required],
    dateDebut: [null as string | null],
    motif: [null as string | null],
  });

  /** Suit le véhicule sélectionné pour restreindre les conducteurs à sa province. */
  private readonly vehiculeId = toSignal(this.form.controls.vehiculeId.valueChanges, {
    initialValue: this.form.controls.vehiculeId.value,
  });

  /**
   * Conducteurs proposés = ceux de la province du véhicule choisi (règle « même province »).
   * Tant qu'aucun véhicule n'est choisi, on montre toute la liste (déjà scopée pour un provincial).
   */
  protected readonly conducteursFiltres = computed(() => {
    const vid = this.vehiculeId();
    if (vid == null) return this.conducteurs();
    const prov = this.vehicules().find((v) => v.id === vid)?.provinceId ?? null;
    if (prov == null) return this.conducteurs();
    return this.conducteurs().filter((c) => c.provinceId === prov);
  });

  constructor() {
    // Si le conducteur choisi n'est plus dans la province du véhicule, on le désélectionne.
    effect(() => {
      const ids = new Set(this.conducteursFiltres().map((c) => c.id));
      const cid = this.form.controls.conducteurId.value;
      if (cid != null && !ids.has(cid)) {
        this.form.controls.conducteurId.setValue(null);
      }
    });
  }

  ngOnInit(): void {
    this.service.vehiculesDisponibles().subscribe((v) => this.vehicules.set(v));
    this.service.conducteurs().subscribe((c) => this.conducteurs.set(c));
  }

  protected isInvalid(name: string): boolean {
    const c = this.form.get(name);
    return !!c && c.invalid && (c.touched || this.submitted());
  }

  protected enregistrer(): void {
    this.submitted.set(true);
    if (this.form.invalid) { return; }
    this.saving.set(true);
    this.error.set(null);
    this.service.creer(this.form.getRawValue() as never).subscribe({
      next: () => {
        this.toast.success('Affectation créée');
        this.router.navigate(['/affectations']);
      },
      error: (e) => {
        this.toast.error('Création impossible', e?.error?.detail);
        this.saving.set(false);
      },
    });
  }
}
