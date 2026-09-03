package ch.css.demo.caseflow.adapter.in.rest.dto;

import java.util.List;

/**
 * Identität und Rollen des angemeldeten Nutzers für das Frontend.
 *
 * @param username      technischer Benutzername aus dem OIDC-Token
 * @param roles         CaseFlow-Rollen (siehe {@code Roles})
 * @param authenticated {@code false}, wenn die Dev-Ersatzidentität greift
 */
public record MeResponse(String username, List<String> roles, boolean authenticated) {
}
