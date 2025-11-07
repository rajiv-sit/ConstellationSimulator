package com.simulator.core;

import com.simulator.math.Vector3D;
import java.util.*;

/**
 * Represents a constellation of satellites.
 *
 * Features:
 *  - Supports single-time and multi-time propagation
 *  - Maintains kinematic/dynamic history for all satellites
 *  - Allows querying individual satellite states or full histories
 *  - Provides access to all satellites for visualization
 *  - Allows dynamic satellite addition (used by ConstellationGenerator)
 */
public final class Constellation {

    private final String name;
    private final List<Satellite> satellites;

    /** Constructor: Create empty constellation with a name. */
    public Constellation(String name) {
        this.name = name;
        this.satellites = new ArrayList<>();
    }

    /** Optional constructor for initializing with existing satellites. */
    public Constellation(List<Satellite> satellites) {
        this.name = "Constellation";
        this.satellites = new ArrayList<>(satellites);
    }

    public String getName() { return name; }

    /** Add a single satellite */
    public void addSatellite(Satellite sat) {
        if (sat != null) satellites.add(sat);
    }

    /** Add multiple satellites */
    public void addSatellites(Collection<Satellite> sats) {
        if (sats != null) satellites.addAll(sats);
    }

    /** Propagate all satellites to a single time (minutes since epoch) */
    public Map<String, Vector3D[]> propagateAllAtTime(double minutes) {
        Map<String, Vector3D[]> result = new LinkedHashMap<>();
        for (Satellite sat : satellites) {
            Vector3D[] state = sat.propagateState(minutes);
            result.put(sat.getName(), state);
        }
        return result;
    }

    /** Propagate all satellites over multiple times */
    public Map<String, List<Vector3D[]>> propagateAll(List<Double> times) {
        Map<String, List<Vector3D[]>> result = new LinkedHashMap<>();
        for (Satellite sat : satellites) {
            result.put(sat.getName(), sat.propagateStates(times));
        }
        return result;
    }

    /** Predict specific satellite’s state without modifying history */
    public Vector3D[] querySatellite(String name, double minutes) {
        for (Satellite sat : satellites) {
            if (sat.getName().equals(name))
                return sat.predictState(minutes);
        }
        return null;
    }

    /** Get full stored history of a satellite */
    public List<Vector3D[]> getSatelliteHistory(String name) {
        for (Satellite sat : satellites) {
            if (sat.getName().equals(name)) {
                List<Vector3D[]> history = new ArrayList<>();
                List<Vector3D> posHist = sat.getPositionHistory();
                List<Vector3D> velHist = sat.getVelocityHistory();
                for (int i = 0; i < posHist.size(); i++) {
                    history.add(new Vector3D[]{posHist.get(i), velHist.get(i)});
                }
                return history;
            }
        }
        return Collections.emptyList();
    }

    /** Get the last N states of a satellite */
    public List<Vector3D[]> getLastNStates(String name, int n) {
        for (Satellite sat : satellites) {
            if (sat.getName().equals(name)) {
                return sat.getLastNStates(n);
            }
        }
        return Collections.emptyList();
    }

    /** Return all satellites for iteration or visualization */
    public List<Satellite> getAllSatellites() {
        return Collections.unmodifiableList(satellites);
    }

    /** Clear histories for all satellites */
    public void clearAllHistories() {
        for (Satellite s : satellites) {
            s.clearHistory();
        }
    }

    // In Constellation.java
    public List<Satellite> getSatellites() {
        return Collections.unmodifiableList(satellites);
    }

    @Override
    public String toString() {
        return String.format("%s: %d satellites", name, satellites.size());
    }
}
