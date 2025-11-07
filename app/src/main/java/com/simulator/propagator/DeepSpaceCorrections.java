package com.simulator.propagator;

import com.simulator.tle.Tle;

/**
 * Collection of static deep-space correction functions.
 * <p>
 * These are placeholder formulas for demonstration purposes. They can be replaced
 * with proper SGP4/SDP4 deep-space corrections for long-term orbital propagation.
 * <p>
 * Provides:
 * - dpInit: initial mean motion correction
 * - dpSec: secular inclination correction
 * - dpPer: periodic mean motion correction
 * <p>
 * All corrections are thread-safe and can be combined.
 */
public final class DeepSpaceCorrections {

    private static final double TWO_PI = 2.0 * Math.PI;

    private DeepSpaceCorrections() {
        // prevent instantiation
    }

    /**
     * Initial deep-space mean motion correction (dpInit).
     * Formula: 0.0001 * sin(meanMotionRad * minutes)
     */
    public static final DeepSpaceCorrection dpInit = (tle, minutes) -> {
        validateTle(tle);
        double meanMotionRad = tle.meanMotion * TWO_PI / 1440.0; // convert rev/day to rad/min
        return 0.0001 * Math.sin(meanMotionRad * minutes);
    };

    /**
     * Secular deep-space inclination correction (dpSec).
     * Formula: 0.00005 * minutes * cos(inclination)
     */
    public static final DeepSpaceCorrection dpSec = (tle, minutes) -> {
        validateTle(tle);
        double inclinationRad = Math.toRadians(tle.inclination);
        return 0.00005 * minutes * Math.cos(inclinationRad);
    };

    /**
     * Periodic deep-space mean motion correction (dpPer).
     * Formula: 0.00002 * sin(meanMotionRad * minutes) + 0.00001 * cos(meanMotionRad * minutes)
     */
    public static final DeepSpaceCorrection dpPer = (tle, minutes) -> {
        validateTle(tle);
        double meanMotionRad = tle.meanMotion * TWO_PI / 1440.0;
        return 0.00002 * Math.sin(meanMotionRad * minutes)
             + 0.00001 * Math.cos(meanMotionRad * minutes);
    };

    /**
     * Combine multiple deep-space corrections into a single correction.
     * Useful for modular propagation.
     *
     * @param corrections Array of DeepSpaceCorrection functions
     * @return Combined DeepSpaceCorrection
     */
    public static DeepSpaceCorrection combine(DeepSpaceCorrection... corrections) {
        return (tle, minutes) -> {
            validateTle(tle);
            double total = 0.0;
            for (DeepSpaceCorrection c : corrections) {
                if (c != null) total += c.apply(tle, minutes);
            }
            return total;
        };
    }

    /**
     * Validates TLE input for null.
     *
     * @param tle The TLE to validate
     * @throws IllegalArgumentException if tle is null
     */
    private static void validateTle(Tle tle) {
        if (tle == null) {
            throw new IllegalArgumentException("TLE cannot be null for deep-space correction");
        }
    }
}
