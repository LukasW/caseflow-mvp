# Glossar — Ubiquitous Language

Begriffe des CaseFlow-Domänenmodells. Wenn ein Begriff mehrdeutig verwendet
wird oder in mehreren Bounded Contexts auftaucht, gehört er hierher.

| Begriff | Bedeutung |
|---|---|
| **Fall** (`Case`) | Zentrales Aggregate Root. Ein Vorgang, den ein Case Manager von der Erfassung bis zum Abschluss betreut. Trägt Status, Zuständigkeit, Fristen und Audit-Trail. |
| **Case Manager** | Fachperson, die Fälle bearbeitet. Rolle `CASE_MANAGER`. |
| **Teamleitung** | Weist Fälle zu und überwacht Fristen und Auslastung. Rolle `TEAM_LEAD`. |
| **Zuweisung** (`Assignment`) | Zuordnung eines Falls zu einem Case Manager. Wird protokolliert; eine Vertretung ist eine zeitlich begrenzte Zuweisung. |
| **Wiedervorlage** | Vom Case Manager gesetzter Termin, an dem ein Fall erneut zur Bearbeitung erscheint. |
| **Fälligkeit** (`Deadline`) | Fachlich oder gesetzlich vorgegebene Frist am Fall. Wird aktiv überwacht; Überschreitung wird sichtbar gemacht. |
| **Statusänderung** | Übergang im Lebenszyklus eines Falls. Jede Statusänderung erzeugt einen Audit-Eintrag. |
| **Audit-Trail** (`AuditEntry`) | Unveränderliches Protokoll aller fachlich relevanten Änderungen an einem Fall — wer, was, wann. |
| **KVG-Fall** | Fall mit Gesundheitsdaten nach Krankenversicherungsgesetz. Besonders schützenswert; Zugriff strikt rollen- und zuständigkeitsbasiert. |
| **Kernsystem** | Bestehendes Umsystem der CSS. Im MVP nicht integriert; Referenzen werden als IDs geführt. |
