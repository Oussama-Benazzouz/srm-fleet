import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/auth/auth.service';
import { FlotteService } from '../vehicules/flotte.service';
import { Fournisseur, TYPE_FOURNISSEUR_LABELS, TypeFournisseur } from '../vehicules/flotte.models';

@Component({
  selector: 'app-fournisseur-list',
  imports: [ReactiveFormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page">
      <header class="page__head">
        <div>
          <div class="page__title">
            <h2>Fournisseurs</h2>
            @if (!loading()) {
              <span class="count" aria-live="polite">{{ fournisseurs().length }}</span>
            }
          </div>
          <p class="text-muted text-sm">Loueurs, réseaux carburant, assureurs et garages (référentiel central).</p>
        </div>
      </header>

      @if (error()) {
        <div class="alert alert--danger" role="alert">{{ error() }}</div>
      }

      <div class="card panel">
        @if (loading()) {
          <div class="panel__state">
            <span class="spinner" aria-hidden="true"></span>
            <span class="text-muted text-sm">Chargement…</span>
          </div>
        } @else if (fournisseurs().length === 0) {
          <div class="panel__state panel__state--empty">
            <svg class="empty-icon" viewBox="0 0 20 20" aria-hidden="true">
              <path
                d="M3 7l7-3 7 3-7 3-7-3z M3 7v6l7 3 7-3V7 M10 10v6"
                fill="none"
                stroke="currentColor"
                stroke-width="1.3"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
            <p class="text-muted text-sm">Aucun fournisseur.</p>
          </div>
        } @else {
          <div class="panel__scroll">
            <table class="table ctable">
              <thead>
                <tr>
                  <th>Raison sociale</th>
                  <th>Type</th>
                  <th>Contact</th>
                  <th>Adresse de facturation</th>
                </tr>
              </thead>
              <tbody>
                @for (f of fournisseurs(); track f.id) {
                  <tr>
                    <td class="fournisseur-name">{{ f.raisonSociale }}</td>
                    <td><span class="pill">{{ typeLabel(f.type) }}</span></td>
                    <td class="text-muted">{{ f.contact || '—' }}</td>
                    <td class="text-muted">{{ f.adresseFacturation || '—' }}</td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }
      </div>

      @if (isAdmin()) {
        <form class="card page__form" [formGroup]="form" (ngSubmit)="ajouter()">
          <h3>Ajouter un fournisseur</h3>
          <div class="form__grid">
            <div class="field">
              <label class="field__label" for="rs">Raison sociale</label>
              <input id="rs" class="input" formControlName="raisonSociale" [attr.aria-invalid]="isInvalid('raisonSociale')" />
              @if (isInvalid('raisonSociale')) {
                <p class="field__error">La raison sociale est obligatoire.</p>
              }
            </div>
            <div class="field">
              <label class="field__label" for="type">Type</label>
              <select id="type" class="input select" formControlName="type">
                @for (t of types; track t) {
                  <option [ngValue]="t">{{ typeLabel(t) }}</option>
                }
              </select>
            </div>
            <div class="field">
              <label class="field__label" for="contact">Contact</label>
              <input id="contact" class="input" formControlName="contact" />
            </div>
            <div class="field">
              <label class="field__label" for="adresse">Adresse de facturation</label>
              <input id="adresse" class="input" formControlName="adresseFacturation" />
            </div>
          </div>
          <div class="form__actions">
            <button type="submit" class="btn btn--primary" [disabled]="saving()">
              @if (saving()) {
                <span class="spinner" aria-hidden="true"></span>
              }
              Ajouter
            </button>
          </div>
        </form>
      }
    </section>
  `,
  styles: [
    `
      .page {
        display: flex;
        flex-direction: column;
        gap: var(--s-5);
        max-width: 1000px;
      }

      .page__head {
        display: flex;
        align-items: flex-start;
        justify-content: space-between;
        gap: var(--s-4);
      }

      .page__title {
        display: flex;
        align-items: center;
        gap: var(--s-3);
      }

      .count {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        min-width: 1.5rem;
        height: 1.5rem;
        padding: 0 var(--s-2);
        font-size: var(--fs-xs);
        font-weight: 600;
        color: var(--c-text-muted);
        background: var(--c-surface-alt);
        border-radius: var(--r-full);
      }

      .page__head p {
        margin-top: var(--s-1);
      }

      .panel {
        padding: 0;
        overflow: hidden;
      }

      .panel__scroll {
        overflow-x: auto;
      }

      .panel__state {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: var(--s-3);
        padding: var(--s-7) var(--s-4);
      }

      .panel__state--empty {
        flex-direction: column;
      }

      .empty-icon {
        width: 2.25rem;
        height: 2.25rem;
        color: var(--c-text-subtle);
      }

      .ctable th {
        font-size: var(--fs-xs);
        font-weight: 600;
        letter-spacing: 0.04em;
        text-transform: uppercase;
        color: var(--c-text-subtle);
      }

      .ctable td {
        padding-top: var(--s-3);
        padding-bottom: var(--s-3);
        vertical-align: middle;
      }

      .fournisseur-name {
        font-weight: 600;
        color: var(--c-text);
      }

      .pill {
        display: inline-flex;
        align-items: center;
        padding: 0.125rem var(--s-2);
        font-size: var(--fs-xs);
        font-weight: 500;
        color: var(--c-text-muted);
        background: var(--c-surface-alt);
        border: 1px solid var(--c-border);
        border-radius: var(--r-sm);
        white-space: nowrap;
      }

      .page__form {
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
        .page__head {
          flex-direction: column;
        }

        .form__grid {
          grid-template-columns: 1fr;
        }
      }
    `,
  ],
})
export class FournisseurListComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(FlotteService);
  private readonly auth = inject(AuthService);

  protected readonly isAdmin = this.auth.isAdmin;

  protected readonly fournisseurs = signal<Fournisseur[]>([]);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly submitted = signal(false);

  protected readonly types: TypeFournisseur[] = ['LOUEUR', 'CARBURANT', 'ASSURANCE', 'GARAGE'];

  protected readonly form = this.fb.nonNullable.group({
    raisonSociale: ['', Validators.required],
    type: ['LOUEUR' as TypeFournisseur, Validators.required],
    contact: [null as string | null],
    adresseFacturation: [null as string | null],
  });

  ngOnInit(): void {
    this.charger();
  }

  protected typeLabel(t: TypeFournisseur): string {
    return TYPE_FOURNISSEUR_LABELS[t];
  }

  protected isInvalid(name: string): boolean {
    const c = this.form.get(name);
    return !!c && c.invalid && (c.touched || this.submitted());
  }

  protected charger(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.listerFournisseurs().subscribe({
      next: (list) => {
        this.fournisseurs.set(list);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Impossible de charger les fournisseurs.');
        this.loading.set(false);
      },
    });
  }

  protected ajouter(): void {
    this.submitted.set(true);
    if (this.form.invalid) return;
    this.saving.set(true);
    this.error.set(null);
    this.service.creerFournisseur(this.form.getRawValue()).subscribe({
      next: () => {
        this.form.reset({ type: 'LOUEUR', raisonSociale: '', contact: null, adresseFacturation: null });
        this.submitted.set(false);
        this.saving.set(false);
        this.charger();
      },
      error: (e) => {
        this.error.set(e?.error?.detail ?? "L'ajout du fournisseur a échoué.");
        this.saving.set(false);
      },
    });
  }
}
