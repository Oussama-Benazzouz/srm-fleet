import { ChangeDetectionStrategy, Component, input } from '@angular/core';

/**
 * Marque SRM Souss-Massa. Le logo officiel (charte : ne jamais recolorer ni
 * déformer) est posé sur une pastille blanche afin de rester lisible sur les
 * deux thèmes. Le libellé « Gestion de flotte » l'accompagne en option.
 */
@Component({
  selector: 'app-brand-logo',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <span class="brand" [class.brand--stacked]="stacked()">
      <span class="brand__chip" [style.width.px]="size()" [style.height.px]="size()">
        <img src="logo-srm.jpg" alt="SRM Souss-Massa" width="40" height="40" />
      </span>
      @if (showLabel()) {
        <span class="brand__text">
          <span class="brand__name">SRM Souss-Massa</span>
          <span class="brand__sub">{{ label() }}</span>
        </span>
      }
    </span>
  `,
  styles: [
    `
      .brand {
        display: inline-flex;
        align-items: center;
        gap: var(--s-3);
        min-width: 0;
      }
      .brand--stacked {
        flex-direction: column;
        gap: var(--s-3);
      }
      .brand__chip {
        display: grid;
        place-items: center;
        flex: none;
        background: #fff;
        border-radius: var(--r-md);
        box-shadow: inset 0 0 0 1px rgba(15, 34, 58, 0.08);
        overflow: hidden;
      }
      .brand__chip img {
        width: 82%;
        height: 82%;
        object-fit: contain;
      }
      .brand__text {
        display: flex;
        flex-direction: column;
        min-width: 0;
        line-height: 1.2;
      }
      .brand--stacked .brand__text {
        align-items: center;
        text-align: center;
      }
      .brand__name {
        font-size: var(--fs-md);
        font-weight: 700;
        letter-spacing: -0.01em;
        color: var(--text-1);
      }
      .brand__sub {
        font-size: var(--fs-xs);
        font-weight: 500;
        color: var(--text-3);
        text-transform: uppercase;
        letter-spacing: 0.06em;
      }
    `,
  ],
})
export class BrandLogoComponent {
  readonly size = input(34);
  readonly showLabel = input(true);
  readonly stacked = input(false);
  readonly label = input('Gestion de flotte');
}
