import { Injectable, signal } from '@angular/core';

export type ToastKind = 'success' | 'error' | 'warning' | 'info';

export interface Toast {
  id: number;
  kind: ToastKind;
  title: string;
  message?: string;
  /** Durée d'affichage en ms ; 0 = persistant jusqu'à fermeture manuelle. */
  duration: number;
}

/**
 * File de notifications non bloquantes (toasts). Remplace les `alert()` et
 * les bannières d'erreur ponctuelles par un retour cohérent en bas d'écran.
 * Le rendu est assuré par ToastContainerComponent, monté une fois dans la
 * coquille applicative.
 */
@Injectable({ providedIn: 'root' })
export class ToastService {
  private seq = 0;
  readonly toasts = signal<Toast[]>([]);

  private push(kind: ToastKind, title: string, message?: string, duration = 4500): number {
    const id = ++this.seq;
    this.toasts.update((list) => [...list, { id, kind, title, message, duration }]);
    if (duration > 0) {
      setTimeout(() => this.dismiss(id), duration);
    }
    return id;
  }

  success(title: string, message?: string): number {
    return this.push('success', title, message);
  }

  error(title: string, message?: string): number {
    // Les erreurs restent un peu plus longtemps à l'écran.
    return this.push('error', title, message, 7000);
  }

  warning(title: string, message?: string): number {
    return this.push('warning', title, message, 6000);
  }

  info(title: string, message?: string): number {
    return this.push('info', title, message);
  }

  dismiss(id: number): void {
    this.toasts.update((list) => list.filter((t) => t.id !== id));
  }
}
