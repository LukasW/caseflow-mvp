# ADR-03: OIDC via Keycloak im BFF-Pattern

| | |
|---|---|
| Status | Akzeptiert |
| Datum | 2026-09-03 |
| Entscheider | Software-Architektur CaseFlow |
| Bezug arc42 | Kapitel 8 (Sicherheit) |

## Kontext

CaseFlow verarbeitet sensible Daten, darunter Gesundheitsdaten aus KVG-Fällen.
Tokens im Browser (localStorage, JS-lesbar) sind ein vermeidbares Risiko.

## Entscheidung

Authentifizierung via **OIDC Authorization Code Flow mit PKCE** gegen Keycloak,
abgewickelt **serverseitig durch Quarkus** (`application-type=web-app`). Der
Browser hält nur ein verschlüsseltes Session-Cookie (HttpOnly, SameSite=Lax).
Rollen sind Client-Rollen des `caseflow`-Clients und werden vom
`RoleMappingAugmentor` auf die internen Namen (`CASE_MANAGER`, …) abgebildet.

In `%dev`/`%test` ist OIDC deaktiviert; eine `DevAuthenticationMechanism`
bzw. `TestAuthenticationMechanism` stellt eine Ersatzidentität bereit.

## Begründung

- Kein Token erreicht den Browser — XSS kann keine Session stehlen.
- Rollen-Mapping am Boundary hält die Anwendung unabhängig vom Realm-Naming.
- Dev-Ersatzidentität erlaubt Entwicklung ohne laufendes Keycloak.

## Konsequenzen

- Der HttpClient muss `X-Requested-With` setzen, damit Quarkus bei abgelaufener
  Session `499` statt `302` liefert — der `authInterceptor` löst den Login aus.
- Header-Limit erhöht (`max-header-size=32K`) wegen grosser Session-Cookies.

## Verworfene Alternativen

- **SPA mit Access-Token (angular-oauth2-oidc)** — Token im Browser.
- **Basic Auth / eigene Nutzerverwaltung** — kein SSO, kein zentrales Rollenmodell.
