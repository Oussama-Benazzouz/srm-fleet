import { Injectable, signal } from '@angular/core';

export interface ConfirmOptions {
  title: string;
  message: string;
  /** Libellé du bouton de confirmation. */
  confirmLabel?: string;
  cancelLabel?: string;
  /** Style destructeur (rouge) pour les suppressions. */
  danger?: boolean;
}

interface ConfirmState extends ConfirmOptions {
  resolve: (ok: boolean) => void;
}

/**
 * Boîte de dialogue de confirmation modale, promise-based. Remplace le
 * `confirm()` natif pour les actions irréversibles (suppression, export
 * global…). Le rendu est assuré par ConfirmDialogComponent dans la coquille.
 */
@Injectable({ providedIn: 'root' })
export class ConfirmService {
  readonly state = signal<ConfirmState | null>(null);

  ask(options: ConfirmOptions): Promise<boolean> {
    return new Promise<boolean>((resolve) => {
      this.state.set({ ...options, resolve });
    });
  }

  /** Confirmation de suppression prête à l'emploi. */
  askDelete(subject: string): Promise<boolean> {
    return this.ask({
      title: 'Confirmer la suppression',
      message: `Voulez-vous vraiment supprimer ${subject} ? Cette action est irréversible.`,
      confirmLabel: 'Supprimer',
      danger: true,
    });
  }

  resolve(ok: boolean): void {
    const s = this.state();
    if (s) {
      s.resolve(ok);
      this.state.set(null);
    }
  }
}
