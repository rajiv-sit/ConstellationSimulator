package com.simulator;

import com.simulator.core.Satellite;
import com.simulator.math.Vector3D;
import com.simulator.utils.CoordinateUtils;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import org.fxyz3d.shapes.composites.PolyLine3D;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DrawUtilsGeodetic
 *
 * Same API as DrawUtils, but positions are converted to **ENU/geodetic coordinates**
 * relative to a reference ground station.
 */
public final class DrawUtilsGeodetic {

    private DrawUtilsGeodetic() {}

    // Ground station reference (radians, km)
    public static double gsLatRad = 0;
    public static double gsLonRad = 0;
    public static double gsAltKm = 0;

    //==================== Earth ====================
    public static Rotate addEarth(Group root, double scale, String textureRes) {
        final double earthRadiusKm = 6371.0;
        Sphere earth = new Sphere(earthRadiusKm * scale);
        PhongMaterial mat = new PhongMaterial();

        if (textureRes != null) {
            try (InputStream is = DrawUtilsGeodetic.class.getResourceAsStream(textureRes)) {
                if (is != null) mat.setDiffuseMap(new Image(is));
            } catch (Exception e) {
                System.err.println("Failed to load earth texture: " + e.getMessage());
            }
        }

        earth.setMaterial(mat);
        Rotate earthRotate = new Rotate(0, Rotate.Y_AXIS);
        earth.getTransforms().add(earthRotate);
        root.getChildren().add(earth);
        return earthRotate;
    }

    //==================== Satellites ====================
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
        double satRadius = 5.0;
        Sphere sphere = new Sphere(satRadius);
        Color color = colors[colorIndex.getAndIncrement() % colors.length];
        sphere.setMaterial(new PhongMaterial(color));

        satelliteNodes.put(sat.getName(), sphere);
        root.getChildren().add(sphere);

        List<Sphere> trajList = new ArrayList<>(maxTrajectoryPoints);
        trajectorySpheres.put(sat.getName(), trajList);
        trajectoryIndex.put(sat.getName(), 0);

