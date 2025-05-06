#!/usr/bin/env python3
"""
WiFi Server for PTZ Camera Control.
This module handles WiFi/TCP communication with the Android tablet app,
receiving control commands and sending status updates.
"""

import os
import time
import logging
import threading
import socket
import json
import select

logger = logging.getLogger('wifi_server')

class WifiServer:
    """WiFi server for PTZ camera control"""
    
    def __init__(self, camera_controller, video_streamer, port=8000):
        """Initialize the WiFi server
        
        Args:
            camera_controller: CameraController instance
            video_streamer: VideoStreamer instance
            port: TCP server port (default: 8000)
        """
        self.camera_controller = camera_controller
        self.video_streamer = video_streamer
        self.port = port
        self.running = False
        self.server_thread = None
        self.server_socket = None
        self.clients = []
        self.lock = threading.Lock()
        self.connection_quality = "good"  # Initial quality status
        self.bad_quality_count = 0
        self.good_quality_count = 0
        self.restart_scheduled = False
        
    def start(self):
        """Start the WiFi server"""
        if self.running:
            logger.warning("WiFi server is already running")
            return
            
        self.running = True
        
        # Register for stream quality reports
        self.video_streamer.set_status_report_callback(self._handle_quality_report)
        
        # Start server thread
        self.server_thread = threading.Thread(target=self._server_loop)
        self.server_thread.daemon = True
        self.server_thread.start()
        
        logger.info(f"WiFi server started on port {self.port}")
        return True
        
    def stop(self):
        """Stop the WiFi server"""
        if not self.running:
            logger.warning("WiFi server is not running")
            return
            
        self.running = False
        
        # Close all client connections
        with self.lock:
            for client_socket, _ in self.clients:
                try:
                    client_socket.close()
                except:
                    pass
            self.clients = []
            
        # Close server socket
        if self.server_socket:
            try:
                self.server_socket.close()
            except:
                pass
            self.server_socket = None
            
        # Wait for server thread to end
        if self.server_thread:
            self.server_thread.join(timeout=2.0)
            
        logger.info("WiFi server stopped")
        
    def send_status(self, status):
        """Send a status update to all connected clients
        
        Args:
            status: Status data dictionary to send
        """
        if not self.running:
            return
            
        # Convert to JSON
        try:
            status_json = json.dumps(status)
        except Exception as e:
            logger.error(f"Error serializing status data: {e}")
            return
            
        # Send to all clients
        with self.lock:
            disconnected = []
            for client_socket, client_addr in self.clients:
                try:
                    message = status_json.encode('utf-8') + b'\n'
                    client_socket.sendall(message)
                except Exception as e:
                    logger.warning(f"Error sending status to {client_addr}: {e}")
                    disconnected.append((client_socket, client_addr))
                    
            # Remove disconnected clients
            for client in disconnected:
                self.clients.remove(client)
                try:
                    client[0].close()
                except:
                    pass
                logger.info(f"Client disconnected: {client[1]}")
        
    def _server_loop(self):
        """Main server loop that handles TCP connections"""
        try:
            # Create server socket
            self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            self.server_socket.bind(('0.0.0.0', self.port))
            self.server_socket.listen(5)
            self.server_socket.settimeout(1.0)  # 1 second timeout for accept()
            
            logger.info(f"Server listening on 0.0.0.0:{self.port}")
            
            while self.running:
                # Handle scheduled server restart if needed
                if self.restart_scheduled:
                    logger.info("Performing scheduled server restart")
                    self._restart_wifi_service()
                    self.restart_scheduled = False
                
                # Accept new connections
                try:
                    client_socket, client_addr = self.server_socket.accept()
                    client_socket.settimeout(0.5)  # Set timeout for recv
                    
                    with self.lock:
                        self.clients.append((client_socket, client_addr))
                    
                    logger.info(f"New client connected: {client_addr}")
                    
                    # Start client handler thread
                    client_thread = threading.Thread(
                        target=self._handle_client,
                        args=(client_socket, client_addr)
                    )
                    client_thread.daemon = True
                    client_thread.start()
                    
                except socket.timeout:
                    # No new connections, continue
                    pass
                except Exception as e:
                    if self.running:  # Only log if we're still supposed to be running
                        logger.error(f"Error accepting connection: {e}")
                
                # Sleep briefly to prevent CPU hogging
                time.sleep(0.01)
                
        except Exception as e:
            logger.error(f"Server error: {e}")
        finally:
            # Clean up
            if self.server_socket:
                try:
                    self.server_socket.close()
                except:
                    pass
                
            logger.info("Server loop ended")
    
    def _handle_client(self, client_socket, client_addr):
        """Handle communication with a connected client"""
        logger.info(f"Client handler started for {client_addr}")
        
        try:
            while self.running:
                # Check if connection is still alive
                try:
                    # Wait for data with select (non-blocking)
                    ready_to_read, _, _ = select.select([client_socket], [], [], 0.5)
                    
                    if ready_to_read:
                        # Receive data
                        data = client_socket.recv(1024)
                        
                        if not data:
                            # Client disconnected
                            logger.info(f"Client disconnected: {client_addr}")
                            break
                            
                        # Process received data
                        try:
                            # Try to parse as JSON
                            command = json.loads(data.decode('utf-8').strip())
                            self._process_command(command)
                        except json.JSONDecodeError:
                            # Not valid JSON, try processing as text
                            text_command = data.decode('utf-8').strip()
                            if text_command:
                                self._process_text_command(text_command)
                        
                except socket.timeout:
                    # No data received, continue
                    continue
                except Exception as e:
                    logger.error(f"Error handling client {client_addr}: {e}")
                    break
                    
        except Exception as e:
            logger.error(f"Client handler error: {e}")
        finally:
            # Remove client from list and close socket
            with self.lock:
                if (client_socket, client_addr) in self.clients:
                    self.clients.remove((client_socket, client_addr))
                    
            try:
                client_socket.close()
            except:
                pass
                
            logger.info(f"Client handler ended for {client_addr}")
            
    def _process_command(self, command):
        """Process a JSON command received from a client"""
        logger.debug(f"Received command: {command}")
        
        if not isinstance(command, dict):
            logger.warning(f"Invalid command format: {command}")
            return
            
        # Forward to camera controller
        try:
            self.camera_controller.process_command(command)
        except Exception as e:
            logger.error(f"Error processing command: {e}")
            
    def _process_text_command(self, command):
        """Process a text command received from a client"""
        logger.debug(f"Received text command: {command}")
        
        # Parse simple text commands
        cmd_parts = command.split()
        if not cmd_parts:
            return
            
        cmd_type = cmd_parts[0].lower()
        
        try:
            if cmd_type == "pan":
                # pan <speed>
                if len(cmd_parts) > 1:
                    speed = int(cmd_parts[1])
                    self.camera_controller.set_pan(speed)
                    
            elif cmd_type == "tilt":
                # tilt <speed>
                if len(cmd_parts) > 1:
                    speed = int(cmd_parts[1])
                    self.camera_controller.set_tilt(speed)
                    
            elif cmd_type == "zoom":
                # zoom <level>
                if len(cmd_parts) > 1:
                    level = int(cmd_parts[1])
                    self.camera_controller.set_zoom(level)
                    
            elif cmd_type == "mode":
                # mode <0|1>
                if len(cmd_parts) > 1:
                    mode = int(cmd_parts[1])
                    self.camera_controller.set_camera_mode(mode)
                    
            elif cmd_type == "stop":
                # stop - stop all movement
                self.camera_controller.set_pan(0)
                self.camera_controller.set_tilt(0)
                
            elif cmd_type == "status":
                # status - request status report
                self._send_status_report()
                
        except Exception as e:
            logger.error(f"Error processing text command: {e}")
            
    def _send_status_report(self):
        """Send a status report to all clients"""
        # Get current status information
        status = {
            "camera_mode": self.camera_controller.get_camera_mode(),
            "stream_url": self.video_streamer.get_stream_url(),
            "stream_quality": self.video_streamer.get_quality_report(),
            "connection_quality": self.connection_quality,
            "timestamp": time.time()
        }
        
        # Send to all clients
        self.send_status(status)
        
    def _handle_quality_report(self, quality_data):
        """Handle quality report from video streamer"""
        logger.debug(f"Received quality report: {quality_data}")
        
        quality = quality_data.get("quality", "unknown")
        
        if quality == "bad":
            self.bad_quality_count += 1
            self.good_quality_count = 0
            
            # If we've had 3 bad quality reports in a row, need to take action
            if self.bad_quality_count >= 3 and not self.restart_scheduled:
                logger.warning("Three consecutive bad quality reports - scheduling WiFi service restart")
                self.restart_scheduled = True
                
                # Send notification to clients
                self.send_status({
                    "event": "wifi_restart_scheduled",
                    "timestamp": time.time()
                })
                
        elif quality == "good":
            self.good_quality_count += 1
            
            # Reset bad quality counter if we have a good report
            if self.bad_quality_count > 0:
                self.bad_quality_count = 0
                
            # If we've had 3 good quality reports after a restart, notify clients
            if self.good_quality_count == 3 and self.connection_quality != "good":
                logger.info("Connection quality restored to good")
                self.connection_quality = "good"
                
                # Send notification to clients
                self.send_status({
                    "event": "wifi_connection_restored",
                    "timestamp": time.time()
                })
                
        # Update connection quality status
        if self.bad_quality_count >= 3:
            self.connection_quality = "bad"
        
    def _restart_wifi_service(self):
        """Restart WiFi service after poor quality reports"""
        logger.info("Restarting WiFi service")
        
        # In a real implementation, this would restart the actual WiFi service
        # For now, we'll just simulate a restart by closing and reopening the server socket
        
        # Close current server socket
        if self.server_socket:
            try:
                self.server_socket.close()
            except:
                pass
            self.server_socket = None
            
        # Sleep briefly to allow socket to close properly
        time.sleep(5)
        
        # Create new server socket
        try:
            self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            self.server_socket.bind(('0.0.0.0', self.port))
            self.server_socket.listen(5)
            self.server_socket.settimeout(1.0)
            
            logger.info(f"WiFi service restarted, listening on 0.0.0.0:{self.port}")
            
        except Exception as e:
            logger.error(f"Error restarting WiFi service: {e}")

