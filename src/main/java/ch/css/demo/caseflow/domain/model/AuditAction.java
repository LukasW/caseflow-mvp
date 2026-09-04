package ch.css.demo.caseflow.domain.model;

/**
 * Art einer protokollierten Statusänderung im Audit-Trail. In Vergangenheitsform,
 * da ein Audit-Eintrag ein bereits geschehenes Ereignis festhält.
 */
public enum AuditAction {
    CASE_CREATED,
    CASE_ASSIGNED
}
