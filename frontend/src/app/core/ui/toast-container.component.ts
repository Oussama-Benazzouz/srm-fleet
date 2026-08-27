import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ToastService } from './toast.service';

/**
 * Zone de rendu des toasts. Montée une seule fois dans la coquille. Chaque
 * toast est une « polite live region » : annoncé sans interrompre, fermable
 * au clic ou à l'attente du délai géré par le service.
 */
@Component({
  selector: 'app-toast-container',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="toast-viewport" aria-live="polite" aria-relevant="additions">
      @for (t of service.toasts(); track t.id) {
        <div class="toast" [class]="'toast--' + t.kind" role="status">
          <span class="toast__icon" aria-hidden="true">
            @switch (t.kind) {
              @case ('success') {
                <svg viewBox="0 0 20 20">
                  <path
                    d="M10 18a8 8 0 1 0 0-16 8 8 0 0 0 0 16Z"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.5"
                  />
                  <path
                    d="m6.5 10 2.2 2.2L13.5 7.5"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.6"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
              }
              @case ('error') {
                <svg viewBox="0 0 20 20">
                  <path
                    d="M10 18a8 8 0 1 0 0-16 8 8 0 0 0 0 16Z"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.5"
                  />
                  <path
                    d="M10 6v5m0 2.6v.2"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.7"
                    stroke-linecap="round"
                  />
                </svg>
              }
              @case ('warning') {
                <svg viewBox="0 0 20 20">
                  <path
                    d="M10 3 2.5 16.5h15L10 3Z"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.5"
                    stroke-linejoin="round"
                  />
                  <path
                    d="M10 8v3.4m0 2.3v.2"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.7"
                    stroke-linecap="round"
                  />
                </svg>
              }
              @default {
                <svg viewBox="0 0 20 20">
                  <path
                    d="M10 18a8 8 0 1 0 0-16 8 8 0 0 0 0 16Z"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.5"
                  />
                  <path
                    d="M10 9v5m0-8.2v.2"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.7"
                    stroke-linecap="round"
                  />
                </svg>
              }
            }
          </span>
          <div class="toast__body">
            <p class="toast__title">{{ t.title }}</p>
            @if (t.message) {
              <p class="toast__message">{{ t.message }}</p>
            }
          </div>
          <button
            type="button"
            class="toast__close"
            (click)="service.dismiss(t.id)"
            aria-label="Fermer la notification"
          >
            <svg viewBox="0 0 20 20" aria-hidden="true">
              <path
                d="m6 6 8 8m0-8-8 8"
                fill="none"
                stroke="currentColor"
                stroke-width="1.6"
                stroke-linecap="round"
              />
            </svg>
          </button>
        </div>
      }
    </div>
  `,
  styles: [
    `
      .toast {
        pointer-events: auto;
        display: flex;
        align-items: flex-start;
        gap: var(--s-3);
        padding: var(--s-3) var(--s-3) var(--s-3) var(--s-4);
        background: var(--surface-1);
        border: 1px solid var(--border);
        border-left: 3px solid var(--brand);
        border-radius: var(--r-md);
        box-shadow: var(--sh-lg);
        animation: toast-in var(--transition-slow) both;
      }

      @keyframes toast-in {
        from {
          opacity: 0;
          transform: translateY(10px) scale(0.98);
        }
        to {
          opacity: 1;
          transform: none;
        }
      }

      .toast--success {
        border-left-color: var(--success);
      }
      .toast--error {
        border-left-color: var(--danger);
      }
      .toast--warning {
        border-left-color: var(--warning);
      }
      .toast--info {
        border-left-color: var(--info);
      }

      .toast__icon {
        flex: none;
        width: 1.25rem;
        height: 1.25rem;
        margin-top: 0.0625rem;
      }
      .toast__icon svg {
        width: 100%;
        height: 100%;
      }
      .toast--success .toast__icon {
        color: var(--success);
      }
      .toast--error .toast__icon {
        color: var(--danger);
      }
      .toast--warning .toast__icon {
        color: var(--warning);
      }
      .toast--info .toast__icon {
        color: var(--info);
      }

      .toast__body {
        flex: 1;
        min-width: 0;
      }
      .toast__title {
        font-size: var(--fs-md);
        font-weight: 600;
        color: var(--text-1);
      }
      .toast__message {
        margin-top: 0.125rem;
        font-size: var(--fs-sm);
        color: var(--text-2);
        word-break: break-word;
      }

      .toast__close {
        flex: none;
        display: grid;
        place-items: center;
        width: 1.75rem;
        height: 1.75rem;
        padding: 0;
        color: var(--text-3);
        background: transparent;
        border: 0;
        border-radius: var(--r-sm);
        cursor: pointer;
        transition:
          color var(--transition),
          background var(--transition);
      }
      .toast__close:hover {
        color: var(--text-1);
        background: var(--surface-3);
      }
      .toast__close svg {
        width: 1.125rem;
        height: 1.125rem;
      }
    `,
  ],
})
export class ToastContainerComponent {
  protected readonly service = inject(ToastService);
}
