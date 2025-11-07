package com.simulator.utils;

import com.simulator.math.Vector3D;

/**
 * CoordinateUtils
 * ----------------
 * Robust coordinate conversion utilities for satellite simulations.
 * Supports transformations among:
 *  - ECI (Earth-Centered Inertial)
 *  - ECEF (Earth-Centered, Earth-Fixed)
 *  - Geodetic (latitude, longitude, altitude)
 *  - ENU (East-North-Up) topocentric frame relative to a ground station
 *
 * Units:
 *  - distances: meters / kilometers (consistent)
 *  - angles: radians
 *  - time: seconds
 */
public final class CoordinateUtils {

    private static final double WGS84_A = 6378.137; // equatorial radius, km
    private static final double WGS84_F = 1.0 / 298.257223563;
    private static final double WGS84_B = WGS84_A * (1.0 - WGS84_F);
    private static final double WGS84_E2 = 1.0 - (WGS84_B * WGS84_B) / (WGS84_A * WGS84_A);
    private static final double EARTH_ROT_RATE = 7.2921159e-5; // rad/s

    private CoordinateUtils() {} // static utility class

    // =======================
    // POSITION CONVERSIONS
    // =======================
    public static Vector3D eciToEcef(Vector3D eci, double t) {
        double theta = EARTH_ROT_RATE * t;
        double cosT = Math.cos(theta);
        double sinT = Math.sin(theta);
        double x = cosT * eci.x + sinT * eci.y;
        double y = -sinT * eci.x + cosT * eci.y;
        double z = eci.z;
        return new Vector3D(x, y, z);
    }

    public static Vector3D ecefToEci(Vector3D ecef, double t) {
        double theta = EARTH_ROT_RATE * t;
        double cosT = Math.cos(theta);
        double sinT = Math.sin(theta);
        double x = cosT * ecef.x - sinT * ecef.y;
        double y = sinT * ecef.x + cosT * ecef.y;
        double z = ecef.z;
        return new Vector3D(x, y, z);
    }

    public static double[] ecefToGeodetic(Vector3D ecef) {
        double x = ecef.x, y = ecef.y, z = ecef.z;
        double lon = Math.atan2(y, x);
        double p = Math.sqrt(x * x + y * y);
        double lat = Math.atan2(z, p * (1 - WGS84_E2));
        double latPrev;
        double N;

        // iterative solution for latitude
        do {
            latPrev = lat;
            N = WGS84_A / Math.sqrt(1.0 - WGS84_E2 * Math.sin(lat) * Math.sin(lat));
            lat = Math.atan2(z + WGS84_E2 * N * Math.sin(lat), p);
        } while (Math.abs(lat - latPrev) > 1e-12);

        N = WGS84_A / Math.sqrt(1.0 - WGS84_E2 * Math.sin(lat) * Math.sin(lat));
        double alt = p / Math.cos(lat) - N;

        return new double[]{lat, lon, alt};
    }

    public static Vector3D geodeticToEcef(double latRad, double lonRad, double alt) {
        double sinLat = Math.sin(latRad);
        double cosLat = Math.cos(latRad);
        double sinLon = Math.sin(lonRad);
        double cosLon = Math.cos(lonRad);

        double N = WGS84_A / Math.sqrt(1 - WGS84_E2 * sinLat * sinLat);

        double x = (N + alt) * cosLat * cosLon;
        double y = (N + alt) * cosLat * sinLon;
        double z = (N * (1 - WGS84_E2) + alt) * sinLat;

        return new Vector3D(x, y, z);
    }

    // =======================
    // VELOCITY CONVERSIONS
    // =======================
    public static Vector3D eciToEcefVelocity(Vector3D eciPos, Vector3D eciVel, double t) {
        double theta = EARTH_ROT_RATE * t;
        double cosT = Math.cos(theta);
        double sinT = Math.sin(theta);

        double vx = cosT * eciVel.x + sinT * eciVel.y + EARTH_ROT_RATE * (sinT * eciPos.x - cosT * eciPos.y);
        double vy = -sinT * eciVel.x + cosT * eciVel.y - EARTH_ROT_RATE * (cosT * eciPos.x + sinT * eciPos.y);
        double vz = eciVel.z;

        return new Vector3D(vx, vy, vz);
    }

