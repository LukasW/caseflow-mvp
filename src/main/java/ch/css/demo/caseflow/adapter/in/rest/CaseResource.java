package ch.css.demo.caseflow.adapter.in.rest;

import ch.css.demo.caseflow.adapter.in.rest.dto.CaseResponse;
import ch.css.demo.caseflow.adapter.in.rest.dto.CreateCaseRequest;
import ch.css.demo.caseflow.adapter.in.rest.security.Roles;
import ch.css.demo.caseflow.domain.model.Case;
import ch.css.demo.caseflow.domain.model.CaseReference;
import ch.css.demo.caseflow.domain.port.in.CreateCase;
import ch.css.demo.caseflow.domain.port.in.CreateCaseCommand;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;

/**
 * Driving Adapter: Erfassen von Fällen unter {@code /api/v1/cases}.
 *
 * <p>Nur {@code CASE_MANAGER} darf erfassen — die Autorisierung wird
 * serverseitig erzwungen. Der Aufruf geht ausschliesslich über den Driving Port
 * {@link CreateCase}, nie direkt an einen Domain-Service.
 */
@Authenticated
@Path("/api/v1/cases")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CaseResource {

    private final CreateCase createCase;
    private final SecurityIdentity identity;

    public CaseResource(CreateCase createCase, SecurityIdentity identity) {
        this.createCase = createCase;
        this.identity = identity;
    }

    @POST
    @RolesAllowed(Roles.CASE_MANAGER)
    public Response create(@Valid CreateCaseRequest request, @Context UriInfo uriInfo) {
        CreateCaseCommand command = new CreateCaseCommand(
                request.caseType(),
                request.priority(),
                request.source(),
                CaseReference.of(request.caseReference()),
                identity.getPrincipal().getName());

        Case created = createCase.handle(command);

        URI location = uriInfo.getAbsolutePathBuilder()
                .path(created.id().value().toString())
                .build();
        return Response.created(location).entity(CaseResponse.from(created)).build();
    }
}
