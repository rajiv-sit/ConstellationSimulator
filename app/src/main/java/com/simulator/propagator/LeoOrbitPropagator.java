package com.simulator.propagator;

import com.simulator.core.OrbitalRegime;
import com.simulator.tle.Tle;

/**
 * LEO-specialized wrapper that keeps SGP4 as the underlying model.
 */
public class LeoOrbitPropagator extends RegimeOrbitPropagator {
    public LeoOrbitPropagator(Tle tle) {
        super(tle, OrbitalRegime.LEO, new Sgp4Propagator(tle));
    }
}
