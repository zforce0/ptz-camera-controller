#!/usr/bin/env python3
"""
Android SDK QR Code Generator

This script generates a QR code and HTML page with information about the Android SDK
setup in this project. It can be used to easily share SDK configuration with other developers.

Usage:
  python generate_sdk_qr.py [OUTPUT_DIR]

Arguments:
  OUTPUT_DIR - Directory to save the QR code and HTML (defaults to current directory)
"""

import os
import sys
import argparse
import qrcode
from PIL import Image, ImageDraw, ImageFont
from datetime import datetime
import json
import shutil

def get_sdk_info():
    """Collect SDK information from the project files."""
    sdk_info = {
        "project_name": "PTZ Camera Controller",
        "sdk_location": None,
        "jdk_version": "17",
        "gradle_version": None,
        "build_tools_version": "34.0.0",  # Set default value for build tools (matches compile SDK)
        "compile_sdk": None
    }
    
    # Read local.properties
    try:
        with open("local.properties", "r") as f:
            for line in f:
                if line.startswith("sdk.dir="):
                    sdk_info["sdk_location"] = line.strip().split("=", 1)[1]
    except FileNotFoundError:
        pass
    
    # Read app/build.gradle
    try:
        with open("app/build.gradle", "r") as f:
            content = f.read()
            
            # Extract compileSdk
            import re
            compile_sdk_match = re.search(r'compileSdk\s+(\d+)', content)
            if compile_sdk_match:
                sdk_info["compile_sdk"] = compile_sdk_match.group(1)
                
            # Extract build tools version
            build_tools_match = re.search(r'buildToolsVersion\s+[\'"](.+?)[\'"]', content)
            if build_tools_match:
                sdk_info["build_tools_version"] = build_tools_match.group(1)
    except FileNotFoundError:
        pass
    
    # Try to get Gradle version from wrapper properties
    try:
        with open("gradle/wrapper/gradle-wrapper.properties", "r") as f:
            for line in f:
                if "distributionUrl" in line:
                    # Extract version from URL like https://services.gradle.org/distributions/gradle-8.2-bin.zip
                    url = line.strip().split("=", 1)[1]
                    version_part = url.split("/")[-1]
                    gradle_version = version_part.split("-")[1]
                    sdk_info["gradle_version"] = gradle_version
    except FileNotFoundError:
        pass
    
    return sdk_info

def generate_qr_code(sdk_info, output_dir="."):
    """Generate a QR code with SDK information."""
    # Create output directory if it doesn't exist
    os.makedirs(output_dir, exist_ok=True)
    
    # Prepare data for QR code
    qr_data = json.dumps(sdk_info, indent=2)
    
    # Generate QR code - using qrcode's simpler API to avoid LSP issues
    # while maintaining the same functionality
    qr_img = qrcode.make(qr_data)
    
    # The make() function returns a PIL Image object directly
    
    # Save the QR code image
    qr_img_path = os.path.join(output_dir, "android_sdk_info_qr.png")
    qr_img.save(qr_img_path)
    print(f"QR code saved to: {qr_img_path}")
    
    # Generate HTML page
    html_path = os.path.join(output_dir, "android_sdk_info.html")
    with open(html_path, 'w') as f:
        f.write(f"""<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Android SDK Configuration - {sdk_info['project_name']}</title>
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
        }}
        h1 {{
            color: #2c3e50;
            margin-bottom: 10px;
            text-align: center;
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
            text-align: center;
        }}
        .qr-code img {{
            max-width: 300px;
            height: auto;
        }}
        .info-section {{
            margin-top: 20px;
            padding: 20px;
            background-color: #f8f9fa;
            border-radius: 10px;
        }}
        table {{
            width: 100%;
            border-collapse: collapse;
            margin: 10px 0;
        }}
        table, th, td {{
            border: 1px solid #ddd;
        }}
        th, td {{
            padding: 12px;
            text-align: left;
        }}
        th {{
            background-color: #f2f2f2;
        }}
        footer {{
            margin-top: 30px;
            color: #7f8c8d;
            font-size: 0.9em;
            text-align: center;
        }}
    </style>
</head>
<body>
    <div class="container">
        <h1>Android SDK Configuration</h1>
        <p style="text-align: center;">{sdk_info['project_name']}</p>
        
        <div class="qr-section">
            <h3>Scan this QR code to get SDK configuration</h3>
            <div class="qr-code">
                <img src="android_sdk_info_qr.png" alt="QR Code with SDK Information">
            </div>
        </div>
        
        <div class="info-section">
            <h3>SDK Configuration Details:</h3>
            <table>
                <tr>
                    <th>Setting</th>
                    <th>Value</th>
                </tr>
                <tr>
                    <td>SDK Location</td>
                    <td>{sdk_info['sdk_location'] or 'Not specified'}</td>
                </tr>
                <tr>
                    <td>JDK Version</td>
                    <td>{sdk_info['jdk_version'] or 'Not specified'}</td>
                </tr>
                <tr>
                    <td>Gradle Version</td>
                    <td>{sdk_info['gradle_version'] or 'Not specified'}</td>
                </tr>
                <tr>
                    <td>Build Tools Version</td>
                    <td>{sdk_info['build_tools_version'] or 'Not specified'}</td>
                </tr>
                <tr>
                    <td>Compile SDK Version</td>
                    <td>{sdk_info['compile_sdk'] or 'Not specified'}</td>
                </tr>
            </table>
        </div>
        
        <div class="info-section">
            <h3>Setup Instructions:</h3>
            <ol>
                <li>Install Android Studio</li>
                <li>Set ANDROID_HOME environment variable to the SDK location</li>
                <li>Ensure you have JDK {sdk_info['jdk_version']} installed</li>
                <li>Make sure Gradle {sdk_info['gradle_version'] or 'latest version'} is configured</li>
                <li>Install Build Tools version {sdk_info['build_tools_version'] or 'latest'} through SDK Manager</li>
                <li>Install Android SDK Platform {sdk_info['compile_sdk'] or 'latest'} through SDK Manager</li>
            </ol>
        </div>
        
        <footer>
            {sdk_info['project_name']} | SDK Configuration | Generated on {datetime.now().strftime('%Y-%m-%d')}
        </footer>
    </div>
</body>
</html>
""")
    print(f"HTML information page saved to: {html_path}")
    
    # Also copy files to root directory for the HTTP server if needed
    if output_dir != ".":
        shutil.copy(qr_img_path, ".")
        shutil.copy(html_path, "index.html")
        print(f"Files also copied to the web server root directory")
    else:
        # If we're already in the root directory, just create an index.html copy
        if html_path != "./index.html":
            shutil.copy(html_path, "index.html")
            print(f"Created index.html for the web server root directory")
    
    return qr_img_path, html_path

def main():
    parser = argparse.ArgumentParser(description='Generate QR code with Android SDK configuration')
    parser.add_argument('output_dir', nargs='?', default='.',
                      help='Directory to save the QR code and HTML')
    args = parser.parse_args()
    
    sdk_info = get_sdk_info()
    qr_path, html_path = generate_qr_code(sdk_info, args.output_dir)
    
    print("\nUse these files to share SDK configuration with other developers.")
    print(f"Open {html_path} in a browser to view the configuration details.")
    print("You can also access this via the running HTTP server at port 5000.")

if __name__ == "__main__":
    main()