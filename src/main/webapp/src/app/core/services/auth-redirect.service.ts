import { Injectable } from '@angular/core';

/**
 * Kapselt die Browser-Navigation, die den serverseitigen OIDC-Login auslöst
 * (ADR-08). Als eigene Klasse ausgelagert, damit der `authInterceptor` ohne
 * echte Navigation getestet werden kann.
 */
@Injectable({ providedIn: 'root' })
export class AuthRedirect {
  /**
   * Navigiert auf eine geschützte `/api`-Ressource. Quarkus startet dadurch
   * den Authorization-Code-Flow und leitet nach dem Login zurück auf die SPA.
   */
  toLogin(): void {
    window.location.assign('/api/v1/me');
  }
}
