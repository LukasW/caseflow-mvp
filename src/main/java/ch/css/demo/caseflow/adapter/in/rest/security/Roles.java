package ch.css.demo.caseflow.adapter.in.rest.security;

import java.util.Set;

/**
 * Rollennamen des CaseFlow-Berechtigungsmodells. Es sind die internen,
 * stabilen Rollennamen. Der {@link RoleMappingAugmentor} bildet die
 * Keycloak-Client-Rollen des {@code caseflow}-Clients ({@code caseflow-case-manager},
 * {@code caseflow-team-lead}, …) am OIDC-Boundary auf diese Werte ab. Sie werden
 * in {@code @RolesAllowed} referenziert — deshalb müssen es Compile-Zeit-Konstanten sein.
 */
public final class Roles {

    /** Bearbeitet Fälle: erfassen, übernehmen, Wiedervorlagen setzen, abschliessen. */
    public static final String CASE_MANAGER = "CASE_MANAGER";
    /** Weist Fälle zu, überwacht Fristen und Auslastung des Teams. */
    public static final String TEAM_LEAD = "TEAM_LEAD";
    /** Verwaltet Stammdaten, Vertretungen und Konfiguration. */
    public static final String ADMIN = "ADMIN";
    /** Lesender Zugriff auf Audit-Trail und Reports. */
    public static final String AUDITOR = "AUDITOR";

    /** Alle bekannten CaseFlow-Rollen — filtert Keycloak-Standardrollen aus dem Token. */
    public static final Set<String> ALL = Set.of(CASE_MANAGER, TEAM_LEAD, ADMIN, AUDITOR);

    private Roles() {
    }
}