    public static Vector3D ecefToEciVelocity(Vector3D ecefPos, Vector3D ecefVel, double t) {
        double theta = EARTH_ROT_RATE * t;
        double cosT = Math.cos(theta);
        double sinT = Math.sin(theta);

        double vx = cosT * ecefVel.x - sinT * ecefVel.y - EARTH_ROT_RATE * (sinT * ecefPos.x + cosT * ecefPos.y);
        double vy = sinT * ecefVel.x + cosT * ecefVel.y + EARTH_ROT_RATE * (cosT * ecefPos.x - sinT * ecefPos.y);
        double vz = ecefVel.z;

        return new Vector3D(vx, vy, vz);
    }

    // =======================
    // ENU / Topocentric
    // =======================
    public static Vector3D ecefToEnu(Vector3D delta, double latRad, double lonRad) {
        double sinLat = Math.sin(latRad), cosLat = Math.cos(latRad);
        double sinLon = Math.sin(lonRad), cosLon = Math.cos(lonRad);

        double e = -sinLon * delta.x + cosLon * delta.y;
        double n = -cosLon * sinLat * delta.x - sinLat * sinLon * delta.y + cosLat * delta.z;
        double u = cosLat * cosLon * delta.x + cosLat * sinLon * delta.y + sinLat * delta.z;

        return new Vector3D(e, n, u);
    }

    public static Vector3D enuToEcef(Vector3D enu, double latRad, double lonRad) {
        double sinLat = Math.sin(latRad), cosLat = Math.cos(latRad);
        double sinLon = Math.sin(lonRad), cosLon = Math.cos(lonRad);

        double dx = -sinLon * enu.x - cosLon * sinLat * enu.y + cosLon * cosLat * enu.z;
        double dy = cosLon * enu.x - sinLon * sinLat * enu.y + sinLon * cosLat * enu.z;
        double dz = cosLat * enu.y + sinLat * enu.z;

        return new Vector3D(dx, dy, dz);
    }

    // =======================
    // ENU / Topocentric with altitude
    // =======================
    public static Vector3D ecefToEnu(Vector3D delta, double latRad, double lonRad, double altKm) {
        double dz = delta.z - altKm;

        double sinLat = Math.sin(latRad), cosLat = Math.cos(latRad);
        double sinLon = Math.sin(lonRad), cosLon = Math.cos(lonRad);

        double e = -sinLon * delta.x + cosLon * delta.y;
        double n = -cosLon * sinLat * delta.x - sinLat * sinLon * delta.y + cosLat * dz;
        double u = cosLat * cosLon * delta.x + cosLat * sinLon * delta.y + sinLat * dz;

        return new Vector3D(e, n, u);
    }

    public static Vector3D enuToEcef(Vector3D enu, double latRad, double lonRad, double altKm) {
        double sinLat = Math.sin(latRad), cosLat = Math.cos(latRad);
        double sinLon = Math.sin(lonRad), cosLon = Math.cos(lonRad);

        double dx = -sinLon * enu.x - cosLon * sinLat * enu.y + cosLon * cosLat * enu.z;
        double dy = cosLon * enu.x - sinLon * sinLat * enu.y + sinLon * cosLat * enu.z;
        double dz = cosLat * enu.y + sinLat * enu.z + altKm;

        return new Vector3D(dx, dy, dz);
    }

