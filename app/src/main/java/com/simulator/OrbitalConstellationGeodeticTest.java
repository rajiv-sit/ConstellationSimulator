package com.simulator;

import com.simulator.generator.ConstellationGenerator;
import com.simulator.core.*;
import com.simulator.math.Vector3D;
import com.simulator.utils.CoordinateUtils;

/**
 * OrbitalConstellationGeodeticTest
 *
 * Generates a sample LEO constellation and visualizes it in 3D
 * using geodetic ENU coordinates relative to a ground station.
 */
public class OrbitalConstellationGeodeticTest {

    public static void main(String[] args) {

        // === STEP 1: Generate LEO Constellation (example: 11 planes × 13 satellites) ===
        ConstellationGenerator.ConstellationConfig config =
                new ConstellationGenerator.ConstellationConfig("LEO", 1, 1);

        Constellation constellation = ConstellationGenerator.generate(config);

        // === STEP 2: Set reference ground station for ENU/geodetic visualization ===
        DrawUtilsGeodetic.gsLatRad = Math.toRadians(0.0); // Example: Greenwich, on the equator
        DrawUtilsGeodetic.gsLonRad = Math.toRadians(0.0);
        DrawUtilsGeodetic.gsAltKm = 0.0;

        // === STEP 4: Initialize 3D Geodetic Visualizer ===
        OrbitVisualizerGeodetic.initData(constellation);
        OrbitVisualizerGeodetic.launch(OrbitVisualizerGeodetic.class);
    }

}
