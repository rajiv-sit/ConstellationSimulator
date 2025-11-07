# Constellation Simulator

A Java/Gradle project that propagates and visualizes satellite constellations (LEO/MEO/GEO) with JavaFX rendering.

## Prerequisites
- JDK 25 or higher on `PATH` (build.gradle uses the Java 25 toolchain).
- Windows with JavaFX runtime pulled automatically from Maven.

## Build
```sh
./gradlew clean build
```

## Run examples
- Full constellation loop (multi-satellite):  
```sh
./gradlew runConstellationTest
```
- Simple generated constellation visualizer:  
```sh
./gradlew runOrbitalConstellationTest
```

If you see `java.lang.System::load` native-access warnings, add this JVM arg (already set in Gradle run tasks):  
`--enable-native-access=ALL-UNNAMED --enable-native-access=javafx.graphics`

## Notes
- 3D visualization depends on the bundled JavaFX modules and `libs/fxyz3d-0.6.0.jar`.
- Orekit data is pre-packaged under `app/src/main/resources/orekit-data/`; no download needed.
- Repository already includes the Gradle wrapper; no external Gradle install required.
