#!/usr/bin/env python3
"""
Bluetooth Server for PTZ Camera Control.
This module handles Bluetooth communication with the Android tablet app,
receiving control commands and sending status updates.
"""

import os
import time
import logging
import threading
import json
import socket

logger = logging.getLogger('bt_server')

# Try to import PyBluez
try:
    import bluetooth
except ImportError:
    logger.warning("PyBluez not available. Using mock implementation for development/testing.")
    # Mock implementation for development/testing
    class MockBluetooth:
        """Mock implementation of PyBluez for simulation"""
        RFCOMM = 1
        PORT_ANY = 0
        SERIAL_PORT_CLASS = "serial-port-class"
        SERIAL_PORT_PROFILE = "serial-port-profile"
        
        class BluetoothSocket:
            """Mock bluetooth socket"""
            def __init__(self, protocol):
                logger.info(f"Creating mock Bluetooth socket with protocol {protocol}")
                self.protocol = protocol
                self.bound = False
                self.listening = False
                self.port = None
                self.closed = False
                self.timeout = None
                self.mock_client_data = [
                    b'{"type":"pan","value":50}\n',
                    b'{"type":"tilt","value":-30}\n',
                    b'{"type":"zoom","value":75}\n',
                    b'{"type":"mode","value":1}\n'
                ]
                self.mock_data_index = 0
                
            def bind(self, address):
                """Simulate binding to an address"""
                logger.info(f"Mock Bluetooth socket bound to {address}")
                self.bound = True
                
            def listen(self, backlog):
                """Simulate listening for connections"""
                logger.info(f"Mock Bluetooth socket listening with backlog {backlog}")
                self.listening = True
                
            def getsockname(self):
                """Return a fake socket name with port"""
                return ("00:00:00:00:00:00", 1)
                
            def settimeout(self, timeout):
                """Set socket timeout"""
                self.timeout = timeout
                
            def accept(self):
                """Simulate accepting a connection"""
                if not self.listening:
                    raise Exception("Socket is not listening")
                    
                # Simulate timeout
                time.sleep(self.timeout if self.timeout else 1.0)
                
                # Create mock client socket
                client_sock = MockBluetooth.BluetoothSocket(self.protocol)
                client_addr = "11:22:33:44:55:66"
                
                logger.info(f"Mock Bluetooth connection accepted from {client_addr}")
                return client_sock, client_addr
                
            def recv(self, bufsize):
                """Simulate receiving data"""
                if self.closed:
                    return b''
                    
                # Simulate timeout or no data
                time.sleep(0.5)
                
                # Return mock data occasionally
                if self.mock_data_index < len(self.mock_client_data) and time.time() % 10 < 3:
                    data = self.mock_client_data[self.mock_data_index]
                    self.mock_data_index = (self.mock_data_index + 1) % len(self.mock_client_data)
                    return data
                    
                return b''
                
            def send(self, data):
                """Simulate sending data"""
                if self.closed:
                    raise Exception("Socket is closed")
                    
                logger.debug(f"Mock Bluetooth socket sending {len(data)} bytes")
                return len(data)
                
            def close(self):
                """Close the socket"""
                logger.info("Mock Bluetooth socket closed")
                self.closed = True
        
        def advertise_service(sock, name, service_id, service_classes, profiles):
            """Simulate advertising a Bluetooth service"""
            logger.info(f"Advertising mock Bluetooth service '{name}' with UUID {service_id}")
    
    # Use mock implementation
    bluetooth = MockBluetooth()

