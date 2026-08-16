import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CarburantService } from './carburant.service';
import { OptionAffectation } from './carburant.models';

@Component({
  selector: 'app-demande-form',
  imports: [ReactiveFormsModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page">
      <header class="page__head">
        <div>
          <a routerLink="/demandes" class="back">← Demandes</a>
          <h2>Nouvelle demande de carte</h2>
          <p class="text-muted text-sm">
            Pour une affectation de votre périmètre. Le plafond est fixé par le central à la
            validation.
          </p>
        </div>
      </header>

      @if (error()) {
        <div class="alert alert--danger" role="alert">{{ error() }}</div>
      }

      <form class="card page__panel" [formGroup]="form" (ngSubmit)="enregistrer()">
        <div class="form__grid">
          <div class="field">
            <label class="field__label" for="affectation"
              >Affectation (véhicule ↔ conducteur)</label
            >
            <select
              id="affectation"
              class="input select"
              formControlName="affectationId"
              [attr.aria-invalid]="isInvalid('affectationId')"
            >
              <option [ngValue]="null" disabled>Sélectionner une affectation</option>
              @for (a of affectations(); track a.id) {
                <option [ngValue]="a.id">{{ a.libelle }}</option>
              }
            </select>
            @if (isInvalid('affectationId')) {
              <p class="field__error">Choisissez une affectation.</p>
            }
            @if (affectations().length === 0) {
              <p class="field__error">Aucune affectation active dans votre périmètre.</p>
            }
          </div>
          <div class="field" style="grid-column:1 / -1;">
            <label class="field__label" for="motif">Motif</label>
            <input
              id="motif"
              class="input"
              formControlName="motif"
              maxlength="255"
              placeholder="Mission, trajet…"
            />
          </div>
        </div>
        <div class="form__actions">
          <button type="submit" class="btn btn--primary" [disabled]="saving()">
            @if (saving()) {
              <span class="spinner" aria-hidden="true"></span>
            }
            Envoyer la demande
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
export class DemandeFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(CarburantService);
  private readonly router = inject(Router);

  protected readonly affectations = signal<OptionAffectation[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly saving = signal(false);
  protected readonly submitted = signal(false);

  protected readonly form = this.fb.nonNullable.group({
    affectationId: [null as number | null, Validators.required],
    motif: [null as string | null],
  });

  ngOnInit(): void {
    this.service.affectationsActives().subscribe((a) => this.affectations.set(a));
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
    this.service.creerDemande(this.form.getRawValue()).subscribe({
      next: () => this.router.navigate(['/demandes']),
      error: (e) => {
        this.error.set(e?.error?.detail ?? 'Échec de la demande.');
        this.saving.set(false);
      },
    });
  }
}
