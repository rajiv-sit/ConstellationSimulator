package com.simulator;

import com.simulator.core.*;
import com.simulator.math.Vector3D;
import com.simulator.propagator.Sgp4Propagator;
import com.simulator.propagator.Sdp4Propagator;
import com.simulator.tle.Tle;
import com.simulator.utils.CoordinateUtils;
import com.simulator.visualizer.OrbitVisualizer;

import javafx.application.Platform;
import java.util.*;

/**
 * Real-time Constellation Simulator with 3D visualization and Earth.
 * Enhanced: cumulative simulation time, thread-safe JavaFX updates, adjustable speed.
 */
public class ConstellationSimulatorTest {

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

        // === STEP 2: Build Satellite objects ===
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

        // === STEP 4: Initialize 3D visualizer ===
        OrbitVisualizer.initData(constellation);
        new Thread(() -> OrbitVisualizer.launch(OrbitVisualizer.class)).start();

        // === STEP 5: Real-time propagation loop ===
        System.out.println("=== Starting Real-Time Constellation Simulation ===");

        double simTimeMinutes = 0.0;           // cumulative simulation time
        long lastUpdateTime = System.currentTimeMillis();
        double simSpeed = 1.0;                 // 1x real time, can increase for faster simulation

        while (true) {
            try {
                long now = System.currentTimeMillis();
                double deltaMinutes = ((now - lastUpdateTime) / 60000.0) * simSpeed;
                lastUpdateTime = now;

                simTimeMinutes += deltaMinutes;

                Map<String, Vector3D[]> states = constellation.propagateAllAtTime(simTimeMinutes);

                // Thread-safe JavaFX update
                Platform.runLater(() -> OrbitVisualizer.updatePositions(states));

                // Print constellation info with units
                System.out.printf("%n=== Simulation Time: %.2f min | Delta: %.2f min ===%n",
                        simTimeMinutes, deltaMinutes);
                for (var entry : states.entrySet()) {
                    String name = entry.getKey();
                    Vector3D pos = entry.getValue()[0];
                    Vector3D vel = entry.getValue()[1];

                    CoordinateUtils.LlaState lla = CoordinateUtils.eciToLla(pos, vel, simTimeMinutes);
                    double latDeg = Math.toDegrees(lla.latRad());
                    double lonDeg = Math.toDegrees(lla.lonRad());
                    double altKm = lla.altKm();
                    Vector3D enuVel = lla.enuVelocity();
                    double speed = Math.sqrt(enuVel.x * enuVel.x + enuVel.y * enuVel.y + enuVel.z * enuVel.z);

                    System.out.printf(
                            "%-12s | Lat: %.4f deg | Lon: %.4f deg | Alt: %.1f km | Vel (E,N,U): [%.3f, %.3f, %.3f] km/s | Speed: %.3f km/s%n",
                            name, latDeg, lonDeg, altKm, enuVel.x, enuVel.y, enuVel.z, speed);
                }

                Thread.sleep(400); // update visualization every ~0.4 s

            } catch (InterruptedException e) {
                System.out.println("Simulation interrupted.");
                break;
            } catch (Exception e) {
                System.err.println("Propagation error: " + e.getMessage());
            }
        }
    }
}
