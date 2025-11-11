# Constellation Simulator 🛰️
A Java-based simulation tool to visualize and test LEO satellite constellations. This project includes a 3D visualization component and uses Gradle as the build system. The simulator allows users to model LEO satellite constellations with orbital propagation, visualize satellite trajectories in 3D, track user terminals, simulate communication metrics, and observe results through an integrated visualizer. Built with Java 21 (JDK), Gradle 8.x, VS Code, and optionally fxyz3d-0.6.0.jar for 3D visualization.

## Prerequisites
Ensure your system meets the following requirements: Windows 10 or newer, minimum 8 GB RAM, and internet access to download dependencies.

## Initial Setup

### 1️⃣ Install Java 21 (JDK)
**Option A – Oracle JDK (Recommended):** Go to [Oracle Java Downloads](https://www.oracle.com/java/technologies/downloads/), under Java SE 21 (LTS) choose Windows x64 Installer (.msi), run the installer and accept defaults. Default installation path: `C:\Program Files\Java\jdk-21`. Verify installation: `java -version`. Expected output: `java version "21.x.x"` and `Java(TM) SE Runtime Environment`.

**Option B – Open-source (Temurin):** Go to [Adoptium Temurin 21](https://adoptium.net/temurin/releases/?version=21), download MSI Installer for Windows x64, run installer and accept defaults, verify installation as above.

### 2️⃣ Install Gradle (Build System)
**Option 1 – via Scoop (Easy):** `scoop install gradle`.

**Option 2 – Manual Setup:** Go to [Gradle Releases](https://gradle.org/releases/), download binary-only ZIP, extract to `C:\Gradle\gradle-8.x`. Add to PATH: Press Win+R → type `sysdm.cpl` → Advanced → Environment Variables → add system variable `GRADLE_HOME` = `C:\Gradle\gradle-8.x`. Edit Path → Add `%GRADLE_HOME%\bin`. Verify installation: `gradle -v`.

### 3️⃣ Install VS Code
Download from [Visual Studio Code](https://code.visualstudio.com/). During setup, check “Add Open with Code to context menu” and “Add VS Code to PATH”.

### 4️⃣ Install VS Code Extensions
Launch VS Code → Ctrl+Shift+X → Install the following extensions: Language Support for Java (`redhat.java`) – Required, Debugger for Java (`vscjava.vscode-java-debug`) – Required, Test Runner for Java (`vscjava.vscode-java-test`) – Required, Project Manager for Java (`vscjava.vscode-java-dependency`) – Required, Gradle Tasks (`richardwillis.vscode-gradle`) – Optional, Lombok Annotations (`gabrielbb.vscode-lombok`) – Optional.

### 5️⃣ Optional: fxyz3d Library
For 3D visualization, download `fxyz3d-0.6.0.jar`, place it in the project folder (e.g., `/libs`), and add it to Gradle dependencies:
```gradle
dependencies {
    implementation files('libs/fxyz3d-0.6.0.jar')
}
```

Build & Run

Open VS Code in the project folder, refresh Gradle projects, and run simulation: gradle run. If fxyz3d-0.6.0.jar is included, satellites and trajectories will be visualized in 3D.

Verify Setup

java -version → should show 21.x.x

gradle -v → should show Gradle 8.x

VS Code → Java extensions installed

Optional visualizer works if fxyz3d-0.6.0.jar is added

Notes

Ensure environment variables for Java and Gradle are set correctly.

Include fxyz3d-0.6.0.jar in dependencies to enable 3D visualization.

For Gradle manual setup, confirm %GRADLE_HOME%\bin is in Path.

## Running Tests

You can run the Constellation Simulator tests using the provided batch file `build-and-run-constellation-simulator-test.bat`. This batch file will build the project using Gradle, run the selected test, and log the output to `run_output.log`.

### Steps to Run

1. Open **Command Prompt** in the project folder.
2. Execute the batch file:  
```cmd
build-and-run-constellation-simulator-test.bat
```

3. You will be prompted to select which test to run:
   Select which test to run:
  [1] ConstellationSimulatorTest
  [2] OrbitalConstellationTest
Enter your choice (1 or 2):
1: ConstellationSimulatorTest – runs a simulation using the main Constellation Simulator scenario.

2: OrbitalConstellationTest – runs orbital constellation scenarios for testing specific orbital configurations.

4. The batch file will first clean and build the project using Gradle. Build output is logged to run_output.log.

5. After building, it will launch the selected test, appending all output to run_output.log.

6. Once the test completes, you can open run_output.log to review the simulation output or debug information.

Notes

Ensure Java 21 and Gradle are installed and configured correctly in your system PATH.

The batch file detects the OS automatically and supports Windows or Unix-like environments (with minor adjustments for Unix shells).

If the build fails, check run_output.log for detailed error messages.

You can re-run the batch file any time to run a different test scenario.

Output:

<img width="1904" height="1007" alt="image" src="https://github.com/user-attachments/assets/ecbafd06-1f73-4ff7-8838-9a7fd0a62371" />
