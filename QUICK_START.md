# PTZ Camera Controller - Quick Start Guide

This quick start guide will help you set up the PTZ Camera Controller system with minimal effort.

## Server Side Setup (Raspberry Pi / Jetson)

### 1. One-Line Installation

```bash
curl -sSL https://raw.githubusercontent.com/your-repo/ptz-camera-controller/main/install.sh | sudo bash
```

### 2. Post-Installation

After installation completes:
- Note your device's IP address (displayed at the end of installation)
- Ensure the service is running with `sudo systemctl status ptz-camera-controller`

## Android App Setup

### 1. Install the App

Download and install the PTZ Camera Controller app from Google Play Store.

### 2. Connect to Server

There are two ways to connect:

#### Option 1: Scan QR Code (Easiest)
1. Open the PTZ Camera Controller app
2. Tap "Scan QR Code"
3. Visit `http://[SERVER-IP]:5000` from any browser on the same network
4. Scan the displayed QR code with your Android device

#### Option 2: Manual Connection
1. Open the app and tap "Manual Setup"
2. Enter the IP address of your server
3. Select connection method (WiFi/Bluetooth)
4. Tap "Connect"

## Using the Controller

Once connected:
- Use the joystick for pan/tilt control
- Pinch gestures for zoom control
- Tap the preset buttons to move to saved positions
- Access settings via the gear icon

## Troubleshooting

If you encounter connection issues:
1. Ensure both devices are on the same network
2. Check that the server service is running
3. Verify firewall settings (ports 5000 and 8000 should be open)
4. Try restarting both the server and the Android app

For detailed instructions, refer to the full documentation in ONBOARD_SETUP_GUIDE.md