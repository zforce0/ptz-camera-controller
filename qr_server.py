#!/usr/bin/env python3
"""
QR Code Server for PTZ Camera Controller App

This script generates and serves QR codes for configuring the Android PTZ Camera Controller app.
It creates an HTTP server that hosts both the QR code image and an HTML page with
connection information.

Usage:
  python qr_server.py [--port PORT] [--wifi IP:PORT] [--bluetooth DEVICE_NAME]
  
Example:
  python qr_server.py --port 5000 --wifi 192.168.1.100:8000
"""

import os
import sys
import json
import argparse
import threading
import http.server
import socketserver
from android_config_qr import generate_config_qr

class QRCodeHandler(http.server.SimpleHTTPRequestHandler):
    """Custom HTTP request handler for serving QR codes."""
    
    def __init__(self, *args, config=None, **kwargs):
        self.config = config
        # For Python < 3.7
        self.directory = "qr_output"
        super().__init__(*args, **kwargs)
    
    def do_GET(self):
        """Handle GET requests."""
        # If config is provided and path is /generate, generate a QR code with query parameters
        if self.path.startswith('/generate'):
            self._handle_generate_request()
            return
        elif self.path == "/":
            self.path = "/index.html"
        
        # Fall back to default behavior for other paths
        return super().do_GET()
    
    def _handle_generate_request(self):
        """Handle QR code generation request with parameters from URL."""
        try:
            # Extract query parameters
            query_params = {}
            if '?' in self.path:
                query = self.path.split('?')[1]
                for param in query.split('&'):
                    if '=' in param:
                        key, value = param.split('=', 1)
                        query_params[key] = value
            
            # Create config from parameters
            config = {}
            if 'wifi' in query_params:
                config['wifi'] = query_params['wifi']
            if 'bluetooth' in query_params:
                config['bluetooth'] = query_params['bluetooth']
            
            # Use default config if no parameters are provided
            if not config and self.config:
                config = self.config
            
            # If still no config, return error
            if not config:
                self.send_error(400, "Missing configuration parameters")
                return
            
            # Generate QR code
            generate_config_qr(config, "qr_output")
            
            # Redirect to index.html
            self.send_response(302)
            self.send_header('Location', '/index.html')
            self.end_headers()
            
        except Exception as e:
            self.send_error(500, f"Error generating QR code: {str(e)}")

def run_server(port=5000, config=None):
    """Run the HTTP server."""
    # Create output directory if it doesn't exist
    os.makedirs("qr_output", exist_ok=True)
    
    # Generate initial QR code and HTML if config is provided
    if config:
        generate_config_qr(config, "qr_output")
    
    # Custom handler with config
    handler = lambda *args, **kwargs: QRCodeHandler(*args, config=config, **kwargs)
    
    # Create and start server
    with socketserver.TCPServer(("0.0.0.0", port), handler) as httpd:
        print(f"Serving QR code at http://0.0.0.0:{port}")
        print(f"Use http://0.0.0.0:{port}/generate?wifi=IP:PORT to generate a custom QR code")
        
        # Start serving
        httpd.serve_forever()

def parse_args():
    """Parse command line arguments."""
    parser = argparse.ArgumentParser(description="Serve QR codes for Android app configuration")
    parser.add_argument("--port", type=int, default=5000, help="Port to serve on (default: 5000)")
    parser.add_argument("--wifi", help="Default WiFi connection in format IP:PORT, e.g. 192.168.1.100:8000")
    parser.add_argument("--bluetooth", help="Default Bluetooth device name to connect to")
    
    return parser.parse_args()

def main():
    """Main function."""
    args = parse_args()
    
    # Create configuration
    config = {}
    if args.wifi:
        config["wifi"] = args.wifi
    if args.bluetooth:
        config["bluetooth"] = args.bluetooth
    
    # If no config provided, create a default one with localhost
    if not config:
        # Default to local WiFi
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