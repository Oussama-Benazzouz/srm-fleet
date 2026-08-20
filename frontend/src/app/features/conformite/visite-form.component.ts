import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ConformiteService } from './conformite.service';
import { OptionVehicule } from './conformite.models';

@Component({
  selector: 'app-visite-form',
  imports: [ReactiveFormsModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page">
      <header class="page__head">
        <div>
          <a routerLink="/visites-techniques" class="back">← Visites techniques</a>
          <h2>Nouvelle visite technique</h2>
          <p class="text-muted text-sm">
            Le conducteur accompagnateur est déterminé automatiquement à partir de l'affectation
            active du véhicule à la date de visite (RG11).
          </p>
        </div>
      </header>

      @if (error()) {
        <div class="alert alert--danger" role="alert">{{ error() }}</div>
      }

      <form class="card page__panel" [formGroup]="form" (ngSubmit)="enregistrer()">
        <div class="form__grid">
          <div class="field" style="grid-column:1 / -1;">
            <label class="field__label" for="vehicule">Véhicule</label>
            <select
              id="vehicule"
              class="input select"
              formControlName="vehiculeId"
              [attr.aria-invalid]="isInvalid('vehiculeId')"
            >
              <option [ngValue]="null" disabled>Sélectionner un véhicule</option>
              @for (v of vehicules(); track v.id) {
                <option [ngValue]="v.id">{{ v.libelle }}</option>
              }
            </select>
            @if (isInvalid('vehiculeId')) {
              <p class="field__error">Choisissez un véhicule.</p>
            }
            @if (vehicules().length === 0) {
              <p class="field__error">Aucun véhicule dans votre périmètre.</p>
            }
          </div>
          <div class="field" style="grid-column:1 / -1;">
            <label class="field__label" for="compagnie">Compagnie ayant réalisé le contrôle</label>
            <select
              id="compagnie"
              class="input select"
              formControlName="nomCompagnie"
              [attr.aria-invalid]="isInvalid('nomCompagnie')"
            >
              <option [ngValue]="null" disabled>Sélectionner un garage</option>
              @for (c of compagnies(); track c) {
                <option [ngValue]="c">{{ c }}</option>
              }
            </select>
            @if (isInvalid('nomCompagnie')) {
              <p class="field__error">La compagnie est obligatoire.</p>
            }
            @if (compagnies().length === 0) {
              <p class="field__error">
                Aucun fournisseur de type « garage ». Créez-en un dans Fournisseurs.
              </p>
            }
          </div>
          <div class="field">
            <label class="field__label" for="visite">Date de visite</label>
            <input id="visite" class="input" type="date" formControlName="dateVisite"
              [attr.aria-invalid]="isInvalid('dateVisite')" />
            @if (isInvalid('dateVisite')) {
              <p class="field__error">La date de visite est obligatoire.</p>
            }
          </div>
          <div class="field">
            <label class="field__label" for="echeance">Date d'échéance</label>
            <input id="echeance" class="input" type="date" formControlName="dateEcheance"
              [attr.aria-invalid]="isInvalid('dateEcheance')" />
            @if (isInvalid('dateEcheance')) {
              <p class="field__error">La date d'échéance est obligatoire.</p>
            }
          </div>
        </div>
        <div class="form__actions">
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
      .page {
        display: flex;
        flex-direction: column;
        gap: var(--s-5);
        max-width: 680px;
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
      .form__actions {
        display: flex;
        justify-content: flex-end;
      }
      @media (max-width: 560px) {
        .form__grid {
          grid-template-columns: 1fr;
        }
      }
    `,
  ],
})
export class VisiteFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ConformiteService);
  private readonly router = inject(Router);

  protected readonly vehicules = signal<OptionVehicule[]>([]);
  protected readonly compagnies = signal<string[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly saving = signal(false);
  protected readonly submitted = signal(false);

  protected readonly form = this.fb.nonNullable.group({
    vehiculeId: [null as number | null, Validators.required],
    nomCompagnie: [null as string | null, Validators.required],
    dateVisite: [null as string | null, Validators.required],
    dateEcheance: [null as string | null, Validators.required],
  });

  ngOnInit(): void {
    this.service.vehicules().subscribe((v) => this.vehicules.set(v));
    this.service.compagnies('GARAGE').subscribe((c) => this.compagnies.set(c));
  }

  protected isInvalid(name: string): boolean {
    const c = this.form.get(name);
    return !!c && c.invalid && (c.touched || this.submitted());
  }

  protected enregistrer(): void {
    this.submitted.set(true);
    if (this.form.invalid) return;
    const v = this.form.getRawValue();
    if (v.dateVisite && v.dateEcheance && v.dateEcheance < v.dateVisite) {
      this.error.set("La date d'échéance doit être postérieure ou égale à la date de visite.");
      return;
    }
    this.saving.set(true);
    this.error.set(null);
    this.service.creerVisite(v).subscribe({
      next: () => this.router.navigate(['/visites-techniques']),
      error: (e) => {
        this.error.set(e?.error?.detail ?? "Échec de l'enregistrement.");
        this.saving.set(false);
      },
    });
  }
}
