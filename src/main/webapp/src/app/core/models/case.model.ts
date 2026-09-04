/** Falltyp — spiegelt `CaseType` im Backend. */
export type CaseType = 'LEISTUNG' | 'BESCHWERDE' | 'ANFRAGE' | 'KUENDIGUNG';

/** Priorität — spiegelt `Priority` im Backend. */
export type Priority = 'NIEDRIG' | 'MITTEL' | 'HOCH';

/** Quelle — spiegelt `Source` im Backend. */
export type Source = 'TELEFON' | 'E_MAIL' | 'BRIEF' | 'PORTAL';

/** Status eines Falls — ein neu erfasster Fall startet auf `NEU`. */
export type CaseStatus = 'NEU';

/**
 * Auswahloption für die Erfassungs-Dropdowns: der ans Backend gesendete Wert
 * und das für den Nutzer angezeigte Label mit korrekten Umlauten.
 */
export interface CaseOption<T> {
  value: T;
  label: string;
}

export const CASE_TYPE_OPTIONS: readonly CaseOption<CaseType>[] = [
  { value: 'LEISTUNG', label: 'Leistung' },
  { value: 'BESCHWERDE', label: 'Beschwerde' },
  { value: 'ANFRAGE', label: 'Anfrage' },
  { value: 'KUENDIGUNG', label: 'Kündigung' },
];

export const PRIORITY_OPTIONS: readonly CaseOption<Priority>[] = [
  { value: 'NIEDRIG', label: 'Niedrig' },
  { value: 'MITTEL', label: 'Mittel' },
  { value: 'HOCH', label: 'Hoch' },
];

export const SOURCE_OPTIONS: readonly CaseOption<Source>[] = [
  { value: 'TELEFON', label: 'Telefon' },
  { value: 'E_MAIL', label: 'E-Mail' },
  { value: 'BRIEF', label: 'Brief' },
  { value: 'PORTAL', label: 'Portal' },
];

/** Anzeige-Label je Status — hält den ASCII-Identifier aus dem Anzeigetext. */
export const CASE_STATUS_LABELS: Record<CaseStatus, string> = {
  NEU: 'Neu',
};

/** Anfrage zum Anlegen eines Falls (`POST /api/v1/cases`), Felder in snake_case. */
export interface CreateCaseRequest {
  case_type: CaseType;
  priority: Priority;
  source: Source;
  case_reference: string;
}

/** Antwort des Backends auf einen erfassten Fall. */
export interface CaseResponse {
  id: string;
  case_number: string;
  case_reference: string;
  case_type: CaseType;
  priority: Priority;
  source: Source;
  status: CaseStatus;
  created_at: string;
}
