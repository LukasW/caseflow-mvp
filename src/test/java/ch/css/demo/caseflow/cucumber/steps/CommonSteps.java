package ch.css.demo.caseflow.cucumber.steps;

import ch.css.demo.caseflow.cucumber.TestAuthenticationMechanism;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

/**
 * Gemeinsame Step-Definitions: Anmeldung als Rolle, generische REST-Aufrufe
 * und Antwort-Prüfungen. Fachliche Steps kommen in eigene
 * {@code <Aggregat>Steps}-Klassen und verwenden {@link #authenticated()}.
 */
public class CommonSteps {

    private String user;
    private String roles;
    private Response lastResponse;

    @Angenommen("ich bin als {string} mit der Rolle {string} angemeldet")
    public void ichBinAngemeldet(String user, String roles) {
        this.user = user;
        this.roles = roles;
    }

    @Angenommen("ich bin nicht angemeldet")
    public void ichBinNichtAngemeldet() {
        this.user = null;
        this.roles = null;
    }

    @Wenn("ich {string} abrufe")
    public void ichRufeAb(String path) {
        lastResponse = authenticated().when().get(path);
    }

    @Dann("erhalte ich den Status {int}")
    public void erhalteIchDenStatus(int status) {
        assertThat(lastResponse.statusCode(), equalTo(status));
    }

    @Dann("enthält die Antwort die Rolle {string}")
    public void enthaeltDieAntwortDieRolle(String role) {
        assertThat(lastResponse.jsonPath().getList("roles", String.class), hasItem(role));
    }

    /** Request-Spezifikation mit den Test-Auth-Headern des aktuellen Szenarios. */
    RequestSpecification authenticated() {
        RequestSpecification spec = given().contentType("application/json");
        if (user != null && roles != null) {
            spec = spec.header(TestAuthenticationMechanism.USER_HEADER, user)
                    .header(TestAuthenticationMechanism.ROLES_HEADER, roles);
        }
        return spec;
    }
}
