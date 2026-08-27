import { ChangeDetectionStrategy, Component, inject, input, signal } from '@angular/core';
import { ExportService, FormatExport } from './export.service';
import { ConfirmService } from '../../core/ui/confirm.service';
import { ToastService } from '../../core/ui/toast.service';

/**
 * Bouton « Exporter » réutilisable, à poser sur chaque écran de liste. Il exporte le module
 * indiqué au format choisi (CSV/PDF) en reprenant les filtres actifs (RF-25) et prévient si
 * aucun filtre n'est appliqué (RF-26).
 */
@Component({
  selector: 'app-export-button',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    '(document:click)': 'fermer()',
    '(document:keydown.escape)': 'fermer()',
  },
  template: `
    <div class="export">
      <button
        type="button"
        class="btn"
        [disabled]="telechargement()"
        [attr.aria-expanded]="ouvert()"
        (click)="basculer($event)"
      >
        @if (telechargement()) {
          <span class="spinner" aria-hidden="true"></span>
        } @else {
          <svg class="icon" viewBox="0 0 20 20" aria-hidden="true">
            <path
              d="M10 3v9m0 0 3.5-3.5M10 12 6.5 8.5M4 14.5v1A1.5 1.5 0 0 0 5.5 17h9a1.5 1.5 0 0 0 1.5-1.5v-1"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        }
        Exporter
      </button>

      @if (ouvert()) {
        <div class="export__menu" role="menu">
          <button type="button" class="export__item" role="menuitem" (click)="exporter('CSV')">CSV</button>
          <button type="button" class="export__item" role="menuitem" (click)="exporter('PDF')">PDF</button>
        </div>
      }

      @if (erreur()) {
        <span class="export__erreur" role="alert">{{ erreur() }}</span>
      }
    </div>
  `,
  styles: [
    `
      .export {
        position: relative;
        display: inline-flex;
      }
      .icon {
        width: 1rem;
        height: 1rem;
      }
      .export__menu {
        position: absolute;
        top: calc(100% + 4px);
        right: 0;
        z-index: 20;
        min-width: 8rem;
        padding: var(--s-1);
        background: var(--c-surface);
        border: 1px solid var(--c-border);
        border-radius: var(--r-md);
        box-shadow: var(--sh-md);
        display: flex;
        flex-direction: column;
        gap: 2px;
      }
      .export__item {
        width: 100%;
        text-align: left;
        padding: var(--s-2) var(--s-3);
        font-size: var(--fs-md);
        color: var(--c-text);
        background: transparent;
        border: 0;
        border-radius: var(--r-sm);
        cursor: pointer;
      }
      .export__item:hover {
        background: var(--c-surface-alt);
      }
      .export__erreur {
        position: absolute;
        top: calc(100% + 4px);
        right: 0;
        white-space: nowrap;
        font-size: var(--fs-xs);
        color: var(--c-danger);
      }
    `,
  ],
})
export class ExportButtonComponent {
  private readonly service = inject(ExportService);
  private readonly confirm = inject(ConfirmService);
  private readonly toast = inject(ToastService);

  readonly resource = input.required<string>();
  readonly params = input<Record<string, unknown>>({});

  protected readonly ouvert = signal(false);
  protected readonly telechargement = signal(false);
  protected readonly erreur = signal<string | null>(null);

  protected basculer(event: MouseEvent): void {
    event.stopPropagation(); // évite la fermeture immédiate par le handler document
    this.erreur.set(null);
    this.ouvert.update((o) => !o);
  }

  protected fermer(): void {
    this.ouvert.set(false);
  }

  protected async exporter(format: FormatExport): Promise<void> {
    this.ouvert.set(false);
    if (!this.filtresActifs()) {
      const ok = await this.confirm.ask({
        title: 'Exporter sans filtre ?',
        message:
          "Aucun filtre n'est appliqué : l'export contiendra l'ensemble des données de votre périmètre. Continuer ?",
        confirmLabel: 'Exporter tout',
      });
      if (!ok) return;
    }
    this.telechargement.set(true);
    this.erreur.set(null);
    this.service.telecharger(this.resource(), format, this.params()).subscribe({
      next: (reponse) => {
        this.service.enregistrer(reponse, `${this.resource()}.${format.toLowerCase()}`);
        this.telechargement.set(false);
        this.toast.success('Export généré', `${this.resource()}.${format.toLowerCase()}`);
      },
      error: () => {
        this.erreur.set('Export impossible.');
        this.telechargement.set(false);
        this.toast.error('Export impossible', 'Veuillez réessayer.');
      },
    });
  }

  /** Vrai si au moins un filtre a une valeur (RF-26 : avertissement sinon). */
  private filtresActifs(): boolean {
    return Object.values(this.params()).some((v) => v !== null && v !== undefined && v !== '');
  }
}
