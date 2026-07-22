import { Component, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-home',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page">
      <header class="page__head">
        <h2>Socle Phase 1 opérationnel</h2>
        <p class="text-muted text-sm">
          L'application est prête pour le développement des modules.
        </p>
      </header>

      <div class="card page__panel">
        <p class="text-sm text-muted">
          Les modules de gestion de flotte apparaîtront ici au fur et à mesure de leur livraison.
        </p>
      </div>
    </section>
  `,
  styles: [
    `
      .page {
        display: flex;
        flex-direction: column;
        gap: var(--s-5);
        max-width: 1100px;
      }

      .page__head {
        display: flex;
        flex-direction: column;
        gap: var(--s-1);
      }

      .page__panel {
        padding: var(--s-5);
      }
    `,
  ],
})
export class HomeComponent {}
