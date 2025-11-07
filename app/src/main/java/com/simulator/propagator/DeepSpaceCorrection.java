package com.simulator.propagator;

import com.simulator.tle.Tle;

/**
 * Functional interface for deep-space orbital corrections.
 * <p>
 * Applies a correction to a satellite’s orbital parameter based on TLE data
 * and time since epoch. Designed for use with lambdas or method references.
 */
@FunctionalInterface
public interface DeepSpaceCorrection {

    /**
     * Computes a deep-space correction for a satellite.
     *
     * @param tle     The satellite TLE containing orbital parameters.
     * @param minutes Minutes since epoch for which the correction is calculated.
     * @return The correction value (units depend on implementation).
     */
    double apply(Tle tle, double minutes);

    /**
     * Returns a new DeepSpaceCorrection that combines this correction with another.
     * <p>
     * The resulting correction is the sum of this and the other correction.
     *
     * @param other Another DeepSpaceCorrection to combine.
     * @return A combined DeepSpaceCorrection.
     */
    default DeepSpaceCorrection combine(DeepSpaceCorrection other) {
        return (tle, minutes) -> this.apply(tle, minutes) + other.apply(tle, minutes);
    }
}
