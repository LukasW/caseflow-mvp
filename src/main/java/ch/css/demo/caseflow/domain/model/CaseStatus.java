package ch.css.demo.caseflow.domain.model;

/**
 * Lebenszyklus-Status eines Falls. Ein neu erfasster Fall startet in
 * {@link #NEU}; die weiteren Übergänge gehören zu den Folge-Stories.
 */
public enum CaseStatus {
    NEU,
    ZUGEWIESEN,
    IN_BEARBEITUNG,
    ABGESCHLOSSEN
}
