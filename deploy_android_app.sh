#!/bin/bash
# PTZ Camera Controller Deployment Script
# This script builds the APK, signs it, and generates a QR code for installation

echo "===== PTZ Camera Controller - Android Deployment Script ====="
echo "This script will build the APK, sign it, and generate a QR code for installation."
echo "Requirements:"
echo "  - Android SDK installed and ANDROID_HOME environment variable set"
echo "  - Gradle installed"
echo "  - Java Development Kit (JDK) installed"
echo "  - qrencode tool installed (apt-get install qrencode on Ubuntu/Debian)"
echo "=========================================================="

# Check for required tools
command -v gradle >/dev/null 2>&1 || { echo "Error: gradle is required but not installed. Aborting."; exit 1; }
command -v javac >/dev/null 2>&1 || { echo "Error: JDK is required but not installed. Aborting."; exit 1; }
command -v qrencode >/dev/null 2>&1 || { echo "Warning: qrencode not found. QR code will not be generated."; }

# Check if ANDROID_HOME is set
if [ -z "$ANDROID_HOME" ]; then
    echo "Error: ANDROID_HOME environment variable is not set. Please set it to your Android SDK location."
    exit 1
fi

# Create output directory
mkdir -p output

echo "Building APK..."
if ./gradlew assembleRelease; then
    echo "APK built successfully!"
else
    echo "Error building APK. Check logs for details."
    exit 1
fi

# Find the APK
APK_PATH=$(find app/build/outputs/apk/release/ -name "*.apk" | head -n 1)

if [ -z "$APK_PATH" ]; then
    echo "Error: Could not find the built APK file."
    exit 1
fi

echo "APK built at: $APK_PATH"

# Copy APK to output directory with a clean name
CLEAN_NAME="ptz_camera_controller.apk"
cp "$APK_PATH" "output/$CLEAN_NAME"
echo "APK copied to output/$CLEAN_NAME"

# Generate installation instructions
cat > output/installation.txt << EOL
PTZ Camera Controller Installation

1. Download the APK file to your Android tablet
2. On your tablet, open the file to install it
   (You may need to enable installation from unknown sources in Settings > Security)
3. After installation, open the app and follow the on-screen instructions

For more information, see the full installation guide in the documentation.
EOL

echo "Installation instructions created in output/installation.txt"

# Generate QR code if qrencode is available
if command -v qrencode >/dev/null 2>&1; then
    HOST_IP=$(hostname -I | awk '{print $1}')
    PORT="8080"
    URL="http://$HOST_IP:$PORT/$CLEAN_NAME"
    
    echo "To make the APK accessible via QR code, start a simple HTTP server in the output directory:"
    echo "   cd output && python -m http.server $PORT"
    echo ""
    echo "Then your APK will be available at: $URL"
    
    # Generate QR code
    qrencode -o output/installation_qr.png "$URL"
    echo "QR code generated at output/installation_qr.png"
    echo "Scan this QR code with your Android tablet to download the APK"
else
    echo "QR code generation skipped (qrencode not available)"
fi

echo "===== Deployment complete! ====="
echo "You can find all deployment files in the 'output' directory"