class BluetoothServer:
    """Bluetooth server for PTZ camera control"""
    
    def __init__(self, camera_controller, uuid="00001101-0000-1000-8000-00805F9B34FB"):
        """Initialize the Bluetooth server
        
        Args:
            camera_controller: CameraController instance
            uuid: Service UUID (default: standard SPP UUID)
        """
        self.camera_controller = camera_controller
        self.uuid = uuid
        self.running = False
        self.server_thread = None
        self.server_socket = None
        self.client_socket = None
        self.client_address = None
        self.client_handler_thread = None
        
    def start(self):
        """Start the Bluetooth server"""
        if self.running:
            logger.warning("Bluetooth server is already running")
            return
            
        self.running = True
        self.server_thread = threading.Thread(target=self._server_loop)
        self.server_thread.daemon = True
        self.server_thread.start()
        
        logger.info("Bluetooth server started")
        return True
        
    def stop(self):
        """Stop the Bluetooth server"""
        if not self.running:
            logger.warning("Bluetooth server is not running")
            return
            
        self.running = False
        
        # Close client connection
        if self.client_socket:
            try:
                self.client_socket.close()
            except:
                pass
            self.client_socket = None
            self.client_address = None
            
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
            
        logger.info("Bluetooth server stopped")
        
    def _server_loop(self):
        """Main server loop that handles Bluetooth connections"""
        try:
            # Create server socket
            self.server_socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
            self.server_socket.bind(("", bluetooth.PORT_ANY))
            self.server_socket.listen(1)  # Only one connection at a time
            
            # Get the port number assigned to the socket
            port = self.server_socket.getsockname()[1]
            
            # Advertise the service
            bluetooth.advertise_service(
                self.server_socket,
                "PTZ Camera Controller",
                self.uuid,
                service_classes=[self.uuid, bluetooth.SERIAL_PORT_CLASS],
                profiles=[bluetooth.SERIAL_PORT_PROFILE]
            )
            
            logger.info(f"Bluetooth server listening on RFCOMM channel {port}")
            
            while self.running:
                # Wait for client connection
                logger.info("Waiting for Bluetooth connection...")
                
                # Set timeout to allow for periodic checks
                self.server_socket.settimeout(1.0)
                
                try:
                    client_sock, client_info = self.server_socket.accept()
                    logger.info(f"Accepted Bluetooth connection from {client_info}")
                    
                    # Store client info
                    self.client_socket = client_sock
                    self.client_address = client_info
                    
                    # Handle client in a separate thread
                    self.client_handler_thread = threading.Thread(
                        target=self._handle_client,
                        args=(client_sock, client_info)
                    )
                    self.client_handler_thread.daemon = True
                    self.client_handler_thread.start()
                    
                except socket.timeout:
                    # No new connections, continue
                    pass
                except Exception as e:
                    if self.running:  # Only log if we're still supposed to be running
                        logger.error(f"Error accepting Bluetooth connection: {e}")
                
                # Sleep briefly to prevent CPU hogging
                time.sleep(0.01)
                
        except Exception as e:
            logger.error(f"Bluetooth server error: {e}")
        finally:
            # Clean up
            if self.server_socket:
                try:
                    self.server_socket.close()
                except:
                    pass
                
            logger.info("Bluetooth server loop ended")
        
    def _handle_client(self, client_sock, client_info):
        """Handle communication with a connected client"""
        logger.info(f"Bluetooth client handler started for {client_info}")
        
        try:
            # Set small timeout to allow checking running state
            client_sock.settimeout(0.5)
            
            while self.running and self.client_socket == client_sock:
                try:
                    # Receive data
                    data = client_sock.recv(1024)
                    
                    if not data:
                        # Client disconnected
                        logger.info(f"Bluetooth client disconnected: {client_info}")
                        break
                        
                    # Process received data
                    try:
                        # Split data by newlines to handle multiple commands
                        commands = data.decode('utf-8').strip().split('\n')
                        
                        for cmd_str in commands:
                            if cmd_str:
                                # Try to parse as JSON
                                try:
                                    command = json.loads(cmd_str)
                                    self._process_command(command)
                                except json.JSONDecodeError:
                                    # Not valid JSON, try as text command
                                    self._process_text_command(cmd_str)
                    except Exception as e:
                        logger.error(f"Error processing Bluetooth data: {e}")
                    
                except socket.timeout:
                    # No data available, continue
                    continue
                except Exception as e:
                    logger.error(f"Error receiving Bluetooth data: {e}")
                    break
                    
        except Exception as e:
            logger.error(f"Bluetooth client handler error: {e}")
        finally:
            # Close the client socket
            try:
                client_sock.close()
            except:
                pass
                
            # Clear client info if this is still the current client
            if self.client_socket == client_sock:
                self.client_socket = None
                self.client_address = None
                
            logger.info(f"Bluetooth client handler ended for {client_info}")
    
    def _process_command(self, command):
        """Process a command received from the client"""
        logger.debug(f"Received Bluetooth command: {command}")
        
        if not isinstance(command, dict):
            logger.warning(f"Invalid command format: {command}")
            return
            
        # Forward to camera controller
        try:
            self.camera_controller.process_command(command)
        except Exception as e:
            logger.error(f"Error processing Bluetooth command: {e}")
    
    def _process_text_command(self, command_str):
        """Process a text command received from the client"""
        logger.debug(f"Received Bluetooth text command: {command_str}")
        
        # Parse simple text commands (same as in WiFi server)
        cmd_parts = command_str.split()
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
                
        except Exception as e:
            logger.error(f"Error processing Bluetooth text command: {e}")
    
    def send_status(self, status):
        """Send a status update to the connected client
        
        Args:
            status: Status data dictionary to send
        """
        if not self.running or not self.client_socket:
            return
            
        # Convert to JSON
        try:
            status_json = json.dumps(status)
        except Exception as e:
            logger.error(f"Error serializing status data: {e}")
            return
            
        # Send to client
        try:
            message = status_json.encode('utf-8') + b'\n'
            self.client_socket.send(message)
        except Exception as e:
            logger.warning(f"Error sending status via Bluetooth: {e}")
            
            # Close the connection if there was an error
            try:
                self.client_socket.close()
            except:
                pass
                
            self.client_socket = None
            self.client_address = None

if __name__ == "__main__":
    # Set up logging for standalone testing
    logging.basicConfig(
        level=logging.DEBUG,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
    )
    
    # Mock camera controller for testing
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
    
    # Test the Bluetooth server
    camera_controller = MockCameraController()
    server = BluetoothServer(camera_controller)
    
    server.start()
    
    try:
        print("Bluetooth server running")
        print("Press Ctrl+C to stop...")
        
        # Keep running for testing
        while True:
            time.sleep(5)
            if server.client_socket:
                server.send_status({"heartbeat": time.time()})
            
    except KeyboardInterrupt:
        print("Test interrupted")
    finally:
        server.stop()