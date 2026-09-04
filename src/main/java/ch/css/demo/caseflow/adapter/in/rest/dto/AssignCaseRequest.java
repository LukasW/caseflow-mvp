package ch.css.demo.caseflow.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Eingabedaten zum Zuweisen oder Umverteilen eines Falls. Die zuständige Person
 * ist eine IAM-Referenz-ID und muss gesetzt sein (fail fast an der REST-Grenze).
 */
public record AssignCaseRequest(
        @NotBlank(message = "Zuständigkeit ist erforderlich") String assignee) {
}
