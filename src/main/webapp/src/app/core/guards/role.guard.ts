import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Role } from '../models/user.model';
import { AuthService } from '../services/auth.service';

/**
 * Erzeugt einen Route-Guard, der die Route nur für die angegebenen Rollen
 * freigibt. Fehlt die Berechtigung, wird auf die Startseite umgeleitet.
 *
 * Der Guard ist eine Komfort-Schicht — die verbindliche Durchsetzung erfolgt
 * serverseitig an den REST-Ressourcen.
 */
export function roleGuard(...allowed: Role[]): CanActivateFn {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    return auth.hasRole(...allowed) ? true : router.createUrlTree(['/']);
  };
}

/** Lässt jede authentisierte Identität durch. */
export const authenticatedGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.isAuthenticated() ? true : router.createUrlTree(['/']);
};

/**
 * Guard für die Startseite: Nutzer ohne CaseFlow-Rolle landen auf `/no-access`,
 * damit ein im Realm angemeldeter, aber CaseFlow-fremder Account (z. B. nur für
 * eine andere App im gleichen Keycloak-Realm berechtigt) den Arbeitsplatz nicht sieht.
 */
export const homeAccessGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.isStaff() ? true : router.createUrlTree(['/no-access']);
};
