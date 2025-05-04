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
    "wifi": "localhost:8000"
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
        }
        .container { 
            margin: 30px auto; 
            padding: 20px; 
            border: 1px solid #ddd; 
            border-radius: 5px; 
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
    </style>
</head>
<body>
    <h1>PTZ Camera Controller</h1>
    
    <div class="container">
        <h2>Android App Configuration</h2>
        <p>Scan this QR code with the PTZ Camera Controller app to configure connection settings:</p>
        
        <div>
            <img src="/qr_code.png" alt="QR Code" width="300" />
        </div>
        
        <h3>Connection Information</h3>
        <table>
            <tr><th>Setting</th><th>Value</th></tr>
            <tr><td>Connection Type</td><td>WiFi</td></tr>
            <tr><td>IP Address</td><td>{{ wifi_host }}</td></tr>
            <tr><td>Port</td><td>{{ wifi_port }}</td></tr>
        </table>
    </div>
    
    <div>
        <h3>How to use:</h3>
        <ol style="text-align: left; display: inline-block;">
            <li>Open the PTZ Camera Controller app on your Android device</li>
            <li>Go to the Connection tab</li>
            <li>Tap "Scan QR Code" button</li>
            <li>Scan this QR code to automatically configure connection settings</li>
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

def generate_qr_code():
    """Generate QR code and save it"""
    qr_data = json.dumps(CONFIG)
    img = qrcode.make(qr_data)
    qr_path = "qr_code.png"
    img.save(qr_path)
    return qr_path

@app.route('/')
def index():
    """Serve the main page with QR code"""
    # Parse wifi config
    parts = CONFIG["wifi"].split(":")
    wifi_host = parts[0] if parts[0] != "localhost" else "127.0.0.1"
    wifi_port = parts[1] if len(parts) > 1 else "8000"
    
    # Generate QR code
    generate_qr_code()
    
    # Render template
    return render_template_string(
        HTML_TEMPLATE, 
        wifi_host=wifi_host,
        wifi_port=wifi_port
    )

@app.route('/qr_code.png')
def serve_qr_code():
    """Serve the QR code image"""
    return send_file('qr_code.png', mimetype='image/png')

if __name__ == '__main__':
    # Generate QR code on startup
    generate_qr_code()
    
    # Start Flask app
    print("Starting Flask server on port 5000")
    print("Access the QR code at http://localhost:5000")
    app.run(host='0.0.0.0', port=5000)