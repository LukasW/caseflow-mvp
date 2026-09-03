# arc42 — Architekturdokumentation CaseFlow

## Case-Management-Tool CSS

---

**Version:** 0.1 – Entwurf (Projektskelett)
**Scope:** Zielarchitektur (Soll) des Case-Management-Tools
**Adressaten:** Software-Architektur, Backend- und Frontend-Entwicklung, QA, Betrieb
**Vorlage:** arc42 Template (12 Kapitel)

---

## 1. Einführung und Ziele

CaseFlow bündelt Erfassung, Zuweisung, Bearbeitung und Abschluss von Fällen in
einem Tool. Qualitätsziele: Nachvollziehbarkeit (Audit-Trail), Fristsicherheit,
Schutz sensibler Daten (Rollen), kurze Time-to-Market (MVP in Wochen).

## 2. Randbedingungen

- Technologie-Stack analog zu bestehenden CSS-Demo-Projekten: Quarkus, Angular, PostgreSQL, Keycloak
- Keine Kernsystem-Integration im MVP

## 3. Kontextabgrenzung

- **Nutzer:** Case Manager, Teamleitung, Administration, Audit
- **Umsysteme:** Keycloak (Identität), PostgreSQL (Persistenz); Kernsystem nur als ID-Referenz

## 4. Lösungsstrategie

Hexagonale Architektur mit DDD Tactical Design ([ADR-01](adr/adr-01-hexagonal-architecture-und-ddd.md)),
ein Quarkus-Deployable als BFF mit eingebetteter Angular-SPA ([ADR-02](adr/adr-02-ein-quarkus-deployable-bff.md)),
OIDC im BFF-Pattern ([ADR-03](adr/adr-03-oidc-keycloak-bff-pattern.md)),
PostgreSQL als Case Store mit Audit-Trail ([ADR-04](adr/adr-04-postgresql-case-store-und-audit.md)),
eine SPA mit rollenbasierten Sichten ([ADR-05](adr/adr-05-eine-angular-spa-mit-rollensichten.md)).

## 5. Bausteinsicht

Siehe `CLAUDE.md` → Architecture. Paket `ch.css.demo.caseflow` mit `domain`,
`application`, `adapter`.

## 6. Laufzeitsicht

Wird mit den ersten User Stories ergänzt (Fall erfassen, Fall zuweisen, Frist überschritten).

## 7. Verteilungssicht

Ein Quarkus-Prozess (BFF mit eingebetteter SPA), PostgreSQL, Keycloak extern.
Das Deployment ist nicht Teil dieses Repos.

## 8. Querschnittliche Konzepte

- **Sicherheit:** Session-Cookie statt JWT im Browser, Rollen serverseitig durchgesetzt
- **Audit:** Jede Statusänderung erzeugt einen Audit-Eintrag am Aggregat
- **Persistenz:** Liquibase verwaltet das Schema, Hibernate validiert
- **Sprache:** Schweizer Hochdeutsch, Du-Form

## 9. Architekturentscheidungen

Siehe [ADR-Index](adr/README.md).

## 10. Qualitätsanforderungen

- ArchUnit sichert die Schichtdisziplin
- Domain Services mit >90 % Line-Coverage (JaCoCo-Check)
- BDD-Szenarien je User Story

## 11. Risiken und technische Schulden

- Kernsystem-Integration ist bewusst ausgeklammert — Doppelerfassung im MVP möglich

## 12. Glossar

Siehe [Glossar](glossary.md).
