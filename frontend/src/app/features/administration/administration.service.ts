import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Departement, Province, Utilisateur, UtilisateurRequest } from './utilisateur.models';

const UTILISATEURS_URL = '/api/v1/utilisateurs';
const PROVINCES_URL = '/api/v1/provinces';
const DEPARTEMENTS_URL = '/api/v1/departements';

@Injectable({ providedIn: 'root' })
export class AdministrationService {
  private readonly http = inject(HttpClient);

  listerUtilisateurs(): Observable<Utilisateur[]> {
    return this.http.get<Utilisateur[]>(UTILISATEURS_URL);
  }

  utilisateurParId(id: number): Observable<Utilisateur> {
    return this.http.get<Utilisateur>(`${UTILISATEURS_URL}/${id}`);
  }

  creerUtilisateur(req: UtilisateurRequest): Observable<Utilisateur> {
    return this.http.post<Utilisateur>(UTILISATEURS_URL, req);
  }

  modifierUtilisateur(id: number, req: UtilisateurRequest): Observable<Utilisateur> {
    return this.http.put<Utilisateur>(`${UTILISATEURS_URL}/${id}`, req);
  }

  desactiver(id: number): Observable<Utilisateur> {
    return this.http.patch<Utilisateur>(`${UTILISATEURS_URL}/${id}/desactiver`, {});
  }

  activer(id: number): Observable<Utilisateur> {
    return this.http.patch<Utilisateur>(`${UTILISATEURS_URL}/${id}/activer`, {});
  }

  listerProvinces(): Observable<Province[]> {
    return this.http.get<Province[]>(PROVINCES_URL);
  }

  creerProvince(libelle: string): Observable<Province> {
    return this.http.post<Province>(PROVINCES_URL, { libelle });
  }

  listerDepartements(): Observable<Departement[]> {
    return this.http.get<Departement[]>(DEPARTEMENTS_URL);
  }

  creerDepartement(libelle: string): Observable<Departement> {
    return this.http.post<Departement>(DEPARTEMENTS_URL, { libelle });
  }
}
