#!/bin/bash
# fix_android_sdk.sh - Fix Android SDK platform detection issues
# This script fixes issues with Android SDK platform detection in CI environments

# Set environment variables
export ANDROID_HOME="/home/runner/Android/Sdk"
export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/tools/bin:$PATH"

# Create local.properties with SDK path
echo "Creating local.properties with SDK path..."
echo "sdk.dir=$ANDROID_HOME" > local.properties

# Create source.properties in platform directories
echo "Creating source.properties in platform directories..."
ANDROID_PLATFORM="android-33"
PLATFORM_DIR="$ANDROID_HOME/platforms/$ANDROID_PLATFORM"

# Create source.properties in the platform directory
cat > "$PLATFORM_DIR/source.properties" << EOF
Pkg.Desc=Android SDK Platform 33
Pkg.Revision=3
Platform.Version=33
AndroidVersion.ApiLevel=33
Layoutlib.Api=18
Layoutlib.Revision=3
EOF

# Update gradle.properties with SDK-related properties
if grep -q "android.enableSdkLocationCheck" gradle.properties; then
  echo "gradle.properties already contains SDK fixes"
else
  echo "Updating gradle.properties with SDK fixes..."
  cat >> gradle.properties << EOF

# Fix for Android SDK platform detection
android.dir.platformDirectory=$PLATFORM_DIR
android.builder.sdkDownload=false
android.sdk.repo.prefetched=true
android.enableSdkLocationCheck=false
EOF
fi

# Update app build.gradle with explicit SDK directory if needed
APP_GRADLE="./app/build.gradle"
if grep -q "sdkDirectory" "$APP_GRADLE"; then
  echo "app/build.gradle already contains SDK directory fix"
else
  echo "Updating app/build.gradle with SDK directory fix..."
  # Create backup
  cp "$APP_GRADLE" "${APP_GRADLE}.bak"
  
  # Add the SDK directory line after compileSdk
  sed -i "/compileSdk/a\\    sdkDirectory = new File(\"$ANDROID_HOME\")" "$APP_GRADLE"
fi

echo "Android SDK fixes applied successfully!"
echo "You should now be able to run './gradlew clean build' without SDK detection issues."