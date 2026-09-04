package ch.css.demo.caseflow.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import java.time.Clock;

/**
 * Stellt eine {@link Clock} als CDI-Bean bereit, damit Application-Services den
 * Erfassungszeitpunkt testbar (fixe Uhr im Test) beziehen können.
 */
@ApplicationScoped
public class ClockProducer {

    @Produces
    public Clock clock() {
        return Clock.systemUTC();
    }
}
