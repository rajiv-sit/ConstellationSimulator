package com.simulator.visualizer;

import com.simulator.core.Satellite;
import com.simulator.math.Vector3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import org.fxyz3d.shapes.composites.PolyLine3D;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DrawUtils
 *
 * Centralized drawing helper utilities for OrbitVisualizer.
 *
 * - Does not change your application structure; provides static helpers to:
 *   * create Earth sphere + rotation handle
 *   * create satellite nodes + trajectory buffers + labels
 *   * draw orbital lines (from precomputed points OR from a Satellite/TLE)
 *   * small helpers to update positions
 *
 */
public final class DrawUtils {

    private DrawUtils() { /* static-only */ }

    /**
     * Create and add an Earth sphere to the provided root group.
     * Returns the Rotate object attached to the Earth so the caller can animate it.
     *
     * @param root       scene root group
     * @param scale      scale factor (km -> scene units)
     * @param textureRes resource path to Earth texture (can be null)
     * @return Rotate object applied to the Earth (rotate about Y axis)
     */
    public static Rotate addEarth(Group root, double scale, String textureRes) {
        final double earthRadiusKm = 6371.0;
        Sphere earth = new Sphere(earthRadiusKm * scale);
        PhongMaterial mat = new PhongMaterial();

        if (textureRes != null) {
            try (InputStream is = DrawUtils.class.getResourceAsStream(textureRes)) {
                if (is != null) {
                    Image img = new Image(Objects.requireNonNull(is));
                    mat.setDiffuseMap(img);
                    mat.setSpecularColor(Color.LIGHTGRAY);
                } else {
                    // leave default material
                }
            } catch (Exception e) {
                System.err.println("DrawUtils: failed to load earth texture: " + e.getMessage());
            }
        }

        earth.setMaterial(mat);
        Rotate earthRotate = new Rotate(0, Rotate.Y_AXIS);
        earth.getTransforms().add(earthRotate);
        root.getChildren().add(earth);
        return earthRotate;
    }

    /**
     * Add a satellite node (Sphere), a label (Text), and pre-allocated trajectory markers to the scene.
     * Populates the provided maps so OrbitVisualizer can reference them directly.
     *
     * @param root               scene root
     * @param sat                Satellite object (for name, tle, etc.)
     * @param satelliteNodes     map name->Sphere (modified)
     * @param satelliteLabels    map name->Text (modified)
     * @param trajectorySpheres  map name->List<Sphere> (modified)
     * @param trajectoryIndex    map name->Integer index (modified)
     * @param colors             color palette array
     * @param colorIndex         AtomicInteger holding current color index (incremented)
     * @param scale              world scale (km -> scene)
     * @param maxTrajectoryPoints number of small spheres to use for the trajectory circular buffer
     */
    public static void addSatelliteNode(
            Group root,
            Satellite sat,
            Map<String, Sphere> satelliteNodes,
            Map<String, Text> satelliteLabels,
            Map<String, List<Sphere>> trajectorySpheres,
            Map<String, Integer> trajectoryIndex,
            Color[] colors,
            AtomicInteger colorIndex,
            double scale,
            int maxTrajectoryPoints
    ) {
        double satRadius = 5.0; // scene units (can be tuned or exposed)
        Sphere sphere = new Sphere(satRadius);
        Color color = colors[colorIndex.getAndIncrement() % colors.length];
        PhongMaterial material = new PhongMaterial(color);
        sphere.setMaterial(material);

        satelliteNodes.put(sat.getName(), sphere);
        root.getChildren().add(sphere);

        // Create trajectory
        List<Sphere> trajList = new java.util.ArrayList<>(maxTrajectoryPoints);
        trajectorySpheres.put(sat.getName(), trajList);
        trajectoryIndex.put(sat.getName(), 0);

        // Label
        Text label = new Text(sat.getName());
        label.setFont(Font.font(8));
        label.setFill(Color.WHITE);
        // rotate label upright relative to scene (optional)
        label.getTransforms().add(new Rotate(-90, Rotate.Y_AXIS));
        satelliteLabels.put(sat.getName(), label);
        root.getChildren().add(label);
    }

    /**
     * Add a PolyLine3D orbit line computed from a list of Vector3D orbit points (ECI).
     *
     * @param root       scene root
     * @param orbitPoints list of Vector3D points in km (ECI)
     * @param scale      scale factor (km->scene)
     * @param color      line color
     * @param width      line width (float)
     */
    public static void addOrbitLine(Group root, List<Vector3D> orbitPoints, double scale, Color color, float width) {
        List<org.fxyz3d.geometry.Point3D> pts = new java.util.ArrayList<>(orbitPoints.size());
        for (Vector3D v : orbitPoints) {
            pts.add(new org.fxyz3d.geometry.Point3D((float)(v.x * scale), (float)(v.y * scale), (float)(v.z * scale)));
        }
        PolyLine3D line = new PolyLine3D(pts, width, color, PolyLine3D.LineType.RIBBON);
        root.getChildren().add(line);
    }

