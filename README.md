# PTZ Camera Controller - Onboard Server

## Overview

The onboard server component of the PTZ Camera Controller system runs on a Raspberry Pi or NVIDIA Jetson device. It communicates with PTZ cameras via the Pelco-D protocol and provides a communication bridge to the Android application through either WiFi or Bluetooth.

## Key Features

- **Dual Connectivity**: Connect via WiFi or Bluetooth for flexible deployment options
- **QR Code Configuration**: Easy setup through scannable QR codes
- **Camera Control**: Full PTZ (Pan, Tilt, Zoom) camera control using the Pelco-D protocol
- **Preset Management**: Save and recall camera positions
- **Video Streaming**: Optional RTSP video stream support
- **Auto-Discovery**: Network device discovery for easy setup
- **Systemd Service**: Runs as a system service for reliability

## Documentation

- [Quick Start Guide](QUICK_START.md) - Get up and running quickly
- [Full Installation Guide](ONBOARD_SETUP_GUIDE.md) - Detailed setup instructions
- [System Architecture](system_diagram.svg) - Visual overview of system components

## System Requirements

### Hardware
- Raspberry Pi 3/4/5 or NVIDIA Jetson Orin Nano
- USB-to-RS485 adapter for camera control
- PTZ camera with Pelco-D protocol support
- Ethernet connection or WiFi adapter
- (Optional) Bluetooth adapter if using Bluetooth connectivity

### Software
- Debian-based Linux OS (Raspbian, Ubuntu, etc.)
- Python 3.7+ (installed by setup script)

## Installation

For detailed installation instructions, see the [Full Installation Guide](ONBOARD_SETUP_GUIDE.md).

Quick installation:

```bash
# Download installation package
wget https://github.com/your-repo/ptz-camera-controller/releases/latest/download/onboard-server.tar.gz
tar -xzf onboard-server.tar.gz
cd onboard-server

# Run installation script
sudo bash install.sh
```

## Usage

After installation, the server runs as a system service and starts automatically on boot.

### Service Management

```bash
# Check service status
sudo systemctl status ptz-camera-controller

# Restart service
sudo systemctl restart ptz-camera-controller

# Stop service
sudo systemctl stop ptz-camera-controller

# View logs
sudo journalctl -u ptz-camera-controller -f
```

### QR Code Configuration

The server provides a web interface for QR code configuration:

1. Access `http://[SERVER-IP]:5000` from any browser on the same network
2. Scan the QR code with the Android app to establish a connection

## Development

The onboard server code is located in the `onboard` directory. Key components:

- `camera_server.py` - Main server application
- `bt_server.py` - Bluetooth communication module
- `ptz_controller.py` - Camera control interface
- `config.py` - Configuration management

### Development Tools

Several utilities are included to aid in development and testing:

- `flask_qr_server.py` - Web-based QR code generator for device configuration
  - Usage: `python flask_qr_server.py [--port PORT] [--wifi IP:PORT] [--bluetooth DEVICE]`
  - With auto-detection: `python flask_qr_server.py --auto-detect-ip --port 5001`

- `setup_test_environment.py` - Creates a complete test environment with mock services
  - Usage: `python setup_test_environment.py [--with-stream] [--port PORT]`
  - Creates a camera control server, QR code server, and optionally a mock video stream

- `start_qr_server.py` - Interactive setup wizard for the QR code server
  - Usage: `python start_qr_server.py`
  - Walks through the configuration options with a menu-based interface

- `generate_sdk_qr.py` - Generate QR codes for SDK configuration sharing
  - Usage: `python generate_sdk_qr.py [OUTPUT_DIR]`

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues and support, please file an issue on our GitHub repository or contact our support team.