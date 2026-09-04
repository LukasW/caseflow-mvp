package ch.css.demo.caseflow.adapter.in.rest;

import ch.css.demo.caseflow.domain.model.CaseNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Übersetzt den Domänen-Fehler {@link CaseNotFoundException} in eine
 * HTTP-Antwort {@code 404 Not Found}.
 */
@Provider
public class CaseNotFoundExceptionMapper implements ExceptionMapper<CaseNotFoundException> {

    @Override
    public Response toResponse(CaseNotFoundException exception) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(exception.getMessage())
                .build();
    }
}
