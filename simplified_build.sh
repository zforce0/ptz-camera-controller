#!/bin/bash
# simplified_build.sh - An ultra-simplified build script for Replit environment
# This script bypasses full Gradle configuration and builds specific components directly

# Environment setup
ANDROID_SDK_DIR="/home/runner/Android/Sdk"
export ANDROID_HOME=$ANDROID_SDK_DIR
export PATH=$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/tools/bin:$PATH
export REPLIT=true

# Force offline mode to prevent network lookups
GRADLE_OFFLINE="--offline"

# Set extreme performance optimizations
export GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.workers.max=1 -Dorg.gradle.parallel=false -Dkotlin.incremental=false -Dkotlin.compiler.execution.strategy=in-process -Dkotlin.daemon.jvm.options=-Xmx500m -Dorg.gradle.jvmargs=-Xmx500m"

# Make sure SDK is set up
if [ ! -d "$ANDROID_SDK_DIR" ] || [ ! -f "$ANDROID_SDK_DIR/source.properties" ]; then
  echo "Setting up Android SDK first..."
  chmod +x ./setup_android_sdk.sh
  ./setup_android_sdk.sh
fi

# Ensure local.properties exists
if [ ! -f "./local.properties" ]; then
  echo "sdk.dir=$ANDROID_SDK_DIR" > local.properties
  echo "Created local.properties"
fi

# Make gradlew executable
chmod +x ./gradlew

# Run gradle command with timeout
run_gradle_with_timeout() {
  echo "Running: ./gradlew $@"
  timeout 60s ./gradlew "$@"
  EXIT_CODE=$?
  if [ $EXIT_CODE -eq 124 ]; then
    echo "Command timed out after 60 seconds."
    return 1
  elif [ $EXIT_CODE -ne 0 ]; then
    echo "Command failed with exit code $EXIT_CODE."
    return $EXIT_CODE
  fi
  return 0
}

# Choose operation based on argument
case "$1" in
  "version")
    echo "Checking Gradle and JDK versions..."
    ./gradlew -v --no-daemon
    ;;
    
  "compile")
    echo "Compiling Java/Kotlin code only (no APK build)..."
    run_gradle_with_timeout compileDebugJavaWithJavac --no-daemon --configure-on-demand --max-workers 1 --console plain -x lint -x lintVitalRelease $GRADLE_OFFLINE
    ;;
    
  "debug")
    echo "Building debug APK only (minimal checks)..."
    run_gradle_with_timeout assembleDebug --no-daemon --configure-on-demand --max-workers 1 --console plain -x lint -x lintVitalRelease $GRADLE_OFFLINE
    ;;
    
  "init")
    echo "Initializing project structure only..."
    run_gradle_with_timeout init --no-daemon --configure-on-demand --max-workers 1 --console plain $GRADLE_OFFLINE
    ;;
    
  "clean")
    echo "Cleaning build files..."
    run_gradle_with_timeout clean --no-daemon --max-workers 1 --console plain $GRADLE_OFFLINE
    ;;
    
  "check-project")
    echo "Checking project structure..."
    ls -la
    echo -e "\nChecking app directory:"
    ls -la ./app
    echo -e "\nChecking app/src directory:"
    ls -la ./app/src
    echo -e "\nChecking local.properties:"
    cat local.properties
    echo -e "\nChecking settings.gradle:"
    cat settings.gradle
    echo -e "\nChecking build.gradle:"
    cat build.gradle
    echo -e "\nChecking app/build.gradle:"
    cat app/build.gradle
    ;;
    
  *)
    echo "Usage: ./simplified_build.sh [version|compile|debug|init|clean|check-project]"
    echo ""
    echo "version       - Display Gradle and JDK versions"
    echo "compile       - Compile Java/Kotlin code only (no APK)"
    echo "debug         - Build debug APK"
    echo "init          - Initialize project structure only"
    echo "clean         - Clean build files"
    echo "check-project - Check project structure"
    ;;
esac

# Print success/failure
if [ $? -eq 0 ]; then
  echo "✓ Operation completed successfully!"
else
  echo "✗ Operation failed!"
fi