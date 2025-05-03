#!/bin/bash
# fix_android_sdk.sh - Script to fix Android SDK platform detection issues

# Set up environment variables
export ANDROID_HOME=/home/runner/Android/Sdk
export PATH=$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools:$PATH

echo "==== Android SDK Fix Tool ===="
echo "Setting up environment for Android build..."

# 1. Fix local.properties file
echo "Creating local.properties with correct SDK path..."
echo "sdk.dir=$ANDROID_HOME" > local.properties

# 2. Ensure platform directory structure is correct
PLATFORM_DIR="$ANDROID_HOME/platforms/android-33"
if [ ! -d "$PLATFORM_DIR" ]; then
  echo "Error: Platform directory $PLATFORM_DIR does not exist!"
  exit 1
fi

# 3. Create or update source.properties in platform directory
echo "Updating source.properties in platform directory..."
cat > "$PLATFORM_DIR/source.properties" << EOF
Pkg.Desc=Android SDK Platform 33
Pkg.Revision=3
Platform.Version=33
AndroidVersion.ApiLevel=33
Layoutlib.Api=18
Layoutlib.Revision=3
EOF

# 4. Create a package.xml file if missing
if [ ! -f "$PLATFORM_DIR/package.xml" ]; then
  echo "Creating package.xml in platform directory..."
  cat > "$PLATFORM_DIR/package.xml" << EOF
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<ns2:repository xmlns:ns2="http://schemas.android.com/repository/android/common/01">
    <localPackage path="platforms;android-33" obsolete="false">
        <type-details xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ns2:platformDetailsType">
            <api-level>33</api-level>
            <codename></codename>
            <revision>
                <major>1</major>
            </revision>
        </type-details>
        <revision>
            <major>3</major>
        </revision>
        <display-name>Android SDK Platform 33</display-name>
    </localPackage>
</ns2:repository>
EOF
fi

# 5. Update gradle.properties with necessary properties
if ! grep -q "android.dir.platformDirectory" gradle.properties; then
  echo "Updating gradle.properties with SDK fixes..."
  cat >> gradle.properties << EOF

# Android SDK platform detection fixes
android.dir.platformDirectory=$PLATFORM_DIR
android.builder.sdkDownload=false
android.sdk.repo.prefetched=true
android.enableSdkLocationCheck=false
EOF
fi

# 6. Skip symlink creation (not needed and can cause issues in some environments)
echo "Skipping symlink creation (not needed for SDK detection)"

echo "Android SDK fixes completed."
echo "You should now be able to build the Android project."
echo "If you still encounter issues, try running: ./gradlew clean build --info"