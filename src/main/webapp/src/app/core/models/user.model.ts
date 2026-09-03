/** CaseFlow-Rollen — spiegeln `Roles` im Backend. */
export type Role = 'CASE_MANAGER' | 'TEAM_LEAD' | 'ADMIN' | 'AUDITOR';

/** Identität und Rollen des angemeldeten Nutzers (`GET /api/v1/me`). */
export interface CurrentUser {
  username: string;
  roles: Role[];
  authenticated: boolean;
}

/** Rollen mit Zugriff auf den Arbeitsplatz. */
export const STAFF_ROLES: readonly Role[] = ['CASE_MANAGER', 'TEAM_LEAD', 'ADMIN', 'AUDITOR'];
