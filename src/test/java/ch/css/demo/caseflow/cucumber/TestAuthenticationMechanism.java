package ch.css.demo.caseflow.cucumber;

import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Test-Authentifizierung für die Cucumber-Szenarien: baut eine
 * {@link SecurityIdentity} aus den Headern {@code X-Test-User} und
 * {@code X-Test-Roles}.
 *
 * <p>OIDC ist im {@code %test}-Profil deaktiviert, und Cucumber-Szenarien sind
 * keine JUnit-Methoden — {@code @TestSecurity} greift hier also nicht. Ohne die
 * Test-Header bleibt der Request anonym, sodass das bestehende
 * {@code @TestSecurity}-Verhalten und die 401-Tests unberührt bleiben.
 */
@ApplicationScoped
public class TestAuthenticationMechanism implements HttpAuthenticationMechanism {

    public static final String USER_HEADER = "X-Test-User";
    public static final String ROLES_HEADER = "X-Test-Roles";

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context,
            IdentityProviderManager identityProviderManager) {
        String user = context.request().getHeader(USER_HEADER);
        String roles = context.request().getHeader(ROLES_HEADER);
        if (user == null || roles == null) {
            return Uni.createFrom().nullItem();
        }
        QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder()
                .setPrincipal(new QuarkusPrincipal(user))
                .addRoles(Arrays.stream(roles.split(","))
                        .map(String::trim)
                        .filter(role -> !role.isEmpty())
                        .collect(Collectors.toSet()));
        return Uni.createFrom().item(builder.build());
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        return Uni.createFrom().item(new ChallengeData(401, null, null));
    }
}
