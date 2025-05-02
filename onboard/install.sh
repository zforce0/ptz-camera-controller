#!/bin/bash
# Installation script for PTZ Camera Controller (Server side)
# Run this script on your Raspberry Pi or NVIDIA Jetson

# Error handling
set -e
trap 'echo "Error: Command failed at line $LINENO. Exiting..."; exit 1' ERR

echo "====================================================="
echo "PTZ Camera Controller - Server Installation"
echo "====================================================="

# Check if running as root
if [ "$EUID" -ne 0 ]; then
    echo "Please run as root (use sudo)"
    exit 1
fi

# Install system dependencies
echo "[1/5] Installing system dependencies..."
apt-get update
apt-get install -y python3 python3-pip python3-opencv python3-numpy \
    python3-dev python3-websockets python3-bluetooth \
    bluetooth libopencv-dev ffmpeg

# Install Python packages
echo "[2/5] Installing Python packages..."
pip3 install --upgrade pip
pip3 install numpy opencv-python websockets pybluez pyserial

# Create service directory
echo "[3/5] Creating installation directory..."
mkdir -p /opt/ptz-camera-controller
cp -r ./* /opt/ptz-camera-controller/

# Create systemd service
echo "[4/5] Creating systemd service..."
cat > /etc/systemd/system/ptz-camera-controller.service << 'EOF'
[Unit]
Description=PTZ Camera Controller Server
After=network.target bluetooth.target
Wants=bluetooth.target

[Service]
Type=simple
ExecStart=/usr/bin/python3 /opt/ptz-camera-controller/onboard/camera_server.py
WorkingDirectory=/opt/ptz-camera-controller
Restart=always
RestartSec=5
StandardOutput=syslog
StandardError=syslog
SyslogIdentifier=ptz-camera
User=root

[Install]
WantedBy=multi-user.target
EOF

# Enable and start the service
echo "[5/5] Enabling and starting service..."
systemctl daemon-reload
systemctl enable ptz-camera-controller.service
systemctl start ptz-camera-controller.service

# Print IP address
IP_ADDRESS=$(hostname -I | awk '{print $1}')
echo "====================================================="
echo "Installation complete!"
echo "The server is now running as a system service."
echo ""
echo "Your IP address is: $IP_ADDRESS"
echo "Use this IP address to connect from the Android app."
echo ""
echo "Command to check service status:"
echo "  sudo systemctl status ptz-camera-controller"
echo ""
echo "Command to view logs:"
echo "  sudo journalctl -u ptz-camera-controller -f"
echo "====================================================="