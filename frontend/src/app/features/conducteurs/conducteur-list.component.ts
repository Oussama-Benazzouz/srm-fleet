import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ConducteurService } from './conducteur.service';
import { Conducteur } from './conducteur.models';

@Component({
  selector: 'app-conducteur-list',
  imports: [RouterLink, FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page">
      <header class="page__head">
        <div>
          <h2>Conducteurs</h2>
          <p class="text-muted text-sm">Fiches conducteurs et permis associés.</p>
        </div>
        <a routerLink="nouveau" class="btn btn--primary">Nouveau conducteur</a>
      </header>

      @if (error()) { <div class="alert alert--danger" role="alert">{{ error() }}</div> }

      <div class="card page__filter">
        <input class="input" [(ngModel)]="recherche" name="recherche"
               placeholder="Rechercher par matricule, nom ou prénom…"
               (keyup.enter)="charger()" aria-label="Recherche" />
        <button type="button" class="btn" (click)="charger()">Rechercher</button>
      </div>

      <div class="card page__panel">
        @if (loading()) {
          <div class="page__state">
            <span class="spinner" aria-hidden="true"></span>
            <span class="text-muted text-sm">Chargement…</span>
          </div>
        } @else if (conducteurs().length === 0) {
          <p class="page__state text-muted text-sm">Aucun conducteur.</p>
        } @else {
          <div class="page__scroll">
            <table class="table">
              <thead>
                <tr>
                  <th>Matricule</th><th>Nom</th><th>Fonction</th>
                  <th>Groupe sanguin</th><th>Permis</th>
                  <th class="table__actions">Actions</th>
                </tr>
              </thead>
              <tbody>
                @for (c of conducteurs(); track c.id) {
                  <tr>
                    <td>{{ c.matricule }}</td>
                    <td>{{ c.prenom }} {{ c.nom }}</td>
                    <td class="text-muted">{{ c.fonction }}</td>
                    <td>{{ c.groupeSanguin }}</td>
                    <td>{{ c.permis.length }}</td>
                    <td class="table__actions">
                      <a class="btn btn--sm" [routerLink]="[c.id]">Détail</a>
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }
      </div>
    </section>
  `,
  styles: [`
    .page { display:flex; flex-direction:column; gap:var(--s-5); max-width:1100px; }
    .page__head { display:flex; align-items:flex-start; justify-content:space-between; gap:var(--s-4); }
    .page__head p { margin-top:var(--s-1); }
    .page__filter { display:flex; gap:var(--s-3); padding:var(--s-4); }
    .page__filter .input { flex:1; }
    .page__panel { padding:0; overflow:hidden; }
    .page__scroll { overflow-x:auto; }
    .page__state { display:flex; align-items:center; justify-content:center; gap:var(--s-3); padding:var(--s-7) var(--s-4); }
  `],
})
export class ConducteurListComponent implements OnInit {
  private readonly service = inject(ConducteurService);
  protected readonly conducteurs = signal<Conducteur[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected recherche = '';

  ngOnInit(): void { this.charger(); }

  protected charger(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.lister(this.recherche).subscribe({
      next: (list) => { this.conducteurs.set(list); this.loading.set(false); },
      error: () => { this.error.set('Impossible de charger les conducteurs.'); this.loading.set(false); },
    });
  }
}