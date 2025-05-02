#!/usr/bin/env python3
"""
WiFi Server for PTZ Camera Control.
This module handles HTTP/TCP communication with the Android tablet app,
receiving control commands and sending status updates.
"""

import os
import json
import logging
import threading
import socket
import time
from http.server import HTTPServer, BaseHTTPRequestHandler
from socketserver import ThreadingMixIn
from urllib.parse import urlparse, parse_qs

logger = logging.getLogger('wifi_server')

class CameraControlHandler(BaseHTTPRequestHandler):
    """HTTP request handler for camera control commands"""
    
    def __init__(self, *args, camera_controller=None, **kwargs):
        self.camera_controller = camera_controller
        # Call parent's __init__
        # Note: BaseHTTPRequestHandler calls do_METHOD automatically
        super().__init__(*args, **kwargs)
    
    def _set_headers(self, content_type="application/json"):
        """Set common headers for responses"""
        self.send_response(200)
        self.send_header('Content-type', content_type)
        self.send_header('Access-Control-Allow-Origin', '*')  # CORS
        self.end_headers()
    
    def _send_json_response(self, data):
        """Send a JSON response"""
        self._set_headers()
        response = json.dumps(data).encode('utf-8')
        self.wfile.write(response)
    
    def do_OPTIONS(self):
        """Handle preflight requests for CORS"""
        self._set_headers()
    
    def do_GET(self):
        """Handle GET requests - mostly for status and stream URLs"""
        parsed_path = urlparse(self.path)
        
        # Handle different endpoints
        if parsed_path.path == '/status':
            # Return camera status
            status = {
                'camera_mode': 'rgb' if self.camera_controller.get_camera_mode().value == 0 else 'ir',
                'pan_speed': self.camera_controller.pan_speed,
                'tilt_speed': self.camera_controller.tilt_speed,
                'zoom_level': self.camera_controller.zoom_level,
                'status': 'ok'
            }
            self._send_json_response(status)
            
        elif parsed_path.path == '/streams':
            # Return stream URLs - assuming our video_streamer has this info
            # This will be implemented by the client
            streams = {
                'rgb': f'rtsp://{self.client_address[0]}:8554/rgb',
                'ir': f'rtsp://{self.client_address[0]}:8554/ir'
            }
            self._send_json_response(streams)
            
        else:
            # Default response for unknown endpoints
            self.send_response(404)
            self.end_headers()
            self.wfile.write(b'{"error": "Not found"}')
    
    def do_POST(self):
        """Handle POST requests - for camera commands"""
        parsed_path = urlparse(self.path)
        
        # Handle different endpoints
        if parsed_path.path == '/command':
            # Process a camera control command
            
            # Get content length to read body
            content_length = int(self.headers['Content-Length'])
            body = self.rfile.read(content_length).decode('utf-8')
            
            try:
                # Parse command from JSON
                command = json.loads(body)
                
                # Send command to camera controller
                if self.camera_controller:
                    result = self.camera_controller.process_command(command)
                    
                    if result:
                        # Command succeeded
                        self._send_json_response({
                            'status': 'ok',
                            'message': f"Command {command.get('type')} processed successfully"
                        })
                    else:
                        # Command failed
                        self._send_json_response({
                            'status': 'error',
                            'message': f"Failed to process command {command}"
                        })
                else:
                    # No camera controller available
                    self._send_json_response({
                        'status': 'error',
                        'message': "Camera controller not available"
                    })
                    
            except json.JSONDecodeError:
                # Invalid JSON in request
                self.send_response(400)
                self.end_headers()
                self.wfile.write(b'{"error": "Invalid JSON in request body"}')
                
            except Exception as e:
                # Other errors
                logger.error(f"Error processing command: {e}")
                self.send_response(500)
                self.end_headers()
                self.wfile.write(f'{{"error": "Internal server error: {str(e)}"}}".encode("utf-8")')
                
        else:
            # Default response for unknown endpoints
            self.send_response(404)
            self.end_headers()
            self.wfile.write(b'{"error": "Not found"}')


class ThreadedHTTPServer(ThreadingMixIn, HTTPServer):
    """Handle requests in a separate thread."""
    pass


