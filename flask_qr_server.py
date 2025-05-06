#!/usr/bin/env python3
"""
Flask-based QR Code Server for Android PTZ Camera Controller
This server uses Flask to serve QR codes for configuration.
"""
import os
import json
import qrcode
from flask import Flask, send_file, render_template_string

# Create Flask app
app = Flask(__name__)

# QR Code configuration
CONFIG = {
    "wifi": "localhost:8000",
    "bluetooth": "",
    "app_url": "https://github.com/user/ptz-camera-controller/releases/latest/download/ptz-camera-controller.apk",
    "version": "1.0.0"
}

# HTML template for the page
HTML_TEMPLATE = """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>PTZ Camera Controller - QR Code</title>
    <style>
        body { 
            font-family: Arial, sans-serif; 
            max-width: 800px; 
            margin: 0 auto; 
            padding: 20px; 
            text-align: center; 
            color: #333;
        }
        .container { 
            margin: 30px auto; 
            padding: 20px; 
            border: 1px solid #ddd; 
            border-radius: 5px;
            background-color: #f9f9f9;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        table { 
            margin: 20px auto; 
            border-collapse: collapse; 
            width: 80%; 
        }
        td, th { 
            border: 1px solid #ddd; 
            padding: 12px; 
            text-align: left; 
        }
        th { 
            background-color: #f2f2f2; 
        }
        h1 {
            color: #333;
            border-bottom: 2px solid #4CAF50;
            padding-bottom: 10px;
        }
        h2 {
            color: #4CAF50;
        }
        .download-button {
            display: inline-block;
            background-color: #4CAF50;
            color: white;
            padding: 10px 20px;
            text-decoration: none;
            border-radius: 5px;
            font-weight: bold;
            margin-top: 10px;
            transition: background-color 0.3s;
        }
        .download-button:hover {
            background-color: #45a049;
        }
        ol li {
            margin-bottom: 10px;
        }
    </style>
</head>
<body>
    <h1>PTZ Camera Controller</h1>
    
    <div class="container">
        <h2>Android App Installation</h2>
        <p>Scan this QR code to download and install the PTZ Camera Controller app:</p>
        
        <div>
            <img src="/app_qr_code.png" alt="App QR Code" width="300" />
        </div>
        
        <p><a href="{{ app_url }}" class="download-button">Download Android App (v{{ version }})</a></p>
    </div>
    
    <div class="container">
        <h2>Camera Connection Configuration</h2>
        <p>After installing the app, scan this QR code to configure connection settings:</p>
        
        <div>
            <img src="/qr_code.png" alt="QR Code" width="300" />
        </div>
        
        <h3>Connection Information</h3>
        <table>
            <tr><th>Setting</th><th>Value</th></tr>
            <tr><td>Primary Connection</td><td>WiFi</td></tr>
            <tr><td>IP Address</td><td>{{ wifi_host }}</td></tr>
            <tr><td>Port</td><td>{{ wifi_port }}</td></tr>
            {% if bluetooth_name %}
            <tr><td>Backup Connection</td><td>Bluetooth</td></tr>
            <tr><td>Bluetooth Device</td><td>{{ bluetooth_name }}</td></tr>
            {% endif %}
        </table>
    </div>
    
    <div>
        <h3>How to use:</h3>
        <ol style="text-align: left; display: inline-block;">
            <li>Install the PTZ Camera Controller app on your Android tablet</li>
            <li>Open the app and go to the Connection tab</li>
            <li>Tap "Scan QR Code" button</li>
            <li>Scan the configuration QR code to automatically set up connection settings</li>
            <li>The app will connect to the camera controller via WiFi</li>
            <li>If WiFi connection fails, it will automatically switch to Bluetooth</li>
        </ol>
    </div>
    
    <div style="margin-top: 40px; font-size: 12px; color: #666;">
        <p>PTZ Camera Controller - Copyright © 2025</p>
    </div>
</body>
</html>
"""

# Ensure output directory exists
os.makedirs("qr_output", exist_ok=True)

def generate_config_qr_code():
    """Generate QR code with connection configuration"""
    qr_data = json.dumps(CONFIG)
    img = qrcode.make(qr_data)
    qr_path = "qr_code.png"
    img.save(qr_path)
    return qr_path
    
def generate_app_qr_code():
    """Generate QR code with app download URL"""
    app_url = CONFIG.get("app_url", "")
    if not app_url:
        return None
        
    img = qrcode.make(app_url)
    qr_path = "app_qr_code.png"
    img.save(qr_path)
    return qr_path

@app.route('/')
def index():
    """Serve the main page with QR codes"""
    # Parse wifi config
    parts = CONFIG["wifi"].split(":")
    wifi_host = parts[0] if parts[0] != "localhost" else "127.0.0.1"
    wifi_port = parts[1] if len(parts) > 1 else "8000"
    
    # Parse bluetooth config
    bluetooth_name = CONFIG.get("bluetooth", "")
    
    # Generate QR codes
    config_qr_path = generate_config_qr_code()
    app_qr_path = generate_app_qr_code()
    
    # Render template
    return render_template_string(
        HTML_TEMPLATE, 
        wifi_host=wifi_host,
        wifi_port=wifi_port,
        bluetooth_name=bluetooth_name,
        app_url=CONFIG.get("app_url", ""),
        version=CONFIG.get("version", "1.0.0")
    )

@app.route('/qr_code.png')
def serve_config_qr_code():
    """Serve the configuration QR code image"""
    return send_file('qr_code.png', mimetype='image/png')

@app.route('/app_qr_code.png')
def serve_app_qr_code():
    """Serve the app download QR code image"""
    if not os.path.exists('app_qr_code.png'):
        generate_app_qr_code()
    return send_file('app_qr_code.png', mimetype='image/png')

if __name__ == '__main__':
    # Generate QR codes on startup
    generate_config_qr_code()
    generate_app_qr_code()
    
    # Start Flask app
    print("Starting Flask server on port 5000")
    print("Access the QR codes at http://localhost:5000")
    app.run(host='0.0.0.0', port=5000)