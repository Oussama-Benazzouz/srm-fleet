import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { map, Observable } from 'rxjs';
import { Affectation, AffectationRequest, OptionConducteur, OptionVehicule } from './affectation.models';

@Injectable({ providedIn: 'root' })
export class AffectationService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/affectations';

  lister(statut?: string): Observable<Affectation[]> {
    let params = new HttpParams();
    if (statut) { params = params.set('statut', statut); }
    return this.http.get<Affectation[]>(this.base, { params });
  }
  creer(req: AffectationRequest): Observable<Affectation> {
    return this.http.post<Affectation>(this.base, req);
  }
  cloturer(id: number, dateFin: string | null): Observable<Affectation> {
    return this.http.patch<Affectation>(`${this.base}/${id}/cloturer`, { dateFin });
  }
  historiqueVehicule(vehiculeId: number): Observable<Affectation[]> {
    return this.http.get<Affectation[]>(`${this.base}/historique/vehicule/${vehiculeId}`);
  }
  historiqueConducteur(conducteurId: number): Observable<Affectation[]> {
    return this.http.get<Affectation[]>(`${this.base}/historique/conducteur/${conducteurId}`);
  }

  // Sélections (déjà scopées par le backend selon le rôle). On conserve provinceId
  // pour pouvoir imposer « même province » côté formulaire (surtout pour l'admin central).
  vehiculesDisponibles(): Observable<OptionVehicule[]> {
    return this.http.get<{ id: number; immatriculation: string; provinceId: number | null }[]>('/api/v1/vehicules', {
      params: new HttpParams().set('statut', 'DISPONIBLE'),
    }).pipe(map((list) => list.map((v) => ({ id: v.id, immatriculation: v.immatriculation, provinceId: v.provinceId }))));
  }
  conducteurs(): Observable<OptionConducteur[]> {
    return this.http.get<{ id: number; matricule: string; nom: string; prenom: string; provinceId: number | null }[]>('/api/v1/conducteurs')
      .pipe(map((list) => list.map((c) => ({
        id: c.id,
        libelle: `${c.matricule} — ${c.prenom} ${c.nom}`,
        provinceId: c.provinceId,
      }))));
  }
}