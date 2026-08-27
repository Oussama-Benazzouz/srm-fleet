import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { ConformiteService } from './conformite.service';
import {
  PieceJointe,
  Sinistre,
  STATUT_SINISTRE_LABELS,
  STATUT_SINISTRE_SUIVANT,
  StatutSinistre,
} from './conformite.models';

@Component({
  selector: 'app-sinistre-detail',
  imports: [RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="page">
      <header class="page__head">
        <div>
          <a routerLink="/sinistres" class="back">← Sinistres</a>
          <h2>Sinistre</h2>
          <p class="text-muted text-sm">Fiche du sinistre, suivi du statut et pièces jointes.</p>
        </div>
      </header>

      @if (error()) {
        <div class="alert alert--danger" role="alert">{{ error() }}</div>
      }

      @if (sinistre(); as s) {
        <div class="card page__panel">
          <dl class="grid">
            <div>
              <dt>Véhicule</dt>
              <dd class="plate">{{ s.immatriculation }}</dd>
            </div>
            <div>
              <dt>Province</dt>
              <dd>{{ s.provinceLibelle ?? '—' }}</dd>
            </div>
            <div>
              <dt>Date des faits</dt>
              <dd>{{ s.dateSinistre }}</dd>
            </div>
            <div>
              <dt>Statut</dt>
              <dd>
                <span
                  class="badge"
                  [class.badge--success]="s.statut === 'CLOTURE'"
                  [class.badge--danger]="s.statut === 'OUVERT'"
                  >{{ statutLabel(s.statut) }}</span
                >
              </dd>
            </div>
            <div>
              <dt>Conducteur responsable</dt>
              <dd>{{ s.conducteurNomComplet ?? 'Aucune affectation à la date' }}</dd>
            </div>
            <div>
              <dt>Affectation</dt>
              <dd>{{ s.affectationId ? '#' + s.affectationId : '—' }}</dd>
            </div>
            <div style="grid-column:1 / -1;">
              <dt>Description</dt>
              <dd>{{ s.description || '—' }}</dd>
            </div>
          </dl>

          @if (statutSuivant(s.statut); as suivant) {
            <div class="form__actions">
              <button type="button" class="btn btn--primary" [disabled]="busy()" (click)="avancer(suivant)">
                @if (busy()) {
                  <span class="spinner" aria-hidden="true"></span>
                }
                {{ suivant === 'EN_COURS' ? 'Passer en cours' : 'Clôturer' }}
              </button>
            </div>
          }
        </div>

        <div class="card page__panel">
          <div class="pj__head">
            <div>
              <h3>Pièces jointes</h3>
              <p class="text-muted text-sm">Tous formats acceptés · 10 Mo maximum par fichier.</p>
            </div>
            <label class="btn btn--sm">
              <input type="file" class="sr-only" (change)="choisir($event)" [disabled]="uploading()" />
              Ajouter un fichier
            </label>
          </div>

          @if (fichierChoisi(); as f) {
            <div class="pj__pending">
              <span class="truncate">{{ f.name }}</span>
              <div class="pj__pending-actions">
                <button type="button" class="btn btn--sm" (click)="annulerChoix()" [disabled]="uploading()">
                  Retirer
                </button>
                <button type="button" class="btn btn--sm btn--primary" (click)="televerser()" [disabled]="uploading()">
                  @if (uploading()) {
                    <span class="spinner" aria-hidden="true"></span>
                  }
                  Téléverser
                </button>
              </div>
            </div>
          }

          @if (documents().length === 0) {
            <p class="text-muted text-sm">Aucune pièce jointe.</p>
          } @else {
            <ul class="pj__list">
              @for (d of documents(); track d.id) {
                <li class="pj__item">
                  <div class="pj__meta">
                    <span class="truncate">{{ d.nomFichier }}</span>
                    <span class="sub">{{ d.dateUpload }}</span>
                  </div>
                  <button type="button" class="btn btn--sm" (click)="telecharger(d)">Télécharger</button>
                </li>
              }
            </ul>
          }
        </div>
      }
    </section>
  `,
  styles: [
    `
      .page {
        display: flex;
        flex-direction: column;
        gap: var(--s-5);
        max-width: 720px;
      }
      .back {
        display: inline-block;
        margin-bottom: var(--s-2);
        font-size: var(--fs-sm);
        color: var(--c-text-muted);
        text-decoration: none;
      }
      .back:hover {
        color: var(--c-text);
      }
      .page__head p {
        margin-top: var(--s-1);
      }
      .page__panel {
        padding: var(--s-5);
        display: flex;
        flex-direction: column;
        gap: var(--s-4);
      }
      .grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: var(--s-4);
        margin: 0;
      }
      dt {
        font-size: var(--fs-xs);
        text-transform: uppercase;
        letter-spacing: 0.04em;
        color: var(--c-text-subtle);
      }
      dd {
        margin: var(--s-1) 0 0;
        color: var(--c-text);
      }
      .plate {
        font-weight: 600;
        font-variant-numeric: tabular-nums;
        letter-spacing: 0.02em;
      }
      .sub {
        font-size: var(--fs-xs);
        color: var(--c-text-subtle);
      }
      .form__actions {
        display: flex;
        justify-content: flex-end;
        gap: var(--s-3);
      }
      .pj__head {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: var(--s-3);
      }
      .pj__head label {
        cursor: pointer;
      }
      .pj__pending {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: var(--s-3);
        padding: var(--s-3);
        background: var(--c-surface-alt);
        border-radius: var(--r-md);
      }
      .pj__pending-actions {
        display: flex;
        gap: var(--s-2);
        flex: none;
      }
      .pj__list {
        list-style: none;
        margin: 0;
        padding: 0;
        display: flex;
        flex-direction: column;
        gap: var(--s-2);
      }
      .pj__item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: var(--s-3);
        padding: var(--s-3);
        border: 1px solid var(--c-border);
        border-radius: var(--r-md);
      }
      .pj__meta {
        display: flex;
        flex-direction: column;
        min-width: 0;
      }
      @media (max-width: 560px) {
        .grid {
          grid-template-columns: 1fr;
        }
      }
    `,
  ],
})
export class SinistreDetailComponent implements OnInit {
  private readonly service = inject(ConformiteService);
  private readonly route = inject(ActivatedRoute);

  protected readonly sinistre = signal<Sinistre | null>(null);
  protected readonly documents = signal<PieceJointe[]>([]);
  protected readonly fichierChoisi = signal<File | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly busy = signal(false);
  protected readonly uploading = signal(false);
  private id = 0;

  ngOnInit(): void {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.charger();
    this.chargerDocuments();
  }

  protected statutLabel(s: StatutSinistre): string {
    return STATUT_SINISTRE_LABELS[s];
  }

  protected statutSuivant(s: StatutSinistre): StatutSinistre | null {
    return STATUT_SINISTRE_SUIVANT[s];
  }

  private charger(): void {
    this.service.sinistre(this.id).subscribe({
      next: (s) => this.sinistre.set(s),
      error: (e) =>
        this.error.set(
          e?.status === 404 ? 'Sinistre introuvable dans votre périmètre.' : 'Impossible de charger le sinistre.',
        ),
    });
  }

  private chargerDocuments(): void {
    this.service.listerDocuments(this.id).subscribe({
      next: (list) => this.documents.set(list),
      error: () => this.documents.set([]),
    });
  }

  protected avancer(cible: StatutSinistre): void {
    this.busy.set(true);
    this.error.set(null);
    this.service.changerStatutSinistre(this.id, cible).subscribe({
      next: (s) => {
        this.sinistre.set(s);
        this.busy.set(false);
      },
      error: (e) => {
        this.error.set(e?.error?.detail ?? 'Changement de statut impossible.');
        this.busy.set(false);
      },
    });
  }

  protected choisir(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.fichierChoisi.set(input.files?.[0] ?? null);
  }

  protected annulerChoix(): void {
    this.fichierChoisi.set(null);
  }

  protected televerser(): void {
    const fichier = this.fichierChoisi();
    if (!fichier) return;
    this.uploading.set(true);
    this.error.set(null);
    this.service.televerserDocument(this.id, fichier).subscribe({
      next: () => {
        this.fichierChoisi.set(null);
        this.uploading.set(false);
        this.chargerDocuments();
      },
      error: (e) => {
        this.error.set(e?.error?.detail ?? 'Téléversement impossible.');
        this.uploading.set(false);
      },
    });
  }

  /** Récupère le contenu (avec JWT) puis déclenche l'enregistrement local sous son nom d'origine. */
  protected telecharger(d: PieceJointe): void {
    this.service.telechargerDocument(d.id).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = d.nomFichier;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => this.error.set('Téléchargement impossible.'),
    });
  }
}
