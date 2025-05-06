#!/usr/bin/env python3
"""
Start QR Code Server Wizard

This script helps start the QR code server with the appropriate configuration
based on the user's requirements.
"""
import os
import sys
import argparse
import subprocess
import socket

def get_local_ip():
    """Get the local IP address of this machine."""
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        local_ip = s.getsockname()[0]
        s.close()
        return local_ip
    except Exception:
        return "127.0.0.1"

def print_header():
    """Print the script header."""
    print("\n=========================================")
    print("       PTZ Camera QR Code Server        ")
    print("=========================================\n")

def print_menu():
    """Print the main menu."""
    print("\nChoose a server configuration:")
    print("1. Start with default settings")
    print("2. Start with auto-detected IP address")
    print("3. Start with custom settings")
    print("4. View local IP address")
    print("5. Exit")
    choice = input("\nEnter your choice (1-5): ")
    return choice

def start_default_server():
    """Start the QR code server with default settings."""
    print("\nStarting QR code server with default settings...")
    subprocess.Popen([sys.executable, "flask_qr_server.py"], stdout=sys.stdout, stderr=sys.stderr)
    print(f"\nQR code server started on port 5000")
    print(f"Access the QR codes at http://localhost:5000")

def start_auto_ip_server():
    """Start the QR code server with auto-detected IP address."""
    ip = get_local_ip()
    print(f"\nAuto-detected IP address: {ip}")
    print("Starting QR code server with auto-detected IP address...")
    subprocess.Popen([sys.executable, "flask_qr_server.py", "--auto-detect-ip"], stdout=sys.stdout, stderr=sys.stderr)
    print(f"\nQR code server started on port 5000")
    print(f"Access the QR codes at http://localhost:5000")

def get_custom_settings():
    """Get custom settings from the user."""
    print("\nEnter custom settings (press Enter to use defaults):")
    
    port = input("Web server port [5000]: ")
    port = port if port else "5000"
    
    auto_ip = input("Auto-detect IP address? [y/N]: ").lower()
    auto_ip = auto_ip.startswith('y')
    
    if not auto_ip:
        wifi = input("WiFi connection (IP:PORT) [192.168.1.100:8000]: ")
        wifi = wifi if wifi else "192.168.1.100:8000"
    else:
        wifi = ""
    
    bluetooth = input("Bluetooth device name [PTZ Camera Server]: ")
    bluetooth = bluetooth if bluetooth else "PTZ Camera Server"
    
    camera = input("Camera name [PTZ Camera System]: ")
    camera = camera if camera else "PTZ Camera System"
    
    rtsp = input("RTSP stream URL [rtsp://192.168.1.100:8554/stream]: ")
    rtsp = rtsp if rtsp else "rtsp://192.168.1.100:8554/stream"
    
    features = input("Features (comma-separated) [pan,tilt,zoom,switch_mode]: ")
    features = features if features else "pan,tilt,zoom,switch_mode"
    
    app_url = input("App download URL [https://github.com/user/ptz-camera-controller/releases/latest/download/ptz-camera-controller.apk]: ")
    app_url = app_url if app_url else "https://github.com/user/ptz-camera-controller/releases/latest/download/ptz-camera-controller.apk"
    
    version = input("App version [1.0.0]: ")
    version = version if version else "1.0.0"
    
    return {
        "port": port,
        "auto_ip": auto_ip,
        "wifi": wifi,
        "bluetooth": bluetooth,
        "camera": camera,
        "rtsp": rtsp,
        "features": features,
        "app_url": app_url,
        "version": version
    }

def start_custom_server(settings):
    """Start the QR code server with custom settings."""
    print("\nStarting QR code server with custom settings...")
    
    cmd = [sys.executable, "flask_qr_server.py", f"--port", settings["port"]]
    
    if settings["auto_ip"]:
        cmd.append("--auto-detect-ip")
    elif settings["wifi"]:
        cmd.extend(["--wifi", settings["wifi"]])
    
    if settings["bluetooth"]:
        cmd.extend(["--bluetooth", settings["bluetooth"]])
        
    if settings["camera"]:
        cmd.extend(["--camera", settings["camera"]])
        
    if settings["rtsp"]:
        cmd.extend(["--rtsp", settings["rtsp"]])
        
    if settings["features"]:
        cmd.extend(["--features", settings["features"]])
        
    if settings["app_url"]:
        cmd.extend(["--app-url", settings["app_url"]])
        
    if settings["version"]:
        cmd.extend(["--version", settings["version"]])
    
    subprocess.Popen(cmd, stdout=sys.stdout, stderr=sys.stderr)
    print(f"\nQR code server started on port {settings['port']}")
    print(f"Access the QR codes at http://localhost:{settings['port']}")

def main():
    """Main function."""
    print_header()
    
    while True:
        choice = print_menu()
        
        if choice == "1":
            start_default_server()
            break
        elif choice == "2":
            start_auto_ip_server()
            break
        elif choice == "3":
            settings = get_custom_settings()
            start_custom_server(settings)
            break
        elif choice == "4":
            ip = get_local_ip()
            print(f"\nLocal IP address: {ip}")
            input("\nPress Enter to continue...")
        elif choice == "5":
            print("\nExiting...")
            break
        else:
            print("\nInvalid choice. Please try again.")

if __name__ == "__main__":
    main()