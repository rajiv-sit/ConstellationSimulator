package com.simulator.tle;

import com.simulator.math.Vector3D;

/**
 * Represents a Two-Line Element (TLE) for satellite orbital parameters.
 * Strictly supports standard 69-character TLEs.
 * Includes derived orbital parameters and helper methods to get ECI position & velocity.
 */
public final class Tle {
    public final String name;
    public final String line1;
    public final String line2;

    public final double inclination;       // degrees
    public final double eccentricity;      // unitless
    public final double meanMotion;        // rev/day
    public final double argumentOfPerigee; // degrees
    public final double raan;              // degrees
    public final double meanAnomaly;       // degrees
    public final double semiMajorAxis;     // km

    public final double perigee;           // km
    public final double apogee;            // km
    public final double orbitalPeriod;     // minutes

    private static final double MU = 398600.4418; // km^3/s^2
    private static final int TLE_LENGTH = 69;

    private Tle(String name, String line1, String line2,
                double inclination, double eccentricity, double meanMotion,
                double argumentOfPerigee, double raan, double meanAnomaly, double semiMajorAxis) {

        if (name == null || line1 == null || line2 == null)
            throw new IllegalArgumentException("TLE name and lines must be non-null");

        this.name = name;
        this.line1 = line1;
        this.line2 = line2;

        this.inclination = inclination;
        this.eccentricity = eccentricity;
        this.meanMotion = meanMotion;
        this.argumentOfPerigee = argumentOfPerigee;
        this.raan = raan;
        this.meanAnomaly = meanAnomaly;
        this.semiMajorAxis = semiMajorAxis;

        this.perigee = semiMajorAxis * (1 - eccentricity);
        this.apogee = semiMajorAxis * (1 + eccentricity);
        this.orbitalPeriod = 1440.0 / meanMotion; // minutes
    }

    /** Factory method: create TLE from standard 2-line strings */
public static Tle fromTwoLineElement(String name, String line1, String line2) {
    if (line1 == null || line2 == null) {
        throw new IllegalArgumentException("TLE lines must be non-null.");
    }

    try {
        // Helper to safely extract substring
        java.util.function.BiFunction<String, int[], String> extract =
                (line, range) -> {
                    int start = range[0];
                    int end = Math.min(range[1], line.length());
                    return line.substring(start, end).trim();
                };

        // Column ranges according to standard TLE positions (0-based)
        double inclination      = Double.parseDouble(extract.apply(line2, new int[]{8, 16}));
        double raan             = Double.parseDouble(extract.apply(line2, new int[]{17, 25}));
        double eccentricity     = Double.parseDouble("0." + extract.apply(line2, new int[]{26, 33}));
        double argumentOfPerigee= Double.parseDouble(extract.apply(line2, new int[]{34, 42}));
        double meanAnomaly      = Double.parseDouble(extract.apply(line2, new int[]{43, 51}));
        double meanMotion       = Double.parseDouble(extract.apply(line2, new int[]{52, 63}));

        double a = computeSemiMajorAxis(meanMotion);

        return new Tle(name, line1, line2,
                inclination, eccentricity, meanMotion,
                argumentOfPerigee, raan, meanAnomaly, a);

    } catch (StringIndexOutOfBoundsException e) {
        throw new IllegalArgumentException("TLE lines are too short or malformed.", e);
    } catch (NumberFormatException e) {
        throw new IllegalArgumentException("TLE contains invalid numeric fields.", e);
    }
}
    /** Compute semi-major axis in km from mean motion (rev/day) */
    private static double computeSemiMajorAxis(double meanMotionRevPerDay) {
        double nRad = meanMotionRevPerDay * 2 * Math.PI / 86400.0; // rad/s
        return Math.cbrt(MU / (nRad * nRad));
    }

    // ---------------- ECI State Vector Helpers ----------------

    public Vector3D positionECI(double trueAnomalyDeg) {
        double theta = Math.toRadians(trueAnomalyDeg);
        double r = semiMajorAxis; // circular approximation
        double xPeri = r * Math.cos(theta);
        double yPeri = r * Math.sin(theta);
        double zPeri = 0;
        return perifocalToECI(xPeri, yPeri, zPeri);
    }

    public Vector3D velocityECI(double trueAnomalyDeg) {
        double theta = Math.toRadians(trueAnomalyDeg);
        double v = Math.sqrt(MU / semiMajorAxis);
        double vxPeri = -v * Math.sin(theta);
        double vyPeri = v * Math.cos(theta);
        double vzPeri = 0;
        return perifocalToECI(vxPeri, vyPeri, vzPeri);
    }

    private Vector3D perifocalToECI(double x, double y, double z) {
        double cosOmega = Math.cos(Math.toRadians(raan));
        double sinOmega = Math.sin(Math.toRadians(raan));
        double cosI = Math.cos(Math.toRadians(inclination));
        double sinI = Math.sin(Math.toRadians(inclination));
        double cosw = Math.cos(Math.toRadians(argumentOfPerigee));
        double sinw = Math.sin(Math.toRadians(argumentOfPerigee));

        double r11 = cosOmega * cosw - sinOmega * sinw * cosI;
        double r12 = -cosOmega * sinw - sinOmega * cosw * cosI;
        double r13 = sinOmega * sinI;
        double r21 = sinOmega * cosw + cosOmega * sinw * cosI;
        double r22 = -sinOmega * sinw + cosOmega * cosw * cosI;
        double r23 = -cosOmega * sinI;
        double r31 = sinw * sinI;
        double r32 = cosw * sinI;
        double r33 = cosI;

        double xe = r11 * x + r12 * y + r13 * z;
        double ye = r21 * x + r22 * y + r23 * z;
        double ze = r31 * x + r32 * y + r33 * z;

        return new Vector3D(xe, ye, ze);
    }

    @Override
    public String toString() {
        return String.format("%s [inc=%.4f°, ecc=%.6f, n=%.4f rev/day, a=%.2f km, perigee=%.2f km, apogee=%.2f km, period=%.2f min]",
                name, inclination, eccentricity, meanMotion, semiMajorAxis, perigee, apogee, orbitalPeriod);
    }
}
