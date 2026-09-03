# ADR-05: Eine Angular-SPA mit rollenbasierten Sichten

| | |
|---|---|
| Status | Akzeptiert |
| Datum | 2026-09-03 |
| Entscheider | Software-Architektur CaseFlow |
| Bezug arc42 | Kapitel 5, 8 |

## Kontext

Case Manager, Teamleitung, Administration und Audit brauchen unterschiedliche
Sichten und Aktionen auf denselben Fällen.

## Entscheidung

**Eine** Angular-SPA mit Rollensichten: Der `AuthService` hält Identität und
Rollen aus `GET /api/v1/me` als Signal; Route-Guards und Template-Bedingungen
blenden Sichten und Aktionen aus. Die **verbindliche** Durchsetzung erfolgt
serverseitig an den REST-Ressourcen (`@RolesAllowed`, Owner-Checks).

## Begründung

- Ein Frontend, ein Build, ein Auth-Kontext.
- Guards sind eine Komfort-Schicht — Sicherheit hängt nie am Frontend.

## Konsequenzen

- Jede neue Rolle wird in `Roles.java`, `RoleMappingAugmentor`, `user.model.ts`
  und dem Keycloak-Realm nachgeführt (siehe CLAUDE.md → "Add a role").
- Komponenten dürfen keine Business-Autorisierung enthalten.

## Verworfene Alternativen

- **Eine SPA pro Rolle** — Code-Duplikation, mehrere Deployables.
