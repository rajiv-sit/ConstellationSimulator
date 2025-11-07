package com.simulator.core;

import com.simulator.math.Vector3D;
import com.simulator.propagator.OrbitPropagator;
import com.simulator.tle.Tle;

import java.util.*;

/**
 * Represents a single satellite with propagation, history, and visualization support.
 *
 * Features:
 *  - TLE-based or user-defined propagation
 *  - Thread-safe propagation and history tracking
 *  - Optional initial position and full orbital path (for visualization)
 *  - Builder pattern for flexible satellite construction
 */
public final class Satellite {

    // ==============================
    // Core Satellite Properties
    // ==============================
    private final String name;
    private final Tle tle;
    private final OrbitPropagator propagator;
    private final Vector3D initialPosition;        // Optional initial ECI position
    private final List<Vector3D> orbitPoints;      // Full orbital line for visualization
    private final int maxHistory;

    // ==============================
    // Dynamic State History
    // ==============================
    private final LinkedList<Vector3D> positionHistory = new LinkedList<>();
    private final LinkedList<Vector3D> velocityHistory = new LinkedList<>();
    private final LinkedList<Double> timeHistory = new LinkedList<>();

    // ==============================
    // Optional Metadata
    // ==============================
    private String type;             // e.g. "LEO", "GEO"
    private double mass;             // kg
    private double crossSection;     // m²

    // ==============================
    // Constructor (private, use Builder)
    // ==============================
    private Satellite(Builder builder) {
        this.name = builder.name;
        this.tle = builder.tle;
        this.propagator = builder.propagator;
        this.type = builder.type;
        this.mass = builder.mass;
        this.crossSection = builder.crossSection;
        this.maxHistory = builder.maxHistory;
        this.initialPosition = builder.initialPosition;
        this.orbitPoints = builder.orbitPoints != null
                ? new ArrayList<>(builder.orbitPoints)
                : Collections.emptyList();
    }

    // ==============================
    // Accessors
    // ==============================
    public String getName() { return name; }
    public Tle getTle() { return tle; }
    public OrbitPropagator getPropagator() { return propagator; }
    public String getType() { return type; }
    public double getMass() { return mass; }
    public double getCrossSection() { return crossSection; }
    public Vector3D getInitialPosition() { return initialPosition; }

    /**
     * Returns the full orbit path (read-only).
     */
    public List<Vector3D> getOrbitPoints() {
        return Collections.unmodifiableList(orbitPoints);
    }

    // ==============================
    // Propagation Methods
    // ==============================

    /**
     * Propagates satellite to a given time (in minutes since epoch).
     * Updates internal state history.
     */
    public synchronized Vector3D[] propagateState(double minutesSinceEpoch) {
        Vector3D[] state = propagator.propagateState(minutesSinceEpoch);
        addToHistory(positionHistory, state[0]);
        addToHistory(velocityHistory, state[1]);
        addToHistory(timeHistory, minutesSinceEpoch);
        return state;
    }

    /**
     * Alternative propagation based directly on TLE orbital parameters.
     * Simplified analytical model for quick demos.
     */
    public Vector3D[] propagateStateTLE(double minutesSinceEpoch) {
        double meanMotionRevPerDay = tle.meanMotion;
        double periodMinutes = 1440.0 / meanMotionRevPerDay;
        double trueAnomaly = (minutesSinceEpoch % periodMinutes) / periodMinutes * 360.0;

        Vector3D pos = tle.positionECI(trueAnomaly);
        Vector3D vel = tle.velocityECI(trueAnomaly);

        return new Vector3D[]{pos, vel};
    }

    /**
     * Propagates the state over multiple time steps.
     */
    public synchronized List<Vector3D[]> propagateStates(List<Double> times) {
        List<Double> sortedTimes = new ArrayList<>(times);
        Collections.sort(sortedTimes);
        List<Vector3D[]> result = new ArrayList<>(sortedTimes.size());
        for (double t : sortedTimes) result.add(propagateState(t));
        return result;
    }

    /**
     * Predicts state without recording to history.
     */
    public Vector3D[] predictState(double minutesSinceEpoch) {
        return propagator.propagateState(minutesSinceEpoch);
    }

    /**
     * Gets most recent propagated position and velocity.
     */
    public Vector3D[] getCurrentState() {
        if (positionHistory.isEmpty() || velocityHistory.isEmpty()) return null;
        return new Vector3D[]{positionHistory.getLast(), velocityHistory.getLast()};
    }

    /**
     * Clears all history records.
     */
    public synchronized void clearHistory() {
        positionHistory.clear();
        velocityHistory.clear();
        timeHistory.clear();
    }

    // ==============================
    // History Access
    // ==============================
    public List<Vector3D> getPositionHistory() {
        return Collections.unmodifiableList(positionHistory);
    }

    public List<Vector3D> getVelocityHistory() {
        return Collections.unmodifiableList(velocityHistory);
    }

    public List<Double> getTimeHistory() {
        return Collections.unmodifiableList(timeHistory);
    }

    public List<Vector3D[]> getLastNStates(int n) {
        List<Vector3D[]> result = new ArrayList<>();
        int start = Math.max(0, positionHistory.size() - n);
        for (int i = start; i < positionHistory.size(); i++) {
            result.add(new Vector3D[]{positionHistory.get(i), velocityHistory.get(i)});
        }
        return result;
    }

    public Vector3D getPositionAt(int index) { return positionHistory.get(index); }
    public Vector3D getVelocityAt(int index) { return velocityHistory.get(index); }
    public Double getTimeAt(int index) { return timeHistory.get(index); }

    // ==============================
    // Internal Utilities
    // ==============================
    private <T> void addToHistory(LinkedList<T> list, T item) {
        if (list.size() >= maxHistory) list.removeFirst();
        list.addLast(item);
    }

    // ==============================
    // Builder Class
    // ==============================
    public static class Builder {
        private String name;
        private Tle tle;
        private OrbitPropagator propagator;
        private String type = "LEO";
        private double mass = 500;
        private double crossSection = 10;
        private int maxHistory = 100;

        private Vector3D initialPosition;
        private List<Vector3D> orbitPoints;

        // ----- Fluent Builder Methods -----
        public Builder name(String name) { this.name = name; return this; }
        public Builder tle(Tle tle) { this.tle = tle; return this; }
        public Builder propagator(OrbitPropagator propagator) { this.propagator = propagator; return this; }
        public Builder type(String type) { this.type = type; return this; }
        public Builder mass(double mass) { this.mass = mass; return this; }
        public Builder crossSection(double crossSection) { this.crossSection = crossSection; return this; }
        public Builder maxHistory(int maxHistory) { this.maxHistory = maxHistory; return this; }
        public Builder initialPosition(Vector3D pos) { this.initialPosition = pos; return this; }
        public Builder orbitPoints(List<Vector3D> points) { this.orbitPoints = points; return this; }

        public Satellite build() {
            if (name == null || tle == null || propagator == null) {
                throw new IllegalStateException("Name, TLE, and Propagator must be provided before building a Satellite");
            }
            return new Satellite(this);
        }
    }
}
