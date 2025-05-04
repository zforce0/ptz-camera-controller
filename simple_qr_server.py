#!/usr/bin/env python3
"""
Simple QR Code Server

This script generates a QR code with WiFi or Bluetooth connection details
and serves it via an HTTP server.

Usage:
  python simple_qr_server.py [--port PORT] [--wifi IP:PORT] [--bluetooth DEVICE]
"""

import os
import sys
import json
import argparse
import http.server
import socketserver
import qrcode
from PIL import Image, ImageDraw, ImageFont

def generate_qr_code(data, output_dir="qr_output"):
    """Generate a QR code for the given data and save to output_dir."""
    # Ensure output directory exists
    os.makedirs(output_dir, exist_ok=True)
    
    # Generate QR code
    img = qrcode.make(data)
    qr_path = os.path.join(output_dir, "android_config.png")
    img.save(qr_path)
    
    print(f"QR code saved to: {qr_path}")
    return qr_path

def generate_html(config, output_dir="qr_output"):
    """Generate an HTML page with QR code and connection info."""
    html_content = f"""<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>PTZ Camera Controller - Configuration</title>
    <style>
        body {{
            font-family: Arial, sans-serif;
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
            text-align: center;
        }}
        .qr-container {{
            margin: 30px auto;
        }}
        table {{
            margin: 20px auto;
            border-collapse: collapse;
            width: 80%;
        }}
        td, th {{
            border: 1px solid #ddd;
            padding: 12px;
            text-align: left;
        }}
        th {{
            background-color: #f2f2f2;
        }}
        .footer {{
            margin-top: 40px;
            font-size: 12px;
            color: #666;
        }}
    </style>
</head>
<body>
    <h1>PTZ Camera Controller</h1>
    <h2>Configuration QR Code</h2>
    
    <div class="qr-container">
        <img src="android_config.png" alt="Configuration QR Code" width="300" />
    </div>
    
    <h3>Connection Information</h3>
    <table>
        <tr>
            <th>Setting</th>
            <th>Value</th>
        </tr>"""
    
    if "wifi" in config:
        html_content += f"""
        <tr>
            <td>Connection Type</td>
            <td>WiFi</td>
        </tr>
        <tr>
            <td>IP Address</td>
            <td>{config["wifi"].split(':')[0]}</td>
        </tr>
        <tr>
            <td>Port</td>
            <td>{config["wifi"].split(':')[1]}</td>
        </tr>"""
    
    if "bluetooth" in config:
        html_content += f"""
        <tr>
            <td>Connection Type</td>
            <td>Bluetooth</td>
        </tr>
        <tr>
            <td>Device Name</td>
            <td>{config["bluetooth"]}</td>
        </tr>"""
        
    html_content += """
    </table>
    
    <div class="instructions">
        <h3>How to use:</h3>
        <ol>
            <li>Open the PTZ Camera Controller app on your Android device</li>
            <li>Go to the Connection tab</li>
            <li>Tap "Scan QR Code" button</li>
            <li>Scan this QR code to automatically configure your connection settings</li>
        </ol>
    </div>
    
    <div class="footer">
        <p>PTZ Camera Controller - Copyright © 2025</p>
    </div>
</body>
</html>
"""
    
    html_path = os.path.join(output_dir, "index.html")
    with open(html_path, "w") as f:
        f.write(html_content)
    
    print(f"HTML page saved to: {html_path}")
    return html_path

def run_server(port, config):
    """Run a simple HTTP server to serve the QR code."""
    # Generate QR code and HTML
    data_json = json.dumps(config)
    generate_qr_code(data_json, "qr_output")
    generate_html(config, "qr_output")
    
    # Create a server directly in the current directory
    os.makedirs("qr_output", exist_ok=True)
    
    # Custom handler to serve files from qr_output directory
    class CustomHandler(http.server.SimpleHTTPRequestHandler):
        def __init__(self, *args, **kwargs):
            super().__init__(*args, directory="qr_output", **kwargs)
    
    # Create and start server
    httpd = socketserver.TCPServer(("0.0.0.0", port), CustomHandler)
    
    print(f"Serving QR code at http://0.0.0.0:{port}")
    print(f"Use http://0.0.0.0:{port} to view the QR code")
    
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("Server stopped")
    finally:
        httpd.server_close()

def main():
    """Parse arguments and run the server."""
    parser = argparse.ArgumentParser(description="Generate and serve QR codes for app configuration")
    parser.add_argument("--port", type=int, default=5001, help="Port to serve on (default: 5001)")
    parser.add_argument("--wifi", help="WiFi connection in format IP:PORT, e.g. 192.168.1.100:8000")
    parser.add_argument("--bluetooth", help="Bluetooth device name to connect to")
    
    args = parser.parse_args()
    
    # Create configuration
    config = {}
    if args.wifi:
        config["wifi"] = args.wifi
    if args.bluetooth:
        config["bluetooth"] = args.bluetooth
    
    # If no config provided, create a default one with localhost
    if not config:
        import socket
        hostname = socket.gethostname()
        try:
            local_ip = socket.gethostbyname(hostname)
        except:
            local_ip = "localhost"
        config["wifi"] = f"{local_ip}:8000"
        print(f"No connection details provided, using default: {config['wifi']}")
    
    # Run server
    run_server(args.port, config)

if __name__ == "__main__":
    main()