    // =======================
    // Topocentric full (position+velocity)
    // =======================
    public static Vector3D[] ecefToTopocentric(Vector3D ecefPos, Vector3D ecefVel,
                                               double gsLatRad, double gsLonRad, double gsAlt) {
        Vector3D gsEcef = geodeticToEcef(gsLatRad, gsLonRad, gsAlt);
        Vector3D deltaPos = new Vector3D(ecefPos.x - gsEcef.x, ecefPos.y - gsEcef.y, ecefPos.z - gsEcef.z);
        Vector3D enuPos = ecefToEnu(deltaPos, gsLatRad, gsLonRad, gsAlt);

        Vector3D gsVel = new Vector3D(-EARTH_ROT_RATE * gsEcef.y, EARTH_ROT_RATE * gsEcef.x, 0);
        Vector3D deltaVel = new Vector3D(ecefVel.x - gsVel.x, ecefVel.y - gsVel.y, ecefVel.z - gsVel.z);
        Vector3D enuVel = ecefToEnu(deltaVel, gsLatRad, gsLonRad, 0);

        return new Vector3D[]{enuPos, enuVel};
    }

    public static Vector3D enuToEcefOffset(Vector3D enu, double refLatRad, double refLonRad) {
        double sinLat = Math.sin(refLatRad), cosLat = Math.cos(refLatRad);
        double sinLon = Math.sin(refLonRad), cosLon = Math.cos(refLonRad);

        double dx = -sinLon * enu.x - cosLon * sinLat * enu.y + cosLon * cosLat * enu.z;
        double dy = cosLon * enu.x - sinLon * sinLat * enu.y + sinLon * cosLat * enu.z;
        double dz = cosLat * enu.y + sinLat * enu.z;

        return new Vector3D(dx, dy, dz);
    }

    // =======================
    // LLA convenience
    // =======================
    public record LlaState(double latRad, double lonRad, double altKm, Vector3D enuVelocity) {}

    /**
     * Convert an ECI position/velocity to latitude, longitude, altitude and ENU velocity.
     * All dynamics remain Earth-centric; this is strictly for presentation/output.
     *
     * @param eciPosKm     ECI position (km)
     * @param eciVelKmPerS ECI velocity (km/s)
     * @param timeMinutes  minutes since epoch (used for Earth rotation)
     * @return LlaState containing lat/lon (rad), altitude (km), and ENU velocity (km/s)
     */
    public static LlaState eciToLla(Vector3D eciPosKm, Vector3D eciVelKmPerS, double timeMinutes) {
        double tSeconds = timeMinutes * 60.0;
        Vector3D ecefPos = eciToEcef(eciPosKm, tSeconds);
        Vector3D ecefVel = eciToEcefVelocity(eciPosKm, eciVelKmPerS, tSeconds);

        double[] lla = ecefToGeodetic(ecefPos);
        double latRad = lla[0];
        double lonRad = normalizeLon(lla[1]);
        double altKm = lla[2];

        Vector3D enuVel = ecefVelocityToEnu(ecefVel, latRad, lonRad);
        return new LlaState(latRad, lonRad, altKm, enuVel);
    }

    /** Convert an ECEF velocity vector into ENU components at the given latitude/longitude. */
    public static Vector3D ecefVelocityToEnu(Vector3D ecefVel, double latRad, double lonRad) {
        double sinLat = Math.sin(latRad), cosLat = Math.cos(latRad);
        double sinLon = Math.sin(lonRad), cosLon = Math.cos(lonRad);

        double e = -sinLon * ecefVel.x + cosLon * ecefVel.y;
        double n = -cosLon * sinLat * ecefVel.x - sinLat * sinLon * ecefVel.y + cosLat * ecefVel.z;
        double u = cosLat * cosLon * ecefVel.x + cosLat * sinLon * ecefVel.y + sinLat * ecefVel.z;

        return new Vector3D(e, n, u);
    }

    private static double normalizeLon(double lonRad) {
        double twoPi = Math.PI * 2.0;
        double wrapped = (lonRad + Math.PI) % twoPi;
        if (wrapped < 0) wrapped += twoPi;
        return wrapped - Math.PI;
    }
}
