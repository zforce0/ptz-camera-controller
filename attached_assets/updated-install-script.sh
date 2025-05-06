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
echo "[1/6] Installing system dependencies..."
apt-get update
apt-get install -y python3 python3-pip python3-opencv python3-numpy \
    python3-dev python3-websockets \
    bluetooth libopencv-dev ffmpeg python3-venv python3-full \
    libbluetooth-dev pkg-config libboost-python-dev libboost-thread-dev \
    libglib2.0-dev python3-bluez

# Create virtual environment
echo "[2/6] Creating Python virtual environment..."
mkdir -p /opt/ptz-camera-controller
python3 -m venv /opt/ptz-camera-controller/venv

# Activate virtual environment for the current script
source /opt/ptz-camera-controller/venv/bin/activate

# Install Python packages in the virtual environment
echo "[3/6] Installing Python packages in virtual environment..."
pip install --upgrade pip
pip install numpy opencv-python websockets pyserial bluepy

# Install system bluez Python bindings
echo "[4/6] Creating symlink to system bluez Python bindings..."
# Create a symlink from the system-installed bluez module to our virtual environment
# This is a workaround for the pybluez installation issues
SITE_PACKAGES=$(python -c "import site; print(site.getsitepackages()[0])")
SYSTEM_BLUEZ=$(find /usr/lib -path "*/dist-packages/bluetooth" -type d | head -n 1)

if [ -n "$SYSTEM_BLUEZ" ]; then
    echo "Found system bluetooth module at $SYSTEM_BLUEZ"
    if [ ! -d "$SITE_PACKAGES/bluetooth" ]; then
        ln -s "$SYSTEM_BLUEZ" "$SITE_PACKAGES/bluetooth"
        echo "Created symlink from $SYSTEM_BLUEZ to $SITE_PACKAGES/bluetooth"
    else
        echo "Bluetooth module already exists in virtual environment"
    fi
else
    echo "Warning: Could not find system bluetooth module. Bluetooth functionality may not work."
fi

# Copy application files
echo "[5/6] Copying application files..."
cp -r ./* /opt/ptz-camera-controller/

# Create systemd service with updated path to Python interpreter
echo "[6/6] Creating systemd service..."
cat > /etc/systemd/system/ptz-camera-controller.service << 'EOF'
[Unit]
Description=PTZ Camera Controller Server
After=network.target bluetooth.target
Wants=bluetooth.target

[Service]
Type=simple
ExecStart=/opt/ptz-camera-controller/venv/bin/python /opt/ptz-camera-controller/onboard/camera_server.py
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

# Deactivate virtual environment
deactivate

# Enable and start the service
echo "Enabling and starting service..."
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