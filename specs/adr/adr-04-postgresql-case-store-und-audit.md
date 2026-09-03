# ADR-04: PostgreSQL als Case Store mit Audit-Trail

| | |
|---|---|
| Status | Akzeptiert |
| Datum | 2026-09-03 |
| Entscheider | Software-Architektur CaseFlow |
| Bezug arc42 | Kapitel 8 (Audit, Persistenz) |

## Kontext

Jede Statusänderung eines Falls muss für Audit und Compliance jederzeit
belegbar sein. Das Schema muss mit jeder User Story kontrolliert wachsen.

## Entscheidung

- **PostgreSQL** als einziger Datenspeicher; Schema-Verwaltung ausschliesslich
  über **Liquibase** (`migrate-at-start`), Hibernate validiert nur.
- Der **Audit-Trail** ist Teil des `Case`-Aggregats: jede fachliche
  Statusänderung erzeugt einen unveränderlichen `AuditEntry` (Zeitpunkt,
  Akteur, Aktion, Vorher/Nachher), der in derselben Transaktion persistiert wird.
- Audit-Einträge werden nie geändert oder gelöscht (append-only).

## Begründung

- Eine Transaktion für Zustandsänderung und Audit — kein Auseinanderlaufen.
- Liquibase-Migrationen sind reviewbar und in Git nachvollziehbar.
- Dev Services liefern in dev/test automatisch eine passende Datenbank.

## Konsequenzen

- Jedes neue Aggregat bringt eine nummerierte Migration in `db/migration/` mit.
- Der Audit-Trail wächst mit — Archivierung ist eine spätere Entscheidung.

## Verworfene Alternativen

- **Audit via Logging/Kafka** — asynchron, nicht transaktional, nicht belegbar.
- **Hibernate `update`-Schema** — unkontrollierte Schema-Drift.
