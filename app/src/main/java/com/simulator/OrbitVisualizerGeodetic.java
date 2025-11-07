package com.simulator;

import com.simulator.utils.*;
import com.simulator.core.*;
import com.simulator.math.Vector3D;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.PickResult;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * OrbitVisualizerGeodetic
 *
 * Interactive 3D visualization of satellites relative to a ground station using ENU coordinates.
 * All info output is shown in latitude, longitude, and altitude.
 */
public class OrbitVisualizerGeodetic extends Application {

    private static Constellation constellation;

    private static final Map<String, Sphere> satelliteNodes = new HashMap<>();
    private static final Map<String, Text> satelliteLabels = new HashMap<>();
    private static final Map<String, List<Sphere>> trajectorySpheres = new HashMap<>();
    private static final Map<String, Integer> trajectoryIndex = new HashMap<>();
    private static final Color[] COLORS = { Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.ORANGE, Color.PURPLE };
    private static final double SCALE = 0.05;          // km -> scene scale
    private static final int MAX_TRAJECTORY_POINTS = 360;

    private final AtomicInteger colorIndex = new AtomicInteger(0);
    private final Text infoDisplay = new Text();
    private final Text timeDisplay = new Text();
    private final String[] selectedSatelliteName = {null};

    private Group root;
    private PerspectiveCamera camera;
    private Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private Rotate earthRotate;

    private double mouseOldX, mouseOldY;
    private double cameraDistance = -1500;
    private static double cameraRotateX = 0;
    private static double cameraRotateY = 0;

    public static void initData(Constellation constel) {
        constellation = constel;
    }

    @Override
    public void start(Stage primaryStage) {
        if (constellation == null) {
            System.err.println("Constellation data not initialized!");
            return;
        }

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double screenWidth = screenBounds.getWidth();
        double screenHeight = screenBounds.getHeight();

        root = new Group();
        root.getTransforms().addAll(rotateY, rotateX);

        SubScene scene3D = new SubScene(root, screenWidth, screenHeight, true, SceneAntialiasing.BALANCED);
        scene3D.setFill(Color.BLACK);

        camera = new PerspectiveCamera(true);
        camera.setTranslateZ(cameraDistance);
        camera.setNearClip(0.1);
        camera.setFarClip(10000);
        scene3D.setCamera(camera);

        root.getChildren().add(new AmbientLight(Color.color(0.3, 0.3, 0.3)));
        PointLight pointLight = new PointLight(Color.WHITE);
        pointLight.setTranslateX(1000);
        pointLight.setTranslateY(-1000);
        pointLight.setTranslateZ(-1000);
        root.getChildren().add(pointLight);

        earthRotate = DrawUtilsGeodetic.addEarth(root, SCALE, "/textures/earth_combined.jpg");

        for (Satellite sat : constellation.getAllSatellites()) {
            DrawUtilsGeodetic.addSatelliteNode(root, sat,
                    satelliteNodes, satelliteLabels,
                    trajectorySpheres, trajectoryIndex,
                    COLORS, colorIndex, SCALE, MAX_TRAJECTORY_POINTS);

            DrawUtilsGeodetic.addTleOrbitLine(root, sat, 720, SCALE, Color.YELLOW, 2f);
        }

        Pane overlay = new Pane();
        overlay.setPickOnBounds(false);

        timeDisplay.setFont(Font.font(16));
        timeDisplay.setFill(Color.CYAN);
        overlay.getChildren().add(timeDisplay);
        timeDisplay.layoutXProperty().bind(overlay.widthProperty().divide(2).subtract(60));
        timeDisplay.setLayoutY(20);

        infoDisplay.setFont(Font.font(14));
        infoDisplay.setFill(Color.LIME);
        overlay.getChildren().add(infoDisplay);
        infoDisplay.layoutXProperty().bind(overlay.widthProperty().subtract(320));
        infoDisplay.setLayoutY(40);

        handleMouse(scene3D);
        setupPicking(scene3D);

        StackPane stack = new StackPane();
        stack.getChildren().addAll(scene3D, overlay);

        Scene scene = new Scene(stack, screenWidth, screenHeight);
        primaryStage.setTitle("3D Constellation Visualizer (Geodetic/ENU)");
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
        primaryStage.show();

        startAnimation();
    }

