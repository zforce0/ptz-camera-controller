#!/bin/bash
# find_android_sdk.sh - Script to locate Android SDK on the system or provide alternatives

# Array of common Android SDK locations
SDK_LOCATIONS=(
  "$ANDROID_HOME"                              # Check environment variable first
  "$ANDROID_SDK_ROOT"                          # Alternative environment variable
  "/home/runner/Android/Sdk"                   # Our custom location
  "/usr/local/android-sdk"                     # Common Linux location
  "/opt/android-sdk"                           # Another common Linux location
  "$HOME/Android/Sdk"                          # User's home directory
)

# Check if a file should be created, requires arg1=path arg2=content
create_file_if_missing() {
  local file_path="$1"
  local content="$2"
  local dir_path=$(dirname "$file_path")
  
  if [ ! -d "$dir_path" ]; then
    mkdir -p "$dir_path"
  fi
  
  if [ ! -f "$file_path" ]; then
    echo "$content" > "$file_path"
    echo "Created file: $file_path"
  else
    echo "File already exists: $file_path"
  fi
}

# Find the first valid SDK location
for location in "${SDK_LOCATIONS[@]}"; do
  if [ -n "$location" ] && [ -d "$location" ]; then
    echo "Found Android SDK at: $location"
    
    # Update or create local.properties
    echo "sdk.dir=$location" > local.properties
    echo "Updated local.properties to use SDK at $location"
    
    # Set environment variable for current session
    export ANDROID_HOME="$location"
    echo "Set ANDROID_HOME=$location for current session"
    
    # We're done
    exit 0
  fi
done

# If no SDK found, create minimal structure
echo "No existing Android SDK found, creating minimal structure at /home/runner/Android/Sdk"

SDK_DIR="/home/runner/Android/Sdk"

# Create essential directories
mkdir -p "$SDK_DIR"
mkdir -p "$SDK_DIR/platforms/android-34"
mkdir -p "$SDK_DIR/build-tools/33.0.1"
mkdir -p "$SDK_DIR/platform-tools"
mkdir -p "$SDK_DIR/licenses"

# Create license files
create_file_if_missing "$SDK_DIR/licenses/android-sdk-license" "8933bad161af4178b1185d1a37fbf41ea5269c55
24333f8a63b6825ea9c5514f83c2829b004d1fee"

create_file_if_missing "$SDK_DIR/licenses/android-sdk-preview-license" "d56f5187479451eabf01fb78af6dfcb131a6481e"

# Create local.properties
echo "sdk.dir=$SDK_DIR" > local.properties

# Set environment variable for current session
export ANDROID_HOME="$SDK_DIR"
echo "Set ANDROID_HOME=$SDK_DIR for current session"

# Add to gradle.properties
if [ ! -f "gradle.properties" ]; then
  echo "Creating gradle.properties with optimized settings"
  cat > gradle.properties << EOF
# Project-wide Gradle settings.
org.gradle.jvmargs=-Xmx1024m -XX:MaxMetaspaceSize=512m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.suppressUnsupportedCompileSdk=34
org.gradle.daemon=false
org.gradle.configureondemand=true
org.gradle.parallel=false
EOF
fi

echo "Android SDK setup complete. Use './gradlew' with ANDROID_HOME=$ANDROID_HOME"