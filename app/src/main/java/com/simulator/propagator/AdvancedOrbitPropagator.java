package com.simulator.propagator;

import com.simulator.tle.Tle;
import com.simulator.math.Vector3D;

/**
 * Advanced orbit propagator base class.
 * Provides:
 *  - Thread-safe propagation
 *  - Caching of last propagated state
 *  - Separation of position & velocity computation
 *  - Easy extension for SGP4 / SDP4 / custom models
 */
public abstract class AdvancedOrbitPropagator extends OrbitPropagator {

    protected static final double MU = 398600.4418; // km^3/s^2
    protected static final double J2 = 1.08263e-3;
    protected static final double RE = 6378.137;    // km
    protected static final double RHO0 = 3.614e-13; // kg/km^3 (LEO)
    protected static final double H_SCALE = 88.667; // km
    protected static final double mass = 500;
    protected static final double crossSection = 10;
   
    // Cached last propagation results
    private Vector3D lastPosition = null;
    private Vector3D lastVelocity = null;
    private double lastMinutes = Double.NaN;

    protected AdvancedOrbitPropagator(Tle tle) {
        super(tle);
    }

    /**
     * Propagate to the given minutes since epoch.
     * Thread-safe. Uses cached state if available.
     */
    @Override
    public synchronized Vector3D propagate(double minutes) {
        if (Double.compare(minutes, lastMinutes) == 0 && lastPosition != null) {
            return lastPosition;
        }

        double[] posArr = computePosition(minutes);
        lastPosition = new Vector3D(posArr[0], posArr[1], posArr[2]);
        lastMinutes = minutes;

        return lastPosition;
    }

    /**
     * Propagate to the given minutes since epoch and return full state.
     * Returns [position, velocity].
     * Thread-safe with caching.
     */
    @Override
    public synchronized Vector3D[] propagateState(double minutes) {
        if (Double.compare(minutes, lastMinutes) == 0 && lastPosition != null && lastVelocity != null) {
            return new Vector3D[]{lastPosition, lastVelocity};
        }

        double[] posArr = computePosition(minutes);
        double[] velArr = computeVelocity(minutes);

        lastPosition = new Vector3D(posArr[0], posArr[1], posArr[2]);
        lastVelocity = new Vector3D(velArr[0], velArr[1], velArr[2]);
        lastMinutes = minutes;

        return new Vector3D[]{lastPosition, lastVelocity};
    }

    /**
     * Reset cached states. Useful if underlying TLE or model changes.
     */
    @Override
    public synchronized void reset() {
        lastPosition = null;
        lastVelocity = null;
        lastMinutes = Double.NaN;
    }

    // ------------------- Abstract methods for concrete propagators -------------------

    /**
     * Compute position vector (km) at given minutes since epoch.
     * To be implemented by subclass (SGP4, SDP4, custom).
     */
    protected abstract double[] computePosition(double minutes);

    /**
     * Compute velocity vector (km/s) at given minutes since epoch.
     * To be implemented by subclass (SGP4, SDP4, custom).
     */
    protected abstract double[] computeVelocity(double minutes);
}
