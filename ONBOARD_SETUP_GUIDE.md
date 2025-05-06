# PTZ Camera Controller - Onboard Server Installation Guide

This guide provides step-by-step instructions for setting up the PTZ Camera Controller server side components on a Raspberry Pi or NVIDIA Jetson Orin Nano device.

## System Requirements

- Raspberry Pi 3/4/5 or NVIDIA Jetson Orin Nano
- Debian-based OS (Raspbian, Ubuntu, etc.)
- Internet connection for package installation
- At least 2GB of free storage space
- Administrative (sudo) privileges

## Installation Steps

### 1. Download the Installation Package

First, download the PTZ Camera Controller server package to your device:

```bash
# Create a directory for the installation
mkdir -p ~/ptz-controller
cd ~/ptz-controller

# Download the latest release (replace URL with actual release URL)
wget https://github.com/your-repo/ptz-camera-controller/releases/latest/download/onboard-server.tar.gz
tar -xzf onboard-server.tar.gz
```

### 2. Run the Installation Script

The installation script will set up all necessary components automatically:

```bash
# Navigate to the extracted directory
cd onboard-server

# Run the installation script with sudo
sudo bash install.sh
```

The installation script performs the following tasks:
- Installs system dependencies (Python, OpenCV, Bluetooth libraries, etc.)
- Creates a Python virtual environment
- Installs required Python packages
- Sets up Bluetooth connectivity
- Creates and starts a system service

### 3. Verify Installation

After installation completes, verify that the service is running:

```bash
sudo systemctl status ptz-camera-controller
```

You should see output indicating that the service is active (running).

### 4. Connecting with the Android App

The installation script will display your device's IP address. You'll need this address to connect from the Android app.

There are two methods to connect your Android tablet to the server:

#### Method 1: Direct QR Code Scan

1. On your Android tablet, open the PTZ Camera Controller app
2. Tap "Scan QR Code" on the connection screen
3. Point the camera at the QR code displayed on your server's screen (if using a device with display)

#### Method 2: Using the QR Code Server

If your device doesn't have a display or you prefer remote setup:

1. From any computer on the same network, open a web browser
2. Navigate to `http://[SERVER-IP]:5000` (replace [SERVER-IP] with your device's IP address)
3. Scan the displayed QR code with the Android app

## Advanced Configuration

### Customizing the Service

To modify the service configuration:

1. Edit the service file:
   ```bash
   sudo nano /etc/systemd/system/ptz-camera-controller.service
   ```

2. After making changes, reload and restart the service:
   ```bash
   sudo systemctl daemon-reload
   sudo systemctl restart ptz-camera-controller
   ```

### Camera Configuration

The default configuration works with most PTZ cameras supporting the Pelco-D protocol. To customize camera settings:

1. Edit the configuration file:
   ```bash
   sudo nano /opt/ptz-camera-controller/config/camera_config.json
   ```

2. Restart the service after making changes:
   ```bash
   sudo systemctl restart ptz-camera-controller
   ```

### Bluetooth Connectivity

The server supports Bluetooth connections for environments where WiFi is unavailable or unreliable:

1. Ensure Bluetooth is enabled on your device:
   ```bash
   sudo systemctl status bluetooth
   ```

2. The server automatically advertises its Bluetooth service when running.

## Troubleshooting

### Viewing Logs

To view the server logs:

```bash
sudo journalctl -u ptz-camera-controller -f
```

### Common Issues

1. **Service fails to start**:
   - Check dependencies: `sudo apt-get install -f`
   - Verify Python virtual environment: `ls -la /opt/ptz-camera-controller/venv`

2. **Bluetooth connectivity issues**:
   - Ensure Bluetooth is enabled: `sudo systemctl status bluetooth`
   - Check Bluetooth adapter: `hciconfig`

3. **Camera control not working**:
   - Verify serial connection: `ls -la /dev/ttyUSB*` or `ls -la /dev/ttyACM*`
   - Check camera settings in configuration file

## Uninstallation

To completely remove the PTZ Camera Controller server:

```bash
# Stop and disable the service
sudo systemctl stop ptz-camera-controller
sudo systemctl disable ptz-camera-controller

# Remove service file
sudo rm /etc/systemd/system/ptz-camera-controller.service
sudo systemctl daemon-reload

# Remove installation files
sudo rm -rf /opt/ptz-camera-controller
```

## Support and Resources

For additional help, check the following resources:
- GitHub repository: [github.com/your-repo/ptz-camera-controller](https://github.com/your-repo/ptz-camera-controller)
- Documentation: [docs.ptz-controller.com](https://docs.ptz-controller.com)
- Community forum: [forum.ptz-controller.com](https://forum.ptz-controller.com)