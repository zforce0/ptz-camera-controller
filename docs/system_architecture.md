# PTZ Camera Controller System Architecture

This document provides a comprehensive view of the PTZ Camera Controller system architecture, showing how components interact across both the Android tablet application and the onboard computer server.

## System Overview

The PTZ Camera Controller system consists of two main parts:
1. **Android Tablet Application**: User interface for controlling cameras and viewing video streams
2. **Onboard Computer Server**: Backend software running on Raspberry Pi or NVIDIA Jetson that interfaces with the cameras

The system supports dual communication methods (WiFi and Bluetooth) for flexibility and redundancy, and handles multiple camera types (RGB and IR/Thermal).

## Architecture Diagram

```mermaid
graph TD
    %% Main System Components
    AndroidApp[Android Tablet App]
    OnboardServer[Onboard Computer Server]
    
    %% Android App Components
    AndroidApp --> MainActivityKt[MainActivity.kt]
    MainActivityKt --> CameraFragmentKt[CameraControlFragment.kt]
    MainActivityKt --> StreamFragmentKt[VideoStreamFragment.kt]
    MainActivityKt --> SettingsFragmentKt[SettingsFragment.kt]
    MainActivityKt --> UpdateCheckerKt[UpdateChecker.kt]
    
    %% Android App Internal Components
    CameraFragmentKt --> PTZController[PTZ Controls]
    CameraFragmentKt --> ModeSwitcher[RGB/IR Mode Switching]
    StreamFragmentKt --> ExoPlayer[ExoPlayer Video Player]
    SettingsFragmentKt --> ConnectionSettings[Connection Settings]
    
    %% Onboard Server Components
    OnboardServer --> CameraServerPy[camera_server.py]
    
    %% Camera Server Components
    CameraServerPy --> CameraControllerPy[camera_controller.py]
    CameraServerPy --> VideoStreamerPy[video_streamer.py]
    CameraServerPy --> CommModulePy{Communication Modules}
    
    %% Communication Components
    CommModulePy --> WifiServerPy[wifi_server.py]
    CommModulePy --> BTServerPy[bt_server.py]
    
    %% Camera Controller Components
    CameraControllerPy --> HWInterface[Hardware Interface]
    HWInterface --> RGBCamera[RGB Camera]
    HWInterface --> IRCamera[IR/Thermal Camera]
    HWInterface --> PTZMotors[PTZ Motors]
    
    %% Video Streamer Components
    VideoStreamerPy --> RTSPStreams[RTSP Streams]
    RTSPStreams --> MainStream[High Quality Stream]
    RTSPStreams --> SubStream[Low Quality Stream]
    VideoStreamerPy --> LocalStreamViewerPy[local_stream_viewer.py]
    
    %% Communication Channels
    AndroidApp <--WiFi/TCP--> WifiServerPy
    AndroidApp <--Bluetooth--> BTServerPy
    StreamFragmentKt <--RTSP Video Streams--> RTSPStreams
    
    %% Installation & Distribution
    GitHubRelease[GitHub Releases]
    QRCodeInstaller[QR Code Installer]
    GitHubWorkflow[GitHub Actions Workflow]
    
    %% Distribution Flow
    GitHubWorkflow --> GitHubRelease
    GitHubRelease --> QRCodeInstaller
    QRCodeInstaller --> AndroidApp
    
    %% Styling
    classDef android fill:#a5d6a7,stroke:#1b5e20,color:#1b5e20
    classDef python fill:#bbdefb,stroke:#0d47a1,color:#0d47a1
    classDef hardware fill:#ffcc80,stroke:#e65100,color:#e65100
    classDef deployment fill:#e1bee7,stroke:#4a148c,color:#4a148c
    classDef communication fill:#fff59d,stroke:#f57f17,color:#f57f17
    
    %% Apply Styles
    class AndroidApp,MainActivityKt,CameraFragmentKt,StreamFragmentKt,SettingsFragmentKt,UpdateCheckerKt,PTZController,ModeSwitcher,ExoPlayer,ConnectionSettings android
    class OnboardServer,CameraServerPy,CameraControllerPy,VideoStreamerPy,WifiServerPy,BTServerPy,LocalStreamViewerPy python
    class HWInterface,RGBCamera,IRCamera,PTZMotors hardware
    class GitHubRelease,QRCodeInstaller,GitHubWorkflow deployment
    class CommModulePy,RTSPStreams,MainStream,SubStream communication
```

## Component Descriptions

### Android Tablet Application

| Component | Description |
|-----------|-------------|
| **MainActivity.kt** | Main entry point for the Android app, handles navigation between fragments and overall app lifecycle. |
| **CameraControlFragment.kt** | User interface for controlling pan, tilt, and zoom functions of the camera. |
| **VideoStreamFragment.kt** | Displays the video stream from the active camera (RGB or IR/Thermal). |
| **SettingsFragment.kt** | Configuration interface for connection settings, stream quality, and app preferences. |
| **UpdateChecker.kt** | Checks for app updates from GitHub releases and notifies users. |
| **ExoPlayer** | Android media player used to display RTSP video streams. |

### Onboard Computer Server

| Component | Description |
|-----------|-------------|
| **camera_server.py** | Main server application that initializes and manages all server-side components. |
| **camera_controller.py** | Interfaces with the physical cameras, handling pan, tilt, zoom operations and mode switching between RGB and IR. |
| **video_streamer.py** | Manages video streaming from cameras to the Android tablet via RTSP protocol. |
| **wifi_server.py** | Handles HTTP/TCP communication with the Android app for commands and status updates. |
| **bt_server.py** | Provides Bluetooth communication capability as an alternative to WiFi. |
| **local_stream_viewer.py** | Optional module for viewing camera streams directly on the onboard computer when a monitor is connected. |

### Hardware Interface

| Component | Description |
|-----------|-------------|
| **RGB Camera** | Daylight camera for normal operation. |
| **IR/Thermal Camera** | Night vision or thermal camera for low-light or heat detection scenarios. |
| **PTZ Motors** | Motors controlling the physical pan, tilt, and zoom movements of the camera system. |

### Deployment & Distribution

| Component | Description |
|-----------|-------------|
| **GitHub Actions Workflow** | Automated CI/CD pipeline for building the Android APK when new versions are tagged. |
| **GitHub Releases** | Hosts the compiled APK files and release information. |
| **QR Code Installer** | Generated QR codes that simplify installation on Android tablets. |

## Communication Flows

1. **Control Commands**: Android app → WiFi/Bluetooth → Onboard server → Camera controller → PTZ motors
2. **Video Feed**: Cameras → Video streamer → RTSP streams → Android app display
3. **Status Updates**: Camera controller → Communication modules → Android app

For a detailed sequence diagram of the communication flow, see the [Communication Flow Documentation](communication_flow.md).

For information on how the system components are deployed across physical devices, see the [Deployment Architecture Documentation](deployment_architecture.md).