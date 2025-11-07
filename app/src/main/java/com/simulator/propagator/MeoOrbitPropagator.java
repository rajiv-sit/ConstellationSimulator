package com.simulator.propagator;

import com.simulator.core.OrbitalRegime;
import com.simulator.tle.Tle;

/**
 * MEO-specialized wrapper. Defaults to SDP4 for deeper-space stability,
 * but can be swapped if a higher-fidelity model is desired.
 */
public class MeoOrbitPropagator extends RegimeOrbitPropagator {
    public MeoOrbitPropagator(Tle tle) {
        super(tle, OrbitalRegime.MEO, new Sdp4Propagator(tle));
    }
}
