# Architecture Decision Records (ADR)

Dieser Ordner enthält die Architekturentscheide für CaseFlow. Jeder Entscheid ist ein
eigener, versionierter Record. Die Architekturdokumentation [`../caseflow-arc42.md`](../caseflow-arc42.md)
verweist in Kapitel 9 auf diesen Ordner.

## Konventionen

- **Dateiname:** `adr-NN-kurztitel.md`
- **ID:** stabil, fortlaufend (`ADR-01`, `ADR-02`, …) — wird nie wiederverwendet
- **Status:** `Vorgeschlagen` → `Akzeptiert` → ggf. `Abgelöst durch ADR-XX`
- **Neue Architekturentscheide** werden als neuer Record hier abgelegt, nicht inline
  in arc42 oder den Spezifikationen.
- Format: Kontext → Entscheidung → Begründung → Konsequenzen → Verworfene Alternativen

## Index

| ID | Titel | Status |
|---|---|---|
| [ADR-01](adr-01-hexagonal-architecture-und-ddd.md) | Hexagonal Architecture und DDD Tactical Design | Akzeptiert |
| [ADR-02](adr-02-ein-quarkus-deployable-bff.md) | Ein Quarkus-Deployable (BFF) mit Angular-SPA via Quinoa | Akzeptiert |
| [ADR-03](adr-03-oidc-keycloak-bff-pattern.md) | OIDC via Keycloak im BFF-Pattern | Akzeptiert |
| [ADR-04](adr-04-postgresql-case-store-und-audit.md) | PostgreSQL als Case Store mit Audit-Trail | Akzeptiert |
| [ADR-05](adr-05-eine-angular-spa-mit-rollensichten.md) | Eine Angular-SPA mit rollenbasierten Sichten | Akzeptiert |
