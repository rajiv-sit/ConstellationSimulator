package com.simulator.core;

import com.simulator.propagator.RegimePropagatorFactory;
import com.simulator.propagator.RegimeOrbitPropagator;
import com.simulator.tle.Tle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * RegimeConstellationFactory
 *
 * Builds constellations using regime-aware propagators while leaving the
 * existing Constellation/Satellite implementations untouched.
 *
 * Time: O(n) over TLE count to allocate satellites.
 * Space: O(n) for satellite storage (same as baseline).
 */
public final class RegimeConstellationFactory {
    private RegimeConstellationFactory() {}

    public static Constellation build(String name, Collection<Tle> tles) {
        Constellation constellation = new Constellation(name);
        for (Tle tle : tles) {
            constellation.addSatellite(buildSatellite(tle));
        }
        return constellation;
    }

    public static Satellite buildSatellite(Tle tle) {
        RegimeOrbitPropagator propagator = RegimePropagatorFactory.create(tle);
        return new Satellite.Builder()
                .name(tle.name)
                .tle(tle)
                .propagator(propagator)
                .type(propagator.getRegime().name())
                .build();
    }

    public static List<Satellite> buildSatellites(Collection<Tle> tles) {
        List<Satellite> sats = new ArrayList<>();
        for (Tle tle : tles) {
            sats.add(buildSatellite(tle));
        }
        return sats;
    }
}
