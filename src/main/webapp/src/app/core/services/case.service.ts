import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CaseResponse, CreateCaseRequest } from '../models/case.model';

/**
 * REST-Zugriff auf die Fall-Ressource. Kapselt `POST /api/v1/cases` für die
 * Fallerfassung; die verbindliche Validierung und Autorisierung erfolgt
 * serverseitig.
 */
@Injectable({ providedIn: 'root' })
export class CaseService {
  private readonly http = inject(HttpClient);
  private readonly casesUrl = `${environment.apiUrl}/cases`;

  /** Legt einen Fall an und liefert den angelegten Fall mit Fallnummer. */
  createCase(request: CreateCaseRequest): Observable<CaseResponse> {
    return this.http.post<CaseResponse>(this.casesUrl, request);
  }
}
