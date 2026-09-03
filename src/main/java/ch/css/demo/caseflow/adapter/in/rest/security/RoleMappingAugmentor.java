package ch.css.demo.caseflow.adapter.in.rest.security;

import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Bildet die Keycloak-Client-Rollen des {@code caseflow}-Clients am OIDC-Boundary
 * auf die internen CaseFlow-Rollennamen ab.
 *
 * <p>Der Realm führt die Client-Rollen in {@code lower-kebab-case} mit
 * {@code caseflow-}-Applikationsprefix ({@code caseflow-case-manager}, …).
 * Quarkus liest sie aus dem Token-Claim {@code resource_access/caseflow/roles}.
 *
 * <p>Dieser Augmentor übersetzt sie in die unpräfigierten Namen aus
 * {@link Roles}, sodass die Anwendung intern mit ihrer stabilen Ubiquitous
 * Language arbeitet. Identitäten ohne abbildbare Rolle (Dev, Test, anonym)
 * werden unverändert durchgereicht.
 */
@ApplicationScoped
public class RoleMappingAugmentor implements SecurityIdentityAugmentor {

    static final Map<String, String> ROLE_MAPPING = Map.of(
            "caseflow-case-manager", Roles.CASE_MANAGER,
            "caseflow-team-lead", Roles.TEAM_LEAD,
            "caseflow-admin", Roles.ADMIN,
            "caseflow-auditor", Roles.AUDITOR);

    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity identity,
            AuthenticationRequestContext context) {
        boolean hasMappableRole = identity.getRoles().stream()
                .anyMatch(ROLE_MAPPING::containsKey);
        if (!hasMappableRole) {
            return Uni.createFrom().item(identity);
        }
        Set<String> mappedRoles = identity.getRoles().stream()
                .map(role -> ROLE_MAPPING.getOrDefault(role, role))
                .collect(Collectors.toSet());
        SecurityIdentity mapped = QuarkusSecurityIdentity.builder()
                .setPrincipal(identity.getPrincipal())
                .addAttributes(identity.getAttributes())
                .addCredentials(identity.getCredentials())
                .addRoles(mappedRoles)
                .build();
        return Uni.createFrom().item(mapped);
    }
}
