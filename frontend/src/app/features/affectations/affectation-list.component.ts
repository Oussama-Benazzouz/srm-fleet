import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ExportButtonComponent } from '../exports/export-button.component';
import { AffectationService } from './affectation.service';
import { Affectation } from './affectation.models';

@Component({
  selector: 'app-affectation-list',
  imports: [RouterLink, FormsModule, ExportButtonComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page">
      <header class="page__head">
        <div>
          <div class="page__title">
            <h2>Affectations</h2>
            @if (!loading()) {
              <span class="count" aria-live="polite">{{ affectations().length }}</span>
            }
          </div>
          <p class="text-muted text-sm">Véhicules affectés à des conducteurs.</p>
        </div>
        <div class="head-actions" style="display:flex;gap:var(--s-2);align-items:center">
          <app-export-button resource="affectations" [params]="{ statut }" />
          <a routerLink="nouvelle" class="btn btn--primary">
            <svg class="icon" viewBox="0 0 20 20" aria-hidden="true">
              <path d="M10 4.5v11M4.5 10h11" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
            </svg>
            Nouvelle affectation
          </a>
        </div>
      </header>

      @if (error()) { <div class="alert alert--danger" role="alert">{{ error() }}</div> }

      <div class="toolbar card">
        <select class="input select toolbar__select" [(ngModel)]="statut" name="statut"
                (ngModelChange)="charger()" aria-label="Filtrer par statut">
          <option value="">Tous les statuts</option>
          <option value="ACTIVE">Actives</option>
          <option value="CLOTUREE">Clôturées</option>
        </select>
        @if (statut) {
          <button type="button" class="btn btn--ghost" (click)="reinitialiser()">Réinitialiser</button>
        }
      </div>

      <div class="card panel">
        @if (loading()) {
          <div class="panel__state">
            <span class="spinner" aria-hidden="true"></span>
            <span class="text-muted text-sm">Chargement…</span>
          </div>
        } @else if (affectations().length === 0) {
          <div class="panel__state panel__state--empty">
            <svg class="empty-icon" viewBox="0 0 20 20" aria-hidden="true">
              <path d="M8 12a3 3 0 0 0 4 0l2-2a3 3 0 0 0-4-4l-1 1M12 8a3 3 0 0 0-4 0l-2 2a3 3 0 0 0 4 4l1-1"
                    fill="none" stroke="currentColor" stroke-width="1.3" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
            <p class="text-muted text-sm">Aucune affectation.</p>
          </div>
        } @else {
          <div class="panel__scroll">
            <table class="table ctable">
              <thead>
                <tr>
                  <th>Véhicule</th>
                  <th>Conducteur</th>
                  <th>Province</th>
                  <th>Début</th>
                  <th>Fin</th>
                  <th>Statut</th>
                  <th class="ctable__actions-head"><span class="sr-only">Actions</span></th>
                </tr>
              </thead>
              <tbody>
                @for (a of affectations(); track a.id) {
                  <tr>
                    <td class="plate">{{ a.immatriculation }}</td>
                    <td>{{ a.conducteurNomComplet }}</td>
                    <td>
                      @if (a.provinceLibelle) {
                        <span class="pill">{{ a.provinceLibelle }}</span>
                      } @else {
                        <span class="text-subtle">—</span>
                      }
                    </td>
                    <td class="text-muted">{{ a.dateDebut }}</td>
                    <td class="text-muted">{{ a.dateFin ?? '—' }}</td>
                    <td>
                      <span class="badge" [class.badge--success]="a.statut === 'ACTIVE'"
                            [class.badge--danger]="a.statut === 'CLOTUREE'">
                        {{ a.statut === 'ACTIVE' ? 'Active' : 'Clôturée' }}
                      </span>
                    </td>
                    <td>
                      <div class="ctable__actions">
                        @if (a.statut === 'ACTIVE') {
                          <button type="button" class="btn btn--sm btn--ghost"
                                  [disabled]="busyId() === a.id" (click)="cloturer(a)">Clôturer</button>
                        }
                      </div>
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
    .page { display:flex; flex-direction:column; gap:var(--s-5); max-width:1180px; }
    .page__head { display:flex; align-items:flex-start; justify-content:space-between; gap:var(--s-4); }
    .page__title { display:flex; align-items:center; gap:var(--s-3); }
    .count { display:inline-flex; align-items:center; justify-content:center; min-width:1.5rem; height:1.5rem; padding:0 var(--s-2); font-size:var(--fs-xs); font-weight:600; color:var(--c-text-muted); background:var(--c-surface-alt); border-radius:var(--r-full); }
    .page__head p { margin-top:var(--s-1); }
    .icon { width:1rem; height:1rem; }
    .toolbar { display:flex; flex-wrap:wrap; align-items:center; gap:var(--s-3); padding:var(--s-3); }
    .toolbar__select { flex:0 1 220px; min-width:170px; }
    .panel { padding:0; overflow:hidden; }
    .panel__scroll { overflow-x:auto; }
    .panel__state { display:flex; align-items:center; justify-content:center; gap:var(--s-3); padding:var(--s-7) var(--s-4); }
    .panel__state--empty { flex-direction:column; }
    .empty-icon { width:2.25rem; height:2.25rem; color:var(--c-text-subtle); }
    .ctable th { font-size:var(--fs-xs); font-weight:600; letter-spacing:0.04em; text-transform:uppercase; color:var(--c-text-subtle); }
    .ctable td { padding-top:var(--s-3); padding-bottom:var(--s-3); vertical-align:middle; }
    .ctable__actions-head { text-align:right; }
    .ctable__actions { display:flex; gap:var(--s-2); justify-content:flex-end; align-items:center; }
    .plate { font-weight:600; color:var(--c-text); font-variant-numeric:tabular-nums; letter-spacing:0.02em; }
    .pill { display:inline-flex; align-items:center; padding:0.125rem var(--s-2); font-size:var(--fs-xs); font-weight:500; color:var(--c-text-muted); background:var(--c-surface-alt); border:1px solid var(--c-border); border-radius:var(--r-sm); white-space:nowrap; }
    .text-subtle { color:var(--c-text-subtle); }
    @media (max-width:640px){ .page__head { flex-direction:column; } .toolbar__select { flex:1 1 100%; } }
  `],
})
export class AffectationListComponent implements OnInit {
  private readonly service = inject(AffectationService);
  protected readonly affectations = signal<Affectation[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly busyId = signal<number | null>(null);
  protected statut = '';

  ngOnInit(): void { this.charger(); }

  protected charger(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.lister(this.statut).subscribe({
      next: (list) => { this.affectations.set(list); this.loading.set(false); },
      error: () => { this.error.set('Impossible de charger les affectations.'); this.loading.set(false); },
    });
  }

  protected reinitialiser(): void {
    this.statut = '';
    this.charger();
  }

  protected cloturer(a: Affectation): void {
    this.busyId.set(a.id);
    this.error.set(null);
    this.service.cloturer(a.id, null).subscribe({
      next: () => { this.busyId.set(null); this.charger(); },
      error: (e) => { this.error.set(e?.error?.detail ?? 'Échec de la clôture.'); this.busyId.set(null); },
    });
  }
}