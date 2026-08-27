import { Injectable, effect, signal } from '@angular/core';

export type ThemePreference = 'light' | 'dark' | 'system';

const STORAGE_KEY = 'srm-theme';

/**
 * Gère le thème clair / sombre de l'application.
 *
 * Trois états : `light` et `dark` forcent le thème via l'attribut
 * `data-theme` sur <html> ; `system` retire l'attribut et laisse la
 * requête `prefers-color-scheme` décider (voir styles.scss). Le choix est
 * mémorisé dans le localStorage et appliqué avant le rendu par un script
 * inline dans index.html afin d'éviter tout flash au chargement.
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly root = document.documentElement;
  private readonly media = window.matchMedia('(prefers-color-scheme: dark)');

  /** Préférence choisie par l'utilisateur. */
  readonly preference = signal<ThemePreference>(this.readStored());

  /** Thème effectivement appliqué (résout `system` en clair/sombre). */
  readonly resolved = signal<'light' | 'dark'>(this.resolve(this.preference()));

  constructor() {
    // Applique l'attribut et persiste à chaque changement de préférence.
    effect(() => {
      const pref = this.preference();
      if (pref === 'system') {
        this.root.removeAttribute('data-theme');
      } else {
        this.root.setAttribute('data-theme', pref);
      }
      try {
        if (pref === 'system') localStorage.removeItem(STORAGE_KEY);
        else localStorage.setItem(STORAGE_KEY, pref);
      } catch {
        /* stockage indisponible : le thème reste valable pour la session. */
      }
      this.resolved.set(this.resolve(pref));
    });

    // Suit les changements système tant que la préférence est « system ».
    this.media.addEventListener('change', () => {
      if (this.preference() === 'system') this.resolved.set(this.resolve('system'));
    });
  }

  /** Bascule simple clair ⇄ sombre à partir du thème affiché. */
  toggle(): void {
    this.preference.set(this.resolved() === 'dark' ? 'light' : 'dark');
  }

  set(pref: ThemePreference): void {
    this.preference.set(pref);
  }

  private resolve(pref: ThemePreference): 'light' | 'dark' {
    if (pref === 'system') return this.media.matches ? 'dark' : 'light';
    return pref;
  }

  private readStored(): ThemePreference {
    try {
      const v = localStorage.getItem(STORAGE_KEY);
      if (v === 'light' || v === 'dark') return v;
    } catch {
      /* ignore */
    }
    return 'system';
  }
}
