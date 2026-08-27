import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ToastService } from '../../core/ui/toast.service';
import { ConformiteService } from './conformite.service';
import { OptionVehicule } from './conformite.models';

@Component({
  selector: 'app-sinistre-form',
  imports: [ReactiveFormsModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page page--form">
      <a routerLink="/sinistres" class="back-link">
        <svg viewBox="0 0 20 20" width="16" height="16" aria-hidden="true">
          <path d="M12 5l-5 5 5 5" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
        </svg>
        Sinistres
      </a>

      <header class="page__head">
        <div>
          <h1>Déclarer un sinistre</h1>
          <p class="page__subtitle">
            Le sinistre est rattaché automatiquement à l'affectation active du véhicule à la date
            des faits (RG7). Les pièces jointes s'ajoutent après création, sur la fiche.
          </p>
        </div>
      </header>

      @if (error()) {
        <div class="alert alert--danger" role="alert">{{ error() }}</div>
      }

      <form class="card page__panel" [formGroup]="form" (ngSubmit)="enregistrer()">
        <div class="form__grid">
          <div class="field">
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
          <div class="field">
            <label class="field__label" for="date">Date des faits</label>
            <input id="date" class="input" type="date" formControlName="dateSinistre"
              [attr.aria-invalid]="isInvalid('dateSinistre')" />
            @if (isInvalid('dateSinistre')) {
              <p class="field__error">La date est obligatoire.</p>
            }
          </div>
          <div class="field" style="grid-column:1 / -1;">
            <label class="field__label" for="description">Description</label>
            <textarea
              id="description"
              class="input"
              rows="4"
              formControlName="description"
              placeholder="Circonstances, dommages constatés…"
            ></textarea>
          </div>
        </div>
        <div class="form__actions">
          <button type="submit" class="btn btn--primary" [disabled]="saving()">
            @if (saving()) {
              <span class="spinner" aria-hidden="true"></span>
            }
            Déclarer
          </button>
        </div>
      </form>
    </section>
  `,
  styles: [
    `
      .form__grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: var(--s-4);
      }
      textarea.input {
        resize: vertical;
        font-family: inherit;
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
export class SinistreFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ConformiteService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  protected readonly vehicules = signal<OptionVehicule[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly saving = signal(false);
  protected readonly submitted = signal(false);

  protected readonly form = this.fb.nonNullable.group({
    vehiculeId: [null as number | null, Validators.required],
    dateSinistre: [null as string | null, Validators.required],
    description: [null as string | null],
  });

  ngOnInit(): void {
    this.service.vehicules().subscribe((v) => this.vehicules.set(v));
  }

  protected isInvalid(name: string): boolean {
    const c = this.form.get(name);
    return !!c && c.invalid && (c.touched || this.submitted());
  }

  protected enregistrer(): void {
    this.submitted.set(true);
    if (this.form.invalid) return;
    this.saving.set(true);
    this.error.set(null);
    this.service.creerSinistre(this.form.getRawValue()).subscribe({
      // On ouvre la fiche pour permettre l'ajout de pièces jointes.
      next: (s) => {
        this.toast.success('Sinistre déclaré', 'Ajoutez les pièces jointes si besoin.');
        this.router.navigate(['/sinistres', s.id]);
      },
      error: (e) => {
        this.toast.error('Déclaration impossible', e?.error?.detail);
        this.saving.set(false);
      },
    });
  }
}
