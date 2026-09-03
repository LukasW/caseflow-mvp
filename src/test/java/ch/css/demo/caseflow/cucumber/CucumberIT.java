package ch.css.demo.caseflow.cucumber;

import io.quarkiverse.cucumber.CucumberOptions;
import io.quarkiverse.cucumber.CucumberQuarkusTest;

/**
 * Java-Cucumber-Runner: führt alle {@code .feature}-Dateien unter
 * {@code src/test/resources/features/} aus, die weder {@code @E2E} (Playwright)
 * noch {@code @Pending} getaggt sind. Läuft als Failsafe-IT gegen die
 * Dev-Services-PostgreSQL.
 */
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "ch.css.demo.caseflow.cucumber",
        tags = "not @E2E and not @Pending")
public class CucumberIT extends CucumberQuarkusTest {
}
