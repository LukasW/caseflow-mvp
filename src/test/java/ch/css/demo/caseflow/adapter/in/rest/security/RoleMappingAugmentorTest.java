package ch.css.demo.caseflow.adapter.in.rest.security;

import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit-Tests des {@link RoleMappingAugmentor}: prüft die Abbildung der
 * Keycloak-Client-Rollen ({@code caseflow-case-manager}, …) auf die internen
 * Rollennamen und das unveränderte Durchreichen von Dev-/Test- und anonymen
 * Identitäten.
 */
class RoleMappingAugmentorTest {

    private final RoleMappingAugmentor augmentor = new RoleMappingAugmentor();

    private SecurityIdentity augment(SecurityIdentity identity) {
        return augmentor.augment(identity, null).await().indefinitely();
    }

    @Test
    void augment_bildetKeycloakRolleAufInternenNamenAb() {
        SecurityIdentity oidc = QuarkusSecurityIdentity.builder()
                .setPrincipal(new QuarkusPrincipal("anna"))
                .addRole("caseflow-case-manager")
                .build();

        SecurityIdentity result = augment(oidc);

        assertEquals(Set.of(Roles.CASE_MANAGER), result.getRoles());
        assertEquals("anna", result.getPrincipal().getName());
    }

    @Test
    void augment_bildetMehrereRollenAbUndBehaeltUnbekannte() {
        SecurityIdentity oidc = QuarkusSecurityIdentity.builder()
                .setPrincipal(new QuarkusPrincipal("lead"))
                .addRoles(Set.of("caseflow-team-lead", "caseflow-auditor", "offline_access"))
                .build();

        SecurityIdentity result = augment(oidc);

        assertEquals(Set.of(Roles.TEAM_LEAD, Roles.AUDITOR, "offline_access"), result.getRoles());
    }

    @Test
    void augment_laesstInterneIdentitaetUnveraendert() {
        SecurityIdentity dev = QuarkusSecurityIdentity.builder()
                .setPrincipal(new QuarkusPrincipal("dev"))
                .addRole(Roles.CASE_MANAGER)
                .build();

        assertSame(dev, augment(dev),
                "Ohne abbildbare Keycloak-Rolle wird die Identität unverändert durchgereicht");
    }

    @Test
    void augment_laesstAnonymeIdentitaetUnveraendert() {
        SecurityIdentity anonymous = QuarkusSecurityIdentity.builder()
                .setAnonymous(true)
                .setPrincipal(new QuarkusPrincipal(""))
                .build();

        assertSame(anonymous, augment(anonymous));
    }
}
