#!/usr/bin/env python3
"""
Main server application for controlling PTZ cameras from an Android tablet.
This script initializes camera control and streaming components and handles
communication with the tablet application via WiFi or Bluetooth.
"""

import os
import sys
import json
import logging
import argparse
import threading
from time import sleep

from camera_controller import CameraController
from video_streamer import VideoStreamer
from wifi_server import WifiServer
from bt_server import BluetoothServer

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger('camera_server')

class CameraServer:
    """Main server class for PTZ camera control"""
    
    def __init__(self, config=None):
        """Initialize the camera server with a given configuration"""
        self.config = config or {
            'rgb_device': '/dev/video0',
            'ir_device': '/dev/video1',
            'stream_port': 8554,
            'control_port': 8000,
            'enable_bluetooth': True,
            'enable_wifi': True,
            'log_level': 'INFO'
        }
        
        # Set up logging
        numeric_level = getattr(logging, self.config.get('log_level', 'INFO').upper(), None)
        if isinstance(numeric_level, int):
            logger.setLevel(numeric_level)
            
        logger.info("Initializing Camera Server")
        
        # Initialize camera controller
        try:
            self.camera_controller = CameraController(
                rgb_device=self.config.get('rgb_device'),
                ir_device=self.config.get('ir_device')
            )
        except Exception as e:
            logger.error(f"Failed to initialize camera controller: {e}")
            raise
            
        # Initialize video streamer
        try:
            self.video_streamer = VideoStreamer(
                camera_controller=self.camera_controller,
                port=self.config.get('stream_port', 8554)
            )
        except Exception as e:
            logger.error(f"Failed to initialize video streamer: {e}")
            raise
            
        # Initialize communication servers
        self.wifi_server = None
        self.bt_server = None
        
        if self.config.get('enable_wifi', True):
            try:
                self.wifi_server = WifiServer(
                    camera_controller=self.camera_controller,
                    port=self.config.get('control_port', 8000)
                )
            except Exception as e:
                logger.error(f"Failed to initialize WiFi server: {e}")
                self.wifi_server = None
                
        if self.config.get('enable_bluetooth', True):
            try:
                self.bt_server = BluetoothServer(
                    camera_controller=self.camera_controller
                )
            except Exception as e:
                logger.error(f"Failed to initialize Bluetooth server: {e}")
                self.bt_server = None
                
        # Check if at least one communication method is available
        if not self.wifi_server and not self.bt_server:
            logger.error("No communication methods available, exiting")
            raise RuntimeError("Failed to initialize any communication method")
            
        logger.info("Camera Server initialized successfully")
        
    def start(self):
        """Start all services"""
        logger.info("Starting Camera Server services")
        
        # Start camera controller
        self.camera_controller.start()
        
        # Start video streamer
        self.video_streamer.start()
        
        # Start communication servers
        if self.wifi_server:
            self.wifi_server.start()
            
        if self.bt_server:
            self.bt_server.start()
            
        logger.info("All services started")
        
    def stop(self):
        """Stop all services"""
        logger.info("Stopping Camera Server services")
        
        # Stop communication servers
        if self.wifi_server:
            self.wifi_server.stop()
            
        if self.bt_server:
            self.bt_server.stop()
            
        # Stop video streamer
        self.video_streamer.stop()
        
        # Stop camera controller
        self.camera_controller.stop()
        
        logger.info("All services stopped")
        
    def run(self):
        """Run the server in the main thread"""
        try:
            self.start()
            
            # Keep the main thread alive
            logger.info("Server running. Press Ctrl+C to stop.")
            while True:
                sleep(1)
                
        except KeyboardInterrupt:
            logger.info("Keyboard interrupt received")
        except Exception as e:
            logger.error(f"Error in main loop: {e}")
        finally:
            self.stop()
            

def parse_arguments():
    """Parse command line arguments"""
    parser = argparse.ArgumentParser(description='PTZ Camera Server for Android Controller')
    
    parser.add_argument('--rgb-device', type=str, default='/dev/video0',
                        help='RGB camera device path (default: /dev/video0)')
    parser.add_argument('--ir-device', type=str, default='/dev/video1',
                        help='IR/Thermal camera device path (default: /dev/video1)')
    parser.add_argument('--stream-port', type=int, default=8554,
                        help='RTSP streaming port (default: 8554)')
    parser.add_argument('--control-port', type=int, default=8000,
                        help='HTTP/TCP control port (default: 8000)')
    parser.add_argument('--no-bluetooth', action='store_true',
                        help='Disable Bluetooth communication')
    parser.add_argument('--no-wifi', action='store_true',
                        help='Disable WiFi communication')
    parser.add_argument('--log-level', type=str, choices=['DEBUG', 'INFO', 'WARNING', 'ERROR'], 
                        default='INFO', help='Logging level (default: INFO)')
    
    return parser.parse_args()

def main():
    """Main function"""
    args = parse_arguments()
    
    # Prepare configuration
    config = {
        'rgb_device': args.rgb_device,
        'ir_device': args.ir_device,
        'stream_port': args.stream_port,
        'control_port': args.control_port,
        'enable_bluetooth': not args.no_bluetooth,
        'enable_wifi': not args.no_wifi,
        'log_level': args.log_level
    }
    
    try:
        server = CameraServer(config)
        server.run()
    except Exception as e:
        logger.error(f"Fatal error: {e}")
        sys.exit(1)
        
if __name__ == "__main__":
    main()
