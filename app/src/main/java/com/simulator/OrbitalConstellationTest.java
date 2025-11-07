package com.simulator;

import com.simulator.math.*;
import com.simulator.generator.ConstellationGenerator;
import com.simulator.core.*;
import com.simulator.visualizer.OrbitVisualizer;

import java.util.List;

public class OrbitalConstellationTest {

    public static void main(String[] args) {
        // Example: LEO Constellation (6 planes × 10 satellites)
        ConstellationGenerator.ConstellationConfig config =
                new ConstellationGenerator.ConstellationConfig("LEO", 1, 1);

        // Generate the constellation
        Constellation constellation = ConstellationGenerator.generate(config);

        // Initialize and visualize
        OrbitVisualizer.initData(constellation);
        OrbitVisualizer.launch(OrbitVisualizer.class);
    }

}
