# PTZ Camera Controller - System Architecture

## Overview

The PTZ Camera Controller is a comprehensive system for controlling Pan-Tilt-Zoom (PTZ) cameras remotely using an Android tablet. The system consists of two main components:

1. **Android Tablet Application**: User interface for controlling the camera and viewing video streams
2. **Onboard Python Server**: Backend server running on the camera hardware (Raspberry Pi or NVIDIA Jetson)

## System Topology

```
+---------------------------+           +---------------------------+           +---------------------------+
|    Android Tablet App     |           |    Onboard Computer       |           |   PTZ Camera Hardware    |
|    (User Interface)       |           |   (Pi/Jetson Server)      |           |                          |
+---------------------------+           +---------------------------+           +---------------------------+
|                           |           |                           |           |                           |
| +---------------------+   |           | +---------------------+   |           | +---------------------+   |
| | Main Activity       |   |           | | Camera Server       |   |           | | RGB Camera Module   |   |
| |                     |   |           | |                     |   |           | |                     |   |
| | - Navigation        |   |           | | - Main controller   |   |           | | - 1080p/4K video    |   |
| | - Fragment mgmt     |   |           | | - Component init    |   |           | | - Daylight mode     |   |
| +---------------------+   |           | +---------------------+   |           | +---------------------+   |
|           |               |           |           |               |           |           |               |
| +---------------------+   |   WiFi    | +---------------------+   |           | +---------------------+   |
| | Camera Control      |<------------->| | WiFi Server         |   |           | | IR/Thermal Camera   |   |
| | Fragment            |   |           | |                     |   |           | |                     |   |
| |                     |   |  (TCP/IP) | | - Command handling  |   |           | | - Thermal imaging   |   |
| | - Control interface |   |           | | - Status updates    |   |   RTSP    | | - Night mode        |   |
| | - Pan/tilt control  |   |           | +---------------------+   |  Stream   | +---------------------+   |
| | - Zoom/mode control |   |           |           |               |           |           |               |
| +---------------------+   |   BT      | +---------------------+   |           | +---------------------+   |
|           |               |<- - - - ->| | BT Server           |   |    RS485  | | PTZ Motors          |   |
| +---------------------+   | (Backup)  | |                     |   |<---------->|                     |   |
| | Video Stream        |   |           | | - Backup comms      |   |  (Pelco-D) | | - Pan/tilt motors  |   |
| | Fragment            |<------------->| | - Command handling  |   |  Protocol  | | - Zoom mechanism   |   |
| |                     |   |   RTSP    | +---------------------+   |           | | - Preset positions  |   |
| | - Video display     |   |  Stream   |           |               |           | +---------------------+   |
| | - Stream controls   |   |           | +---------------------+   |           |                           |
| | - Quality monitor   |   |           | | Camera Controller   |   |           |                           |
| +---------------------+   |           | |                     |<----------------------+                   |
|           |               |           | | - Direct hardware   |   |                                       |
| +---------------------+   |   HTTP    | | - Camera switching  |   |                                       |
| | Connection Setup    |<------------->| | - PTZ control       |   |                                       |
| | Fragment            |   |  (QR Code)| +---------------------+   |                                       |
| |                     |   |  Config   |           |               |                                       |
| | - WiFi setup        |   |           | +---------------------+   |                                       |
| | - Bluetooth setup   |   |           | | Video Streamer      |   |                                       |
| | - QR code scanning  |   |           | |                     |   |                                       |
| +---------------------+   |           | | - RTSP server       |   |                                       |
|                           |           | | - Video encoding    |   |                                       |
|                           |           | | - Quality monitor   |   |                                       |
|                           |           | +---------------------+   |                                       |
|                           |           |           |               |                                       |
|                           |           | +---------------------+   |                                       |
|                           |           | | Local Stream Viewer |   |                                       |
|                           |           | | (Optional)          |   |                                       |
|                           |           | | - Debug display     |   |                                       |
|                           |           | | - Local control     |   |                                       |
|                           |           | +---------------------+   |                                       |
|                           |           |                           |                                       |
+---------------------------+           +---------------------------+                                       |
```

## Software Components

### Android Application (Java/Kotlin)

- **Main Activity**: Primary app container and navigation controller
- **Camera Control Fragment**: UI for controlling pan, tilt, zoom, and camera mode
- **Video Stream Fragment**: Displays video stream with quality monitoring
- **Connection Setup Fragment**: Handles WiFi/Bluetooth connectivity and QR code scanning
- **Settings Fragment**: Application configuration options
- **Communication Managers**:
  - **WiFi Manager**: Primary communication method
  - **Bluetooth Manager**: Backup communication method
  - **Connection Monitor**: Quality monitoring and auto-switching

### Onboard Python Server

- **Camera Server**: Main server application that initializes and coordinates all components
- **Camera Controller**: Interfaces with the PTZ camera hardware via RS-485/Pelco-D
- **Video Streamer**: Manages video capture and streaming via RTSP
- **WiFi Server**: TCP server for primary communication with the Android app
- **Bluetooth Server**: Backup communication if WiFi fails
- **Local Stream Viewer**: Optional component for local debugging with a connected monitor

## Data Flow

### Command Flow
1. User interacts with the Android app UI (Camera Control Fragment)
2. Commands are sent to the onboard server via WiFi (primary) or Bluetooth (backup)
3. Camera Server processes commands 
4. Camera Controller executes hardware control via Pelco-D protocol
5. Status updates are sent back to the Android app

### Video Flow
1. Camera captures video (RGB or IR/Thermal mode)
2. Video Streamer encodes and serves the video via RTSP
3. Android app receives and displays the video stream
4. Quality monitoring periodically checks stream quality
5. If quality deteriorates, the system can switch cameras or restart the stream

### Configuration Flow
1. QR Code Server generates configuration QR codes
2. User scans QR code with Android app
3. App automatically configures connection settings
4. App connects to the onboard server with the new settings

## Connection Fault Tolerance

The system implements several fault tolerance mechanisms:

1. **WiFi Quality Monitoring**: Checks stream quality periodically
2. **Automatic Reconnection**: Attempts to reconnect if connection is lost
3. **Bluetooth Fallback**: Switches to Bluetooth if WiFi fails for a period
4. **Service Restart**: Can automatically restart services after multiple failures
5. **WiFi Recovery**: Attempts to restore WiFi connection after Bluetooth fallback

## Software Architecture Patterns

- **Client-Server Architecture**: Clear separation between the Android app (client) and onboard software (server)
- **Component-Based Design**: Modular components with clear interfaces
- **Observer Pattern**: For status updates and event notifications
- **Command Pattern**: For camera control commands
- **Strategy Pattern**: For different communication methods (WiFi/Bluetooth)
- **Factory Pattern**: For creating different types of controllers and connections

## Key Technologies

- **Android SDK**: For the mobile application
- **OpenCV**: For camera access and video processing
- **Flask**: For the QR code configuration server
- **PyBluez**: For Bluetooth communication
- **RTSP**: For video streaming
- **Pelco-D Protocol**: For camera control via RS-485