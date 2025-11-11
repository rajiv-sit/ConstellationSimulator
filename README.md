# Constellation Simulator 🛰️

A Java-based simulation tool to visualize and test LEO satellite constellations.  
This project includes a 3D visualization component and uses Gradle as the build system.

---

## 📖 Table of Contents

1. [Project Overview](#project-overview)  
2. [Prerequisites](#prerequisites)  
3. [Initial Setup](#initial-setup)  
   - [1️⃣ Install Java 21 (JDK)](#1️⃣-install-java-21-jdk)  
   - [2️⃣ Install Gradle (Build System)](#2️⃣-install-gradle-build-system)  
   - [3️⃣ Install VS Code](#3️⃣-install-vs-code)  
   - [4️⃣ Install VS Code Extensions](#4️⃣-install-vs-code-extensions)  
   - [5️⃣ Optional: fxyz3d Library](#5️⃣-optional-fxyz3d-library)  
4. [Build & Run](#build--run)  
5. [Verify Setup](#verify-setup)  
6. [License](#license)  

---

## Project Overview

The Constellation Simulator allows users to:

- Model LEO satellite constellations with orbital propagation  
- Visualize satellite trajectories in 3D  
- Track user terminals and simulate communication metrics  
- Observe results through an integrated visualizer  

Built with:

- **Java 21 (JDK)**  
- **Gradle 8.x**  
- **VS Code** (recommended IDE)  
- **fxyz3d-0.6.0.jar** for 3D visualization  

---

## Prerequisites

Ensure your system meets the following requirements:

- Windows 10 or newer  
- Minimum 8 GB RAM  
- Internet access to download dependencies  

---

## Initial Setup

### 🧩 1️⃣ Install Java 21 (JDK)

#### ✅ Option A: Oracle JDK (Recommended)
1. Go to: [Oracle Java Downloads](https://www.oracle.com/java/technologies/downloads/)  
2. Under **Java SE 21 (LTS)** → choose **Windows x64 Installer (.msi)**  
3. Run installer → accept defaults  
4. Default installation path:

C:\Program Files\Java\jdk-21

5. Verify installation:

```bash
java -version
```
Expected output:

java version "21.x.x"
Java(TM) SE Runtime Environment

✅ Option B: Open-source (Temurin)

Go to: Adoptium Temurin 21

Download MSI Installer for Windows x64

Install and verify as above

🧩 2️⃣ Install Gradle (Build System)
Option 1 – via Scoop (Easy)
scoop install gradle

Option 2 – Manual Setup

Go to: Gradle Releases

Download binary-only ZIP

Extract to:
C:\Gradle\gradle-8.x

Add to PATH:

Press Win + R, type sysdm.cpl

Go → Advanced → Environment Variables

Add system variable:
Variable name: GRADLE_HOME
Variable value: C:\Gradle\gradle-8.x

Edit Path → Add:
%GRADLE_HOME%\bin

Verify installation:
gradle -v

🧩 3️⃣ Install VS Code

Download from Visual Studio Code

During setup, check:

✅ Add “Open with Code” to context menu

✅ Add to PATH

🧩 4️⃣ Install VS Code Extensions

Launch VS Code → Ctrl+Shift+X → Install:

Extension	Identifier	Notes
Language Support for Java	redhat.java	Required
Debugger for Java	vscjava.vscode-java-debug	Required
Test Runner for Java	vscjava.vscode-java-test	Required
Project Manager for Java	vscjava.vscode-java-dependency	Required
Gradle Tasks	richardwillis.vscode-gradle	Optional
Lombok Annotations	gabrielbb.vscode-lombok	Optional

🧩 5️⃣ Optional: fxyz3d Library

For 3D visualization:

Download fxyz3d-0.6.0.jar

Place in your project folder, e.g., /libs

Add it to project dependencies in Gradle:

dependencies {
    implementation files('libs/fxyz3d-0.6.0.jar')
}

Build & Run

Open VS Code in project folder

Refresh Gradle projects

Run simulation:
gradle run


✅ Verify Setup

java -version → should show 21.x.x

gradle -v → should show Gradle 8.x

VS Code → Java extensions installed

Optional visualizer works with fxyz3d-0.6.0.jar



Output:

<img width="1904" height="1007" alt="image" src="https://github.com/user-attachments/assets/ecbafd06-1f73-4ff7-8838-9a7fd0a62371" />