class WifiServer:
    """WiFi server for PTZ camera control using HTTP"""
    
    def __init__(self, camera_controller, port=8000):
        """Initialize the WiFi server"""
        self.camera_controller = camera_controller
        self.port = port
        
        self.server = None
        self.running = False
        self.server_thread = None
        
        # Lock for thread safety
        self.lock = threading.Lock()
        
        # TCP server for direct commands
        self.tcp_server = None
        self.tcp_thread = None
        
        logger.info(f"WiFi server initialized on port {port}")
    
    def start(self):
        """Start the WiFi server"""
        with self.lock:
            if self.running:
                logger.warning("WiFi server is already running")
                return
                
            self.running = True
            
            # Start HTTP server in a separate thread
            self.server_thread = threading.Thread(target=self._http_server_loop)
            self.server_thread.daemon = True
            self.server_thread.start()
            
            # Start TCP server in a separate thread
            self.tcp_thread = threading.Thread(target=self._tcp_server_loop)
            self.tcp_thread.daemon = True
            self.tcp_thread.start()
            
            logger.info("WiFi server started")
    
    def stop(self):
        """Stop the WiFi server"""
        with self.lock:
            if not self.running:
                logger.warning("WiFi server is not running")
                return
                
            self.running = False
            
            # Shutdown HTTP server
            if self.server:
                self.server.shutdown()
                self.server.server_close()
                self.server = None
            
            # Close TCP server
            if self.tcp_server:
                self.tcp_server.close()
                self.tcp_server = None
            
            # Wait for server threads to end
            if self.server_thread:
                self.server_thread.join(timeout=2.0)
                self.server_thread = None
                
            if self.tcp_thread:
                self.tcp_thread.join(timeout=2.0)
                self.tcp_thread = None
                
            logger.info("WiFi server stopped")
    
    def _http_server_loop(self):
        """HTTP server loop that handles REST API requests"""
        logger.info(f"HTTP server loop started on port {self.port}")
        
        try:
            # Create a handler class with access to the camera controller
            handler = lambda *args, **kwargs: CameraControlHandler(
                *args, camera_controller=self.camera_controller, **kwargs)
            
            # Create and start the server
            self.server = ThreadedHTTPServer(('0.0.0.0', self.port), handler)
            
            logger.info(f"HTTP server listening on port {self.port}")
            
            # Run the server until stopped
            self.server.serve_forever()
            
        except Exception as e:
            logger.error(f"Error in HTTP server loop: {e}")
            
        finally:
            if self.server:
                try:
                    self.server.server_close()
                except Exception as e:
                    logger.error(f"Error closing HTTP server: {e}")
                self.server = None
                
            logger.info("HTTP server loop ended")
    
    def _tcp_server_loop(self):
        """TCP server loop that handles direct socket connections"""
        logger.info(f"TCP server loop started on port {self.port + 1}")
        
        try:
            # Create TCP socket
            self.tcp_server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.tcp_server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            self.tcp_server.bind(('0.0.0.0', self.port + 1))
            self.tcp_server.listen(5)
            self.tcp_server.settimeout(1.0)  # 1 second timeout for accept()
            
            logger.info(f"TCP server listening on port {self.port + 1}")
            
            while self.running:
                try:
                    # Accept connections
                    client_sock, client_addr = self.tcp_server.accept()
                    
                    logger.info(f"Accepted TCP connection from {client_addr}")
                    
                    # Handle client in a separate thread
                    client_thread = threading.Thread(
                        target=self._handle_tcp_client,
                        args=(client_sock, client_addr)
                    )
                    client_thread.daemon = True
                    client_thread.start()
                    
                except socket.timeout:
                    # Timeout on accept, just continue the loop and check if we should exit
                    continue
                except Exception as e:
                    if self.running:
                        logger.error(f"Error accepting TCP connection: {e}")
                        time.sleep(1)
                    
        except Exception as e:
            logger.error(f"Error in TCP server loop: {e}")
            
        finally:
            if self.tcp_server:
                try:
                    self.tcp_server.close()
                except Exception as e:
                    logger.error(f"Error closing TCP server: {e}")
                self.tcp_server = None
                
            logger.info("TCP server loop ended")
    
    def _handle_tcp_client(self, client_sock, client_addr):
        """Handle communication with a TCP client"""
        logger.info(f"Handling TCP client {client_addr}")
        
        try:
            # Set socket options
            client_sock.settimeout(0.5)
            
            buffer = ""
            
            while self.running:
                try:
                    # Read data from the client
                    data = client_sock.recv(1024).decode('utf-8')
                    
                    if not data:
                        logger.info("TCP client disconnected")
                        break
                        
                    # Append data to buffer
                    buffer += data
                    
                    # Process complete commands (delimited by newlines)
                    while '\n' in buffer:
                        line, buffer = buffer.split('\n', 1)
                        self._process_tcp_command(line.strip(), client_sock)
                        
                except socket.timeout:
                    # Timeout reading from client, try again
                    continue
                except Exception as e:
                    logger.error(f"Error reading from TCP client: {e}")
                    break
                    
        except Exception as e:
            logger.error(f"Error handling TCP client: {e}")
            
        finally:
            # Clean up
            try:
                client_sock.close()
            except Exception as e:
                logger.error(f"Error closing TCP client socket: {e}")
                
            logger.info(f"TCP client {client_addr} disconnected")
    
    def _process_tcp_command(self, command_str, client_sock):
        """Process a command received from a TCP client"""
        logger.debug(f"Received TCP command: {command_str}")
        
        try:
            # Parse the JSON command
            command = json.loads(command_str)
            
            # Forward the command to the camera controller
            result = self.camera_controller.process_command(command)
            
            # Send response back to client
            response = {
                'status': 'ok' if result else 'error',
                'message': f"Command {command.get('type')} processed successfully" if result else "Failed to process command"
            }
            
            client_sock.send((json.dumps(response) + '\n').encode('utf-8'))
            
            if result:
                logger.debug(f"TCP command processed successfully: {command}")
            else:
                logger.warning(f"Failed to process TCP command: {command}")
                
        except json.JSONDecodeError:
            logger.error(f"Invalid JSON TCP command: {command_str}")
            error_response = {
                'status': 'error',
                'message': "Invalid JSON format"
            }
            client_sock.send((json.dumps(error_response) + '\n').encode('utf-8'))
        except Exception as e:
            logger.error(f"Error processing TCP command: {e}")
            error_response = {
                'status': 'error',
                'message': f"Internal error: {str(e)}"
            }
            try:
                client_sock.send((json.dumps(error_response) + '\n').encode('utf-8'))
            except:
                pass
    
    def send_status_to_all_clients(self, status):
        """Send a status update to all connected TCP clients
        
        This method would be implemented to broadcast status updates to all connected clients.
        For now, it's just a placeholder since we don't track all clients in this implementation.
        """
        logger.debug(f"Would send status to all clients: {status}")
        # In a complete implementation, we would maintain a list of all connected clients
        # and send the status to each one
