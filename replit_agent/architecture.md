# PTZ Camera Controller - Architecture Documentation

## 1. Overview

The PTZ Camera Controller is a system designed for controlling Pan-Tilt-Zoom (PTZ) cameras remotely. It consists of two main components:

1. **Android Application**: A user interface for controlling cameras and viewing video streams
2. **Onboard Python Server**: A backend service running on the camera hardware (Raspberry Pi or NVIDIA Jetson) that interfaces with the physical cameras

The system supports both WiFi and Bluetooth connections between the Android app and the onboard server, providing redundancy in communication methods. It also supports multiple camera types (RGB and IR/Thermal) with video streaming capabilities.

## 2. System Architecture

The PTZ Camera Controller follows a client-server architecture:

```
[Android Tablet App] <---> [Onboard Python Server] <---> [PTZ Camera Hardware]
       (Client)              (Server)                  (Physical Device)
```

### 2.1 Communication Flow

The system supports two communication channels:

1. **Primary: WiFi/TCP** - For high-speed video streaming and control commands
2. **Fallback: Bluetooth** - For basic control when WiFi is unavailable

### 2.2 High-Level Component Diagram

```
+------------------------+    +---------------------------+    +-----------------+
|   Android Application  |    |   Onboard Python Server   |    |  PTZ Hardware   |
|                        |    |                           |    |                 |
| +-------------------+  |    | +---------------------+   |    | +-----------+   |
| | UI (Fragments)    |  |    | | Camera Controller   |   |    | | RGB Camera |   |
| +-------------------+  |    | +---------------------+   |    | +-----------+   |
|                        |    |                           |    |                 |
| +-------------------+  |    | +---------------------+   |    | +-----------+   |
| | Video Player      |<------->| Video Streamer      |<------>| IR Camera  |   |
| +-------------------+  |    | +---------------------+   |    | +-----------+   |
|                        |    |                           |    |                 |
| +-------------------+  |    | +---------------------+   |    | +-----------+   |
| | WiFi Client       |<------->| WiFi Server         |   |    | | PTZ Motors |   |
| +-------------------+  |    | +---------------------+   |    | +-----------+   |
|                        |    |                           |    |                 |
| +-------------------+  |    | +---------------------+   |    |                 |
| | Bluetooth Client  |<------->| Bluetooth Server    |   |    |                 |
| +-------------------+  |    | +---------------------+   |    |                 |
+------------------------+    +---------------------------+    +-----------------+
```

## 3. Key Components

### 3.1 Android Application

The Android application is built using Kotlin and follows a fragment-based architecture.

#### Core Components:

- **MainActivity**: Entry point and navigation controller
- **Fragments**:
  - **CameraControlFragment**: UI for PTZ controls
  - **VideoStreamFragment**: Video streaming display
  - **ConnectionFragment**: Connection settings and management
  - **SettingsFragment**: General settings

#### Technologies:

- **Kotlin**: Primary programming language
- **Android SDK 35**: Target platform
- **ExoPlayer**: For video stream playback
- **RTSP**: Protocol for video streaming
- **Gradle 8.11.1**: Build system

### 3.2 Onboard Python Server

The onboard server runs on the camera hardware (Raspberry Pi or NVIDIA Jetson) and provides an interface between the Android app and the physical PTZ camera.

#### Core Components:

- **camera_server.py**: Main server application that initializes all components
- **camera_controller.py**: Controls PTZ movements and camera modes
- **video_streamer.py**: Handles video streaming via RTSP
- **wifi_server.py**: HTTP/TCP server for communication with the Android app
- **bt_server.py**: Bluetooth server for fallback communication
- **local_stream_viewer.py**: Optional component for displaying the stream on a local monitor

#### Technologies:

- **Python 3.11+**: Primary programming language
- **Flask**: HTTP server framework
- **OpenCV**: For camera interface and image processing
- **RTSP**: Protocol for video streaming
- **PyBluez**: For Bluetooth communication (simulated in some cases)

### 3.3 QR Code Configuration

