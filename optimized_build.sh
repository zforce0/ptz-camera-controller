#!/bin/bash
# optimized_build.sh - Script for optimized Android builds in resource-constrained environments

# Source find_android_sdk.sh to locate and set up the Android SDK
echo "Locating Android SDK..."
source ./find_android_sdk.sh

# Check for JAVA_HOME, set if needed
if [ -z "$JAVA_HOME" ]; then
  if [ -d "/usr/lib/jvm/java-17-openjdk" ]; then
    export JAVA_HOME="/usr/lib/jvm/java-17-openjdk"
  elif [ -d "/usr/lib/jvm/java-17" ]; then
    export JAVA_HOME="/usr/lib/jvm/java-17"
  elif command -v java &> /dev/null; then
    # Try to find from java command
    JAVA_PATH=$(which java)
    JAVA_DIR=$(dirname $(dirname $(readlink -f "$JAVA_PATH")))
    export JAVA_HOME="$JAVA_DIR"
  fi
fi

echo "Using JAVA_HOME: $JAVA_HOME"
echo "Using ANDROID_HOME: $ANDROID_HOME"

# Optimize Gradle settings for limited resources
echo "org.gradle.jvmargs=-Xmx512m -XX:MaxMetaspaceSize=256m -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8" > gradle.properties
echo "org.gradle.daemon=false" >> gradle.properties
echo "org.gradle.configureondemand=true" >> gradle.properties
echo "org.gradle.caching=true" >> gradle.properties
echo "org.gradle.parallel=false" >> gradle.properties
echo "android.enableJetifier=false" >> gradle.properties
echo "android.useAndroidX=true" >> gradle.properties
echo "android.enableR8.fullMode=false" >> gradle.properties
echo "kotlin.code.style=official" >> gradle.properties
echo "android.suppressUnsupportedCompileSdk=34" >> gradle.properties
echo "android.enableBuildCache=true" >> gradle.properties
echo "android.enableDexingArtifactTransform=false" >> gradle.properties
echo "kapt.incremental.apt=false" >> gradle.properties

# Display build environment info
echo "=== Build Environment ==="
echo "Java version: $(java -version 2>&1 | head -1)"
echo "Gradle version: $(./gradlew --version 2>&1 | grep "Gradle" | head -1)"
echo "Android SDK: $ANDROID_HOME"
echo "Memory available: $(free -h | grep Mem | awk '{print $2}')"
echo "========================="

# Add offline flag to prevent network lookups
if [ "$1" == "--offline" ]; then
  OFFLINE_FLAG="--offline"
  shift
else
  OFFLINE_FLAG=""
fi

# Strip debugging information for faster builds
if [ "$1" == "--fast" ]; then
  GRADLE_DEBUG=""
  shift
else
  GRADLE_DEBUG="--debug"
fi

echo "Starting build with optimized settings..."
# Check if gradlew exists and is executable
if [ ! -x "./gradlew" ]; then
  echo "Making gradlew executable..."
  chmod +x ./gradlew
fi

echo "Running gradlew with: $OFFLINE_FLAG $GRADLE_DEBUG --no-daemon --max-workers 1 --no-parallel --no-configure-on-demand $@"
./gradlew $OFFLINE_FLAG $GRADLE_DEBUG --no-daemon --max-workers 1 --no-parallel --no-configure-on-demand "$@"

# Check build result
if [ $? -eq 0 ]; then
  echo "✓ Build completed successfully!"
else
  echo "✗ Build failed. Check the logs for details."
fi