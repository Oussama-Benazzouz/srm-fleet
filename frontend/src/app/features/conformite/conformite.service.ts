import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { map, Observable } from 'rxjs';
import {
  Assurance,
  AssuranceRequest,
  OptionVehicule,
  PieceJointe,
  Sinistre,
  SinistreRequest,
  StatutSinistre,
  Visite,
  VisiteRequest,
} from './conformite.models';

@Injectable({ providedIn: 'root' })
export class ConformiteService {
  private readonly http = inject(HttpClient);

  // --- Assurances (RF-18) ---
  listerAssurances(vehiculeId?: number | null): Observable<Assurance[]> {
    let params = new HttpParams();
    if (vehiculeId) params = params.set('vehiculeId', vehiculeId);
    return this.http.get<Assurance[]>('/api/v1/assurances', { params });
  }
  creerAssurance(req: AssuranceRequest): Observable<Assurance> {
    return this.http.post<Assurance>('/api/v1/assurances', req);
  }

  // --- Visites techniques (RF-19) ---
  listerVisites(vehiculeId?: number | null): Observable<Visite[]> {
    let params = new HttpParams();
    if (vehiculeId) params = params.set('vehiculeId', vehiculeId);
    return this.http.get<Visite[]>('/api/v1/visites-techniques', { params });
  }
  creerVisite(req: VisiteRequest): Observable<Visite> {
    return this.http.post<Visite>('/api/v1/visites-techniques', req);
  }

  // --- Sélection de véhicule (scopée par le backend selon le rôle) ---
  vehicules(): Observable<OptionVehicule[]> {
    return this.http
      .get<{ id: number; immatriculation: string; typeVehicule: string }[]>('/api/v1/vehicules')
      .pipe(
        map((list) =>
          list.map((v) => ({ id: v.id, libelle: `${v.immatriculation} — ${v.typeVehicule}` })),
        ),
      );
  }

  /**
   * Raisons sociales des fournisseurs d'un type donné, pour renseigner le nom de la compagnie
   * (assurance → ASSURANCE, contrôle technique → GARAGE). Le backend stocke un simple texte :
   * on soumet donc la raison sociale sélectionnée.
   */
  compagnies(type: 'ASSURANCE' | 'GARAGE'): Observable<string[]> {
    return this.http
      .get<{ raisonSociale: string; type: string }[]>('/api/v1/fournisseurs')
      .pipe(map((list) => list.filter((f) => f.type === type).map((f) => f.raisonSociale)));
  }

  // --- Sinistres (RF-23, RF-24) ---
  listerSinistres(statut?: StatutSinistre | '', vehiculeId?: number | null): Observable<Sinistre[]> {
    let params = new HttpParams();
    if (statut) params = params.set('statut', statut);
    if (vehiculeId) params = params.set('vehiculeId', vehiculeId);
    return this.http.get<Sinistre[]>('/api/v1/sinistres', { params });
  }
  sinistre(id: number): Observable<Sinistre> {
    return this.http.get<Sinistre>(`/api/v1/sinistres/${id}`);
  }
  creerSinistre(req: SinistreRequest): Observable<Sinistre> {
    return this.http.post<Sinistre>('/api/v1/sinistres', req);
  }
  changerStatutSinistre(id: number, statut: StatutSinistre): Observable<Sinistre> {
    return this.http.patch<Sinistre>(`/api/v1/sinistres/${id}/statut`, { statut });
  }

  // --- Pièces jointes (RF-24) ---
  listerDocuments(sinistreId: number): Observable<PieceJointe[]> {
    return this.http.get<PieceJointe[]>(`/api/v1/sinistres/${sinistreId}/documents`);
  }
  televerserDocument(sinistreId: number, fichier: File): Observable<PieceJointe> {
    const data = new FormData();
    data.append('fichier', fichier);
    return this.http.post<PieceJointe>(`/api/v1/sinistres/${sinistreId}/documents`, data);
  }
  /** Contenu binaire d'une pièce jointe (le JWT est ajouté par l'intercepteur). */
  telechargerDocument(documentId: number): Observable<Blob> {
    return this.http.get(`/api/v1/documents/${documentId}/contenu`, { responseType: 'blob' });
  }
}
