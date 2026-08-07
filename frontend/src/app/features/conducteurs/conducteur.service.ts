import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Conducteur, ConducteurRequest, Permis, PermisRequest, ConducteurFilter } from './conducteur.models';

@Injectable({ providedIn: 'root' })
export class ConducteurService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/conducteurs';

  lister(filtre: ConducteurFilter): Observable<Conducteur[]> {
    let params = new HttpParams();
    if (filtre.recherche) params = params.set('recherche', filtre.recherche);
    if (filtre.provinceId != null) params = params.set('provinceId', filtre.provinceId);
    if (filtre.departementId != null) params = params.set('departementId', filtre.departementId);
    return this.http.get<Conducteur[]>(this.base, { params });
  }
  parId(id: number): Observable<Conducteur> {
    return this.http.get<Conducteur>(`${this.base}/${id}`);
  }
  creer(req: ConducteurRequest): Observable<Conducteur> {
    return this.http.post<Conducteur>(this.base, req);
  }
  modifier(id: number, req: ConducteurRequest): Observable<Conducteur> {
    return this.http.put<Conducteur>(`${this.base}/${id}`, req);
  }
  ajouterPermis(id: number, req: PermisRequest): Observable<Permis> {
    return this.http.post<Permis>(`${this.base}/${id}/permis`, req);
  }
  supprimerPermis(id: number, permisId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}/permis/${permisId}`);
  }
  supprimer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}