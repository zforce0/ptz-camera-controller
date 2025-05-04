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

For easy configuration of the Android app, we've implemented a QR code based setup system:

1. Run the QR code server:
   ```
   python qr_server.py --wifi <server_ip>:<port>
   ```
   
2. Access the QR code at:
   http://localhost:5001
   
3. Scan the QR code with the Android app to automatically configure connection settings

### Customizing QR Codes

You can generate custom QR codes with different connection settings:

- For WiFi connection:
  http://localhost:5001/generate?wifi=192.168.1.100:8000
  
- For Bluetooth connection:
  http://localhost:5001/generate?bluetooth=PTZ_Camera_Controller
  
- For both connection types:
  http://localhost:5001/generate?wifi=192.168.1.100:8000&bluetooth=PTZ_Camera_Controller

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