# Fachliche Spezifikation

## Case-Management-Tool CaseFlow

---

**Version:** 0.2 – Entwurf (konsolidiert)
**Adressaten:** Software-Architektur, Backend-/Frontend-Entwicklung, QA, Fachbereich Case Management, Compliance
**Grundlage:** Projektskelett v0.1 und «Spezifikation MVP Case Management Tool» v0.1 (03.09.2026)

*Diese Spezifikation beschreibt Ziel, Scope, Rollenmodell und Konturen des
Case-Management-Tools. Sie wird mit den User Stories unter `user-stories/`
Schritt für Schritt konkretisiert. Abschnitt 12 hält fest, wo die beiden
Ausgangsdokumente voneinander abwichen und wie entschieden wurde.*

---

## 1. Ausgangslage

Case Manager jonglieren heute zwischen E-Mail, Excel und dem Kernsystem. Fälle
gehen unter, Fristen werden knapp, niemand hat den vollen Überblick.

Konkret fehlt:

- Übersicht über offene Fälle und deren Status
- Nachvollziehbarkeit (wer hat was wann gemacht)
- Entlastung bei Zuweisung, Eskalation und Wiedervorlage
- Sicherheit bei Fristen, insbesondere gesetzlichen Antwortfristen nach KVG/VVG

## 2. Zielbild

**Ein Tool. Ein Fall. Volle Kontrolle.**

| Versprechen | Fachliche Bedeutung |
| --- | --- |
| Alles an einem Ort | Erfassung, Zuweisung, Bearbeitung, Abschluss ohne Systemwechsel |
| Keine verpassten Fristen | Wiedervorlagen und Fälligkeiten werden aktiv nachverfolgt |
| Lückenlose Nachvollziehbarkeit | Jede Statusänderung ist im Audit-Trail belegbar |
| Gebaut für sensible Daten | Rollenbasierter Zugriff von Anfang an, auch für KVG-Gesundheitsdaten |
| Schnell startklar | MVP in wenigen Wochen, ohne Kernsystem-Integration |

**Leitfrage des MVP:** Kann ein Case Manager einen Fall von Eingang bis
Abschluss vollständig im Tool bearbeiten, ohne auf ein Parallelsystem
auszuweichen? Wenn ja, ist der MVP erfolgreich.

**Business Case:** Weniger manueller Koordinationsaufwand, weniger Fristrisiko,
mehr Zeit für die eigentliche Fallbearbeitung statt für die Suche nach dem Fall.

## 3. MVP-Scope

### 3.1 In Scope

| Bereich | Inhalt |
| --- | --- |
| Fall-Lebenszyklus | Erfassen mit Grunddaten (Referenz, Typ, Priorität, Quelle), zuweisen, bearbeiten, abschliessen |
| Fallbearbeitung | Statuswechsel, Notizen, Bearbeitungshistorie |
| Fristen | Wiedervorlagen und Fälligkeiten setzen; System zeigt fällige und überfällige Fälle aktiv an |
| Fallabschluss | Abschlussgrund pflichtig; abgeschlossener Fall ist read-only |
| Fallübersicht | «Meine Fälle» und Team-Sicht; Filter nach Status, Zuständigkeit, Falltyp, Priorität, Fälligkeit |
| Audit-Trail | Jede Statusänderung und Zuweisung wird unveränderbar protokolliert |
| Rollenbasierter Zugriff | Vier Rollen, serverseitig durchgesetzt (Kapitel 4) |

### 3.2 Ausser Scope (MVP)

- Integration ins Kernsystem – Referenzen (Personen-/Policennummer) werden als IDs geführt, nicht validiert
- Dokumentenablage und Upload
- Reporting über Audit-Trail und Team-Sicht hinaus (keine Dashboards)
- Automatisierte Zuweisung (Round-Robin, Skill-based Routing)
- Automatisierte Kommunikation (E-Mail-Vorlagen, Benachrichtigungen, Chatbots)
- Kundenportal / Self-Service
- Volltextsuche über Notizen
- Vertretungsregelungen als eigene Logik (im MVP: Umverteilung durch `TEAM_LEAD`)
- Mehrsprachigkeit (MVP: Deutsch)
- Native Mobile App (Web-Responsive genügt)

**Das bedeutet:** Diese Punkte sind bewusst auf spätere Phasen verschoben, nicht
vergessen. Eine Änderung dieser Liste braucht einen Review mit Fachbereich und
Compliance.

## 4. Rollenmodell

| Rolle | Darf | Bedürfnis dahinter |
| --- | --- | --- |
| `CASE_MANAGER` | Fälle erfassen, eigene Fälle bearbeiten, Wiedervorlagen setzen, abschliessen | Übersicht über eigene Fälle, keine verpassten Fristen |
| `TEAM_LEAD` | Fälle zuweisen und umverteilen, Fristen und Auslastung des Teams sehen | Überblick über Team und offene Fälle |
| `ADMIN` | Stammdaten, Konfiguration | Wartbares System |
| `AUDITOR` | Audit-Trail lesen, keine Fallbearbeitung | Nachvollziehbarkeit jeder Fallhistorie |

