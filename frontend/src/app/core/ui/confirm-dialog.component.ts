import { ChangeDetectionStrategy, Component, effect, inject, viewChild, ElementRef } from '@angular/core';
import { ConfirmService } from './confirm.service';

/**
 * Rendu de la boîte de confirmation. Modale accessible : focus déplacé sur le
 * bouton de confirmation à l'ouverture, fermeture par Échap ou clic sur le
 * voile, `role="alertdialog"`.
 */
@Component({
  selector: 'app-confirm-dialog',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { '(document:keydown.escape)': 'onEscape()' },
  template: `
    @if (service.state(); as s) {
      <div class="scrim">
        <div
          class="dialog card"
          role="alertdialog"
          aria-modal="true"
          aria-labelledby="confirm-title"
          aria-describedby="confirm-message"
        >
          <div class="dialog__icon" [class.dialog__icon--danger]="s.danger" aria-hidden="true">
            @if (s.danger) {
              <svg viewBox="0 0 24 24">
                <path
                  d="M4 7h16M9 7V5.5A1.5 1.5 0 0 1 10.5 4h3A1.5 1.5 0 0 1 15 5.5V7m2 0v12a1.5 1.5 0 0 1-1.5 1.5h-7A1.5 1.5 0 0 1 7 19V7M10 11v6m4-6v6"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.6"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            } @else {
              <svg viewBox="0 0 24 24">
                <circle cx="12" cy="12" r="9" fill="none" stroke="currentColor" stroke-width="1.6" />
                <path
                  d="M12 8v5m0 3v.2"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.8"
                  stroke-linecap="round"
                />
              </svg>
            }
          </div>
          <h2 class="dialog__title" id="confirm-title">{{ s.title }}</h2>
          <p class="dialog__message" id="confirm-message">{{ s.message }}</p>
          <div class="dialog__actions">
            <button type="button" class="btn" (click)="service.resolve(false)">
              {{ s.cancelLabel ?? 'Annuler' }}
            </button>
            <button
              #confirmBtn
              type="button"
              class="btn"
              [class.btn--danger]="s.danger"
              [class.btn--primary]="!s.danger"
              (click)="service.resolve(true)"
            >
              {{ s.confirmLabel ?? 'Confirmer' }}
            </button>
          </div>
        </div>
      </div>
    }
  `,
  styles: [
    `
      .scrim {
        position: fixed;
        inset: 0;
        z-index: 1100;
        display: grid;
        place-items: center;
        padding: var(--s-4);
        background: var(--overlay);
        backdrop-filter: blur(2px);
        animation: fade var(--transition) both;
      }
      @keyframes fade {
        from {
          opacity: 0;
        }
        to {
          opacity: 1;
        }
      }
      .dialog {
        width: min(420px, 100%);
        padding: var(--s-5);
        text-align: center;
        box-shadow: var(--sh-lg);
        animation: pop var(--transition-slow) both;
      }
      @keyframes pop {
        from {
          opacity: 0;
          transform: translateY(8px) scale(0.97);
        }
        to {
          opacity: 1;
          transform: none;
        }
      }
      .dialog__icon {
        display: grid;
        place-items: center;
        width: 3rem;
        height: 3rem;
        margin: 0 auto var(--s-4);
        color: var(--brand-text);
        background: var(--brand-soft);
        border-radius: var(--r-full);
      }
      .dialog__icon svg {
        width: 1.5rem;
        height: 1.5rem;
      }
      .dialog__icon--danger {
        color: var(--danger-text);
        background: var(--danger-soft);
      }
      .dialog__title {
        font-size: var(--fs-lg);
      }
      .dialog__message {
        margin-top: var(--s-2);
        font-size: var(--fs-md);
        color: var(--text-2);
      }
      .dialog__actions {
        display: flex;
        justify-content: center;
        gap: var(--s-3);
        margin-top: var(--s-5);
      }
      .dialog__actions .btn {
        min-width: 7rem;
      }
    `,
  ],
})
export class ConfirmDialogComponent {
  protected readonly service = inject(ConfirmService);
  private readonly confirmBtn = viewChild<ElementRef<HTMLButtonElement>>('confirmBtn');

  constructor() {
    // Place le focus sur le bouton de confirmation à l'ouverture.
    effect(() => {
      if (this.service.state()) {
        queueMicrotask(() => this.confirmBtn()?.nativeElement.focus());
      }
    });
  }

  protected onEscape(): void {
    if (this.service.state()) this.service.resolve(false);
  }
}
