package ch.css.demo.caseflow;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Triggert die Vitest-Suite im Frontend via Quinoa.
 *
 * <p>Quinoa führt {@code npm run test} während des Quarkus-Boots aus, sobald
 * {@code quarkus.quinoa.run-tests=true} aktiv ist. Genutztes Node/npm stammt
 * aus {@code quarkus.quinoa.package-manager-install}, daher ist keine
 * system-weite Installation nötig.
 */
@QuarkusTest
@TestProfile(FrontendTestsIT.RunFrontendTests.class)
class FrontendTestsIT {

    @Test
    void vitestSuitePasses() {
        // Erreicht der Test diese Zeile, hat Quinoa `npm run test` erfolgreich ausgeführt.
    }

    public static final class RunFrontendTests implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("quarkus.quinoa.run-tests", "true");
        }
    }
}