Die Durchsetzung erfolgt serverseitig an den REST-Ressourcen. Das Frontend
blendet Sichten und Aktionen nur aus.

`CASE_MANAGER` sieht nur eigene Fälle, `TEAM_LEAD` alle Fälle des Teams. Für
Fälle mit Gesundheitsdaten (KVG) muss das Modell diese Unterscheidung von
Anfang an tragen, auch wenn die Regeln im MVP noch einfach sind.

## 5. Prozesse

1. **Fall erfassen** – manuell durch `CASE_MANAGER`; Eingänge aus E-Mail werden im MVP nacherfasst
2. **Fall zuweisen** – durch `TEAM_LEAD` an eine Person; Umverteilung jederzeit möglich
3. **Fall bearbeiten** – Status ändern, Notiz hinzufügen
4. **Frist setzen** – Wiedervorlage oder Fälligkeit mit Datum und Grund; fällige Fälle erscheinen in der Übersicht markiert
5. **Fall abschliessen** – Abschlussgrund pflichtig; danach read-only, nur `ADMIN` kann wiedereröffnen (Audit-Eintrag)

Statusmodell: `NEU` → `IN_BEARBEITUNG` → `WARTEND` ↔ `IN_BEARBEITUNG` → `ABGESCHLOSSEN`; Wiedereröffnung nur durch `ADMIN` zurück nach `IN_BEARBEITUNG`. Die Übergänge sind in `user-stories/` festgeschrieben (US-3, US-6, US-7).

## 6. Domänenmodell (Kontur)

- **`Case`** – Aggregate Root: Fallnummer, Falltyp, Status, Priorität, Kundenreferenz (ID), Zuständigkeit, Fristen, Abschlussgrund, Audit-Trail
- **`Deadline`** – Value Object: Art (`WIEDERVORLAGE` / `FAELLIGKEIT`), Termin, Grund, Status
- **`AuditEntry`** – Value Object: Zeitpunkt, Akteur, Aktion, Vorher/Nachher

`User` und `Team` sind keine eigenen Aggregate: Identität, Rolle und
Teamzugehörigkeit kommen aus dem IAM und werden im `Case` nur referenziert.

Detaillierung erfolgt in den User Stories und ADRs.

## 7. Nicht-funktionale Anforderungen

| Kategorie | Anforderung |
| --- | --- |
| Datenschutz | Personendaten gemäss DSG; besondere Schutzwürdigkeit bei Gesundheitsdaten (KVG-Fälle) |
| Compliance | Audit-Trail unveränderbar (append-only); Aufbewahrungsfristen gemäss VVG/KVG |
| Zugriff | Rollenbasiert, serverseitig; kein Fall ohne Zuständigkeit sichtbar für Dritte |
| Authentifizierung | OIDC gegen bestehendes IAM, kein separates Login |
| Verfügbarkeit | 99.5 % während Geschäftszeiten; kein 24/7-SLA im MVP |
| Performance | Fallübersicht mit bis zu 10'000 Fällen unter 2 s |
| Hosting | Innerhalb bestehender Infrastruktur; keine Daten an Drittanbieter-Cloud ohne Prüfung |

## 8. Schnittstellen

| Schnittstelle | Zweck | MVP |
| --- | --- | --- |
| IAM | Authentifizierung, Rollen, Teams | Ja, zwingend |
| Kernsystem | Fallreferenz validieren | Nein – Referenz als ID |
| E-Mail (ausgehend) | Benachrichtigung bei Zuweisung/Fälligkeit | Nein – Phase 2 |
| Dokumentenablage | Anhänge | Nein – Phase 2 |

## 9. API-Konturen

| Endpoint | Zweck | Status |
| --- | --- | --- |
| `GET /api/v1/me` | Identität und Rollen des Nutzers | vorhanden |
| `GET /api/v1/version` | Build-Informationen | vorhanden |
| `/api/v1/cases` | Fall-Lebenszyklus (erfassen, bearbeiten, abschliessen, wiedereröffnen, Übersicht) | geplant (US-1, US-3, US-6, US-7, US-8) |
| `/api/v1/cases/{id}/assignment` | Zuweisung und Umverteilung | geplant (US-2) |
| `/api/v1/cases/{id}/deadlines` | Fristen | geplant (US-4, US-5) |
| `/api/v1/cases/{id}/audit` | Audit-Trail (lesend, `AUDITOR`/`TEAM_LEAD`) | geplant (US-9) |

DTOs werden als `snake_case`-JSON ausgeliefert. OpenAPI unter `/q/openapi`,
Swagger-UI unter `/q/swagger-ui`.

