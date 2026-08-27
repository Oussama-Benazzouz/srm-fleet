import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { Segment } from './dashboard.models';

interface Arc {
  libelle: string;
  valeur: number;
  couleur: string;
  dasharray: string;
  dashoffset: number;
}

/**
 * Anneau (donut) en SVG pur : chaque part est un cercle avec un tiret proportionnel,
 * un petit espace de 1 unité entre parts, et une légende sous le graphe. Aucune dépendance.
 */
@Component({
  selector: 'app-donut-chart',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <figure class="donut">
      @if (total() === 0) {
        <div class="donut__empty text-muted text-sm">Aucune donnée</div>
      } @else {
        <svg viewBox="0 0 42 42" class="donut__svg" role="img" [attr.aria-label]="titre()">
          <circle cx="21" cy="21" r="15.915" fill="none" stroke="var(--chart-track)" stroke-width="4" />
          @for (a of arcs(); track $index) {
            <circle
              cx="21"
              cy="21"
              r="15.915"
              fill="none"
              stroke-linecap="round"
              [style.stroke]="a.couleur"
              stroke-width="4"
              [attr.stroke-dasharray]="a.dasharray"
              [attr.stroke-dashoffset]="a.dashoffset"
            >
              <title>{{ a.libelle }} : {{ a.valeur }}</title>
            </circle>
          }
          <text x="21" y="20.5" text-anchor="middle" class="donut__total">{{ total() }}</text>
          <text x="21" y="25.5" text-anchor="middle" class="donut__unit">{{ unite() }}</text>
        </svg>
      }
      <ul class="legend">
        @for (s of segments(); track s.libelle) {
          <li class="legend__item">
            <span class="legend__dot" [style.background]="s.couleur" aria-hidden="true"></span>
            <span class="legend__label truncate">{{ s.libelle }}</span>
            <span class="legend__value">{{ s.valeur }}</span>
          </li>
        }
      </ul>
    </figure>
  `,
  styles: [
    `
      .donut {
        margin: 0;
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: var(--s-4);
      }
      .donut__svg {
        width: 148px;
        height: 148px;
      }
      .donut__empty {
        display: grid;
        place-items: center;
        width: 148px;
        height: 148px;
        border-radius: var(--r-full);
        background: var(--c-surface-alt);
      }
      .donut__total {
        font-size: 8px;
        font-weight: 700;
        fill: var(--text-1);
      }
      .donut__unit {
        font-size: 3px;
        letter-spacing: 0.04em;
        text-transform: uppercase;
        fill: var(--text-3);
      }
      .legend {
        list-style: none;
        margin: 0;
        padding: 0;
        width: 100%;
        display: flex;
        flex-direction: column;
        gap: var(--s-2);
      }
      .legend__item {
        display: flex;
        align-items: center;
        gap: var(--s-2);
        font-size: var(--fs-md);
      }
      .legend__dot {
        flex: none;
        width: 10px;
        height: 10px;
        border-radius: var(--r-sm);
      }
      .legend__label {
        flex: 1;
        color: var(--c-text-muted);
      }
      .legend__value {
        font-weight: 600;
        font-variant-numeric: tabular-nums;
      }
    `,
  ],
})
export class DonutChartComponent {
  readonly segments = input.required<Segment[]>();
  readonly unite = input<string>('');
  readonly titre = input<string>('Répartition');

  protected readonly total = computed(() =>
    this.segments().reduce((somme, s) => somme + s.valeur, 0),
  );

  /** Découpe l'anneau : tiret proportionnel, 1 unité d'espace, départ en haut (offset 25). */
  protected readonly arcs = computed<Arc[]>(() => {
    const total = this.total();
    if (total === 0) return [];
    const gap = 1;
    let cumul = 0;
    return this.segments()
      .filter((s) => s.valeur > 0)
      .map((s) => {
        const pct = (s.valeur / total) * 100;
        const dash = Math.max(pct - gap, 0.01);
        const arc: Arc = {
          libelle: s.libelle,
          valeur: s.valeur,
          couleur: s.couleur,
          dasharray: `${dash} ${100 - dash}`,
          dashoffset: 25 - cumul,
        };
        cumul += pct;
        return arc;
      });
  });
}
