#!/usr/bin/env python3
"""
PTZ Camera Controller Test Environment Setup

This script sets up a test environment for the PTZ Camera Controller by:
1. Starting the camera control server in the background
2. Starting the QR code server with auto-detected IP addresses
3. Optionally, starting a mock video stream server

Usage:
  python setup_test_environment.py [--with-stream] [--port PORT]

Example:
  python setup_test_environment.py --with-stream --port 8080
"""

import os
import sys
import argparse
import subprocess
import time
import socket
import signal
import webbrowser

# Process tracking
processes = []

def signal_handler(sig, frame):
    """Handle Ctrl+C by terminating all processes"""
    print("\nShutting down test environment...")
    for process in processes:
        try:
            process.terminate()
        except:
            pass
    print("Test environment shutdown complete.")
    sys.exit(0)

def get_local_ip():
    """Get the local IP address of this machine."""
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        local_ip = s.getsockname()[0]
        s.close()
        return local_ip
    except Exception:
        return "127.0.0.1"

def start_camera_server(port):
    """Start the PTZ camera control server"""
    print(f"Starting camera control server on port {port}...")
    
    # Mock implementation for testing
    server_cmd = ["python", "-c", f"""
import http.server
import socketserver
import json
import time

class CameraControlHandler(http.server.SimpleHTTPRequestHandler):
    def do_POST(self):
        content_length = int(self.headers['Content-Length'])
        post_data = self.rfile.read(content_length).decode('utf-8')
        
        try:
            command = json.loads(post_data)
            print(f"Received command: {{command}}")
            
            # Send response
            self.send_response(200)
            self.send_header('Content-Type', 'application/json')
            self.end_headers()
            
            response = {{
                "status": "success",
                "timestamp": time.time(),
                "message": f"Processed command: {{command.get('command', 'unknown')}}"
            }}
            
            self.wfile.write(json.dumps(response).encode('utf-8'))
            
        except Exception as e:
            self.send_response(500)
            self.send_header('Content-Type', 'application/json')
            self.end_headers()
            
            response = {{
                "status": "error",
                "message": str(e)
            }}
            
            self.wfile.write(json.dumps(response).encode('utf-8'))
    
    def do_GET(self):
        if self.path == '/status':
            self.send_response(200)
            self.send_header('Content-Type', 'application/json')
            self.end_headers()
            
            status = {{
                "status": "running",
                "uptime": time.time(),
                "camera_mode": "rgb",
                "zoom_level": 50,
                "pan_position": 0,
                "tilt_position": 0
            }}
            
            self.wfile.write(json.dumps(status).encode('utf-8'))
        else:
            super().do_GET()

# Start server
PORT = {port}
Handler = CameraControlHandler
httpd = socketserver.TCPServer(("0.0.0.0", PORT), Handler)
print(f"Camera control server running at http://0.0.0.0:{{PORT}}")
httpd.serve_forever()
"""]
    
    process = subprocess.Popen(server_cmd)
    processes.append(process)
    print(f"Camera control server started (PID: {process.pid})")
    return process

def start_video_stream(port):
    """Start a mock video stream server"""
    print(f"Starting mock video stream on port {port}...")
    
    # This would normally start a real RTSP stream
    # For testing, we'll just start a simple HTTP server that serves a message
    stream_cmd = ["python", "-c", f"""
import http.server
import socketserver

PORT = {port}
class StreamHandler(http.server.SimpleHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200)
        self.send_header('Content-Type', 'text/plain')
        self.end_headers()
        self.wfile.write(b"Mock RTSP stream - in a real deployment, this would be a video stream")

httpd = socketserver.TCPServer(("0.0.0.0", PORT), StreamHandler)
print(f"Mock video stream server running at http://0.0.0.0:{{PORT}}")
httpd.serve_forever()
"""]
    
    process = subprocess.Popen(stream_cmd)
    processes.append(process)
    print(f"Mock video stream started (PID: {process.pid})")
    return process

def start_qr_code_server(camera_port, stream_port):
    """Start the QR code server"""
    local_ip = get_local_ip()
    
    print(f"Starting QR code server with configuration:")
    print(f"Local IP: {local_ip}")
    print(f"Camera server: {local_ip}:{camera_port}")
    print(f"Video stream: rtsp://{local_ip}:{stream_port}/stream")
    
    # Start the QR code server
    qr_cmd = [
        "python", "flask_qr_server.py", 
        "--port", "5000",
        "--auto-detect-ip",
        "--wifi", f"{local_ip}:{camera_port}",
        "--rtsp", f"rtsp://{local_ip}:{stream_port}/stream",
        "--bluetooth", "TestPTZCamera",
        "--camera", "Test PTZ Camera System",
        "--features", "pan,tilt,zoom,switch_mode,ir_mode,presets"
    ]
    
    process = subprocess.Popen(qr_cmd)
    processes.append(process)
    print(f"QR code server started (PID: {process.pid})")
    return process

def main():
    parser = argparse.ArgumentParser(description='Setup test environment for PTZ Camera Controller')
    
    parser.add_argument('--with-stream', action='store_true',
                      help='Start a mock video stream server')
    parser.add_argument('--port', type=int, default=8000,
                      help='Port for the camera control server (default: 8000)')
    parser.add_argument('--stream-port', type=int, default=8554,
                      help='Port for the video stream server (default: 8554)')
    
    args = parser.parse_args()
    
    # Set up signal handler for graceful shutdown
    signal.signal(signal.SIGINT, signal_handler)
    
    # Start camera control server
    camera_server = start_camera_server(args.port)
    
    # Start video stream if requested
    if args.with_stream:
        stream_server = start_video_stream(args.stream_port)
    
    # Start QR code server
    qr_server = start_qr_code_server(args.port, args.stream_port)
    
    # Wait for servers to initialize
    time.sleep(2)
    
    # Open browser with QR code page
    qr_url = "http://localhost:5000"
    print(f"\nOpening browser at {qr_url}")
    try:
        webbrowser.open(qr_url)
    except:
        print(f"Could not open browser automatically. Please navigate to {qr_url}")
    
    print("\nTest environment is running. Press Ctrl+C to stop all servers.")
    
    # Keep the script running to maintain the processes
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        signal_handler(None, None)

if __name__ == "__main__":
    main()