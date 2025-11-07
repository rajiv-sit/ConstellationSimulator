package com.simulator.core;

import com.simulator.math.Vector3D;
import com.simulator.utils.CoordinateUtils;

import java.util.Map;

/**
 * RegimeAwareSimulator
 *
 * Optional extension layer for high-throughput simulations. It wraps an
 * existing Constellation instance and exposes regime-aware summaries without
 * modifying the base simulator classes.
 *
 * Time: propagateAllAtTime is delegated (O(n)); per-satellite summaries stay O(1).
 * Space: O(1) overhead beyond the wrapped constellation.
 */
public class RegimeAwareSimulator {

    private final Constellation constellation;

    public RegimeAwareSimulator(Constellation constellation) {
        this.constellation = constellation;
    }

    public Map<String, Vector3D[]> propagateAllAtTime(double minutes) {
        return constellation.propagateAllAtTime(minutes);
    }

    /**
     * Produce a lat/lon/alt + ENU velocity view for the current propagation
     * without altering underlying state storage.
     */
    public Map<String, CoordinateUtils.LlaState> propagateLlaAtTime(double minutes) {
        Map<String, Vector3D[]> eciStates = propagateAllAtTime(minutes);
        java.util.LinkedHashMap<String, CoordinateUtils.LlaState> result = new java.util.LinkedHashMap<>(eciStates.size());
        for (var entry : eciStates.entrySet()) {
            Vector3D pos = entry.getValue()[0];
            Vector3D vel = entry.getValue()[1];
            result.put(entry.getKey(), CoordinateUtils.eciToLla(pos, vel, minutes));
        }
        return result;
    }
}
