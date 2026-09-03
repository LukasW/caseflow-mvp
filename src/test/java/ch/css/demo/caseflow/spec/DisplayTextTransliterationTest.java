package ch.css.demo.caseflow.spec;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Umlaut-Lint (Guardrail aus dem SDD-Review): verhindert, dass die
 * ASCII-Transliteration eines Identifiers (ae/ue/oe) in kundensichtbaren
 * deutschen Anzeigetext leckt.
 *
 * <p>Geprüft wird ausschliesslich sichtbarer Text in Angular-Templates: Textknoten
 * zwischen den Tags sowie die Anzeige-Attribute {@code aria-label}, {@code title},
 * {@code placeholder}, {@code alt} und {@code matTooltip}. Klassennamen, Bindings
 * und andere Identifier bleiben aussen vor.
 *
 * <p>Das Schweizer «ss» (schliessen, Stoss, muss) ist korrektes Hochdeutsch und
 * bewusst nicht Teil der Sperrliste — nur echte Umlaut-Transliterationen zählen.
 */
class DisplayTextTransliterationTest {

    private static final Path ANGULAR_APP = Path.of("src/main/webapp/src/app");

    private static final List<String> FORBIDDEN = List.of(
            "fuer", "ueber", "zurueck", "zuruecksetzen", "oeffnen", "eroeffnen",
            "wiedereroeffnen", "aendern", "geaendert", "waehlen", "auswaehlen",
            "faellig", "faelligkeit", "faelle", "naechste", "koennen", "muessen",
            "loeschen", "loeschung", "hinzufuegen", "pruefen", "ueberpruefen",
            "bestaetigen", "zustaendig", "zustaendigkeit", "uebersicht",
            "uebernehmen", "verfuegbar", "ausfuehren", "gueltig", "ungueltig");

    private static final Pattern FORBIDDEN_WORD =
            Pattern.compile("(?i)\\b(" + String.join("|", FORBIDDEN) + ")\\b");
    private static final Pattern TEXT_NODE = Pattern.compile(">([^<]+)<");
    private static final Pattern DISPLAY_ATTRIBUTE =
            Pattern.compile("(?i)(?:aria-label|title|placeholder|alt|matTooltip)\\s*=\\s*\"([^\"]*)\"");
    private static final Pattern INTERPOLATION = Pattern.compile("\\{\\{[^}]*}}");

    @Test
    void angularTemplatesUseUmlautsInDisplayText() {
        if (!Files.isDirectory(ANGULAR_APP)) {
            return;
        }

        List<String> violations = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(ANGULAR_APP)) {
            paths.filter(p -> p.getFileName().toString().endsWith(".html"))
                    .sorted()
                    .forEach(template -> collectViolations(template, violations));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        assertTrue(violations.isEmpty(),
                "ASCII-Transliteration in kundensichtbarem Text (Umlaute verwenden):\n  - "
                        + String.join("\n  - ", violations));
    }

    private void collectViolations(Path template, List<String> violations) {
        String content = read(template);
        String fileName = template.getFileName().toString();

        checkSnippets(TEXT_NODE.matcher(content), fileName, violations);
        checkSnippets(DISPLAY_ATTRIBUTE.matcher(content), fileName, violations);
    }

    private void checkSnippets(Matcher source, String fileName, List<String> violations) {
        while (source.find()) {
            String snippet = INTERPOLATION.matcher(source.group(1)).replaceAll(" ");
            Matcher hit = FORBIDDEN_WORD.matcher(snippet);
            while (hit.find()) {
                violations.add("%s: \"%s\" — Umlaut fehlt in \"%s\""
                        .formatted(fileName, hit.group(1), snippet.trim()));
            }
        }
    }

    private static String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