## 10. Erfolgskriterien

1. Ein `CASE_MANAGER` bearbeitet einen Fall vollständig im Tool, von Erfassung bis Abschluss
2. Keine Statusänderung ohne Audit-Eintrag – nachweisbar per Stichprobe durch `AUDITOR`
3. Fällige Fälle werden zuverlässig angezeigt; keine verpasste Frist im Pilotbetrieb
4. Pilotteam (3–5 Case Manager) arbeitet mindestens 4 Wochen produktiv ohne Rückfall auf Excel/E-Mail

Auswertung nach 4 Wochen Pilotbetrieb, danach Entscheid über Rollout oder Erweiterung.

## 11. Risiken

| Risiko | Auswirkung | Massnahme |
| --- | --- | --- |
| Doppelerfassung wegen fehlender Kernsystem-Anbindung | Akzeptanzproblem | Als MVP-Einschränkung kommunizieren; Anbindung Phase 2 |
| Gesundheitsdaten ungenügend geschützt | Compliance-Verstoss | Rollenmodell und Datenschutz-Review vor Go-Live zwingend |
| Fehlende Dokumentenablage führt zu Parallelablage in E-Mail | Leitfrage nicht erfüllt | Im Pilot messen, ob Dokumente den Rückfall auslösen; falls ja, Phase-2-Priorität 1 |
| Scope-Creep | Verzögerung | Abschnitt 3.2 verbindlich, Änderung nur mit Review |

## 12. Konsolidierungsentscheide

Abweichungen zwischen Projektskelett v0.1 und MVP-Spezifikation v0.1, mit Entscheid:

| Thema | MVP-Spezifikation | Projektskelett | Entscheid v0.2 |
| --- | --- | --- | --- |
| Dokumente | In Scope (Upload, Objekt-Storage) | Ausser Scope | **Ausser Scope.** Objekt-Storage plus Gesundheitsdaten verdoppelt den Datenschutz-Aufwand; Leitfrage ist auch ohne Dokumente prüfbar. Als Risiko geführt. |
| Zuweisungsregel | Manuell oder Round-Robin | Manuell durch `TEAM_LEAD` | **Manuell.** Round-Robin Phase 2. |
| Erinnerung | Optional E-Mail (SMTP) | «aktiv nachverfolgt», unspezifiziert | **In-App.** Fällige Fälle in Übersicht markiert; E-Mail Phase 2. |
| Rollen | Personas (Case Manager, Teamleiter, Compliance, IT) | Vier Rollen inkl. `AUDITOR`, `ADMIN` | **Vier Rollen des Skeletts**, Personas als Bedürfnisse zugeordnet. |
| Vertretungen | Nicht erwähnt | `ADMIN` pflegt Vertretungen | **Keine eigene Logik im MVP.** Vertretung = Umverteilung durch `TEAM_LEAD`. |
| Fristen | Ein `dueDate` | `Deadline` als Value Object mit Art | **`Deadline`-VO.** Trennung Wiedervorlage/Fälligkeit ist fachlich nötig (gesetzliche vs. interne Fristen). |
| Audit | `HistoryEntry` (Aktion, Notiz) | `AuditEntry` (Vorher/Nachher) | **`AuditEntry` mit Vorher/Nachher.** Notizen sind Fallinhalt, nicht Audit. |
| User/Team | Eigene Entitäten | Nicht modelliert | **Referenzen aus IAM**, keine eigenen Aggregate. |
| Suche | Filter + Volltext | Übersicht | **Filter ja, Volltext Phase 2.** |
| Wiedereröffnen | Nicht erwähnt | Nicht erwähnt | **Nur `ADMIN`, mit Audit-Eintrag.** Neu, weil «read-only nach Abschluss» sonst keinen Ausweg hat. |
| Erfolgskriterien, NFRs, Risiken | Vorhanden | Fehlen | **Übernommen** (Kapitel 7, 10, 11). |

## 13. Empfehlungen

1. Scope (Kapitel 3) und Konsolidierungsentscheide (Kapitel 12) mit Fachbereich und Compliance gegenzeichnen, bevor die erste User Story umgesetzt wird.
2. Pilotgruppe (3–5 Case Manager) vor Sprint 1 benennen und Statusmodell sowie Pflichtfelder mit ihr validieren.
3. Datenschutz-Review des Rollenmodells vor Go-Live, mit Fokus auf KVG-Fälle.
4. Bounded Context `Case` so schneiden, dass Kernsystem-Integration und Dokumentenablage in Phase 2 ohne Rewrite anschliessen (Referenzen als IDs, Ports statt direkter Aufrufe).
5. Im Pilotbetrieb explizit messen, ob fehlende Dokumentenablage den Rückfall auf E-Mail auslöst – das entscheidet die Phase-2-Reihenfolge.
