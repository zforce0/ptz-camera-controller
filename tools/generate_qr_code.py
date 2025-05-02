#!/usr/bin/env python3
"""
QR Code Generator for PTZ Camera Controller App

This script generates a QR code image and an HTML page that can be used
to easily install the PTZ Camera Controller app on Android tablets.

Usage:
  python generate_qr_code.py [APK_URL] [OUTPUT_DIR]

Arguments:
  APK_URL    - URL to the APK file (defaults to GitHub releases URL)
  OUTPUT_DIR - Directory to save the QR code and HTML (defaults to 'qr_output')
"""

import os
import sys
import argparse
import qrcode
from PIL import Image, ImageDraw, ImageFont
from datetime import datetime

def generate_qr_code(url, output_dir, repository="zforce0/ptz-camera-controller"):
    """Generate a QR code for the given URL."""
    # Create output directory if it doesn't exist
    os.makedirs(output_dir, exist_ok=True)
    
    # Generate QR code
    qr = qrcode.QRCode(
        version=1,
        error_correction=qrcode.constants.ERROR_CORRECT_L,
        box_size=10,
        border=4,
    )
    qr.add_data(url)
    qr.make(fit=True)
    
    # Create QR code image with logo
    qr_img = qr.make_image(fill_color="black", back_color="white")
    
    # Save the QR code image
    qr_img_path = os.path.join(output_dir, "ptz_controller_qr.png")
    qr_img.save(qr_img_path)
    print(f"QR code saved to: {qr_img_path}")
    
    # Generate HTML installer page
    html_path = os.path.join(output_dir, "install.html")
    with open(html_path, 'w') as f:
        f.write(f"""<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>PTZ Camera Controller - Installation</title>
    <style>
        body {{
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background-color: #f8f9fa;
            margin: 0;
            padding: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
        }}
        .container {{
            background-color: white;
            border-radius: 10px;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
            padding: 30px;
            max-width: 800px;
            width: 90%;
            text-align: center;
        }}
        h1 {{
            color: #2c3e50;
            margin-bottom: 10px;
        }}
        .qr-section {{
            display: flex;
            flex-direction: column;
            align-items: center;
            margin: 30px 0;
            padding: 20px;
            background-color: #f7f9fc;
            border-radius: 10px;
        }}
        .qr-code {{
            margin: 20px 0;
            padding: 15px;
            background-color: white;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
        }}
        .qr-code img {{
            max-width: 300px;
            height: auto;
        }}
        .download-button {{
            display: inline-block;
            background-color: #3498db;
            color: white;
            padding: 12px 25px;
            border-radius: 30px;
            text-decoration: none;
            font-weight: bold;
            margin-top: 15px;
            transition: background-color 0.3s;
        }}
        .download-button:hover {{
            background-color: #2980b9;
        }}
        .instructions {{
            text-align: left;
            margin-top: 20px;
            padding: 20px;
            background-color: #f8f9fa;
            border-radius: 10px;
        }}
        footer {{
            margin-top: 30px;
            color: #7f8c8d;
            font-size: 0.9em;
        }}
    </style>
</head>
<body>
    <div class="container">
        <h1>PTZ Camera Controller</h1>
        <p>Android Tablet Application</p>
        
        <div class="qr-section">
            <h3>Scan this QR code with your Android tablet</h3>
            <div class="qr-code">
                <img src="ptz_controller_qr.png" alt="QR Code for Installation">
            </div>
            <a href="{url}" class="download-button">Direct Download Link</a>
        </div>
        
        <div class="instructions">
            <h3>Installation Instructions:</h3>
            <ol>
                <li>Scan the QR code with your Android tablet's camera</li>
                <li>Download the APK file</li>
                <li>Enable installation from unknown sources in Settings > Security</li>
                <li>Open the downloaded APK to install</li>
                <li>Launch the app and connect to your onboard computer</li>
            </ol>
        </div>
        
        <footer>
            PTZ Camera Controller | Generated on {datetime.now().strftime('%Y-%m-%d')}
        </footer>
    </div>
</body>
</html>
""")
    print(f"HTML installer page saved to: {html_path}")
    
    return qr_img_path, html_path

def main():
    parser = argparse.ArgumentParser(description='Generate QR code for PTZ Camera Controller app installation')
    parser.add_argument('url', nargs='?', default='https://github.com/zforce0/ptz-camera-controller/releases/latest/download/ptz-camera-controller.apk',
                        help='URL to the APK file')
    parser.add_argument('output_dir', nargs='?', default='qr_output',
                        help='Directory to save the QR code and HTML')
    args = parser.parse_args()
    
    qr_path, html_path = generate_qr_code(args.url, args.output_dir)
    print("\nUse these files to share the app with users.")
    print(f"Open {html_path} in a browser to test the installation page.")

if __name__ == "__main__":
    main()