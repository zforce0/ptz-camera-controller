#!/bin/bash
# run_android_build.sh - Script to set up environment variables and run the Android build

# Set Android SDK environment variable
export ANDROID_HOME="/home/runner/Android/Sdk"
export PATH="$ANDROID_HOME/platform-tools:$PATH"

# Check if Android SDK directory exists
if [ ! -d "$ANDROID_HOME" ]; then
    echo "Android SDK directory not found at $ANDROID_HOME"
    echo "Running setup_android_sdk.sh to create it..."
    ./setup_android_sdk.sh
fi

# Verify the setup
echo "Verifying Android SDK setup:"
if [ -d "$ANDROID_HOME/platforms" ] && [ -d "$ANDROID_HOME/build-tools" ]; then
    echo "✓ Basic SDK directory structure is in place"
else
    echo "✗ SDK directory structure is incomplete"
    echo "Running setup_android_sdk.sh again to ensure proper setup..."
    ./setup_android_sdk.sh
fi

# Print build information
echo "Building Android app with:"
echo "- ANDROID_HOME: $ANDROID_HOME"
echo "- JDK version: $(java -version 2>&1 | grep version | head -1)"
echo "- Gradle version: $(./gradlew --version | grep Gradle | head -1)"

# Run the actual build command with all environment variables properly set
echo "Starting build process..."
./gradlew "$@"

# Check build result
if [ $? -eq 0 ]; then
    echo "✓ Build completed successfully!"
    
    # Check for APK files
    if [ -d "./app/build/outputs/apk" ]; then
        echo "APK files found at: ./app/build/outputs/apk"
        find ./app/build/outputs/apk -name "*.apk" | while read apk; do
            echo "  - $apk"
        done
    else
        echo "No APK files found. Build may not have produced artifacts."
    fi
else
    echo "✗ Build failed with errors"
fi