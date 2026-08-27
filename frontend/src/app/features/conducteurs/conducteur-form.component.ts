import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AdministrationService } from '../administration/administration.service';
import { Departement, Province } from '../administration/utilisateur.models';
import { ToastService } from '../../core/ui/toast.service';
import { ConfirmService } from '../../core/ui/confirm.service';
import { Permis } from './conducteur.models';
import { ConducteurService } from './conducteur.service';

@Component({
  selector: 'app-conducteur-form',
  imports: [ReactiveFormsModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page page--form">
      <a [routerLink]="retour()" class="back-link">
        <svg viewBox="0 0 20 20" width="16" height="16" aria-hidden="true">
          <path d="M12 5l-5 5 5 5" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
        </svg>
        {{ id() ? 'Fiche conducteur' : 'Conducteurs' }}
      </a>

      <header class="page__head">
        <div>
          <h1>{{ id() ? 'Modifier le conducteur' : 'Nouveau conducteur' }}</h1>
          <p class="page__subtitle">Identité, rattachement géographique et permis.</p>
        </div>
      </header>

      @if (error()) {
        <div class="alert alert--danger" role="alert">{{ error() }}</div>
      }

      <form class="card page__panel" [formGroup]="form" (ngSubmit)="enregistrer()">
        <div class="form-grid">
          <div class="field">
            <label class="field__label" for="matricule" data-required>Matricule</label>
            <input id="matricule" class="input" formControlName="matricule" [attr.aria-invalid]="isInvalid('matricule')" />
            @if (isInvalid('matricule')) {
              <p class="field__error">Le matricule est obligatoire.</p>
            }
          </div>
          <div class="field">
            <label class="field__label" for="fonction">Fonction</label>
            <input id="fonction" class="input" formControlName="fonction" />
          </div>
          <div class="field">
            <label class="field__label" for="nom" data-required>Nom</label>
            <input id="nom" class="input" formControlName="nom" [attr.aria-invalid]="isInvalid('nom')" />
            @if (isInvalid('nom')) {
              <p class="field__error">Le nom est obligatoire.</p>
            }
          </div>
          <div class="field">
            <label class="field__label" for="prenom" data-required>Prénom</label>
            <input id="prenom" class="input" formControlName="prenom" [attr.aria-invalid]="isInvalid('prenom')" />
            @if (isInvalid('prenom')) {
              <p class="field__error">Le prénom est obligatoire.</p>
            }
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
              <p class="field__error">La province est obligatoire.</p>
            }
          </div>
          <div class="field">
            <label class="field__label" for="departement" data-required>Département</label>
            <select id="departement" class="input select" formControlName="departementId" [attr.aria-invalid]="isInvalid('departementId')">
              <option [ngValue]="null" disabled>Sélectionner un département</option>
              @for (d of departements(); track d.id) {
                <option [ngValue]="d.id">{{ d.libelle }}</option>
              }
            </select>
            @if (isInvalid('departementId')) {
              <p class="field__error">Le département est obligatoire.</p>
            }
          </div>
          <div class="field">
            <label class="field__label" for="groupe">Groupe sanguin</label>
            <select id="groupe" class="input select" formControlName="groupeSanguin">
              <option [ngValue]="null">—</option>
              @for (g of groupes; track g) {
                <option [ngValue]="g">{{ g }}</option>
              }
            </select>
          </div>
        </div>

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

      @if (id()) {
        <div class="card page__panel">
          <h2 class="detail__section-title">Permis</h2>
          @if (permis().length === 0) {
            <p class="text-muted text-sm">Aucun permis enregistré.</p>
          } @else {
            <ul class="permis-list">
              @for (p of permis(); track p.id) {
                <li class="permis-list__item">
                  <span><strong>{{ p.typePermis }}</strong> — valide jusqu'au {{ p.dateFinValidite }}</span>
                  <button type="button" class="btn btn--sm btn--ghost" (click)="retirer(p)">Retirer</button>
                </li>
              }
            </ul>
          }

          <form class="permis-add" [formGroup]="permisForm" (ngSubmit)="ajouterPermis()">
            <div class="field">
              <label class="field__label" for="typePermis" data-required>Type</label>
              <input id="typePermis" class="input" formControlName="typePermis" placeholder="B, C, EC…" />
            </div>
            <div class="field">
              <label class="field__label" for="dateFin" data-required>Fin de validité</label>
              <input id="dateFin" class="input" type="date" formControlName="dateFinValidite" />
            </div>
            <button type="submit" class="btn" [disabled]="permisForm.invalid">Ajouter un permis</button>
          </form>
        </div>
      }
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
      .detail__section-title {
        font-size: var(--fs-base);
        font-weight: 600;
        padding-bottom: var(--s-3);
        border-bottom: 1px solid var(--border);
      }
      .permis-list {
        list-style: none;
        margin: 0;
        padding: 0;
        display: flex;
        flex-direction: column;
        gap: var(--s-2);
      }
      .permis-list__item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: var(--s-3);
        padding: var(--s-2) var(--s-3);
        border: 1px solid var(--border);
        border-radius: var(--r-md);
        background: var(--surface-2);
      }
      .permis-add {
        display: grid;
        grid-template-columns: 1fr 1fr auto;
        align-items: end;
        gap: var(--s-4);
      }
      @media (max-width: 560px) {
        .permis-add {
          grid-template-columns: 1fr;
        }
      }
    `,
  ],
})
export class ConducteurFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ConducteurService);
  private readonly referentiel = inject(AdministrationService);
  private readonly toast = inject(ToastService);
  private readonly confirm = inject(ConfirmService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly id = signal<number | null>(null);
  protected readonly permis = signal<Permis[]>([]);
  protected readonly provinces = signal<Province[]>([]);
  protected readonly departements = signal<Departement[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly saving = signal(false);
  protected readonly submitted = signal(false);
  protected readonly groupes = ['O+', 'O-', 'A+', 'A-', 'B+', 'B-', 'AB+', 'AB-'];

  protected readonly form = this.fb.nonNullable.group({
    matricule: ['', Validators.required],
    nom: ['', Validators.required],
    prenom: ['', Validators.required],
    fonction: [''],
    groupeSanguin: [null as string | null],
    provinceId: [null as number | null, Validators.required],
    departementId: [null as number | null, Validators.required],
  });

  protected readonly permisForm = this.fb.nonNullable.group({
    typePermis: ['', Validators.required],
    dateFinValidite: ['', Validators.required],
  });

  protected retour(): string {
    return this.id() ? `/conducteurs/${this.id()}` : '/conducteurs';
  }

  ngOnInit(): void {
    this.referentiel.listerProvinces().subscribe((list) => this.provinces.set(list));
    this.referentiel.listerDepartements().subscribe((list) => this.departements.set(list));

    const param = this.route.snapshot.paramMap.get('id');
    if (param && param !== 'nouveau') {
      this.id.set(Number(param));
      this.service.parId(this.id()!).subscribe((c) => {
        this.form.patchValue({
          matricule: c.matricule,
          nom: c.nom,
          prenom: c.prenom,
          fonction: c.fonction ?? '',
          groupeSanguin: c.groupeSanguin,
          provinceId: c.provinceId,
          departementId: c.departementId,
        });
        this.permis.set(c.permis);
      });
    }
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
    const req = this.form.getRawValue();
    const edition = this.id() != null;
    const obs = edition ? this.service.modifier(this.id()!, req) : this.service.creer(req);
    obs.subscribe({
      next: (c) => {
        this.toast.success(edition ? 'Conducteur mis à jour' : 'Conducteur créé', `${c.prenom} ${c.nom}`);
        this.router.navigate(['/conducteurs', c.id]);
      },
      error: (e) => {
        this.toast.error('Enregistrement impossible', e?.error?.detail);
        this.saving.set(false);
      },
    });
  }

  protected ajouterPermis(): void {
    if (this.permisForm.invalid) return;
    this.service.ajouterPermis(this.id()!, this.permisForm.getRawValue()).subscribe({
      next: (p) => {
        this.permis.update((list) => [...list, p]);
        this.permisForm.reset();
        this.toast.success('Permis ajouté', p.typePermis);
      },
      error: (e) => this.toast.error('Ajout impossible', e?.error?.detail),
    });
  }

  protected async retirer(p: Permis): Promise<void> {
    const ok = await this.confirm.askDelete(`le permis ${p.typePermis}`);
    if (!ok) return;
    this.service.supprimerPermis(this.id()!, p.id).subscribe({
      next: () => {
        this.permis.update((list) => list.filter((x) => x.id !== p.id));
        this.toast.success('Permis retiré', p.typePermis);
      },
      error: (e) => this.toast.error('Suppression impossible', e?.error?.detail),
    });
  }
}
