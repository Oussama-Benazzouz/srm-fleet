import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { map, Observable } from 'rxjs';
import { Assurance, AssuranceRequest, OptionVehicule, Visite, VisiteRequest } from './conformite.models';

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
}
