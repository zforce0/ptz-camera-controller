# PTZ Camera Controller Documentation

Welcome to the documentation for the PTZ Camera Controller system. This documentation provides comprehensive information about the system architecture, communication flows, and deployment considerations.

## Core Documentation

| Document | Description |
|----------|-------------|
| [System Architecture](system_architecture.md) | Overall system structure and component interactions |
| [Communication Flow](communication_flow.md) | Sequence diagrams showing how components communicate |
| [Deployment Architecture](deployment_architecture.md) | Physical deployment across devices and hardware interfaces |
| [Development Roadmap](development_roadmap.md) | Planned features and development timeline |
| [JDK 17 Upgrade Notes](release_notes_jdk17.md) | Details on the migration from JDK 11 to JDK 17 |

## Additional Resources

- [Project README](../README.md) - Main project overview and getting started guide
- [Installation Guide](../installation_guide.html) - Detailed installation instructions
- [QR Code Installer](../qr_code_installer.html) - QR code for easy Android app installation
- [Release Build Instructions](../release_build_instructions.md) - Instructions for building release versions

## Development References

The PTZ Camera Controller system is divided into two main components:

1. **Android Application** - User interface for controlling cameras and viewing video streams
2. **Onboard Python Server** - Backend software running on the onboard computer (Raspberry Pi or NVIDIA Jetson)

### Android App Structure

The Android application is built using Kotlin and follows a fragment-based architecture:

- MainActivity - Entry point and navigation controller
- CameraControlFragment - PTZ controls user interface
- VideoStreamFragment - Video streaming display
- SettingsFragment - Configuration options

### Onboard Server Structure

The onboard server is built using Python and follows a modular architecture:

- camera_server.py - Main server application
- camera_controller.py - Camera hardware interface
- video_streamer.py - Video streaming management
- wifi_server.py - WiFi communication interface
- bt_server.py - Bluetooth communication interface
- local_stream_viewer.py - Optional local monitoring