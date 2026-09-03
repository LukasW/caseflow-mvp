package ch.css.demo.caseflow.adapter.in.rest.security;

import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Set;

/**
 * Dev-Authentifizierung: stellt jedem Request die in {@link AuthDevConfig}
 * konfigurierte Ersatzidentität als echte {@link SecurityIdentity} bereit,
 * solange OIDC deaktiviert ist ({@code %dev}).
 *
 * <p>Ohne diesen Mechanismus wäre in {@code %dev} jeder Request anonym — alle
 * {@code @Authenticated}/{@code @RolesAllowed}-Endpunkte würden mit {@code 403}
 * abgewiesen, obwohl das SPA die Rollensichten bereits rendert. Er ist das
 * {@code %dev}-Pendant zum {@code TestAuthenticationMechanism} der Tests.
 *
 * <p>Sicherheitsabgrenzung: {@code @IfBuildProfile("dev")} hält die Klasse aus
 * dem Prod- und Test-Build heraus. Zusätzlich greift sie zur Laufzeit nur bei
 * deaktiviertem OIDC — bei {@code dev,keycloak} übernimmt OIDC.
 */
@ApplicationScoped
@IfBuildProfile("dev")
public class DevAuthenticationMechanism implements HttpAuthenticationMechanism {

    private final AuthDevConfig devConfig;
    private final boolean oidcEnabled;

    @Inject
    public DevAuthenticationMechanism(AuthDevConfig devConfig,
            @ConfigProperty(name = "quarkus.oidc.enabled") boolean oidcEnabled) {
        this.devConfig = devConfig;
        this.oidcEnabled = oidcEnabled;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context,
            IdentityProviderManager identityProviderManager) {
        if (oidcEnabled) {
            // dev,keycloak: OIDC authentifiziert — die Dev-Identität tritt zurück.
            return Uni.createFrom().nullItem();
        }
        QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder()
                .setPrincipal(new QuarkusPrincipal(devConfig.devUser()))
                .addRoles(Set.of(devConfig.devRole()));
        return Uni.createFrom().item(builder.build());
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        return Uni.createFrom().item(new ChallengeData(401, null, null));
    }
}
