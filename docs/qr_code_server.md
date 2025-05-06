# QR Code Configuration Server

This document explains how to use the QR code configuration server for the PTZ Camera Controller system.

## Overview

The QR code server provides a simple way to configure the Android tablet app to connect to the camera controller. It generates and serves QR codes that contain configuration information such as:

- WiFi connection details (IP address and port)
- Bluetooth device name for fallback connection
- RTSP stream URL for video playback
- Camera features and system settings

## Running the Server

The QR code server can be started in multiple ways with different configuration options:

### Basic Usage

```bash
python flask_qr_server.py
```

This will start the server with default settings on port 5000.

### Auto-Detect IP Address

```bash
python flask_qr_server.py --auto-detect-ip
```

This will automatically detect the local IP address of the machine running the server and use it in the configuration. This is useful when you don't know the IP address of the device or when it might change.

### Custom Configuration

```bash
python flask_qr_server.py --port 8080 --wifi 192.168.1.100:8000 --bluetooth "PTZ Camera" --camera "Main Camera" --rtsp rtsp://192.168.1.100:8554/stream
```

### Available Command-Line Options

- `--port PORT`: Port for the web server (default: 5000)
- `--wifi IP:PORT`: WiFi connection details (default: 192.168.1.100:8000)
- `--bluetooth NAME`: Bluetooth device name (default: PTZ Camera Server)
- `--camera NAME`: Camera system name (default: PTZ Camera System)
- `--rtsp URL`: RTSP stream URL (default: rtsp://192.168.1.100:8554/stream)
- `--features LIST`: Comma-separated list of supported features (default: pan,tilt,zoom,switch_mode)
- `--app-url URL`: URL for downloading the Android app
- `--version VERSION`: App version number (default: 1.0.0)
- `--auto-detect-ip`: Automatically detect and use the local IP address
- `--quality-threshold N`: Quality threshold for connection switching (default: 3)

## Using the QR Codes

1. Start the QR code server on the device connected to the camera controller
2. Open a web browser and navigate to http://localhost:5000 (or the port you specified)
3. You'll see two QR codes:
   - The first QR code is for downloading and installing the Android app
   - The second QR code contains the connection configuration

4. Install the Android app on the tablet (if not already installed)
5. Open the app and go to the Connection tab
6. Use the "Scan QR Code" feature to scan the configuration QR code
7. The app will automatically configure itself to connect to the camera controller

## Workflow Integration

Two workflow configurations are available:

1. **QR Code Server**: Runs the basic server on port 5000
2. **QR Code Server With Auto-Detect**: Runs the server with auto-detected IP address on port 5001

These workflows can be started using the Replit workflow interface or by running the commands directly.

## Auto-Reconnect Feature

The configuration includes an auto-reconnect feature that allows the Android app to automatically switch between WiFi and Bluetooth connections based on connection quality. The quality threshold value controls how many failed communication attempts are allowed before switching.