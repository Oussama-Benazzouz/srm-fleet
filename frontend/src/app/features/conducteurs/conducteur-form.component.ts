import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ConducteurService } from './conducteur.service';
import { Permis } from './conducteur.models';

@Component({
  selector: 'app-conducteur-form',
  imports: [ReactiveFormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page">
      <header class="page__head">
        <div>
          <h2>{{ id() ? 'Conducteur' : 'Nouveau conducteur' }}</h2>
          <p class="text-muted text-sm">Identité, département et permis.</p>
        </div>
      </header>

      @if (error()) { <div class="alert alert--danger" role="alert">{{ error() }}</div> }

      <form class="card page__panel" [formGroup]="form" (ngSubmit)="enregistrer()">
        <div class="form__grid">
          <div class="field">
            <label class="field__label" for="matricule">Matricule</label>
            <input id="matricule" class="input" formControlName="matricule"
                   [attr.aria-invalid]="isInvalid('matricule')" />
            @if (isInvalid('matricule')) { <p class="field__error">Le matricule est obligatoire.</p> }
          </div>
          <div class="field">
            <label class="field__label" for="fonction">Fonction</label>
            <input id="fonction" class="input" formControlName="fonction" />
          </div>
          <div class="field">
            <label class="field__label" for="nom">Nom</label>
            <input id="nom" class="input" formControlName="nom" [attr.aria-invalid]="isInvalid('nom')" />
            @if (isInvalid('nom')) { <p class="field__error">Le nom est obligatoire.</p> }
          </div>
          <div class="field">
            <label class="field__label" for="prenom">Prénom</label>
            <input id="prenom" class="input" formControlName="prenom" [attr.aria-invalid]="isInvalid('prenom')" />
            @if (isInvalid('prenom')) { <p class="field__error">Le prénom est obligatoire.</p> }
          </div>
          <div class="field">
            <label class="field__label" for="groupe">Groupe sanguin</label>
            <select id="groupe" class="input select" formControlName="groupeSanguin">
              <option [ngValue]="null">—</option>
              @for (g of groupes; track g) { <option [ngValue]="g">{{ g }}</option> }
            </select>
          </div>
        </div>

        <div class="form__actions">
          <button type="submit" class="btn btn--primary" [disabled]="saving()">
            @if (saving()) { <span class="spinner" aria-hidden="true"></span> }
            Enregistrer
          </button>
        </div>
      </form>

      @if (id()) {
        <div class="card page__panel">
          <h3>Permis</h3>
          @if (permis().length === 0) {
            <p class="text-muted text-sm">Aucun permis enregistré.</p>
          } @else {
            <ul class="permis-list">
              @for (p of permis(); track p.id) {
                <li>
                  <span><strong>{{ p.typePermis }}</strong> — valide jusqu'au {{ p.dateFinValidite }}</span>
                  <button type="button" class="btn btn--sm btn--ghost" (click)="retirer(p)">Retirer</button>
                </li>
              }
            </ul>
          }

          <form class="form__grid" [formGroup]="permisForm" (ngSubmit)="ajouterPermis()">
            <div class="field">
              <label class="field__label" for="typePermis">Type</label>
              <input id="typePermis" class="input" formControlName="typePermis" placeholder="B, C, EC…" />
            </div>
            <div class="field">
              <label class="field__label" for="dateFin">Fin de validité</label>
              <input id="dateFin" class="input" type="date" formControlName="dateFinValidite" />
            </div>
            <div class="form__actions form__actions--full">
              <button type="submit" class="btn" [disabled]="permisForm.invalid">Ajouter un permis</button>
            </div>
          </form>
        </div>
      }
    </section>
  `,
  styles: [`
    .page { display:flex; flex-direction:column; gap:var(--s-5); max-width:680px; }
    .page__head p { margin-top:var(--s-1); }
    .page__panel { padding:var(--s-5); display:flex; flex-direction:column; gap:var(--s-4); }
    .form__grid { display:grid; grid-template-columns:repeat(2,1fr); gap:var(--s-4); }
    .form__actions { display:flex; justify-content:flex-end; gap:var(--s-3); }
    .form__actions--full { grid-column:1 / -1; }
    .permis-list { list-style:none; margin:0; padding:0; display:flex; flex-direction:column; gap:var(--s-2); }
    .permis-list li { display:flex; align-items:center; justify-content:space-between;
      padding:var(--s-2) var(--s-3); border:1px solid var(--c-border); border-radius:var(--r-md); }
    @media (max-width:560px){ .form__grid { grid-template-columns:1fr; } }
  `],
})
export class ConducteurFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ConducteurService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly id = signal<number | null>(null);
  protected readonly permis = signal<Permis[]>([]);
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
    departementId: [null as number | null],
  });

  protected readonly permisForm = this.fb.nonNullable.group({
    typePermis: ['', Validators.required],
    dateFinValidite: ['', Validators.required],
  });

  ngOnInit(): void {
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
    if (this.form.invalid) { return; }
    this.saving.set(true);
    this.error.set(null);
    const req = this.form.getRawValue();
    const obs = this.id() ? this.service.modifier(this.id()!, req) : this.service.creer(req);
    obs.subscribe({
      next: (c) => this.router.navigate(['/conducteurs', c.id]),
      error: (e) => { this.error.set(e?.error?.detail ?? 'Erreur lors de l’enregistrement.'); this.saving.set(false); },
    });
  }

  protected ajouterPermis(): void {
    if (this.permisForm.invalid) { return; }
    this.service.ajouterPermis(this.id()!, this.permisForm.getRawValue()).subscribe((p) => {
      this.permis.update((list) => [...list, p]);
      this.permisForm.reset();
    });
  }

  protected retirer(p: Permis): void {
    this.service.supprimerPermis(this.id()!, p.id).subscribe(() => {
      this.permis.update((list) => list.filter((x) => x.id !== p.id));
    });
  }
}