# PTZ Camera Controller System

This repository contains the controller system for PTZ (Pan-Tilt-Zoom) cameras, consisting of two main components:

1. An Android application for user interface and control
2. An onboard Python server for hardware interface and video streaming

## Android Application

The Android app provides an interface for controlling the PTZ cameras and viewing video streams. It supports:

- WiFi and Bluetooth connections to the camera server
- Pan, tilt, and zoom controls with adjustable speed
- Video streaming with support for RGB and IR/Thermal modes
- Easy configuration via QR code scanning

### Current Development Status

The Android app is currently under development with the following components implemented:

- Basic UI framework with bottom navigation
- Connection management for WiFi and Bluetooth
- Camera control interface
- Video streaming interface (using dummy displays for compilation)
- Settings screen

To compile and build the Android app:

1. Open the project in Android Studio
2. Build and run the app on an Android device or emulator
3. Use the Connection tab to connect to the camera server

## Onboard Python Server

The onboard server runs on the camera hardware (Raspberry Pi or NVIDIA Jetson) and handles:

- Camera hardware interface
- Video streaming via RTSP
- Communication with the Android app via WiFi or Bluetooth

### Current Development Status

The onboard server code includes:

- Camera controller for PTZ operations
- Video streamer for RTSP video streams
- WiFi server for HTTP/TCP communication
- Bluetooth server for RFCOMM communication
- Local stream viewer for debugging

## QR Code Configuration

We've implemented a flexible QR code configuration system to simplify the setup process for the Android app. This allows users to quickly configure connection settings by scanning a QR code.

### Using the Flask QR Server

Our enhanced Flask-based QR code server offers more configuration options:

1. Run the QR server wizard for a guided setup:
   ```
   python start_qr_server.py
   ```

2. Or start the server directly with command-line options:
   ```
   python flask_qr_server.py [options]
   ```
   
3. Access the QR codes at:
   http://localhost:5000 (default port)
   
4. Scan the QR code with the Android app to automatically configure connection settings

### Auto-detecting IP Address

For easier deployment, the server can automatically detect and use the local IP address:

```
python flask_qr_server.py --auto-detect-ip
```

This will:
- Detect the device's IP address on the local network
- Update both WiFi connection and RTSP stream URLs with this IP
- Generate QR codes with the correct IP address

### Available Configuration Options

The Flask QR server supports various configuration options:

- `--port PORT`: Web server port (default: 5000)
- `--wifi IP:PORT`: WiFi connection (default: 192.168.1.100:8000)
- `--bluetooth NAME`: Bluetooth device name (default: PTZ Camera Server)
- `--camera NAME`: Camera name (default: PTZ Camera System)
- `--rtsp URL`: RTSP stream URL (default: rtsp://192.168.1.100:8554/stream)
- `--features LIST`: Supported features (default: pan,tilt,zoom,switch_mode)
- `--auto-detect-ip`: Automatically detect and use local IP address
- `--quality-threshold N`: Connection switching threshold (default: 3)

For detailed documentation, see [QR Code Server Documentation](docs/qr_code_server.md).

## Development Workflow

1. Make changes to the Android app or onboard server code
2. Test with dummy implementations first
3. Gradually replace dummy implementations with actual functionality
4. Use the QR code for easy configuration during testing

## Next Steps

- Complete dummy implementations for remaining app components
- Implement QR code scanning functionality in the app
- Add unit tests for core functionality
- Integrate with actual camera hardware