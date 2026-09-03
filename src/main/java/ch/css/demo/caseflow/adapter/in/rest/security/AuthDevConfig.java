package ch.css.demo.caseflow.adapter.in.rest.security;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Ersatzidentität für {@code GET /api/v1/me}, solange OIDC in {@code %dev} und
 * {@code %test} deaktiviert ist. Erlaubt das Durchspielen der rollenbasierten
 * Sichten ohne laufendes Keycloak.
 */
@ConfigMapping(prefix = "caseflow.auth")
public interface AuthDevConfig {

    /** Login-Kennung der Dev-Ersatzidentität. */
    @WithDefault("dev")
    String devUser();

    /** Rolle der Dev-Ersatzidentität — einer der Werte aus {@link Roles}. */
    @WithDefault("CASE_MANAGER")
    String devRole();
}
