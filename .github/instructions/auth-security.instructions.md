---
applyTo: "src/main/java/ch/css/demo/caseflow/adapter/in/rest/**,src/main/resources/application*.properties,keycloak/**"
---

# Auth & Security — OIDC, Keycloak, BFF

CaseFlow nutzt OIDC via Keycloak im **BFF-Pattern** (Quarkus `application-type=web-app`):
der Browser hält nur ein Session-Cookie, nie ein JWT. In `%dev`/`%test` ist OIDC
deaktiviert, in `%keycloak` und `%prod` aktiv (siehe
`specs/adr/adr-03-oidc-keycloak-bff-pattern.md`). Nach Änderungen den Custom Agent
`auth-security-reviewer` laufen lassen.

## Authentifizierung & Autorisierung (Quarkus)
- REST-Resources in `adapter/in/rest/` tragen `@Authenticated` auf Klassen-Ebene
- Public Endpoints haben `@PermitAll` **und** einen Kommentar, warum
- `@RolesAllowed` oder expliziter Owner-Check auf jedem Endpoint, der fremde Fälle
  lesen/schreiben kann — Rollen: `CASE_MANAGER`, `TEAM_LEAD`, `ADMIN`, `AUDITOR`
  (`adapter/in/rest/security/Roles.java`)
- Ein Case Manager sieht nur Fälle seines Zuständigkeitsbereichs — Owner-Check serverseitig
- Gesundheitsdaten (KVG) sind besonders schützenswert — keine Business-Autorisierung im Frontend

## BFF & Session-Cookies
- Nie ein JWT/Access-Token an den Browser geben
- Session-Cookies `HttpOnly`, `Secure` (in `%prod`), `SameSite=Strict` oder `Lax` — nie `None` ohne Begründung
- Downstream-Calls via `OidcClient`/Token-Propagation — keine manuellen Header
- Keine Tokens/Secrets in Logs, Exception-Messages oder Stacktraces

## Konfiguration (`application.properties`)
- `quarkus.http.cors.origins` eng gesetzt — `*` braucht eine Begründung
- Security-Header in `%prod`: `Strict-Transport-Security`, `Content-Security-Policy`,
  `X-Content-Type-Options`, `X-Frame-Options`/`frame-ancestors`
- Keine echten Secrets eingecheckt — Defaults wie `OIDC_CLIENT_SECRET:dev-secret` nur für
  Dev; in `%prod` Pflicht-Env-Variable
- `%prod` setzt eine explizite `auth-server-url` — kein `localhost`

## Keycloak-Realm (`keycloak/caseflow-realm-dev.json`)
- Neue Rolle: Client-Rolle + Testuser hier, Konstante in `Roles.java`, Mapping im
  `RoleMappingAugmentor`, `Role`-Union in `core/models/user.model.ts`, Policy `staff` prüfen
- Nur Dev-Credentials — nie produktive Werte
