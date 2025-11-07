@echo off
REM ============================================================
REM Build and Run ConstellationSimulator with OrbitVisualizer
REM Logs output to run_output.log
REM ============================================================

set LOG_FILE=run_output.log

REM -----------------------------
REM Detect OS
REM -----------------------------
ver | findstr /i "windows" > nul
if %ERRORLEVEL% == 0 (
    set OS_TYPE=WINDOWS
) else (
    set OS_TYPE=UNIX
)

REM -----------------------------
REM Step 0: Prompt for test selection
REM -----------------------------
echo Select which test to run:
echo   [1] ConstellationSimulatorTest
echo   [2] OrbitalConstellationTest
set /p TEST_CHOICE=Enter your choice (1 or 2): 

if "%TEST_CHOICE%"=="1" (
    set RUN_TASK=runConstellationTest
    echo Running ConstellationSimulatorTest...
) else if "%TEST_CHOICE%"=="2" (
    set RUN_TASK=runOrbitalConstellationTest
    echo Running OrbitalConstellationTest...
) else (
    echo Invalid choice. Exiting.
    pause
    exit /b 1
)

REM -----------------------------
REM Step 1: Build the project
REM -----------------------------
echo Building project with Gradle...
if "%OS_TYPE%"=="WINDOWS" (
    call gradle clean build --no-daemon --console=plain > %LOG_FILE% 2>&1
) else (
    gradle clean build --no-daemon --console=plain | tee %LOG_FILE%
)

if %ERRORLEVEL% neq 0 (
    echo Build failed. Check %LOG_FILE% for details.
    pause
    exit /b 1
)

REM -----------------------------
REM Step 2: Run the selected test
REM -----------------------------
echo Launching %RUN_TASK%...
if "%OS_TYPE%"=="WINDOWS" (
    call gradle %RUN_TASK% --no-daemon --console=plain >> %LOG_FILE% 2>&1
) else (
    gradle %RUN_TASK% --no-daemon --console=plain | tee -a %LOG_FILE%
)

echo Done. Output saved to %LOG_FILE%
pause
