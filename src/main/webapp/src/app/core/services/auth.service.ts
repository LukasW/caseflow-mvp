import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CurrentUser, Role, STAFF_ROLES } from '../models/user.model';

/**
 * Hält Identität und Rollen des angemeldeten Nutzers als Signal-Kontext.
 * Die Rolle stammt aus dem OIDC-Token und wird vom Backend (`GET /api/v1/me`)
 * geliefert. Der Kontext ist eine Komfort-Schicht für die UI — die
 * verbindliche Durchsetzung erfolgt serverseitig.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly meUrl = `${environment.apiUrl}/me`;

  private readonly currentUser = signal<CurrentUser | undefined>(undefined);

  /** Der angemeldete Nutzer, oder `undefined` solange nicht geladen. */
  readonly user = this.currentUser.asReadonly();

  /** Rollen des angemeldeten Nutzers. */
  readonly roles = computed<readonly Role[]>(() => this.currentUser()?.roles ?? []);

  /** Benutzername des angemeldeten Nutzers, `undefined` solange nicht geladen. */
  readonly username = computed<string | undefined>(() => this.currentUser()?.username);

  /** `true`, sobald eine echte OIDC-Identität vorliegt. */
  readonly isAuthenticated = computed(() => this.currentUser()?.authenticated ?? false);

  /** `true`, wenn der Nutzer Zugriff auf den Arbeitsplatz hat. */
  readonly isStaff = computed(() => this.roles().some((role) => STAFF_ROLES.includes(role)));

  /** Lädt Identität und Rollen vom Backend und füllt den Signal-Kontext. */
  loadCurrentUser(): Observable<CurrentUser> {
    return this.http.get<CurrentUser>(this.meUrl).pipe(tap((user) => this.currentUser.set(user)));
  }

  /** Prüft, ob der Nutzer mindestens eine der angegebenen Rollen besitzt. */
  hasRole(...allowed: Role[]): boolean {
    const current = this.roles();
    return allowed.some((role) => current.includes(role));
  }
}
