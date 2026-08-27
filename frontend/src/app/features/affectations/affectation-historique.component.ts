import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AffectationService } from './affectation.service';
import { Affectation } from './affectation.models';

@Component({
  selector: 'app-affectation-historique',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page">
      <header class="page__head">
        <div>
          <div class="page__title">
            <h2>Historique des affectations</h2>
            @if (!loading()) {
              <span class="count" aria-live="polite">{{ affectations().length }}</span>
            }
          </div>
          <p class="text-muted text-sm">{{ sousTitre() }}</p>
        </div>
      </header>

      @if (error()) { <div class="alert alert--danger" role="alert">{{ error() }}</div> }

      <div class="card panel">
        @if (loading()) {
          <div class="panel__state">
            <span class="spinner" aria-hidden="true"></span>
            <span class="text-muted text-sm">Chargement…</span>
          </div>
        } @else if (affectations().length === 0) {
          <div class="panel__state panel__state--empty">
            <svg class="empty-icon" viewBox="0 0 20 20" aria-hidden="true">
              <circle cx="10" cy="10" r="7" fill="none" stroke="currentColor" stroke-width="1.3" />
              <path d="M10 6v4l2.5 1.5" fill="none" stroke="currentColor" stroke-width="1.3"
                    stroke-linecap="round" stroke-linejoin="round" />
            </svg>
            <p class="text-muted text-sm">Aucune affectation dans l'historique.</p>
          </div>
        } @else {
          <div class="panel__scroll">
            <table class="table ctable">
              <thead>
                <tr>
                  <th>Véhicule</th>
                  <th>Conducteur</th>
                  <th>Début</th>
                  <th>Fin</th>
                  <th>Statut</th>
                </tr>
              </thead>
              <tbody>
                @for (a of affectations(); track a.id) {
                  <tr>
                    <td class="plate">{{ a.immatriculation }}</td>
                    <td>{{ a.conducteurNomComplet }}</td>
                    <td class="text-muted">{{ a.dateDebut }}</td>
                    <td class="text-muted">{{ a.dateFin ?? '—' }}</td>
                    <td>
                      <span class="badge" [class.badge--success]="a.statut === 'ACTIVE'"
                            [class.badge--danger]="a.statut === 'CLOTUREE'">
                        {{ a.statut === 'ACTIVE' ? 'Active' : 'Clôturée' }}
                      </span>
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
    .page { display:flex; flex-direction:column; gap:var(--s-5); }
    .page__head p { margin-top:var(--s-1); }
    .page__title { display:flex; align-items:center; gap:var(--s-3); }
    .count { display:inline-flex; align-items:center; justify-content:center; min-width:1.5rem; height:1.5rem; padding:0 var(--s-2); font-size:var(--fs-xs); font-weight:600; color:var(--c-text-muted); background:var(--c-surface-alt); border-radius:var(--r-full); }
    .panel { padding:0; overflow:hidden; }
    .panel__scroll { overflow-x:auto; }
    .panel__state { display:flex; align-items:center; justify-content:center; gap:var(--s-3); padding:var(--s-7) var(--s-4); }
    .panel__state--empty { flex-direction:column; }
    .empty-icon { width:2.25rem; height:2.25rem; color:var(--c-text-subtle); }
    .ctable th { font-size:var(--fs-xs); font-weight:600; letter-spacing:0.04em; text-transform:uppercase; color:var(--c-text-subtle); }
    .ctable td { padding-top:var(--s-3); padding-bottom:var(--s-3); vertical-align:middle; }
    .plate { font-weight:600; color:var(--c-text); font-variant-numeric:tabular-nums; letter-spacing:0.02em; }
  `],
})
export class AffectationHistoriqueComponent implements OnInit {
  private readonly service = inject(AffectationService);
  private readonly route = inject(ActivatedRoute);

  protected readonly affectations = signal<Affectation[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly sousTitre = signal('');

  ngOnInit(): void {
    const type = this.route.snapshot.paramMap.get('type');   // 'vehicule' | 'conducteur'
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loading.set(true);
    this.error.set(null);

    const obs = type === 'conducteur'
      ? this.service.historiqueConducteur(id)
      : this.service.historiqueVehicule(id);
    this.sousTitre.set(type === 'conducteur' ? `Conducteur #${id}` : `Véhicule #${id}`);

    obs.subscribe({
      next: (list) => { this.affectations.set(list); this.loading.set(false); },
      error: (e) => {
        this.error.set(e?.status === 404 ? 'Élément introuvable dans votre périmètre.' : 'Impossible de charger l’historique.');
        this.loading.set(false);
      },
    });
  }
}