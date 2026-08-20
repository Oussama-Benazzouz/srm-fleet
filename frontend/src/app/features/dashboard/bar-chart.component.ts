import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { CompteParLibelle } from './dashboard.models';

/** Barres horizontales (une seule série, une seule teinte). Sans dépendance. */
@Component({
  selector: 'app-bar-chart',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (items().length === 0) {
      <p class="text-muted text-sm">Aucune donnée</p>
    } @else {
      <ul class="bars">
        @for (it of items(); track it.libelle) {
          <li class="bar">
            <div class="bar__head">
              <span class="bar__label truncate">{{ it.libelle }}</span>
              <span class="bar__value">{{ it.valeur }}</span>
            </div>
            <div class="bar__track">
              <div
                class="bar__fill"
                [style.width.%]="largeur(it.valeur)"
                [style.background]="couleur()"
              ></div>
            </div>
          </li>
        }
      </ul>
    }
  `,
  styles: [
    `
      .bars {
        list-style: none;
        margin: 0;
        padding: 0;
        display: flex;
        flex-direction: column;
        gap: var(--s-4);
      }
      .bar__head {
        display: flex;
        justify-content: space-between;
        align-items: baseline;
        gap: var(--s-3);
        margin-bottom: var(--s-1);
        font-size: var(--fs-md);
      }
      .bar__label {
        color: var(--c-text-muted);
      }
      .bar__value {
        font-weight: 600;
        font-variant-numeric: tabular-nums;
      }
      .bar__track {
        height: 10px;
        border-radius: var(--r-full);
        background: var(--c-surface-alt);
        overflow: hidden;
      }
      .bar__fill {
        height: 100%;
        border-radius: var(--r-full);
        min-width: 2px;
        transition: width var(--transition);
      }
    `,
  ],
})
export class BarChartComponent {
  readonly items = input.required<CompteParLibelle[]>();
  readonly couleur = input<string>('var(--c-accent)');

  private readonly max = computed(() =>
    Math.max(1, ...this.items().map((i) => i.valeur)),
  );

  protected largeur(valeur: number): number {
    if (valeur <= 0) return 0;
    return Math.max((valeur / this.max()) * 100, 3);
  }
}
