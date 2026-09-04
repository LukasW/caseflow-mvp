package ch.css.demo.caseflow.domain.model;

import java.util.regex.Pattern;

/**
 * Fachliche, für Menschen lesbare Fallnummer im Format {@code CASE-000001}.
 * Eindeutig über den Fallbestand; wird an der Persistenzgrenze vergeben.
 */
public record CaseNumber(String value) {

    private static final Pattern FORMAT = Pattern.compile("CASE-\\d{6,}");

    public CaseNumber {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "Fallnummer muss dem Format CASE-000001 entsprechen: " + value);
        }
    }

    public static CaseNumber of(String value) {
        return new CaseNumber(value);
    }
}
