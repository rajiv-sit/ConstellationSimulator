package com.simulator;

import com.simulator.utils.*;
import com.simulator.core.*;
import com.simulator.math.Vector3D;
import com.simulator.propagator.Sgp4Propagator;
import com.simulator.propagator.Sdp4Propagator;
import com.simulator.tle.Tle;

import java.util.*;

/**
 * Real-time Constellation Simulator with ENU Geodetic visualization.
 * Positions and velocities are printed in latitude, longitude, altitude,
 * and all satellite positions are visualized relative to a reference ground station.
 */
public class ConstellationSimulatorGeodeticTest {

    public static void main(String[] args) {

        // === STEP 1: Define TLEs ===
        List<Tle> tles = new ArrayList<>();
        try {
            tles.add(Tle.fromTwoLineElement(
                    "VANGUARD 1",
                    "00005U 58002B   58003.68785767  .00000090  00000-0  13807-3 0  2810",
                    "1 00005  34.0000  00.0000 0000001  00.0000  00.0000 10.84400000"));

            tles.add(Tle.fromTwoLineElement(
                    "GEO SAT",
                    "12345U 99001A   99003.68785767  .00000090  00000-0  12345-3 0  0001",
                    "2 12345   0.0000   0.0000 0000001   0.0000   0.0000  1.00000000"));

            tles.add(Tle.fromTwoLineElement(
                    "LEO SAT 1",
                    "54321U 21001A   21003.68785767  .00000123  00000-0  54321-3 0  1234",
                    "3 54321  50.0000  45.0000 0001234  10.0000 200.0000 12.00000000"));

            tles.add(Tle.fromTwoLineElement(
                    "LEO SAT 2",
                    "67890U 22001A   22003.68785767  .00000156  00000-0  67890-3 0  5678",
                    "4 67890  60.0000  90.0000 0001567  20.0000 220.0000 14.00000000"));

            tles.add(Tle.fromTwoLineElement(
                    "GEO SAT 2",
                    "98765U 23001A   23003.68785767  .00000085  00000-0  98765-3 0  9876",
                    "5 98765   0.0000 180.0000 0000000   0.0000   0.0000  1.50000000"));

        } catch (IllegalArgumentException e) {
            System.err.println("Error parsing TLEs: " + e.getMessage());
            return;
        }

        // === STEP 2: Build Satellite objects with proper propagators ===
        List<Satellite> satellites = new ArrayList<>();
        for (Tle tle : tles) {
            double altKm = tle.semiMajorAxis - 6371; // approximate altitude
            boolean isDeepSpace = altKm > 20000;     // GEO/MEO threshold

            Satellite sat = new Satellite.Builder()
                    .name(tle.name)
                    .tle(tle)
                    .propagator(isDeepSpace ? new Sdp4Propagator(tle) : new Sgp4Propagator(tle))
                    .type(isDeepSpace ? "GEO/MEO" : "LEO")
                    .build();

            satellites.add(sat);
        }

        // === STEP 3: Create constellation ===
        Constellation constellation = new Constellation(satellites);

        // === STEP 4: Set reference ground station (Geodetic) ===
        DrawUtilsGeodetic.gsLatRad = Math.toRadians(43.65107); // Toronto
        DrawUtilsGeodetic.gsLonRad = Math.toRadians(-79.347015);
        DrawUtilsGeodetic.gsAltKm = 0.0;

        // Precompute ground station ECEF once
        Vector3D gsEcef = CoordinateUtils.geodeticToEcef(
                DrawUtilsGeodetic.gsLatRad,
                DrawUtilsGeodetic.gsLonRad,
                DrawUtilsGeodetic.gsAltKm
        );

        // === STEP 5: Initialize 3D geodetic visualizer ===
        OrbitVisualizerGeodetic.initData(constellation);
        new Thread(() -> OrbitVisualizerGeodetic.launch(OrbitVisualizerGeodetic.class)).start();

        // === STEP 6: Real-time propagation loop ===
        System.out.println("=== Starting Real-Time Geodetic Constellation Simulation ===");

        long lastUpdateTime = System.currentTimeMillis();

        while (true) {
            try {
                long now = System.currentTimeMillis();
                double deltaMinutes = (now - lastUpdateTime) / 60000.0;
                lastUpdateTime = now;

                Map<String, Vector3D[]> states = constellation.propagateAllAtTime(deltaMinutes);
                OrbitVisualizerGeodetic.updatePositions(states);

                // Print constellation info in latitude, longitude, altitude
                System.out.printf("%n--- Time delta: %.2f min --- %n", deltaMinutes);
                for (var entry : states.entrySet()) {
                    String name = entry.getKey();
                    Vector3D enuPos = entry.getValue()[0];
                    Vector3D vel = entry.getValue()[1];

                    // Proper ENU -> ECEF relative to ground station
                    Vector3D satEcef = CoordinateUtils.enuToEcefOffset(
                            enuPos,
                            DrawUtilsGeodetic.gsLatRad,
                            DrawUtilsGeodetic.gsLonRad
                    ).add(gsEcef);

                    double[] lla = CoordinateUtils.ecefToGeodetic(satEcef);
                    double latDeg = Math.toDegrees(lla[0]);
                    double lonDeg = Math.toDegrees(lla[1]);
                    double altKm = lla[2];

                    System.out.printf("%-12s | Lat: %.6f°, Lon: %.6f°, Alt: %.2f km | Vel: [%.3f km/s, %.3f km/s, %.3f km/s]%n",
                            name, latDeg, lonDeg, altKm, vel.x, vel.y, vel.z);
                }

                Thread.sleep(400); // update ~0.4 s

            } catch (InterruptedException e) {
                System.out.println("Simulation interrupted.");
                break;
            } catch (Exception e) {
                System.err.println("Propagation error: " + e.getMessage());
            }
        }
    }
}
