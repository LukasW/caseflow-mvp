package ch.css.demo.caseflow.adapter.in.rest;

import ch.css.demo.caseflow.adapter.in.rest.dto.VersionDto;
import io.quarkus.security.Authenticated;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Driving Adapter: liefert dem Frontend Version, Build-Zeitstempel und
 * Git-Commit-Hash unter {@code /api/v1/version}.
 *
 * <p>Mit {@code @Authenticated} geschützt. Werte werden via Maven-Filtering
 * zur Build-Zeit in {@code application.properties} eingesetzt und sind zur
 * Laufzeit als Config-Properties verfügbar.
 */
@Authenticated
@Path("/api/v1/version")
@Produces(MediaType.APPLICATION_JSON)
public class VersionResource {

    private final String version;
    private final String buildTimestamp;
    private final String gitCommitShort;

    public VersionResource(
            @ConfigProperty(name = "quarkus.application.version", defaultValue = "dev") String version,
            @ConfigProperty(name = "caseflow.build.timestamp", defaultValue = "") String buildTimestamp,
            @ConfigProperty(name = "caseflow.build.git-commit", defaultValue = "") String gitCommitShort) {
        this.version = version;
        this.buildTimestamp = buildTimestamp;
        this.gitCommitShort = gitCommitShort;
    }

    @GET
    public VersionDto getVersion() {
        return new VersionDto(version, buildTimestamp, gitCommitShort);
    }
}
