import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ConfigurationAlerte, TYPE_SOURCE_LABELS, TypeSourceAlerte } from './alertes.models';
import { AlertesService } from './alertes.service';

@Component({
  selector: 'app-seuils-alerte',
  imports: [FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page">
      <header class="page__head">
        <div>
          <h2>Seuils d'alerte</h2>
          <p class="text-muted text-sm">
            Nombre de jours avant l'échéance à partir duquel une alerte est déclenchée, par type
            (RF-20).
          </p>
        </div>
      </header>

      @if (error()) {
        <div class="alert alert--danger" role="alert">{{ error() }}</div>
      }
      @if (info()) {
        <div class="alert alert--info" role="status">{{ info() }}</div>
      }

      <div class="card panel">
        @if (loading()) {
          <div class="panel__state">
            <span class="spinner" aria-hidden="true"></span>
            <span class="text-muted text-sm">Chargement…</span>
          </div>
        } @else {
          <div class="panel__scroll">
            <table class="table">
              <thead>
                <tr>
                  <th>Type d'échéance</th>
                  <th>Seuil (jours)</th>
                  <th class="actions-head"><span class="sr-only">Actions</span></th>
                </tr>
              </thead>
              <tbody>
                @for (c of configs(); track c.typeSource) {
                  <tr>
                    <td>{{ typeLabel(c.typeSource) }}</td>
                    <td>
                      <input
                        class="input seuil"
                        type="number"
                        min="1"
                        [(ngModel)]="c.seuilJours"
                        [name]="c.typeSource"
                        [attr.aria-label]="'Seuil pour ' + typeLabel(c.typeSource)"
                      />
                    </td>
                    <td>
                      <div class="row-actions">
                        <button
                          type="button"
                          class="btn btn--sm btn--primary"
                          [disabled]="saving() === c.typeSource || !valide(c.seuilJours)"
                          (click)="enregistrer(c)"
                        >
                          @if (saving() === c.typeSource) {
                            <span class="spinner" aria-hidden="true"></span>
                          }
                          Enregistrer
                        </button>
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
  styles: [
    `
      .page {
        display: flex;
        flex-direction: column;
        gap: var(--s-5);
        max-width: 720px;
      }
      .page__head p {
        margin-top: var(--s-1);
      }
      .alert--info {
        color: var(--c-accent);
        background: var(--c-accent-soft);
        border-color: var(--c-accent-ring);
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
      .seuil {
        width: 8rem;
      }
      .actions-head {
        text-align: right;
      }
      .row-actions {
        display: flex;
        justify-content: flex-end;
      }
    `,
  ],
})
export class SeuilsAlerteComponent implements OnInit {
  private readonly service = inject(AlertesService);

  protected readonly configs = signal<ConfigurationAlerte[]>([]);
  protected readonly loading = signal(false);
  protected readonly saving = signal<TypeSourceAlerte | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly info = signal<string | null>(null);

  ngOnInit(): void {
    this.loading.set(true);
    this.service.listerConfigurations().subscribe({
      next: (list) => {
        this.configs.set(list);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Impossible de charger les seuils.');
        this.loading.set(false);
      },
    });
  }

  protected typeLabel(t: TypeSourceAlerte): string {
    return TYPE_SOURCE_LABELS[t];
  }

  protected valide(seuil: number): boolean {
    return Number.isInteger(seuil) && seuil > 0;
  }

  protected enregistrer(c: ConfigurationAlerte): void {
    if (!this.valide(c.seuilJours)) return;
    this.saving.set(c.typeSource);
    this.error.set(null);
    this.info.set(null);
    this.service.modifierSeuil(c.typeSource, c.seuilJours).subscribe({
      next: () => {
        this.info.set(`Seuil « ${this.typeLabel(c.typeSource)} » enregistré à ${c.seuilJours} jours.`);
        this.saving.set(null);
      },
      error: (e) => {
        this.error.set(e?.error?.detail ?? "Échec de l'enregistrement.");
        this.saving.set(null);
      },
    });
  }
}
