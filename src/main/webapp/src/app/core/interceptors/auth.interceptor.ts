import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthRedirect } from '../services/auth-redirect.service';

/**
 * Quarkus-Status für einen AJAX-Request ohne gültige BFF-Session: Bei
 * `java-script-auto-redirect=false` antwortet Quarkus auf einen als AJAX
 * markierten Request mit `499` statt eines `302`-Redirects auf Keycloak.
 */
const AUTH_REQUIRED_STATUS = 499;

/**
 * Markiert HttpClient-Requests als AJAX und leitet bei fehlender oder
 * abgelaufener BFF-Session auf den serverseitigen OIDC-Login um (ADR-08).
 *
 * Der Header `X-Requested-With` ist dabei entscheidend: Ohne ihn beantwortet
 * Quarkus einen nicht authentifizierten `/api`-Request mit einem `302` auf
 * Keycloak — diesem Cross-Origin-Redirect kann ein `fetch` wegen der CSP
 * `connect-src 'self'` nicht folgen, der Request scheitert wirkungslos. Mit dem
 * Header liefert Quarkus `499`; der Interceptor löst daraufhin über
 * {@link AuthRedirect} eine Vollseiten-Navigation aus, die den Login startet.
 * Alle übrigen Fehler werden unverändert an den Aufrufer weitergereicht.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const redirect = inject(AuthRedirect);
  const ajaxReq = req.clone({ setHeaders: { 'X-Requested-With': 'XMLHttpRequest' } });
  return next(ajaxReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === AUTH_REQUIRED_STATUS || error.status === 401) {
        redirect.toLogin();
      }
      return throwError(() => error);
    }),
  );
};
