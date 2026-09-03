# Fachliche Spezifikation

## Case-Management-Tool CaseFlow

---

**Version:** 0.1 – Entwurf (Projektskelett)
**Adressaten:** Software-Architektur, Backend-/Frontend-Entwicklung, QA, Fachbereich Case Management, Compliance

*Diese Spezifikation beschreibt Ziel, Scope, Rollenmodell und Konturen des
Case-Management-Tools. Sie wird mit den User Stories unter `user-stories/`
Schritt für Schritt konkretisiert.*

---

## 1. Ausgangslage

Case Manager jonglieren heute zwischen E-Mail, Excel und dem Kernsystem. Fälle
gehen unter, Fristen werden knapp, niemand hat den vollen Überblick.

## 2. Zielbild

**Ein Tool. Ein Fall. Volle Kontrolle.**

| Versprechen | Fachliche Bedeutung |
|---|---|
| Alles an einem Ort | Erfassung, Zuweisung, Bearbeitung, Abschluss ohne Systemwechsel |
| Keine verpassten Fristen | Wiedervorlagen und Fälligkeiten werden aktiv nachverfolgt |
| Lückenlose Nachvollziehbarkeit | Jede Statusänderung ist im Audit-Trail belegbar |
| Gebaut für sensible Daten | Rollenbasierter Zugriff von Anfang an, auch für KVG-Gesundheitsdaten |
| Schnell startklar | MVP in wenigen Wochen, ohne Kernsystem-Integration |

**Business Case:** Weniger manueller Koordinationsaufwand, weniger Fristrisiko,
mehr Zeit für die eigentliche Fallbearbeitung statt für die Suche nach dem Fall.

## 3. MVP-Scope

In Scope:

- Fall erfassen, zuweisen, bearbeiten, abschliessen (Lebenszyklus)
- Wiedervorlagen und Fälligkeiten mit aktiver Nachverfolgung
- Audit-Trail je Fall
- Rollenbasierter Zugriff (siehe Kapitel 4)
- Fallübersicht («Meine Fälle», Team-Sicht)

Ausser Scope (MVP):

- Integration ins Kernsystem (Referenzen werden als IDs geführt)
- Dokumentenablage
- Reporting über den Audit-Trail hinaus

## 4. Rollenmodell

| Rolle | Darf |
|---|---|
| `CASE_MANAGER` | Fälle erfassen, eigene Fälle bearbeiten, Wiedervorlagen setzen, abschliessen |
| `TEAM_LEAD` | Fälle zuweisen und umverteilen, Fristen und Auslastung des Teams sehen |
| `ADMIN` | Stammdaten, Vertretungen, Konfiguration |
| `AUDITOR` | Audit-Trail und Reports lesen, keine Fallbearbeitung |

Die Durchsetzung erfolgt serverseitig an den REST-Ressourcen. Das Frontend
blendet Sichten und Aktionen nur aus.

## 5. Domänenmodell (Kontur)

- **`Case`** — Aggregate Root: Fallnummer, Status, Zuständigkeit, Fristen, Audit-Trail
- **`Deadline`** — Value Object: Art (Wiedervorlage/Fälligkeit), Termin, Status
- **`AuditEntry`** — Value Object: Zeitpunkt, Akteur, Aktion, Vorher/Nachher

Detaillierung erfolgt in den User Stories und ADRs.

## 6. API-Konturen

| Endpoint | Zweck | Status |
|---|---|---|
| `GET /api/v1/me` | Identität und Rollen des Nutzers | vorhanden |
| `GET /api/v1/version` | Build-Informationen | vorhanden |
| `/api/v1/cases` | Fall-Lebenszyklus | geplant |

DTOs werden als `snake_case`-JSON ausgeliefert. OpenAPI unter `/q/openapi`,
Swagger-UI unter `/q/swagger-ui`.
