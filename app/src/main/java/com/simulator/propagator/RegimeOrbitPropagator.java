package com.simulator.propagator;

import com.simulator.core.OrbitalRegime;
import com.simulator.math.Vector3D;
import com.simulator.tle.Tle;

/**
 * RegimeOrbitPropagator
 *
 * Decorator that routes propagation calls to a regime-appropriate delegate.
 * Keeps existing propagators intact while enabling polymorphic selection
 * (LEO/MEO/GEO) without touching callers.
 *
 * Time: propagate* remains O(1) since it simply delegates.
 * Space: O(1) per instance (only a reference to the delegate).
 */
public class RegimeOrbitPropagator extends OrbitPropagator {

    protected final OrbitalRegime regime;
    protected final OrbitPropagator delegate;

    public RegimeOrbitPropagator(Tle tle, OrbitalRegime regime, OrbitPropagator delegate) {
        super(tle);
        this.regime = regime;
        this.delegate = delegate;
    }

    public OrbitalRegime getRegime() {
        return regime;
    }

    public OrbitPropagator getDelegate() {
        return delegate;
    }

    @Override
    public Vector3D propagate(double minutes) {
        return delegate.propagate(minutes);
    }

    @Override
    public Vector3D[] propagateState(double minutes) {
        return delegate.propagateState(minutes);
    }

    @Override
    public void reset() {
        delegate.reset();
    }

    @Override
    public void setEffModel(boolean enable) {
        super.setEffModel(enable);
        delegate.setEffModel(enable);
    }
}
