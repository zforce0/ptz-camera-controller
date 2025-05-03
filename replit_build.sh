#!/bin/bash
# replit_build.sh - Simplified build script optimized for Replit environment
# This script streamlines the Android build process for Replit's resource constraints

# Set up environment
export ANDROID_HOME=/home/runner/Android/Sdk
export PATH=$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/tools/bin:$PATH
export REPLIT=true
export GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.workers.max=1 -Dorg.gradle.parallel=false -Dkotlin.incremental=false -Dkotlin.compiler.execution.strategy=in-process"

# Ensure SDK is properly set up
if [ ! -d "$ANDROID_HOME" ] || [ ! -f "$ANDROID_HOME/source.properties" ]; then
  echo "Setting up Android SDK first..."
  chmod +x ./setup_android_sdk.sh
  ./setup_android_sdk.sh
fi

# Create a basic local.properties file if it doesn't exist
if [ ! -f "./local.properties" ]; then
  echo "sdk.dir=$ANDROID_HOME" > local.properties
  echo "Created local.properties"
fi

# Make gradlew executable
chmod +x ./gradlew

echo "Starting minimal build process..."
echo "Command: ./gradlew --no-daemon --max-workers 1 --no-parallel --configure-on-demand $@"

# Run a minimal gradle command with suppressed output
./gradlew --no-daemon --max-workers 1 --no-parallel --configure-on-demand "$@" | grep -v "Download" | grep -v "Transform" | grep -v "Task" | grep -v ">" | grep -v "^$"

# Check build result
BUILD_RESULT=$?
if [ $BUILD_RESULT -eq 0 ]; then
  echo "✓ Build completed successfully!"
else
  echo "✗ Build failed with exit code $BUILD_RESULT"
fi

exit $BUILD_RESULT