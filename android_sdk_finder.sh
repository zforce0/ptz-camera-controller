#!/bin/bash
# android_sdk_finder.sh - Utility to locate and map Android SDK platforms

# Define SDK location
ANDROID_SDK_DIR="/home/runner/Android/Sdk"
PLATFORMS_DIR="$ANDROID_SDK_DIR/platforms"

# Print current environment
echo "===== Android SDK Environment ====="
echo "ANDROID_HOME: $ANDROID_HOME"
echo "Platforms directory: $PLATFORMS_DIR"
echo "Available platforms:"
ls -la $PLATFORMS_DIR

# Skip symbolic link creation (caused issues in container)
echo "Skipping symbolic link creation (not needed)"

# Update local.properties
echo "Updating local.properties..."
echo "sdk.dir=$ANDROID_SDK_DIR" > local.properties
echo "android.sdk.path=$ANDROID_SDK_DIR" >> local.properties

# Create a source.properties file with API level in each platform directory if missing
for dir in $PLATFORMS_DIR/android-*; do
  if [ -d "$dir" ]; then
    PLATFORM_NUMBER=$(echo "$dir" | grep -o '[0-9]\+' | head -1)
    if [ ! -f "$dir/source.properties" ]; then
      echo "Creating source.properties in $dir"
      cat > "$dir/source.properties" << EOF
Pkg.Desc=Android SDK Platform $PLATFORM_NUMBER
Pkg.Revision=3
Platform.Version=$PLATFORM_NUMBER
AndroidVersion.ApiLevel=$PLATFORM_NUMBER
Layoutlib.Api=18
Layoutlib.Revision=3
EOF
    fi
  fi
done

echo "===== SDK Finder Complete ====="