#!/usr/bin/env python3
"""
Bluetooth Server for PTZ Camera Control.
This module handles Bluetooth communication with the Android tablet app,
receiving control commands and sending status updates.
"""

import os
import json
import logging
import threading
import time

# For demo, we'll simulate bluetooth rather than requiring PyBluez
# try:
#     import bluetooth as bt
# except ImportError:
#     bt = None

# Mock bluetooth module for simulation
class MockBluetooth:
    """Mock implementation of PyBluez for simulation"""
    RFCOMM = 1
    PORT_ANY = 0
    SERIAL_PORT_CLASS = "serial-port-class"
    SERIAL_PORT_PROFILE = "serial-port-profile"
    
    class BluetoothSocket:
        """Mock bluetooth socket"""
        def __init__(self, protocol):
            self.protocol = protocol
            self.bound = False
            self.listening = False
            self.timeout = None
            self.data_buffer = []
            
        def bind(self, address):
            """Simulate binding to an address"""
            self.bound = True
            
        def listen(self, backlog):
            """Simulate listening for connections"""
            self.listening = True
            
        def getsockname(self):
            """Return a fake socket name with port"""
            return ("00:11:22:33:44:55", 1)
            
        def settimeout(self, timeout):
            """Set socket timeout"""
            self.timeout = timeout
            
        def accept(self):
            """Simulate accepting a connection"""
            # Always time out so it doesn't block in a real loop
            # But also return mock client data occasionally for simulation
            if time.time() % 30 < 5:  # Only accept connections 5 seconds out of every 30
                mock_client_sock = MockBluetooth.BluetoothSocket(MockBluetooth.RFCOMM)
                mock_client_info = ("11:22:33:44:55:66", "MockClient")
                return (mock_client_sock, mock_client_info)
            else:
                raise TimeoutError("Simulated timeout")
                
        def recv(self, bufsize):
            """Simulate receiving data"""
            # Simulate timeouts most of the time
            if time.time() % 10 < 9:  # 90% of the time
                raise TimeoutError("Simulated timeout")
                
            # Occasionally return some simulated commands
            commands = [
                '{"type":"pan", "value":50}',
                '{"type":"tilt", "value":-30}',
                '{"type":"zoom", "value":75}',
                '{"type":"mode", "value":1}'
            ]
            
            # Choose a random command
            import random
            command = random.choice(commands)
            
            # Log what we're simulating
            logger.debug(f"Simulating received data: {command}")
            
            # Return it as bytes
            return (command + '\n').encode('utf-8')
            
        def send(self, data):
            """Simulate sending data"""
            # Just log what would be sent
            logger.debug(f"Would send: {data.decode('utf-8', errors='replace')}")
            return len(data)  # Return number of bytes that would be sent
            
        def close(self):
            """Close the socket"""
            self.bound = False
            self.listening = False
            
    @staticmethod
    def advertise_service(sock, name, service_id, service_classes, profiles):
        """Simulate advertising a Bluetooth service"""
        logger.info(f"Simulating Bluetooth service advertisement: {name}")

# Use the mock bluetooth module for simulation
bt = MockBluetooth()

logger = logging.getLogger('bluetooth_server')

