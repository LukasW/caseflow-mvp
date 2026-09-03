package ch.css.demo.caseflow.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Schutznetz für die Hexagonal-Schichtdisziplin (ADR-01).
 *
 * <p>Bricht den Build, sobald Domain-Klassen Framework-Imports erhalten,
 * die Application-Schicht Adapter referenziert, der Persistence-Adapter
 * andere Adapter anfasst, REST-DTOs ausserhalb des dafür vorgesehenen
 * Pakets landen oder der REST-Adapter an der Application-Schicht vorbei
 * direkt Domain-Services aufruft.
 */
class HexagonArchitectureTest {

    private static final String BASE_PACKAGE = "ch.css.demo.caseflow";
    private static final String JSON_PROPERTY_ANNOTATION = "com.fasterxml.jackson.annotation.JsonProperty";

    private static JavaClasses productionClasses;

    @BeforeAll
    static void importProductionClasses() {
        productionClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE_PACKAGE);
    }

    @Test
    void domainHasNoFrameworkImports() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "jakarta..",
                        "io.quarkus..",
                        "com.fasterxml..",
                        "org.hibernate..")
                .as("Klassen in ..domain.. dürfen keine Framework-Abhängigkeiten haben (R1)")
                .allowEmptyShould(true)
                .check(productionClasses);
    }

    @Test
    void applicationDoesNotDependOnAdapters() {
        noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("..adapter..")
                .as("Klassen in ..application.. dürfen ..adapter.. nicht importieren (R2)")
                .allowEmptyShould(true)
                .check(productionClasses);
    }

    @Test
    void persistenceAdapterDoesNotReferenceOtherAdapters() {
        noClasses()
                .that().resideInAPackage("..adapter.out.persistence..")
                .should().dependOnClassesThat(belongToOtherAdapterPackages())
                .as("Klassen in ..adapter.out.persistence.. dürfen andere Adapter nicht referenzieren (R3)")
                .allowEmptyShould(true)
                .check(productionClasses);
    }

    @Test
    void restDtosLiveOnlyInDtoPackage() {
        classes()
                .that().areRecords()
                .and(useJsonPropertyAnywhere())
                .should().resideInAPackage("..adapter.in.rest.dto..")
                .as("REST-DTOs (record mit @JsonProperty) gehören ausschliesslich nach ..adapter.in.rest.dto.. (R4)")
                .allowEmptyShould(true)
                .check(productionClasses);
    }

    /**
     * Der REST-Adapter ist ein Driving Adapter und ruft ausschliesslich
     * Driving Ports ({@code domain.port.in}) auf. Ein direkter Zugriff auf
     * einen Domain-Service umginge die Application-Schicht und die
     * Transaktionsgrenze.
     */
    @Test
    void restAdapterDoesNotDependOnDomainServices() {
        noClasses()
                .that().resideInAPackage("..adapter.in.rest..")
                .should().dependOnClassesThat().resideInAPackage("..domain.service..")
                .as("REST-Adapter (..adapter.in.rest..) darf keine Domain-Services "
                        + "(..domain.service..) referenzieren — Aufrufe gehen via Driving Port (R5)")
                .allowEmptyShould(true)
                .check(productionClasses);
    }

    private static DescribedPredicate<JavaClass> belongToOtherAdapterPackages() {
        return new DescribedPredicate<>("liegen in einem Adapter-Paket ausserhalb der Persistence") {
            @Override
            public boolean test(JavaClass javaClass) {
                String packageName = javaClass.getPackageName();
                if (!packageName.startsWith(BASE_PACKAGE + ".adapter")) {
                    return false;
                }
                return !packageName.contains(".adapter.out.persistence");
            }
        };
    }

    private static DescribedPredicate<JavaClass> useJsonPropertyAnywhere() {
        return new DescribedPredicate<>("verwenden @JsonProperty") {
            @Override
            public boolean test(JavaClass javaClass) {
                if (javaClass.isAnnotatedWith(JSON_PROPERTY_ANNOTATION)) {
                    return true;
                }
                if (javaClass.getFields().stream()
                        .anyMatch(field -> field.isAnnotatedWith(JSON_PROPERTY_ANNOTATION))) {
                    return true;
                }
                if (javaClass.getMethods().stream()
                        .anyMatch(method -> method.isAnnotatedWith(JSON_PROPERTY_ANNOTATION))) {
                    return true;
                }
                return javaClass.getConstructors().stream()
                        .flatMap(constructor -> constructor.getParameters().stream())
                        .anyMatch(parameter -> parameter.isAnnotatedWith(JSON_PROPERTY_ANNOTATION));
            }
        };
    }
}
