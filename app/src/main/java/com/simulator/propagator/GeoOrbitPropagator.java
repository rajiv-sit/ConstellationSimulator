package com.simulator.propagator;

import com.simulator.core.OrbitalRegime;
import com.simulator.tle.Tle;

/**
 * GEO-specialized wrapper leveraging SDP4 for deep-space propagation.
 */
public class GeoOrbitPropagator extends RegimeOrbitPropagator {
    public GeoOrbitPropagator(Tle tle) {
        super(tle, OrbitalRegime.GEO, new Sdp4Propagator(tle));
    }
}
