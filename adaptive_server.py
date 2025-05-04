#!/usr/bin/env python3
"""
Adaptive QR Code Server for Android PTZ Camera Controller
This server tries different ports until it finds one that works.
"""
import os
import http.server
import socketserver
import qrcode
import json

def generate_qr(port=8000):
    """Generate QR code with configuration data"""
    # Create output directory if it doesn't exist
    os.makedirs("qr_output", exist_ok=True)
    
    # QR code data
    qr_data = json.dumps({"wifi": f"localhost:{port}"})
    
    # Generate and save QR code
    img = qrcode.make(qr_data)
    img.save("qr_code.png")
    
    # Create a basic HTML page
    html_content = f"""<!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
        <title>PTZ Camera Controller - QR Code</title>
        <style>
            body {{ font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px; text-align: center; }}
            .container {{ margin: 30px auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px; }}
            table {{ margin: 20px auto; border-collapse: collapse; width: 80%; }}
            td, th {{ border: 1px solid #ddd; padding: 12px; text-align: left; }}
            th {{ background-color: #f2f2f2; }}
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
                <tr><th>Setting</th><th>Value</th></tr>
                <tr><td>Connection Type</td><td>WiFi</td></tr>
                <tr><td>IP Address</td><td>localhost</td></tr>
                <tr><td>Port</td><td>{port}</td></tr>
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
    
    # Write HTML file
    with open("index.html", "w") as f:
        f.write(html_content)
    
    return html_content

def run_server():
    """Run the HTTP server trying different ports"""
    # List of ports to try
    ports = [5000, 8080, 8000, 3000, 9000]
    
    # Handler class
    handler = http.server.SimpleHTTPRequestHandler
    
    # Try each port
    for port in ports:
        try:
            # Generate QR with current port
            generate_qr(port)
            
            # Create and start server
            with socketserver.TCPServer(("", port), handler) as httpd:
                print(f"Starting server on port {port}")
                print(f"Access the QR code at http://localhost:{port}")
                httpd.serve_forever()
                
            # If we get here, server was started successfully
            break
            
        except OSError as e:
            print(f"Failed to start server on port {port}: {e}")
            print(f"Trying next port...")
            continue
    else:
        print("Failed to start server on any port.")

if __name__ == "__main__":
    run_server()