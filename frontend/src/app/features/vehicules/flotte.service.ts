import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Fournisseur, FournisseurRequest, RechercheVehicule, Vehicule, VehiculeRequest } from './flotte.models';

@Injectable({ providedIn: 'root' })
export class FlotteService {
  private readonly http = inject(HttpClient);
  private readonly vehicules = '/api/v1/vehicules';
  private readonly fournisseurs = '/api/v1/fournisseurs';

  rechercherVehicules(f: RechercheVehicule): Observable<Vehicule[]> {
    let params = new HttpParams();
    if (f.immatriculation) params = params.set('immatriculation', f.immatriculation);
    if (f.type) params = params.set('type', f.type);
    if (f.statut) params = params.set('statut', f.statut);
    return this.http.get<Vehicule[]>(this.vehicules, { params });
  }
  vehicule(id: number): Observable<Vehicule> {
    return this.http.get<Vehicule>(`${this.vehicules}/${id}`);
  }
  creerVehicule(req: VehiculeRequest): Observable<Vehicule> {
    return this.http.post<Vehicule>(this.vehicules, req);
  }
  modifierVehicule(id: number, req: VehiculeRequest): Observable<Vehicule> {
    return this.http.put<Vehicule>(`${this.vehicules}/${id}`, req);
  }

  listerFournisseurs(): Observable<Fournisseur[]> {
    return this.http.get<Fournisseur[]>(this.fournisseurs);
  }
  creerFournisseur(req: FournisseurRequest): Observable<Fournisseur> {
    return this.http.post<Fournisseur>(this.fournisseurs, req);
  }
}
