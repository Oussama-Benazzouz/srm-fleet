import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ExportButtonComponent } from '../exports/export-button.component';
import { ConformiteService } from './conformite.service';
import { OptionVehicule, Visite } from './conformite.models';

@Component({
  selector: 'app-visite-list',
  imports: [RouterLink, FormsModule, ExportButtonComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page">
      <header class="page__head">
        <div>
          <div class="page__title">
            <h2>Visites techniques</h2>
            @if (!loading()) {
              <span class="count" aria-live="polite">{{ visites().length }}</span>
            }
          </div>
          <p class="text-muted text-sm">
            Contrôles techniques par véhicule, avec compagnie et conducteur accompagnateur (RF-19).
          </p>
        </div>
        <div class="head-actions" style="display:flex;gap:var(--s-2);align-items:center">
          <app-export-button resource="visites-techniques" [params]="{ vehiculeId }" />
          <a routerLink="nouvelle" class="btn btn--primary">
            <svg class="icon" viewBox="0 0 20 20" aria-hidden="true">
              <path
                d="M10 4.5v11M4.5 10h11"
                fill="none"
                stroke="currentColor"
                stroke-width="1.6"
                stroke-linecap="round"
              />
            </svg>
            Nouvelle visite
          </a>
        </div>
      </header>

      @if (error()) {
        <div class="alert alert--danger" role="alert">{{ error() }}</div>
      }

      <div class="toolbar card">
        <select
          class="input select toolbar__select"
          [(ngModel)]="vehiculeId"
          name="vehicule"
          (ngModelChange)="charger()"
          aria-label="Filtrer par véhicule"
        >
          <option [ngValue]="null">Tous les véhicules</option>
          @for (v of vehicules(); track v.id) {
            <option [ngValue]="v.id">{{ v.libelle }}</option>
          }
        </select>
        @if (vehiculeId) {
          <button type="button" class="btn btn--ghost" (click)="reinitialiser()">Réinitialiser</button>
        }
      </div>

      <div class="card panel">
        @if (loading()) {
          <div class="panel__state">
            <span class="spinner" aria-hidden="true"></span>
            <span class="text-muted text-sm">Chargement…</span>
          </div>
        } @else if (visites().length === 0) {
          <div class="panel__state panel__state--empty">
            <p class="text-muted text-sm">Aucune visite technique enregistrée.</p>
          </div>
        } @else {
          <div class="panel__scroll">
            <table class="table ctable">
              <thead>
                <tr>
                  <th>Véhicule</th>
                  <th>Compagnie</th>
                  <th>Date visite</th>
                  <th>Échéance</th>
                  <th>Conducteur accompagnateur</th>
                </tr>
              </thead>
              <tbody>
                @for (v of visites(); track v.id) {
                  <tr>
                    <td class="plate">{{ v.immatriculation }}</td>
                    <td>{{ v.nomCompagnie }}</td>
                    <td class="text-muted">{{ v.dateVisite }}</td>
                    <td>
                      <span class="badge" [class.badge--danger]="depassee(v.dateEcheance)">
                        {{ v.dateEcheance }}
                      </span>
                    </td>
                    <td>
                      {{ v.conducteurNomComplet }}
                      <span class="sub">{{ v.matriculeConducteur }}</span>
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
  styles: [
    `
      .page {
        display: flex;
        flex-direction: column;
        gap: var(--s-5);
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
      .icon {
        width: 1rem;
        height: 1rem;
      }
      .toolbar {
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        gap: var(--s-3);
        padding: var(--s-3);
      }
      .toolbar__select {
        flex: 0 1 280px;
        min-width: 200px;
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
      .plate {
        font-weight: 600;
        color: var(--c-text);
        font-variant-numeric: tabular-nums;
        letter-spacing: 0.02em;
      }
      .sub {
        display: block;
        font-size: var(--fs-xs);
        color: var(--c-text-subtle);
      }
      @media (max-width: 640px) {
        .page__head {
          flex-direction: column;
        }
        .toolbar__select {
          flex: 1 1 100%;
        }
      }
    `,
  ],
})
export class VisiteListComponent implements OnInit {
  private readonly service = inject(ConformiteService);

  protected readonly visites = signal<Visite[]>([]);
  protected readonly vehicules = signal<OptionVehicule[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected vehiculeId: number | null = null;

  ngOnInit(): void {
    this.service.vehicules().subscribe((v) => this.vehicules.set(v));
    this.charger();
  }

  protected depassee(dateEcheance: string): boolean {
    return dateEcheance < new Date().toISOString().slice(0, 10);
  }

  protected charger(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.listerVisites(this.vehiculeId).subscribe({
      next: (list) => {
        this.visites.set(list);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Impossible de charger les visites techniques.');
        this.loading.set(false);
      },
    });
  }

  protected reinitialiser(): void {
    this.vehiculeId = null;
    this.charger();
  }
}
