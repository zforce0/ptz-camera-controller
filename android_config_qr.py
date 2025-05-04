#!/usr/bin/env python3
"""
Android Configuration QR Code Generator

This script generates QR codes for easily configuring the Android PTZ Camera Controller app.
It creates both the QR code image and an HTML page with connection information.

The QR code contains connection details (WiFi/Bluetooth) that the app can scan to
automatically configure its connection settings.

Usage:
  python android_config_qr.py [--wifi IP:PORT] [--bluetooth DEVICE_NAME] [--output DIR]
  
Example:
  python android_config_qr.py --wifi 192.168.1.100:8000 --output ./qr_codes
"""

import os
import sys
import json
import argparse
import qrcode
from PIL import Image, ImageDraw, ImageFont

def generate_config_qr(config, output_dir="qr_output"):
    """Generate a QR code with configuration information."""
    
    # Ensure output directory exists
    os.makedirs(output_dir, exist_ok=True)
    
    # Convert config to JSON
    config_json = json.dumps(config)
    
    # Generate QR code
    qr = qrcode.QRCode(
        version=1,
        error_correction=qrcode.ERROR_CORRECT_L,
        box_size=10,
        border=4,
    )
    qr.add_data(config_json)
    qr.make(fit=True)
    
    # Create QR code image
    img = qr.make_image(fill_color="black", back_color="white")
    qr_path = os.path.join(output_dir, "android_config.png")
    img.save(qr_path)
    
    # Generate HTML with connection info
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
    
    print(f"QR code saved to: {qr_path}")
    print(f"HTML page saved to: {html_path}")
    
    return qr_path, html_path

def parse_args():
    """Parse command line arguments."""
    parser = argparse.ArgumentParser(description="Generate QR code for Android app configuration")
    parser.add_argument("--wifi", help="WiFi connection details in format IP:PORT, e.g. 192.168.1.100:8000")
    parser.add_argument("--bluetooth", help="Bluetooth device name to connect to")
    parser.add_argument("--output", default="qr_output", help="Output directory for QR code and HTML (default: qr_output)")
    
    args = parser.parse_args()
    
    # Ensure at least one connection method is specified
    if not args.wifi and not args.bluetooth:
        parser.error("At least one connection method (--wifi or --bluetooth) must be specified")
    
    return args

def main():
    """Main function."""
    args = parse_args()
    
    # Create configuration
    config = {}
    if args.wifi:
        config["wifi"] = args.wifi
    if args.bluetooth:
        config["bluetooth"] = args.bluetooth
    
    # Generate QR code
    generate_config_qr(config, args.output)

if __name__ == "__main__":
    main()