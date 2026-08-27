import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export type FormatExport = 'CSV' | 'PDF';

@Injectable({ providedIn: 'root' })
export class ExportService {
  private readonly http = inject(HttpClient);

  /**
   * Télécharge l'export d'un module. Les filtres nuls/vides sont ignorés (l'export reflète
   * exactement les filtres actifs à l'écran). Le JWT est ajouté par l'intercepteur.
   */
  telecharger(
    resource: string,
    format: FormatExport,
    params: Record<string, unknown>,
  ): Observable<HttpResponse<Blob>> {
    let httpParams = new HttpParams().set('format', format);
    for (const [cle, valeur] of Object.entries(params)) {
      if (valeur !== null && valeur !== undefined && valeur !== '') {
        httpParams = httpParams.set(cle, String(valeur));
      }
    }
    return this.http.get(`/api/v1/exports/${resource}`, {
      params: httpParams,
      responseType: 'blob',
      observe: 'response',
    });
  }

  /** Déclenche l'enregistrement local du fichier reçu, sous le nom fourni par le backend. */
  enregistrer(reponse: HttpResponse<Blob>, nomParDefaut: string): void {
    const blob = reponse.body;
    if (!blob) return;
    const nom = this.nomFichier(reponse) ?? nomParDefaut;
    const url = URL.createObjectURL(blob);
    const lien = document.createElement('a');
    lien.href = url;
    lien.download = nom;
    lien.click();
    URL.revokeObjectURL(url);
  }

  private nomFichier(reponse: HttpResponse<Blob>): string | null {
    const disposition = reponse.headers.get('Content-Disposition');
    if (!disposition) return null;
    const correspondance = /filename="?([^"]+)"?/.exec(disposition);
    return correspondance ? correspondance[1] : null;
  }
}
