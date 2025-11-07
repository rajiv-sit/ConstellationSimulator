package com.simulator.core;

/**
 * OrbitalRegime
 *
 * Lightweight classification helper for LEO, MEO, and GEO.
 * Keeps thresholds centralized so higher-level factories can stay O(1) when
 * determining which propagator to use for a given satellite.
 */
public enum OrbitalRegime {
    LEO,
    MEO,
    GEO;

    // Thresholds are semi-major axis based (km). Exposed for reuse.
    public static final double EARTH_RADIUS_KM = 6371.0;
    public static final double LEO_MAX_ALT_KM = 2000.0;
    public static final double MEO_MAX_ALT_KM = 35786.0;

    /**
     * Classify a regime using semi-major axis in km.
     */
    public static OrbitalRegime fromSemiMajorAxis(double semiMajorAxisKm) {
        double altKm = Math.max(0, semiMajorAxisKm - EARTH_RADIUS_KM);
        if (altKm <= LEO_MAX_ALT_KM) return LEO;
        if (altKm <= MEO_MAX_ALT_KM) return MEO;
        return GEO;
    }

    /**
     * Classify a regime using mean motion (rev/day), keeping complexity O(1).
     * Useful when semi-major axis is unavailable but TLE mean motion exists.
     */
    public static OrbitalRegime fromMeanMotion(double meanMotionRevPerDay) {
        // ~15 rev/day corresponds to ~400 km LEO; GEO ~1 rev/day.
        if (meanMotionRevPerDay > 10.0) return LEO;
        if (meanMotionRevPerDay > 1.1) return MEO;
        return GEO;
    }
}
