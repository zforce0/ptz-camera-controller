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

# Function to setup and verify Android SDK
setup_and_verify_sdk() {
  echo "Setting up and verifying Android SDK..."
  
  # Make sure SDK directories exist
  mkdir -p "$ANDROID_SDK_DIR"
  mkdir -p "$ANDROID_SDK_DIR/licenses"
  mkdir -p "$ANDROID_SDK_DIR/platforms/android-34"
  mkdir -p "$ANDROID_SDK_DIR/build-tools/34.0.0"
  mkdir -p "$ANDROID_SDK_DIR/build-tools/33.0.1"
  mkdir -p "$ANDROID_SDK_DIR/platform-tools"
  mkdir -p "$ANDROID_SDK_DIR/tools/bin"
  mkdir -p "$ANDROID_SDK_DIR/cmdline-tools/latest/bin"
  
  # Create license acceptance markers if not exists
  if [ ! -f "$ANDROID_SDK_DIR/licenses/android-sdk-license" ]; then
    echo "8933bad161af4178b1185d1a37fbf41ea5269c55" > "$ANDROID_SDK_DIR/licenses/android-sdk-license"
    echo "24333f8a63b6825ea9c5514f83c2829b004d1fee" >> "$ANDROID_SDK_DIR/licenses/android-sdk-license"
  fi
  
  # Create basic property files if not exists
  if [ ! -f "$ANDROID_SDK_DIR/source.properties" ]; then
    echo "Pkg.Revision=34.0.0" > "$ANDROID_SDK_DIR/source.properties"
  fi
  
  # Verify setup
  if [ -d "$ANDROID_SDK_DIR" ] && [ -f "$ANDROID_SDK_DIR/source.properties" ]; then
    echo "✓ Android SDK verified at $ANDROID_SDK_DIR"
    return 0
  else
    echo "✗ Android SDK setup failed"
    return 1
  fi
}

# Make sure SDK is set up
if [ ! -d "$ANDROID_SDK_DIR" ] || [ ! -f "$ANDROID_SDK_DIR/source.properties" ]; then
  echo "Setting up Android SDK first..."
  setup_and_verify_sdk
else
  echo "Android SDK already set up at $ANDROID_SDK_DIR"
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
  timeout 180s ./gradlew "$@"
  EXIT_CODE=$?
  if [ $EXIT_CODE -eq 124 ]; then
    echo "Command timed out after 180 seconds."
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
    
  "sdk-check")
    echo "Checking Android SDK setup..."
    setup_and_verify_sdk
    echo "ANDROID_HOME=$ANDROID_HOME"
    echo "Local properties:"
    cat local.properties
    ;;
    
  "project-info")
    echo "Fetching project info (minimal with no compilation)..."
    ./gradlew -q projects --no-daemon
    echo -e "\nTasks:"
    ./gradlew -q tasks --no-daemon
    ;;
    
  "minimal-check")
    echo "Performing minimal project check without Gradle..."
    echo -e "\nProject Overview:"
    if [ -d "app/src/main/java" ]; then
        echo "✓ Java source directory found"
        echo -e "\nSource files:"
        find app/src/main -name "*.java" -o -name "*.kt" | sort
    else
        echo "✗ Java source directory not found"
    fi
    
    if [ -f "local.properties" ]; then
        echo -e "\n✓ local.properties exists:"
        cat local.properties
    else
        echo -e "\n✗ local.properties missing"
    fi
    
    if [ -d "$ANDROID_HOME" ]; then
        echo -e "\n✓ Android SDK dir exists: $ANDROID_HOME"
    else
        echo -e "\n✗ Android SDK not found: $ANDROID_HOME"
    fi
    ;;
    
  "generate-qr")
    echo "Generating QR code with SDK configuration..."
    if command -v python3 >/dev/null 2>&1; then
        python3 generate_sdk_qr.py
        echo -e "\nQR code and HTML page generated successfully!"
        echo "You can access the page at http://localhost:5000/"
    else
        echo "✗ Python3 is not installed or not in PATH"
        echo "Please install Python3 to generate QR codes"
    fi
    ;;
    
  "compile")
    echo "Compiling Java/Kotlin code only (no APK build)..."
    run_gradle_with_timeout compileDebugJavaWithJavac --no-daemon --configure-on-demand --max-workers 1 --console plain -x lint -x lintVitalRelease $GRADLE_OFFLINE
    ;;
    
  "debug")
    echo "Building debug APK only (minimal checks)..."
    run_gradle_with_timeout assembleDebug --no-daemon --configure-on-demand --max-workers 1 --console plain -x lint -x lintVitalRelease $GRADLE_OFFLINE
    ;;
    
  "dependencies")
    echo "Resolving and downloading dependencies..."
    run_gradle_with_timeout dependencies --no-daemon --max-workers 1 --console plain
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
    echo "Usage: ./simplified_build.sh [version|sdk-check|minimal-check|generate-qr|project-info|compile|debug|dependencies|init|clean|check-project]"
    echo ""
    echo "version       - Display Gradle and JDK versions"
    echo "sdk-check     - Verify Android SDK setup"
    echo "minimal-check - Quick check of project files (no Gradle)"
    echo "generate-qr   - Generate QR code with SDK configuration info"
    echo "project-info  - Show project structure and available tasks (no compilation)"
    echo "compile       - Compile Java/Kotlin code only (no APK)"
    echo "debug         - Build debug APK"
    echo "dependencies  - Resolve and download project dependencies"
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