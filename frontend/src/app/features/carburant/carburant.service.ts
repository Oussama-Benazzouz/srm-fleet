import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { map, Observable } from 'rxjs';
import {
  Carte,
  Demande,
  DemandeRequest,
  OptionAffectation,
  OptionFournisseur,
  ValiderRequest,
} from './carburant.models';

@Injectable({ providedIn: 'root' })
export class CarburantService {
  private readonly http = inject(HttpClient);

  // --- Demandes ---
  listerDemandes(statut?: string): Observable<Demande[]> {
    let params = new HttpParams();
    if (statut) params = params.set('statut', statut);
    return this.http.get<Demande[]>('/api/v1/demandes', { params });
  }
  demande(id: number): Observable<Demande> {
    return this.http.get<Demande>(`/api/v1/demandes/${id}`);
  }
  creerDemande(req: DemandeRequest): Observable<Demande> {
    return this.http.post<Demande>('/api/v1/demandes', req);
  }
  validerDemande(id: number, req: ValiderRequest): Observable<Demande> {
    return this.http.patch<Demande>(`/api/v1/demandes/${id}/valider`, req);
  }
  refuserDemande(id: number): Observable<Demande> {
    return this.http.patch<Demande>(`/api/v1/demandes/${id}/refuser`, {});
  }

  // --- Cartes ---
  listerCartes(statut?: string): Observable<Carte[]> {
    let params = new HttpParams();
    if (statut) params = params.set('statut', statut);
    return this.http.get<Carte[]>('/api/v1/cartes', { params });
  }
  cloturerCarte(id: number): Observable<Carte> {
    return this.http.patch<Carte>(`/api/v1/cartes/${id}/cloturer`, {});
  }

  // --- Sélections (déjà scopées par le backend selon le rôle) ---
  affectationsActives(): Observable<OptionAffectation[]> {
    return this.http
      .get<{ id: number; immatriculation: string; conducteurNomComplet: string }[]>(
        '/api/v1/affectations',
        { params: new HttpParams().set('statut', 'ACTIVE') },
      )
      .pipe(
        map((list) =>
          list.map((a) => ({
            id: a.id,
            libelle: `${a.immatriculation} — ${a.conducteurNomComplet}`,
          })),
        ),
      );
  }
  fournisseursCarburant(): Observable<OptionFournisseur[]> {
    return this.http
      .get<{ id: number; raisonSociale: string; type: string }[]>('/api/v1/fournisseurs')
      .pipe(
        map((list) =>
          list
            .filter((f) => f.type === 'CARBURANT')
            .map((f) => ({ id: f.id, raisonSociale: f.raisonSociale })),
        ),
      );
  }
}
