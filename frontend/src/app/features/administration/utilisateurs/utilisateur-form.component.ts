import { HttpErrorResponse } from '@angular/common/http';
import { Component, ChangeDetectionStrategy, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AdministrationService } from '../administration.service';
import { Province, UtilisateurRequest } from '../utilisateur.models';
import { ToastService } from '../../../core/ui/toast.service';

const LIST_URL = '/administration/utilisateurs';

@Component({
  selector: 'app-utilisateur-form',
  imports: [ReactiveFormsModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page page--form">
      <a [routerLink]="listUrl" class="back-link">
        <svg viewBox="0 0 20 20" width="16" height="16" aria-hidden="true">
          <path d="M12 5l-5 5 5 5" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
        </svg>
        Comptes utilisateurs
      </a>

      <header class="page__head">
        <div>
          <h1>{{ id() ? 'Modifier le compte' : 'Nouveau compte' }}</h1>
          <p class="page__subtitle">
            {{
              id()
                ? 'Mettez à jour les informations du compte.'
                : 'Renseignez les informations du nouveau collaborateur.'
            }}
          </p>
        </div>
      </header>

      @if (error()) {
        <div class="alert alert--danger" role="alert">{{ error() }}</div>
      }

      <div class="card page__panel">
        @if (loading()) {
          <div class="page__state">
            <span class="spinner" aria-hidden="true"></span>
            <span class="text-muted text-sm">Chargement…</span>
          </div>
        } @else {
          <form class="form" [formGroup]="form" (ngSubmit)="submit()" novalidate>
            <div class="form__grid">
              <div class="field">
                <label class="field__label" for="nom">Nom</label>
                <input id="nom" class="input" formControlName="nom" />
              </div>

              <div class="field">
                <label class="field__label" for="prenom">Prénom</label>
                <input id="prenom" class="input" formControlName="prenom" />
              </div>

              <div class="field">
                <label class="field__label" for="login">Login</label>
                <input id="login" class="input" formControlName="login" autocapitalize="none" />
              </div>

              <div class="field">
                <label class="field__label" for="email">Email</label>
                <input id="email" class="input" type="email" formControlName="email" />
              </div>

              <div class="field form__full">
                <label class="field__label" for="motDePasse">
                  Mot de passe
                  @if (id()) {
                    <span class="field__hint">— laisser vide pour ne pas le changer</span>
                  }
                </label>
                <input
                  id="motDePasse"
                  class="input"
                  type="password"
                  formControlName="motDePasse"
                  [autocomplete]="id() ? 'new-password' : 'off'"
                />
              </div>

              <div class="field">
                <label class="field__label" for="role">Rôle</label>
                <select id="role" class="input select" formControlName="role">
                  <option value="ADMINISTRATEUR_CENTRAL">Administrateur central</option>
                  <option value="RESPONSABLE_PROVINCIAL">Responsable provincial</option>
                </select>
              </div>

              <div class="field">
                <label class="field__label" for="provinceId">Province</label>
                <select id="provinceId" class="input select" formControlName="provinceId">
                  <option [ngValue]="null">— (administrateur central)</option>
                  @for (p of provinces(); track p.id) {
                    <option [ngValue]="p.id">{{ p.libelle }}</option>
                  }
                </select>
              </div>
            </div>

            <div class="form__actions">
              <a class="btn" [routerLink]="listUrl">Annuler</a>
              <button type="submit" class="btn btn--primary" [disabled]="form.invalid || saving()">
                @if (saving()) {
                  <span class="spinner" aria-hidden="true"></span>
                  <span>Enregistrement…</span>
                } @else {
                  <span>Enregistrer</span>
                }
              </button>
            </div>
          </form>
        }
      </div>
    </section>
  `,
  styles: [
    `
      .page__panel {
        padding: var(--s-5);
      }

      .page__state {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: var(--s-3);
        padding: var(--s-6) 0;
      }

      .form {
        display: flex;
        flex-direction: column;
        gap: var(--s-5);
      }

      .form__grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: var(--s-4);
      }

      .form__full {
        grid-column: 1 / -1;
      }

      .field__hint {
        font-weight: 400;
        color: var(--c-text-subtle);
      }

      .form__actions {
        display: flex;
        justify-content: flex-end;
        gap: var(--s-3);
        padding-top: var(--s-1);
      }

      @media (max-width: 560px) {
        .form__grid {
          grid-template-columns: 1fr;
        }
      }
    `,
  ],
})
export class UtilisateurFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(AdministrationService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  protected readonly listUrl = LIST_URL;
  protected readonly id = signal<number | null>(null);
  protected readonly provinces = signal<Province[]>([]);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    nom: ['', Validators.required],
    prenom: ['', Validators.required],
    login: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    motDePasse: [''],
    role: this.fb.nonNullable.control<'ADMINISTRATEUR_CENTRAL' | 'RESPONSABLE_PROVINCIAL'>(
      'RESPONSABLE_PROVINCIAL',
      Validators.required,
    ),
    provinceId: this.fb.control<number | null>(null),
  });

  constructor() {
    // Un administrateur central n'est rattaché à aucune province (RG14) : le
    // champ est alors vidé et verrouillé ; un responsable provincial doit en
    // choisir une.
    this.form.controls.role.valueChanges.pipe(takeUntilDestroyed()).subscribe((role) => {
      this.appliquerContrainteProvince(role);
    });
  }

  ngOnInit(): void {
    this.service.listerProvinces().subscribe((list) => this.provinces.set(list));

    const param = this.route.snapshot.paramMap.get('id');
    if (param && param !== 'nouveau') {
      this.charger(Number(param));
    } else {
      // Création : le mot de passe est obligatoire.
      this.form.controls.motDePasse.addValidators(Validators.required);
      this.appliquerContrainteProvince(this.form.controls.role.value);
    }
  }

  private charger(id: number): void {
    this.id.set(id);
    this.loading.set(true);
    this.service.utilisateurParId(id).subscribe({
      next: (u) => {
        this.form.patchValue({
          nom: u.nom,
          prenom: u.prenom,
          login: u.login,
          email: u.email,
          role: u.role,
          provinceId: u.provinceId,
        });
        this.appliquerContrainteProvince(u.role);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Compte introuvable.');
        this.loading.set(false);
      },
    });
  }

  private appliquerContrainteProvince(role: string): void {
    const province = this.form.controls.provinceId;
    if (role === 'ADMINISTRATEUR_CENTRAL') {
      province.setValue(null);
      province.clearValidators();
      province.disable();
    } else {
      province.enable();
      province.setValidators(Validators.required);
    }
    province.updateValueAndValidity();
  }

  protected submit(): void {
    if (this.form.invalid || this.saving()) return;

    this.saving.set(true);
    this.error.set(null);

    const value = this.form.getRawValue();
    const req: UtilisateurRequest = {
      nom: value.nom,
      prenom: value.prenom,
      login: value.login,
      email: value.email,
      role: value.role,
      provinceId: value.provinceId,
      ...(value.motDePasse ? { motDePasse: value.motDePasse } : {}),
    };

    const id = this.id();
    const request$ = id
      ? this.service.modifierUtilisateur(id, req)
      : this.service.creerUtilisateur(req);

    request$.subscribe({
      next: () => {
        this.toast.success(id ? 'Compte mis à jour' : 'Compte créé', req.login);
        this.router.navigateByUrl(LIST_URL);
      },
      error: (err: HttpErrorResponse) => {
        this.toast.error('Enregistrement impossible', err?.error?.detail);
        this.saving.set(false);
      },
    });
  }
}