    private void handleMouse(SubScene scene) {
        scene.setOnScroll((ScrollEvent event) -> {
            cameraDistance += event.getDeltaY();
            camera.setTranslateZ(cameraDistance);
        });

        scene.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                mouseOldX = event.getSceneX();
                mouseOldY = event.getSceneY();
            }
        });

        scene.setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                double dx = event.getSceneX() - mouseOldX;
                double dy = event.getSceneY() - mouseOldY;
                cameraRotateY += dx * 0.5;
                cameraRotateX -= dy * 0.5;
                rotateY.setAngle(cameraRotateY);
                rotateX.setAngle(cameraRotateX);
                mouseOldX = event.getSceneX();
                mouseOldY = event.getSceneY();
            }
        });
    }

    private void setupPicking(SubScene scene3D) {
        scene3D.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                PickResult pick = event.getPickResult();
                Node node = pick.getIntersectedNode();
                selectedSatelliteName[0] = null;

                if (node instanceof Sphere) {
                    for (var entry : satelliteNodes.entrySet()) {
                        if (entry.getValue() == node) {
                            selectedSatelliteName[0] = entry.getKey();
                            break;
                        }
                    }
                }

                if (selectedSatelliteName[0] == null) {
                    infoDisplay.setText("");
                }
            }
        });
    }

    private void startAnimation() {
        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdate = 0;
            private double simTimeMinutes = 0.0;

            @Override
            public void handle(long now) {
                if (lastUpdate > 0) {
                    double deltaSeconds = (now - lastUpdate) / 1e9;
                    earthRotate.setAngle((earthRotate.getAngle() + deltaSeconds * 360 / 86400.0) % 360);

                    Map<String, Vector3D[]> states = constellation.propagateAllAtTime(simTimeMinutes);

                    for (var entry : states.entrySet()) {
                        String name = entry.getKey();
                        Vector3D pos = entry.getValue()[0];
                        Vector3D vel = entry.getValue()[1];

                        DrawUtilsGeodetic.updateSatellitePosition(name, pos, SCALE,
                                satelliteNodes, satelliteLabels, trajectorySpheres, trajectoryIndex);

                        // Inside startAnimation() → handle() method, the conversion now includes altitude
                        if (selectedSatelliteName[0] != null && selectedSatelliteName[0].equals(name)) {
                        // Convert ENU -> ECEF -> Geodetic (lat/lon/alt)
                            Vector3D ecef = CoordinateUtils.enuToEcef(pos,
                                            DrawUtilsGeodetic.gsLatRad,
                                            DrawUtilsGeodetic.gsLonRad,
                                            DrawUtilsGeodetic.gsAltKm); // altitude now properly included
                                                      
                                            double[] lla = CoordinateUtils.ecefToGeodetic(ecef); // iterative accurate conversion
                            double latDeg = Math.toDegrees(lla[0]);
                            double lonDeg = Math.toDegrees(lla[1]);
                            double altKm = lla[2]; // altitude in km

                            infoDisplay.setText(String.format("%s\nLat: %.6f°, Lon: %.6f°, Alt: %.2f km",
                                name, latDeg, lonDeg, altKm));

                            System.out.printf("%s\nLat: %.6f°, Lon: %.6f°, Alt: %.2f km%n",
                            name, latDeg, lonDeg, altKm);

                        }

                    }

                    String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                    timeDisplay.setText("RTO Time: " + currentTime);

                    simTimeMinutes += deltaSeconds / 60.0;
                }
                lastUpdate = now;
            }
        };
        timer.start();
    }

    public static void updatePositions(Map<String, Vector3D[]> states) {
        if (states == null || states.isEmpty()) return;

        Platform.runLater(() -> {
            for (var entry : states.entrySet()) {
                String name = entry.getKey();
                Vector3D pos = entry.getValue()[0];
                Vector3D vel = entry.getValue()[1];

                DrawUtilsGeodetic.updateSatellitePosition(name, pos, SCALE,
                        satelliteNodes, satelliteLabels, trajectorySpheres, trajectoryIndex);
            }
        });
    }
}
