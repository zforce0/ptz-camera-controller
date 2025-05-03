# PTZ Camera Control System: Communication Flow

This sequence diagram illustrates the typical communication flow between the Android tablet application and the onboard computer components during normal operation of the PTZ Camera Control system.

## Key Communication Sequences

The diagram shows:
1. **Connection Establishment**: Initial connection via WiFi (primary) or Bluetooth (fallback)
2. **Camera Movement Control**: How pan/tilt commands flow through the system
3. **Camera Mode Switching**: Process for switching between RGB and IR/Thermal cameras
4. **Video Streaming**: Continuous video feed from cameras to the Android app
5. **Failover Handling**: Automatic switching to Bluetooth if WiFi connection is lost

## Sequence Diagram

```mermaid
sequenceDiagram
    participant User
    participant AndroidApp as Android Tablet App
    participant WiFi as WiFi/TCP Server
    participant BT as Bluetooth Server
    participant CameraController as Camera Controller
    participant VideoStreamer as Video Streamer
    participant Cameras as RGB/IR Cameras

    User->>AndroidApp: Open App
    AndroidApp->>WiFi: Connect (Primary)
    AndroidApp-->>BT: Connect (Fallback)
    
    Note over AndroidApp,BT: Connection Established
    
    User->>AndroidApp: Move Camera (Pan/Tilt)
    AndroidApp->>WiFi: Send Control Command
    WiFi->>CameraController: Forward Command
    CameraController->>Cameras: Adjust Camera Position
    CameraController-->>WiFi: Send Status Update
    WiFi-->>AndroidApp: Update UI
    
    Note over AndroidApp,Cameras: Camera Movement Complete
    
    User->>AndroidApp: Switch Camera Mode
    AndroidApp->>WiFi: Send Mode Change Command
    WiFi->>CameraController: Forward Command
    CameraController->>Cameras: Switch Active Camera
    CameraController->>VideoStreamer: Update Active Stream
    
    Note over VideoStreamer,AndroidApp: Continuous Video Streaming
    
    VideoStreamer->>Cameras: Capture Video
    VideoStreamer->>AndroidApp: Stream Video (RTSP)
    AndroidApp->>User: Display Video Feed
    
    User->>AndroidApp: Adjust Zoom
    AndroidApp->>WiFi: Send Zoom Command
    WiFi->>CameraController: Forward Command
    CameraController->>Cameras: Change Zoom Level
    
    Note over AndroidApp,Cameras: System Operation
    
    alt Connection Lost
        AndroidApp-xWiFi: Connection Timeout
        AndroidApp->>BT: Failover to Bluetooth
        AndroidApp->>User: Display Connection Status
    end
```