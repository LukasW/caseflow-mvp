package ch.css.demo.caseflow.adapter.in.rest;

import ch.css.demo.caseflow.adapter.in.rest.dto.MeResponse;
import ch.css.demo.caseflow.adapter.in.rest.security.AuthDevConfig;
import ch.css.demo.caseflow.adapter.in.rest.security.Roles;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;

/**
 * Driving Adapter: liefert dem Frontend Identität und Rollen des angemeldeten
 * Nutzers unter {@code /api/v1/me}.
 *
 * <p>Bewusst ohne {@code @Authenticated}: In {@code %keycloak}/{@code %prod}
 * erzwingt die HTTP-Permission auf {@code /api/*} bereits die Anmeldung. In
 * {@code %dev}/{@code %test} ist OIDC deaktiviert — dann greift die
 * Dev-Ersatzidentität, damit die rollenbasierten Sichten ohne Keycloak
 * nutzbar bleiben. Bei aktivem OIDC erhält ein anonymer Request niemals eine
 * Ersatzidentität mit Rollen.
 */
@Path("/api/v1/me")
@Produces(MediaType.APPLICATION_JSON)
public class MeResource {

    private static final String ANONYMOUS_USER = "anonymous";

    private final SecurityIdentity identity;
    private final AuthDevConfig devConfig;
    private final boolean oidcEnabled;

    public MeResource(SecurityIdentity identity, AuthDevConfig devConfig,
            @ConfigProperty(name = "quarkus.oidc.enabled") boolean oidcEnabled) {
        this.identity = identity;
        this.devConfig = devConfig;
        this.oidcEnabled = oidcEnabled;
    }

    /**
     * Gibt Benutzername und CaseFlow-Rollen zurück. Liegt keine OIDC-Identität
     * vor, greift bei deaktiviertem OIDC die Dev-Ersatzidentität, sonst eine
     * rollenlose Anonym-Antwort.
     */
    @GET
    public MeResponse getMe() {
        if (!identity.isAnonymous()) {
            List<String> roles = identity.getRoles().stream()
                    .filter(Roles.ALL::contains)
                    .sorted()
                    .toList();
            return new MeResponse(identity.getPrincipal().getName(), roles, true);
        }
        if (oidcEnabled) {
            return new MeResponse(ANONYMOUS_USER, List.of(), false);
        }
        return new MeResponse(devConfig.devUser(), List.of(devConfig.devRole()), false);
    }
}
