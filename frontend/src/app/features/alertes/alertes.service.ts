import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Alerte, ConfigurationAlerte, GenerationResult, TypeSourceAlerte } from './alertes.models';

@Injectable({ providedIn: 'root' })
export class AlertesService {
  private readonly http = inject(HttpClient);

  // --- Tableau de bord (RF-22) ---
  listerAlertes(): Observable<Alerte[]> {
    return this.http.get<Alerte[]>('/api/v1/alertes');
  }
  marquerLue(id: number): Observable<Alerte> {
    return this.http.patch<Alerte>(`/api/v1/alertes/${id}/lue`, {});
  }
  /** Déclenchement manuel du moteur (Administrateur Central). */
  generer(): Observable<GenerationResult> {
    return this.http.post<GenerationResult>('/api/v1/alertes/generer', {});
  }

  // --- Seuils (RF-20, Administrateur Central) ---
  listerConfigurations(): Observable<ConfigurationAlerte[]> {
    return this.http.get<ConfigurationAlerte[]>('/api/v1/configuration-alertes');
  }
  modifierSeuil(typeSource: TypeSourceAlerte, seuilJours: number): Observable<ConfigurationAlerte> {
    return this.http.patch<ConfigurationAlerte>(`/api/v1/configuration-alertes/${typeSource}`, {
      seuilJours,
    });
  }
}