The system includes multiple Python scripts for generating QR codes that facilitate easy configuration of the Android app:

- **flask_qr_server.py**: Flask-based server for serving QR codes
- **adaptive_server.py**: Server that tries different ports until finding one that works
- **simple_qr_server.py**: Simplified QR code server
- **generate_sdk_qr.py**: Generates QR codes for Android SDK configuration
- **android_config_qr.py**: Generates QR codes with app configuration details

## 4. Data Flow

### 4.1 Control Flow

1. User interacts with Android app UI
2. App sends commands to the onboard server via WiFi or Bluetooth
3. Onboard server processes commands and controls the PTZ camera hardware
4. Status updates are sent back to the Android app

### 4.2 Video Flow

1. Onboard server captures video from the active camera (RGB or IR)
2. Video is encoded and streamed via RTSP
3. Android app receives and decodes the RTSP stream
4. Video is displayed in the VideoStreamFragment

### 4.3 Configuration Flow

1. Onboard server generates a QR code with connection details
2. QR code is displayed on a web page served by the onboard server
3. User scans the QR code with the Android app
4. App automatically configures connection settings based on QR code data

## 5. External Dependencies

### 5.1 Android Application Dependencies

- **AndroidX Core**: Core Android framework libraries
- **Android SDK Build Tools 33.0.1/34.0.0**: For building the application
- **JDK 17**: Java Development Kit for building the application

### 5.2 Onboard Server Dependencies

- **Flask**: Web framework for HTTP server
- **NumPy**: For numerical operations
- **OpenCV**: For camera interface and image processing
- **PIL/Pillow**: For image manipulation
- **QRCode**: For generating QR codes
- **PyBluez** (optional): For Bluetooth communication
- **PySerial**: For serial communication with camera hardware

## 6. Deployment Strategy

### 6.1 Android Application Deployment

The Android application is built as an APK and distributed to users. The repository includes:

- **deploy_android_app.sh**: Script for building and signing the APK
- **GitHub Release Guide**: Documentation for creating GitHub releases for distribution
- **QR code-based installation method**: For easy distribution and installation

### 6.2 Onboard Server Deployment

The onboard server is deployed to the camera hardware (Raspberry Pi or NVIDIA Jetson) with:

- **install.sh**: Installation script for the onboard server
- **Systemd Service**: For automatic startup and management of the server
- **requirements.txt**: Python dependencies for installation

### 6.3 Development Environment

- **setup_android_sdk.sh**: Script for setting up the Android SDK
- **simplified_build.sh**: Script for building the Android app with simplified options
- **run_android_build.sh**: Script for running Android builds with proper environment variables

### 6.4 QR Code Configuration

- QR code servers run on port 5000 (default)
- Additional ports (5001, 5002) are configured for other services

## 7. Architectural Decisions

### 7.1 Dual Communication Channels

**Decision**: Support both WiFi and Bluetooth for communication between the Android app and onboard server.

**Rationale**: 
- WiFi provides higher bandwidth for video streaming
- Bluetooth serves as a fallback for situations where WiFi is unavailable or unreliable
- Redundancy improves system reliability

### 7.2 Modular Server Architecture

**Decision**: Structure the onboard server as separate modules (camera controller, video streamer, communication servers).

**Rationale**:
- Improves maintainability by separating concerns
- Allows for easier testing of individual components
- Enables future extension with new features

### 7.3 QR Code Configuration

**Decision**: Use QR codes for configuring the Android app.

**Rationale**:
- Simplifies the setup process for users
- Reduces potential for user error in manual configuration
- Provides a consistent way to distribute configuration details

### 7.4 JDK 17 Upgrade

**Decision**: Migrate from JDK 11 to JDK 17 for the Android application.

**Rationale**:
- Access to newer Java language features
- Better performance and security
- Long-term support

### 7.5 Local Stream Viewer

**Decision**: Include an optional local stream viewer for displaying video on a monitor connected to the onboard computer.

**Rationale**:
- Useful for debugging and setup
- Allows for local monitoring without the Android app
- Conditionally imported to avoid dependencies when not needed