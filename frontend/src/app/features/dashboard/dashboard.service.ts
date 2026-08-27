import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { DashboardData } from './dashboard.models';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);

  consulter(): Observable<DashboardData> {
    return this.http.get<DashboardData>('/api/v1/tableau-de-bord');
  }
}
