#!/usr/bin/env python3
"""
Simple HTTP Server to serve the QR code
"""

import os
import http.server
import socketserver

PORT = 5000

class SimpleHTTPRequestHandler(http.server.SimpleHTTPRequestHandler):
    """Simple request handler that displays directory contents"""
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)

# Create output directory if it doesn't exist
os.makedirs("qr_output", exist_ok=True)

# Create a basic HTML page
html_content = """<!DOCTYPE html>
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
            <img src="qr_code.png" alt="QR Code" width="300" />
        </div>
        
        <h3>Connection Information</h3>
        <table>
            <tr>
                <th>Setting</th>
                <th>Value</th>
            </tr>
            <tr>
                <td>Connection Type</td>
                <td>WiFi</td>
            </tr>
            <tr>
                <td>IP Address</td>
                <td>localhost</td>
            </tr>
            <tr>
                <td>Port</td>
                <td>8000</td>
            </tr>
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

# Generate a simple QR code
import qrcode
import json
qr_data = json.dumps({"wifi": "localhost:8000"})
img = qrcode.make(qr_data)
img.save("qr_code.png")

# Write HTML file
with open("index.html", "w") as f:
    f.write(html_content)

print("Starting HTTP server on port", PORT)
print("Access the QR code at http://localhost:5000")

# Start HTTP server
Handler = http.server.SimpleHTTPRequestHandler
httpd = socketserver.TCPServer(("0.0.0.0", PORT), Handler)
httpd.serve_forever()