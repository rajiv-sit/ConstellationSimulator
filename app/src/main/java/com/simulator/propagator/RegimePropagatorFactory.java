package com.simulator.propagator;

import com.simulator.core.OrbitalRegime;
import com.simulator.tle.Tle;

/**
 * RegimePropagatorFactory
 *
 * O(1) factory that selects a regime-aware propagator without mutating
 * existing callers. Encourages polymorphic use in new code paths.
 */
public final class RegimePropagatorFactory {
    private RegimePropagatorFactory() {}

    public static RegimeOrbitPropagator create(Tle tle) {
        OrbitalRegime regime = OrbitalRegime.fromSemiMajorAxis(tle.semiMajorAxis);
        return switch (regime) {
            case LEO -> new LeoOrbitPropagator(tle);
            case MEO -> new MeoOrbitPropagator(tle);
            case GEO -> new GeoOrbitPropagator(tle);
        };
    }
}