        Text label = new Text(sat.getName());
        label.setFont(Font.font(8));
        label.setFill(Color.WHITE);
        label.getTransforms().add(new Rotate(-90, Rotate.Y_AXIS));
        satelliteLabels.put(sat.getName(), label);
        root.getChildren().add(label);
    }

    //==================== Orbit lines ====================
    public static void addOrbitLine(Group root, List<Vector3D> orbitPoints, double scale, Color color, float width) {
        List<org.fxyz3d.geometry.Point3D> pts = new ArrayList<>(orbitPoints.size());
        for (Vector3D v : orbitPoints) {
            // v must be ECEF (km). Convert to ENU for visualization.
            Vector3D enu = CoordinateUtils.ecefToEnu(v, gsLatRad, gsLonRad, gsAltKm);
            pts.add(new org.fxyz3d.geometry.Point3D((float)(enu.x*scale), (float)(enu.y*scale), (float)(enu.z*scale)));
        }
        PolyLine3D line = new PolyLine3D(pts, width, color, PolyLine3D.LineType.RIBBON);
        root.getChildren().add(line);
    }

    /**
     * Draw TLE orbit line by sampling the propagated orbit.
     * Important: sat.propagateState(t) MUST return position in ECEF (km).
     * If it returns ECI, you MUST convert to ECEF first (see comment inside).
     */
    public static void addTleOrbitLine(Group root, Satellite sat, int points, double scale, Color color, float width) {
        List<org.fxyz3d.geometry.Point3D> pts = new ArrayList<>(points);
        double mm = sat.getTle().meanMotion;
        double periodMinutes = mm > 0 ? 1440.0/mm : 60.0;

        for (int i = 0; i < points; i++) {
            double t = i * periodMinutes / points;

            // propagateState(t)[0] assumed to return ECEF position in km.
            // If your propagateState returns ECI, convert ECI -> ECEF here using a proper
            // Earth rotation / GMST calculation (not provided here).
            Vector3D pos = sat.propagateState(t)[0]; // treat as ECEF
            Vector3D enu = CoordinateUtils.ecefToEnu(pos, gsLatRad, gsLonRad, gsAltKm);
            pts.add(new org.fxyz3d.geometry.Point3D((float)(enu.x*scale), (float)(enu.y*scale), (float)(enu.z*scale)));
        }
        PolyLine3D line = new PolyLine3D(pts, width, color, PolyLine3D.LineType.RIBBON);
        root.getChildren().add(line);
    }

    //==================== Update satellite ====================
    public static void updateSatellitePosition(
            String name,
            Vector3D posEcef,
            double scale,
            Map<String, Sphere> satelliteNodes,
            Map<String, Text> satelliteLabels,
            Map<String, List<Sphere>> trajectorySpheres,
            Map<String, Integer> trajectoryIndex
    ) {
        // posEcef must be in ECEF (km).
        Vector3D enu = CoordinateUtils.ecefToEnu(posEcef, gsLatRad, gsLonRad, gsAltKm);

        Sphere s = satelliteNodes.get(name);
        if (s != null) {
            s.setTranslateX(enu.x * scale);
            s.setTranslateY(enu.y * scale);
            s.setTranslateZ(enu.z * scale);
        }

        Text lbl = satelliteLabels.get(name);
        if (lbl != null) {
            lbl.setTranslateX(enu.x * scale);
            lbl.setTranslateY(enu.y * scale + 15);
            lbl.setTranslateZ(enu.z * scale);
        }

        if (trajectorySpheres != null && trajectoryIndex != null) {
            List<Sphere> traj = trajectorySpheres.get(name);
            if (traj != null && !traj.isEmpty()) {
                int idx = trajectoryIndex.getOrDefault(name, 0);
                Sphere p = traj.get(idx);
                p.setTranslateX(enu.x * scale);
                p.setTranslateY(enu.y * scale);
                p.setTranslateZ(enu.z * scale);
                p.setVisible(true);
                trajectoryIndex.put(name, (idx + 1) % traj.size());
            }
        }
    }

    //==================== Circular orbit helper (returns ENU) ====================
    /**
     * Compute a circular orbit sample point and return its ENU vector relative to GS.
     *
     * Notes:
     *  - This computes a simple position in the orbital plane (ECI-like coordinates).
     *  - If you need accurate ECEF positions, you must rotate ECI -> ECEF using GMST/epoch.
     */
    public static Vector3D computeOrbitPosition(double radiusKm, double inclinationDeg, double raanDeg, double trueAnomalyDeg) {
        // Compute ECI-like position in orbital plane (perifocal -> ECI without argPerigee)
        double inc = Math.toRadians(inclinationDeg);
        double raan = Math.toRadians(raanDeg);
        double theta = Math.toRadians(trueAnomalyDeg);

        double xOrb = radiusKm * Math.cos(theta);
        double yOrb = radiusKm * Math.sin(theta);
        double zOrb = 0.0;

        // rotate by inclination (about x-axis) to get into ECI
        double xInc = xOrb;
        double yInc = yOrb * Math.cos(inc) - zOrb * Math.sin(inc);
        double zInc = yOrb * Math.sin(inc) + zOrb * Math.cos(inc);

        // rotate by RAAN (about z-axis)
        double xEci = xInc * Math.cos(raan) - yInc * Math.sin(raan);
        double yEci = xInc * Math.sin(raan) + yInc * Math.cos(raan);
        double zEci = zInc;

        Vector3D posEci = new Vector3D(xEci, yEci, zEci);

        // --- IMPORTANT ---
        // At this point posEci is *ECI-like*. If your visualization pipeline expects ECEF,
        // you MUST convert ECI -> ECEF using Earth rotation angle (GMST) at the epoch/time
        // of this sample. Without that conversion the lat/lon/alt will be wrong.
        //
        // If you do NOT have a proper ECI->ECEF conversion handy and want a quick
        // visualization (assuming epoch where ECI == ECEF), fall back to identity:
        Vector3D posEcef = posEci; // <<-- replace with real ECI->ECEF conversion if needed

        // Convert to ENU relative to GS
        return CoordinateUtils.ecefToEnu(posEcef, gsLatRad, gsLonRad, gsAltKm);
    }
}
