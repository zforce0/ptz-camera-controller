# PTZ Camera Controller - Installation Guide

This guide provides step-by-step instructions for installing the PTZ Camera Controller app on your Android tablet.

## Installation via QR Code (Recommended)

1. **Scan the QR Code**
   
   Using your Android tablet's camera app or QR code scanner:
   
   - Open the camera app
   - Point the camera at the QR code below
   - Tap the notification that appears

   ![QR Code for Installation](../screenshots/qr_code_placeholder.png)

2. **Download the APK**
   
   - Your browser will open and begin downloading the APK file
   - If prompted, tap "Download" to confirm

3. **Enable Unknown Sources**
   
   If this is your first time installing an app outside the Play Store:
   
   - Go to Settings > Security (or Privacy)
   - Enable "Install from Unknown Sources" or "Install Unknown Apps"
   - On newer Android versions, you may be prompted to allow your browser to install apps

4. **Install the App**
   
   - Open the downloaded APK file
   - Tap "Install"
   - Wait for the installation to complete
   - Tap "Open" to launch the app

## Manual Installation

If you cannot scan the QR code, follow these steps:

1. **Download the APK**
   
   - On your Android tablet, visit:
     https://github.com/zforce0/ptz-camera-controller/releases/latest
   - Scroll down to "Assets"
   - Tap on "ptz-camera-controller.apk" to download

2. **Install the App**
   
   - Follow steps 3-4 from the QR code installation method above

## Connecting to the PTZ Camera Server

After installation, you'll need to connect to your camera server:

1. **Start the Server**
   
   - Ensure the PTZ Camera Server is running on your Raspberry Pi or Jetson
   - Note the IP address of your server (use `hostname -I` command)

2. **Connect via WiFi**
   
   - Open the PTZ Camera Controller app
   - Select "WiFi Connection"
   - Enter the IP address of your server
   - Default port is 8000
   - Tap "Connect"

3. **Connect via Bluetooth**
   
   - Ensure Bluetooth is enabled on both devices
   - Open the PTZ Camera Controller app
   - Select "Bluetooth Connection"
   - Select "PTZCameraServer" from the list
   - Tap "Connect"

## Troubleshooting

If you encounter issues:

- **Installation Blocked**: Make sure you've enabled installation from unknown sources
- **App Won't Open**: Check that your Android version is 6.0 (Marshmallow) or higher
- **Connection Failed (WiFi)**: Verify your tablet and server are on the same network
- **Connection Failed (Bluetooth)**: Ensure your devices are paired properly
- **Video Stream Issues**: Try switching to lower quality stream in settings
- **Controls Unresponsive**: Check server logs for connection issues

## Updates

The app will check for updates automatically. When a new version is available:

1. You'll receive a notification
2. Tap "Update Now" to download the latest version
3. Install the update just like the original installation