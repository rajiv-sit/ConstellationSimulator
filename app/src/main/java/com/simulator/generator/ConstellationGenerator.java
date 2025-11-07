
package com.simulator.generator;

import com.simulator.math.Vector3D;
import com.simulator.propagator.OrbitPropagator;
import com.simulator.propagator.Sgp4Propagator;
import com.simulator.tle.Tle;
import com.simulator.core.Constellation;
import com.simulator.core.Satellite;

import java.util.*;

/**
 * Procedurally generates realistic satellite constellations.
 * 
 * Features:
 *  - Even satellite spacing along each orbital plane.
 *  - Configurable number of orbits and satellites per orbit.
 *  - Supports LEO, MEO, and GEO regimes.
 *  - Provides orbit geometry for visualization (full path lines).
 */
public class ConstellationGenerator {

    /** Configuration for a satellite constellation */
    public static class ConstellationConfig {
        public final String orbitType; // "LEO", "MEO", or "GEO"
        public final int numOrbits;
        public final int satsPerOrbit;

        public ConstellationConfig(String orbitType, int numOrbits, int satsPerOrbit) {
            this.orbitType = orbitType.toUpperCase(Locale.ROOT);
            this.numOrbits = numOrbits;
            this.satsPerOrbit = satsPerOrbit;
        }
    }

    /**
     * Main method: generates a complete constellation with orbits and satellites.
     *
     * @param config configuration defining orbit type and counts
     * @return a Constellation instance populated with all satellites
     */
    public static Constellation generate(ConstellationConfig config) {
        // --- Step 1: Determine orbit parameters based on type ---
        double altitudeKm;
        double inclinationDeg;
        double meanMotion; // revolutions per day

        switch (config.orbitType) {
            case "LEO" -> {
                altitudeKm = 1400;
                inclinationDeg = 53;
                meanMotion = 15.5;
            }
            case "MEO" -> {
                altitudeKm = 20200;
                inclinationDeg = 55;
                meanMotion = 2.0;
            }
            case "GEO" -> {
                altitudeKm = 35786;
                inclinationDeg = 0;
                meanMotion = 1.0;
            }
            default -> throw new IllegalArgumentException("Unknown orbit type: " + config.orbitType);
        }

        // --- Step 2: Initialize geometry constants ---
        double earthRadiusKm = 6371.0;
        double orbitRadiusKm = earthRadiusKm + altitudeKm;

        Constellation constellation = new Constellation(config.orbitType + "_Constellation");

        double raanSpacingDeg = 360.0 / config.numOrbits;
        double satSpacingDeg = 360.0 / config.satsPerOrbit;

        Random rand = new Random(42);

        // --- Step 3: Generate orbits ---
        for (int orbitIdx = 0; orbitIdx < config.numOrbits; orbitIdx++) {
            double raanDeg = orbitIdx * raanSpacingDeg;

            // Create full orbital line for visualization
            List<Vector3D> orbitLine = generateOrbitLine(orbitRadiusKm, inclinationDeg, raanDeg, 720);

            int tleBaseId = rand.nextInt(90000) + 10000;

            // --- Step 4: Generate satellites along this orbit ---
            for (int satIdx = 0; satIdx < config.satsPerOrbit; satIdx++) {
                double anomalyDeg = satIdx * satSpacingDeg;

                Vector3D initialPos = computeOrbitPosition(orbitRadiusKm, inclinationDeg, raanDeg, anomalyDeg);

                String satName = String.format("%s-%02d-%02d", config.orbitType, orbitIdx + 1, satIdx + 1);

                // ================================
                // Construct realistic TLE for propagation
                // ================================
                double eccentricity = 0.0001 + rand.nextDouble() * 0.001;  // near-circular
                double argPerigeeDeg = rand.nextDouble() * 360.0;          // argument of perigee
                double meanAnomalyDeg = anomalyDeg;                        // initial mean anomaly
                double meanMotionRevsPerDay = meanMotion;

                String line1 = String.format(Locale.US,
                        "1 %05dU 25000A   25001.00000000  .00000000  00000-0  00000-0 0 0000",
                        tleBaseId + satIdx);

                // Eccentricity must be 7-digit without decimal point
                int eccInt = (int) (eccentricity * 1e7);

                String line2 = String.format(Locale.US,
                        "2 %05d %8.4f %8.4f %07d %8.4f %8.4f %11.8f",
                        tleBaseId + satIdx,
                        inclinationDeg,      // Inclination [deg]
                        raanDeg,             // RAAN [deg]
                        eccInt,              // Eccentricity (no decimal point)
                        argPerigeeDeg,       // Argument of perigee [deg]
                        meanAnomalyDeg,      // Mean anomaly [deg]
                        meanMotionRevsPerDay); // Mean motion [rev/day]

                // --- Step 5: Build satellite ---
                Tle tle = Tle.fromTwoLineElement(satName, line1, line2);
                OrbitPropagator propagator = new Sgp4Propagator(tle);

                Satellite satellite = new Satellite.Builder()
                        .name(satName)
                        .tle(tle)
                        .propagator(propagator)
                        .type(config.orbitType)
                        .initialPosition(initialPos)
                        .orbitPoints(orbitLine)
                        .build();

                constellation.addSatellite(satellite);
            }
        }

        return constellation;
    }

    // ================================================================
    // Helper Methods
    // ================================================================

    private static Vector3D computeOrbitPosition(double radiusKm, double inclinationDeg, double raanDeg, double trueAnomalyDeg) {
        double inc = Math.toRadians(inclinationDeg);
        double raan = Math.toRadians(raanDeg);
        double theta = Math.toRadians(trueAnomalyDeg);

        double xOrb = radiusKm * Math.cos(theta);
        double yOrb = radiusKm * Math.sin(theta);
        double zOrb = 0.0;

        double xInc = xOrb;
        double yInc = yOrb * Math.cos(inc) - zOrb * Math.sin(inc);
        double zInc = yOrb * Math.sin(inc) + zOrb * Math.cos(inc);

        double xEci = xInc * Math.cos(raan) - yInc * Math.sin(raan);
        double yEci = xInc * Math.sin(raan) + yInc * Math.cos(raan);
        double zEci = zInc;

        return new Vector3D(xEci, yEci, zEci);
    }

    private static List<Vector3D> generateOrbitLine(double radiusKm, double inclinationDeg, double raanDeg, int numPoints) {
        List<Vector3D> points = new ArrayList<>(numPoints);
        for (int i = 0; i < numPoints; i++) {
            double angleDeg = (i * 360.0) / numPoints;
            points.add(computeOrbitPosition(radiusKm, inclinationDeg, raanDeg, angleDeg));
        }
        return points;
    }
}