if __name__ == "__main__":
    # Set up logging for standalone testing
    logging.basicConfig(
        level=logging.DEBUG,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
    )
    
    # Mock classes for testing
    class MockCameraController:
        def process_command(self, command):
            print(f"Processing command: {command}")
        def set_pan(self, speed):
            print(f"Pan speed: {speed}")
        def set_tilt(self, speed):
            print(f"Tilt speed: {speed}")
        def set_zoom(self, level):
            print(f"Zoom level: {level}")
        def set_camera_mode(self, mode):
            print(f"Camera mode: {mode}")
        def get_camera_mode(self):
            return 0
    
    class MockVideoStreamer:
        def set_status_report_callback(self, callback):
            self.callback = callback
            # Simulate some quality reports
            threading.Thread(target=self._simulate_reports, daemon=True).start()
        def get_stream_url(self):
            return "rtsp://localhost:8554/stream"
        def get_quality_report(self):
            return {"quality": "good", "dropped_frames": 0}
        def _simulate_reports(self):
            import random
            while True:
                time.sleep(5)
                quality = "good" if random.random() > 0.3 else "bad"
                self.callback({"quality": quality, "dropped_frames": random.randint(0, 10)})
    
    # Test the WiFi server
    camera_controller = MockCameraController()
    video_streamer = MockVideoStreamer()
    server = WifiServer(camera_controller, video_streamer)
    
    server.start()
    
    try:
        print(f"Server running on port {server.port}")
        print("Press Ctrl+C to stop...")
        
        # Keep running for testing
        while True:
            time.sleep(10)
            server.send_status({"heartbeat": time.time()})
            
    except KeyboardInterrupt:
        print("Test interrupted")
    finally:
        server.stop()