    /**
     * Build an orbit line by sampling the satellite's propagator (TLE/Sgp4). Samples one period.
     *
     * @param root   scene root
     * @param sat    satellite to sample (must have valid propagator)
     * @param points number of sample points around orbit (recommended 180..720)
     * @param scale  scale factor (km->scene)
     * @param color  color of line
     * @param width  width (float)
     */
    public static void addTleOrbitLine(Group root, Satellite sat, int points, double scale, Color color, float width) {
        List<org.fxyz3d.geometry.Point3D> pts = new java.util.ArrayList<>(points);
        // meanMotion is rev/day. Convert to minutes per revolution:
        double mm = sat.getTle().meanMotion;
        if (mm <= 0) mm = 1.0; // fallback
        double periodMinutes = 1440.0 / mm;
        for (int i = 0; i < points; i++) {
            // sample times across one period
            double t = (i * periodMinutes) / points;
            Vector3D[] st = sat.propagateState(t);
            Vector3D p = st[0];
            pts.add(new org.fxyz3d.geometry.Point3D((float)(p.x * scale), (float)(p.y * scale), (float)(p.z * scale)));
        }
        PolyLine3D line = new PolyLine3D(pts, width, color, PolyLine3D.LineType.RIBBON);
        root.getChildren().add(line);
    }

    /**
     * Update a single satellite's node + label position from an ECI Vector3D (km).
     *
     * @param name           satellite name (key into maps)
     * @param posKm          position in km (ECI)
     * @param scale          scale (km->scene)
     * @param satelliteNodes map name->Sphere
     * @param satelliteLabels map name->Text
     * @param trajectorySpheres map name->List<Sphere> (optional, may be null)
     * @param trajectoryIndex map name->Integer index (optional, may be null)
     */
    public static void updateSatellitePosition(
            String name,
            Vector3D posKm,
            double scale,
            Map<String, Sphere> satelliteNodes,
            Map<String, Text> satelliteLabels,
            Map<String, List<Sphere>> trajectorySpheres,
            Map<String, Integer> trajectoryIndex
    ) {
        Sphere s = satelliteNodes.get(name);
        if (s != null) {
            s.setTranslateX(posKm.x * scale);
            s.setTranslateY(posKm.y * scale);
            s.setTranslateZ(posKm.z * scale);
        }
        Text lbl = satelliteLabels.get(name);
        if (lbl != null) {
            lbl.setTranslateX(posKm.x * scale);
            lbl.setTranslateY(posKm.y * scale + 15); // offset label slightly above
            lbl.setTranslateZ(posKm.z * scale);
        }

        if (trajectorySpheres != null && trajectoryIndex != null) {
            List<Sphere> traj = trajectorySpheres.get(name);
            if (traj != null && !traj.isEmpty()) {
                int idx = trajectoryIndex.getOrDefault(name, 0);
                Sphere p = traj.get(idx);
                p.setTranslateX(posKm.x * scale);
                p.setTranslateY(posKm.y * scale);
                p.setTranslateZ(posKm.z * scale);
                p.setVisible(true);
                trajectoryIndex.put(name, (idx + 1) % traj.size());
            }
        }
    }

    /**
     * Utility: compute a circular-orbit position in ECI coordinates (km).
     * This is the same formula used in OrbitVisualizer; provided here in case you want
     * the drawing helpers to compute things too.
     *
     * @param radiusKm       orbit radius (Earth radius + altitude)
     * @param inclinationDeg orbit inclination (deg)
     * @param raanDeg        RAAN (deg)
     * @param trueAnomalyDeg true anomaly (deg)
     * @return Vector3D ECI position (km)
     */
    public static Vector3D computeOrbitPosition(double radiusKm, double inclinationDeg, double raanDeg, double trueAnomalyDeg) {
        double inc = Math.toRadians(inclinationDeg);
        double raan = Math.toRadians(raanDeg);
        double theta = Math.toRadians(trueAnomalyDeg);

        // Relative coordinates in orbital plane (perifocal with argument of perigee = 0)
        double xOrb = radiusKm * Math.cos(theta);
        double yOrb = radiusKm * Math.sin(theta);
        double zOrb = 0.0;

        // Rotate by inclination about X-axis
        double xInc = xOrb;
        double yInc = yOrb * Math.cos(inc) - zOrb * Math.sin(inc);
        double zInc = yOrb * Math.sin(inc) + zOrb * Math.cos(inc);

        // Rotate by RAAN about Z-axis to ECI
        double xEci = xInc * Math.cos(raan) - yInc * Math.sin(raan);
        double yEci = xInc * Math.sin(raan) + yInc * Math.cos(raan);
        double zEci = zInc;

        return new Vector3D(xEci, yEci, zEci);
    }
}