class BluetoothServer:
    """Bluetooth server for PTZ camera control"""
    
    def __init__(self, camera_controller, uuid="00001101-0000-1000-8000-00805F9B34FB"):
        """Initialize the Bluetooth server"""
        self.camera_controller = camera_controller
        self.uuid = uuid
        
        # Since we're using a mock Bluetooth module for simulation,
        # we don't need to check if it's available
        logger.info("Using simulated Bluetooth module for demonstration")
        
        self.server_sock = None
        self.client_sock = None
        self.client_info = None
        self.running = False
        self.server_thread = None
        
        # Lock for thread safety
        self.lock = threading.Lock()
        
        logger.info("Bluetooth server initialized")
    
    def start(self):
        """Start the Bluetooth server"""
        with self.lock:
            if self.running:
                logger.warning("Bluetooth server is already running")
                return
                
            self.running = True
            self.server_thread = threading.Thread(target=self._server_loop)
            self.server_thread.daemon = True
            self.server_thread.start()
            logger.info("Bluetooth server started")
    
    def stop(self):
        """Stop the Bluetooth server"""
        with self.lock:
            if not self.running:
                logger.warning("Bluetooth server is not running")
                return
                
            self.running = False
            
            # Close client connection
            if self.client_sock:
                try:
                    self.client_sock.close()
                except Exception as e:
                    logger.error(f"Error closing client socket: {e}")
                self.client_sock = None
                self.client_info = None
            
            # Close server socket
            if self.server_sock:
                try:
                    self.server_sock.close()
                except Exception as e:
                    logger.error(f"Error closing server socket: {e}")
                self.server_sock = None
            
            # Wait for server thread to end
            if self.server_thread:
                self.server_thread.join(timeout=2.0)
                self.server_thread = None
                
            logger.info("Bluetooth server stopped")
    
    def _server_loop(self):
        """Main server loop that handles Bluetooth connections"""
        logger.info("Bluetooth server loop started")
        
        try:
            # Create a Bluetooth server socket
            self.server_sock = bt.BluetoothSocket(bt.RFCOMM)
            self.server_sock.bind(("", bt.PORT_ANY))
            self.server_sock.listen(1)
            
            # Get the port and advertise the service
            port = self.server_sock.getsockname()[1]
            bt.advertise_service(
                self.server_sock,
                "PTZCameraServer",
                service_id=self.uuid,
                service_classes=[self.uuid, bt.SERIAL_PORT_CLASS],
                profiles=[bt.SERIAL_PORT_PROFILE]
            )
            
            logger.info(f"Bluetooth server listening on port {port}")
            
            while self.running:
                # Set a timeout so we can check if we should exit
                self.server_sock.settimeout(1.0)
                
                try:
                    # Wait for a connection
                    client_sock, client_info = self.server_sock.accept()
                    
                    with self.lock:
                        self.client_sock = client_sock
                        self.client_info = client_info
                        
                    logger.info(f"Accepted connection from {client_info}")
                    
                    # Handle client in a separate thread
                    client_thread = threading.Thread(
                        target=self._handle_client,
                        args=(client_sock, client_info)
                    )
                    client_thread.daemon = True
                    client_thread.start()
                    
                except TimeoutError:
                    # Timeout occurred, check if we should exit
                    continue
                except Exception as e:
                    logger.error(f"Error accepting connection: {e}")
                    time.sleep(1)
                    
        except Exception as e:
            logger.error(f"Error in Bluetooth server loop: {e}")
            
        finally:
            # Make sure we clean up
            if self.server_sock:
                try:
                    self.server_sock.close()
                except Exception as e:
                    logger.error(f"Error closing server socket: {e}")
                self.server_sock = None
                
            self.running = False
            logger.info("Bluetooth server loop ended")
    
    def _handle_client(self, client_sock, client_info):
        """Handle communication with a connected client"""
        logger.info(f"Handling client {client_info}")
        
        try:
            # Set socket options
            client_sock.settimeout(0.5)
            
            buffer = ""
            
            while self.running and client_sock == self.client_sock:
                try:
                    # Read data from the client
                    data = client_sock.recv(1024).decode('utf-8')
                    
                    if not data:
                        logger.info("Client disconnected")
                        break
                        
                    # Append data to buffer
                    buffer += data
                    
                    # Process complete commands (delimited by newlines)
                    while '\n' in buffer:
                        line, buffer = buffer.split('\n', 1)
                        self._process_command(line.strip())
                        
                except TimeoutError:
                    # Timeout reading from client, try again
                    continue
                except Exception as e:
                    logger.error(f"Error reading from client: {e}")
                    break
                    
        except Exception as e:
            logger.error(f"Error handling client: {e}")
            
        finally:
            # Clean up
            try:
                client_sock.close()
            except Exception as e:
                logger.error(f"Error closing client socket: {e}")
                
            # Update server state
            with self.lock:
                if self.client_sock == client_sock:
                    self.client_sock = None
                    self.client_info = None
                    
            logger.info(f"Client {client_info} disconnected")
    
    def _process_command(self, command_str):
        """Process a command received from the client"""
        logger.debug(f"Received command: {command_str}")
        
        try:
            # Parse the JSON command
            command = json.loads(command_str)
            
            # Forward the command to the camera controller
            result = self.camera_controller.process_command(command)
            
            if result:
                logger.debug(f"Command processed successfully: {command}")
            else:
                logger.warning(f"Failed to process command: {command}")
                
        except json.JSONDecodeError:
            logger.error(f"Invalid JSON command: {command_str}")
        except Exception as e:
            logger.error(f"Error processing command: {e}")
    
    def send_status(self, status):
        """Send a status update to the connected client"""
        if not self.client_sock:
            return False
            
        try:
            # Convert status to JSON
            status_json = json.dumps(status)
            
            # Send the status
            self.client_sock.send(status_json.encode('utf-8') + b'\n')
            return True
            
        except Exception as e:
            logger.error(f"Error sending status: {e}")
            return False
