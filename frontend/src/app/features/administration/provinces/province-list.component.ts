import { HttpErrorResponse } from '@angular/common/http';
import { Component, ChangeDetectionStrategy, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AdministrationService } from '../administration.service';
import { Province } from '../utilisateur.models';

@Component({
  selector: 'app-province-list',
  imports: [FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page">
      <header class="page__head">
        <h2>Provinces</h2>
        <p class="text-muted text-sm">Référentiel des provinces rattachées aux responsables.</p>
      </header>

      @if (error()) {
        <div class="alert alert--danger" role="alert">{{ error() }}</div>
      }

      <div class="card page__panel">
        <form class="add" (ngSubmit)="ajouter()">
          <div class="field add__field">
            <label class="sr-only" for="nouvelle-province">Nouvelle province</label>
            <input
              id="nouvelle-province"
              class="input"
              name="nouveau"
              [(ngModel)]="nouveau"
              placeholder="Nom d'une nouvelle province"
              autocomplete="off"
            />
          </div>
          <button type="submit" class="btn btn--primary" [disabled]="!nouveau.trim() || saving()">
            @if (saving()) {
              <span class="spinner" aria-hidden="true"></span>
            }
            <span>Ajouter</span>
          </button>
        </form>

        <div class="list">
          @if (loading()) {
            <p class="list__state text-muted text-sm">Chargement…</p>
          } @else if (provinces().length === 0) {
            <p class="list__state text-muted text-sm">Aucune province enregistrée.</p>
          } @else {
            <ul class="list__items">
              @for (p of provinces(); track p.id) {
                <li class="list__item">{{ p.libelle }}</li>
              }
            </ul>
          }
        </div>
      </div>
    </section>
  `,
  styles: [
    `
      .page {
        display: flex;
        flex-direction: column;
        gap: var(--s-5);
      }

      .page__head p {
        margin-top: var(--s-1);
      }

      .page__panel {
        padding: 0;
        overflow: hidden;
      }

      .add {
        display: flex;
        align-items: flex-start;
        gap: var(--s-3);
        padding: var(--s-4);
        border-bottom: 1px solid var(--c-border);
      }

      .add__field {
        flex: 1;
      }

      .list__state {
        padding: var(--s-5) var(--s-4);
        text-align: center;
      }

      .list__items {
        margin: 0;
        padding: 0;
        list-style: none;
      }

      .list__item {
        padding: var(--s-3) var(--s-4);
        border-bottom: 1px solid var(--c-border);
      }

      .list__item:last-child {
        border-bottom: 0;
      }
    `,
  ],
})
export class ProvinceListComponent implements OnInit {
  private readonly service = inject(AdministrationService);

  protected readonly provinces = signal<Province[]>([]);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);
  protected nouveau = '';

  ngOnInit(): void {
    this.charger();
  }

  private charger(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.listerProvinces().subscribe({
      next: (list) => {
        this.provinces.set(list);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Impossible de charger les provinces.');
        this.loading.set(false);
      },
    });
  }

  protected ajouter(): void {
    const libelle = this.nouveau.trim();
    if (!libelle || this.saving()) return;

    this.saving.set(true);
    this.error.set(null);
    this.service.creerProvince(libelle).subscribe({
      next: (province) => {
        this.provinces.update((list) => [...list, province]);
        this.nouveau = '';
        this.saving.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(err?.error?.detail ?? 'Ajout impossible.');
        this.saving.set(false);
      },
    });
  }
}
