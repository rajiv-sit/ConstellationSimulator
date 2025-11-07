package com.simulator.propagator;

import com.simulator.tle.Tle;
import com.simulator.math.Vector3D;
import java.util.Objects;

/**
 * Abstract base class for orbit propagation.
 * <p>
 * Can be extended for SGP4, SDP4, or custom propagators.
 * Provides common null-safety, caching hooks, and modular interface for state queries.
 */
public abstract class OrbitPropagator {

    /** Associated TLE defining the satellite orbit */
    protected final Tle tle;

    /** Optional effective model flag for simplified propagation */
    protected boolean useEffModel = true;

    /**
     * Constructor.
     *
     * @param tle Non-null TLE defining satellite orbit
     * @throws NullPointerException if tle is null
     */
    public OrbitPropagator(Tle tle) {
        this.tle = Objects.requireNonNull(tle, "TLE cannot be null");
    }

    /**
     * Propagate orbit to a specific time (minutes since epoch) and return position only.
     * <p>
     * Implementations may use simplified or full SGP4/SDP4 formulas.
     *
     * @param minutes Minutes since TLE epoch
     * @return Position vector in ECI frame (km)
     */
    public abstract Vector3D propagate(double minutes);

    /**
     * Propagate orbit to a specific time (minutes since epoch) and return both position and velocity.
     *
     * @param minutes Minutes since TLE epoch
     * @return Array of length 2: [position, velocity] in ECI frame (km and km/s)
     */
    public abstract Vector3D[] propagateState(double minutes);

    /**
     * Reset internal cached states or propagator memory.
     * Useful for multi-pass simulations or restarting from epoch.
     */
    public abstract void reset();

    /**
     * Toggle the use of the simplified "effective model" (EFF) for propagation.
     * <p>
     * When true, computations may be faster with approximations.
     * When false, full SGP4/SDP4 (or custom) computations are expected.
     *
     * @param enable true to use EFF model, false for full propagation
     */
    public void setEffModel(boolean enable) {
        this.useEffModel = enable;
    }

    /**
     * Returns whether the effective model is enabled.
     *
     * @return true if EFF model is active
     */
    public boolean isEffModelEnabled() {
        return useEffModel;
    }

    /**
     * Safe helper: propagates to multiple times and returns states in order.
     * This method is optional for subclasses to override for efficiency.
     *
     * @param times Array of minutes since epoch
     * @return Array of [position, velocity] vectors
     */
    public Vector3D[][] propagateStates(double[] times) {
        if (times == null) return new Vector3D[0][];
        Vector3D[][] states = new Vector3D[times.length][];
        for (int i = 0; i < times.length; i++) {
            states[i] = propagateState(times[i]);
        }
        return states;
    }
}

