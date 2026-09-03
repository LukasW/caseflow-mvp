# ADR-02: Ein Quarkus-Deployable (BFF) mit Angular-SPA via Quinoa

| | |
|---|---|
| Status | Akzeptiert |
| Datum | 2026-09-03 |
| Entscheider | Software-Architektur CaseFlow |
| Bezug arc42 | Kapitel 4, 7 |

## Kontext

Das MVP soll in wenigen Wochen einsatzbereit sein. Getrennte Deployables für
Backend und Frontend bedeuten zwei Pipelines, zwei Images, CORS-Konfiguration
und ein zweites Auth-Setup.

## Entscheidung

Ein einziges Maven-Modul liefert Backend und Frontend: Quarkus dient als
Backend-For-Frontend (BFF) und liefert die Angular-SPA über **Quinoa** aus
derselben Origin aus. Im Dev-Modus proxied Quarkus auf den Angular-Dev-Server.

## Begründung

- Ein Build, ein Image, ein Deployment.
- Kein CORS, Session-Cookie gilt für API und SPA gleichermassen.
- Quinoa installiert Node/npm selbst — reproduzierbare Frontend-Builds in CI.

## Konsequenzen

- Frontend-Änderungen erfordern einen Backend-Build (Quinoa-Build-Zeit).
- Skalierung von Frontend und Backend ist gekoppelt — für das MVP unproblematisch.

## Verworfene Alternativen

- **Getrennte Deployables** — mehr Infrastruktur, langsamerer Start.
- **Server-Side Rendering (Qute)** — weniger reaktive UI, schlechtere Trennung.
