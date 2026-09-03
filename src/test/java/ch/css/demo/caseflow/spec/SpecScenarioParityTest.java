package ch.css.demo.caseflow.spec;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Spec-Test-Parität (Guardrail aus dem SDD-Review): hält jede implementierte
 * User Story und ihre Cucumber-Feature-Datei(en) synchron, damit die fachliche
 * Spezifikation nicht von den ausgeführten Tests wegdriftet.
 *
 * <p>Die Verknüpfung läuft über die Fallnummer: Eine {@code .feature}-Datei,
 * deren Titel oder Tag {@code US-<n>} nennt, gehört zur User Story
 * {@code specs/user-stories/<n>-*.md}. Geprüft wird pro verknüpfter Story:
 * <ul>
 *   <li>die Story-Datei existiert (keine verwaisten Features),</li>
 *   <li>die Anzahl BDD-Szenarien in Story und Feature(s) stimmt überein.</li>
 * </ul>
 *
 * <p>Noch nicht implementierte Stories (ohne Feature) lösen den Test nicht aus:
 * Die Parität greift erst, sobald ein Feature die Story referenziert. Wird eine
 * Story auf Backend- und Frontend-Feature aufgeteilt, zählt die Summe.
 */
class SpecScenarioParityTest {

    private static final Path USER_STORIES = Path.of("specs/user-stories");
    private static final Path FEATURES = Path.of("src/test/resources/features");

    private static final Pattern US_MARKER = Pattern.compile("US-(\\d+)");
    private static final Pattern STORY_SCENARIO = Pattern.compile("(?m)^###\\s+Szenario\\b");
    private static final Pattern FEATURE_SCENARIO =
            Pattern.compile("(?m)^\\s*(Szenario|Szenariogrundriss|Scenario|Scenario Outline)\\s*:");

    @Test
    void featuresAndUserStoriesStayInSync() {
        if (!Files.isDirectory(FEATURES)) {
            return;
        }

        Map<Integer, LinkedStory> linked = collectLinkedStories();
        List<String> violations = new ArrayList<>();

        for (Map.Entry<Integer, LinkedStory> entry : linked.entrySet()) {
            int storyNumber = entry.getKey();
            LinkedStory story = entry.getValue();

            Optional<Path> storyFile = findStoryFile(storyNumber);
            if (storyFile.isEmpty()) {
                violations.add("US-%d: Feature(s) %s referenzieren die Story, aber specs/user-stories/%d-*.md fehlt."
                        .formatted(storyNumber, story.features, storyNumber));
                continue;
            }

            int storyScenarios = count(STORY_SCENARIO, read(storyFile.get()));
            if (storyScenarios != story.scenarioCount) {
                violations.add(
                        "US-%d: %d Szenario(s) in %s, aber %d in Feature(s) %s. Story und Feature müssen dieselben Szenarien führen."
                                .formatted(storyNumber, storyScenarios, storyFile.get().getFileName(),
                                        story.scenarioCount, story.features));
            }
        }

        assertTrue(violations.isEmpty(),
                "Spec-Test-Parität verletzt:\n  - " + String.join("\n  - ", violations));
    }

    private Map<Integer, LinkedStory> collectLinkedStories() {
        Map<Integer, LinkedStory> linked = new LinkedHashMap<>();
        try (Stream<Path> paths = Files.walk(FEATURES)) {
            paths.filter(p -> p.getFileName().toString().endsWith(".feature"))
                    .sorted()
                    .forEach(feature -> {
                        String content = read(feature);
                        findStoryNumber(content).ifPresent(number -> {
                            int scenarios = count(FEATURE_SCENARIO, content);
                            linked.computeIfAbsent(number, n -> new LinkedStory())
                                    .add(feature.getFileName().toString(), scenarios);
                        });
                    });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return linked;
    }

    private Optional<Integer> findStoryNumber(String featureContent) {
        Matcher scenario = FEATURE_SCENARIO.matcher(featureContent);
        String header = scenario.find() ? featureContent.substring(0, scenario.start()) : featureContent;
        Matcher marker = US_MARKER.matcher(header);
        return marker.find() ? Optional.of(Integer.parseInt(marker.group(1))) : Optional.empty();
    }

    private Optional<Path> findStoryFile(int storyNumber) {
        if (!Files.isDirectory(USER_STORIES)) {
            return Optional.empty();
        }
        try (Stream<Path> paths = Files.list(USER_STORIES)) {
            return paths.filter(p -> p.getFileName().toString().matches("^" + storyNumber + "-.*\\.md$"))
                    .findFirst();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static int count(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private static String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static final class LinkedStory {
        private int scenarioCount;
        private final List<String> features = new ArrayList<>();

        private void add(String featureName, int scenarios) {
            this.scenarioCount += scenarios;
            this.features.add(featureName);
        }
    }
}
