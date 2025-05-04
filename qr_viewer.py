#!/usr/bin/env python3
"""
QR Viewer - Simplest possible QR code server for the PTZ Camera Controller
"""
import os
import http.server
import socketserver
import qrcode
import json

# Using port 8080 as main port seems to be often available
PORT = 8080

def create_files():
    """Create QR code and HTML files"""
    # Generate QR code
    qr_data = json.dumps({"wifi": "localhost:8000"})
    img = qrcode.make(qr_data)
    img.save("qr_code.png")
    
    # Create HTML file
    html = """<!DOCTYPE html>
    <html><head><title>PTZ Camera QR Code</title></head>
    <body style="text-align:center; font-family:sans-serif">
        <h1>PTZ Camera Controller</h1>
        <div>
            <img src="qr_code.png" alt="QR Code" width="300" />
        </div>
        <p>Scan this QR code with the PTZ Camera Controller app</p>
    </body></html>
    """
    
    with open("index.html", "w") as f:
        f.write(html)

def main():
    """Main function"""
    # Create files
    create_files()
    
    # Set up and start server
    handler = http.server.SimpleHTTPRequestHandler
    
    try:
        with socketserver.TCPServer(("", PORT), handler) as httpd:
            print(f"Server started at port {PORT}")
            print(f"View QR code at http://localhost:{PORT}")
            httpd.serve_forever()
    except OSError as e:
        print(f"Server error: {e}")
        print("Perhaps the port is already in use")

if __name__ == "__main__":
    main()