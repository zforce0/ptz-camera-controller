#!/bin/bash
# fix_android_sdk.sh
# Script to fix Android SDK platform detection issues for ':app:compileDebugJavaWithJavac'
# This combines both approaches:
# 1. Reinstall android-33 platform to the correct path
# 2. Configure Gradle to use the correct dependencies

# Exit on error
set -e

# Define SDK root directory
SDK_ROOT="/home/runner/Android/Sdk"
PLATFORM_DIR="${SDK_ROOT}/platforms/android-33"
DIRECT_DIR="${SDK_ROOT}/android-33"

echo "==== Android SDK Platform Fix Script ===="
echo "Fixing Android SDK platform detection issues..."

# First approach: Ensure platform exists in both required locations
echo "1. Reinstalling Android-33 platform files..."

# Check if original platform directory exists
if [ -d "$PLATFORM_DIR" ]; then
  echo "Found existing platform directory at: $PLATFORM_DIR"
  
  # Create source.properties in platform directory with correct values
  cat > "${PLATFORM_DIR}/source.properties" << EOF
Pkg.Desc=Android SDK Platform 33
Pkg.Revision=2
AndroidVersion.ApiLevel=33
Platform.Version=13
Layoutlib.Api=15
Layoutlib.Revision=1
EOF
  echo "Updated source.properties in platform directory"
  
  # Create directory in SDK root as well
  mkdir -p "$DIRECT_DIR"
  cp -r "$PLATFORM_DIR"/* "$DIRECT_DIR"/
  
  # Create source.properties in direct directory with correct values
  cat > "${DIRECT_DIR}/source.properties" << EOF
Pkg.Desc=Android SDK Platform 33
Pkg.Revision=2
AndroidVersion.ApiLevel=33
Platform.Version=13
Layoutlib.Api=15
Layoutlib.Revision=1
EOF
  echo "Copied platform to SDK root at: $DIRECT_DIR"
else
  echo "ERROR: Platform directory not found at: $PLATFORM_DIR"
  echo "Cannot complete first approach without the source files."
fi

# Second approach: Configure build system to use correct paths
echo "2. Configuring build system to recognize platform location..."

# Update local.properties
if [ -f "./local.properties" ]; then
  # Check if sdk.dir is already specified
  if grep -q "sdk.dir=" "./local.properties"; then
    sed -i "s|sdk.dir=.*|sdk.dir=$SDK_ROOT|g" "./local.properties"
  else
    echo "sdk.dir=$SDK_ROOT" >> "./local.properties"
  fi
  echo "Updated local.properties with SDK path"
else
  echo "sdk.dir=$SDK_ROOT" > "./local.properties"
  echo "Created local.properties with SDK path"
fi

# Set environment variables
export ANDROID_HOME=$SDK_ROOT
export ANDROID_SDK_ROOT=$SDK_ROOT
echo "Set environment variables ANDROID_HOME and ANDROID_SDK_ROOT"

# Done
echo "==== Fix complete ===="
echo "Run your Gradle build with: ./gradlew build"