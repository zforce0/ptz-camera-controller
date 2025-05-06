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

# Import our components
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

# Try to import the local stream viewer for debugging with a connected monitor
try:
    from local_stream_viewer import LocalStreamViewer
    HAS_LOCAL_VIEWER = True
except ImportError:
    logger.warning("Local stream viewer module not available, monitor display disabled")
    HAS_LOCAL_VIEWER = False

class CameraServer:
    """Main server class for PTZ camera control"""
    
    def __init__(self, config=None):
        """Initialize the camera server with a given configuration"""
        # Set default configuration
        self.config = {
            "rgb_device": "/dev/video0",
            "ir_device": "/dev/video1",
            "wifi_port": 8000,
            "rtsp_port": 8554,
            "use_bluetooth": True,
            "use_local_viewer": False
        }
        
        # Update with provided configuration
        if config:
            self.config.update(config)
            
        logger.info(f"Initializing camera server with config: {json.dumps(self.config)}")
        
        # Create components
        self.camera_controller = None
        self.video_streamer = None
        self.wifi_server = None
        self.bt_server = None
        self.local_viewer = None
        
        # Initialize components
        self._init_components()
        
    def _init_components(self):
        """Initialize all server components"""
        logger.info("Initializing camera controller")
        self.camera_controller = CameraController(
            rgb_device=self.config["rgb_device"],
            ir_device=self.config["ir_device"]
        )
        
        logger.info("Initializing video streamer")
        self.video_streamer = VideoStreamer(
            camera_controller=self.camera_controller,
            port=self.config["rtsp_port"]
        )
        
        logger.info("Initializing WiFi server")
        self.wifi_server = WifiServer(
            camera_controller=self.camera_controller,
            video_streamer=self.video_streamer,
            port=self.config["wifi_port"]
        )
        
        if self.config["use_bluetooth"]:
            logger.info("Initializing Bluetooth server")
            self.bt_server = BluetoothServer(
                camera_controller=self.camera_controller
            )
        
        if HAS_LOCAL_VIEWER and self.config["use_local_viewer"]:
            logger.info("Initializing local stream viewer")
            self.local_viewer = LocalStreamViewer(
                camera_controller=self.camera_controller,
                video_streamer=self.video_streamer
            )
        
    def start(self):
        """Start all services"""
        logger.info("Starting all services")
        
        try:
            # Start camera controller
            logger.info("Starting camera controller")
            self.camera_controller.start()
            
            # Start video streamer
            logger.info("Starting video streamer")
            self.video_streamer.start()
            
            # Start WiFi server
            logger.info("Starting WiFi server")
            self.wifi_server.start()
            
            # Start Bluetooth server if enabled
            if self.bt_server:
                logger.info("Starting Bluetooth server")
                self.bt_server.start()
                
            # Start local viewer if enabled
            if self.local_viewer:
                logger.info("Starting local stream viewer")
                self.local_viewer.start()
                
            logger.info("All services started successfully")
            return True
            
        except Exception as e:
            logger.error(f"Error starting services: {e}")
            self.stop()
            return False
        
    def stop(self):
        """Stop all services"""
        logger.info("Stopping all services")
        
        # Stop in reverse order
        if self.local_viewer:
            try:
                logger.info("Stopping local stream viewer")
                self.local_viewer.stop()
            except Exception as e:
                logger.error(f"Error stopping local viewer: {e}")
                
        if self.bt_server:
            try:
                logger.info("Stopping Bluetooth server")
                self.bt_server.stop()
            except Exception as e:
                logger.error(f"Error stopping Bluetooth server: {e}")
                
        try:
            logger.info("Stopping WiFi server")
            self.wifi_server.stop()
        except Exception as e:
            logger.error(f"Error stopping WiFi server: {e}")
            
        try:
            logger.info("Stopping video streamer")
            self.video_streamer.stop()
        except Exception as e:
            logger.error(f"Error stopping video streamer: {e}")
            
        try:
            logger.info("Stopping camera controller")
            self.camera_controller.stop()
        except Exception as e:
            logger.error(f"Error stopping camera controller: {e}")
            
        logger.info("All services stopped")
        
    def run(self):
        """Run the server in the main thread"""
        if not self.start():
            logger.error("Failed to start server")
            return 1
            
        try:
            # Print service info
            wifi_addr = f"0.0.0.0:{self.config['wifi_port']}"
            rtsp_url = self.video_streamer.get_stream_url()
            
            logger.info("========================================")
            logger.info("PTZ Camera Controller Server")
            logger.info("========================================")
            logger.info(f"WiFi control: {wifi_addr}")
            logger.info(f"Video stream: {rtsp_url}")
            logger.info(f"Bluetooth: {'enabled' if self.bt_server else 'disabled'}")
            logger.info(f"Local viewer: {'enabled' if self.local_viewer else 'disabled'}")
            logger.info("----------------------------------------")
            logger.info("Press Ctrl+C to stop the server")
            logger.info("========================================")
            
            # Keep the main thread running
            while True:
                sleep(1)
                
        except KeyboardInterrupt:
            logger.info("Server interrupted by user")
        except Exception as e:
            logger.error(f"Server error: {e}")
        finally:
            self.stop()
            
        return 0

def parse_arguments():
    """Parse command line arguments"""
    parser = argparse.ArgumentParser(description="PTZ Camera Controller Server")
    
    parser.add_argument("--rgb", dest="rgb_device", default="/dev/video0",
                        help="RGB camera device (default: /dev/video0)")
    parser.add_argument("--ir", dest="ir_device", default="/dev/video1",
                        help="IR/Thermal camera device (default: /dev/video1)")
    parser.add_argument("--wifi-port", dest="wifi_port", type=int, default=8000,
                        help="WiFi server port (default: 8000)")
    parser.add_argument("--rtsp-port", dest="rtsp_port", type=int, default=8554,
                        help="RTSP streaming port (default: 8554)")
    parser.add_argument("--no-bluetooth", dest="use_bluetooth", action="store_false",
                        help="Disable Bluetooth server")
    parser.add_argument("--local-viewer", dest="use_local_viewer", action="store_true",
                        help="Enable local stream viewer for connected monitor")
    
    return parser.parse_args()

def main():
    """Main function"""
    # Parse command line arguments
    args = parse_arguments()
    
    # Create configuration from arguments
    config = {
        "rgb_device": args.rgb_device,
        "ir_device": args.ir_device,
        "wifi_port": args.wifi_port,
        "rtsp_port": args.rtsp_port,
        "use_bluetooth": args.use_bluetooth,
        "use_local_viewer": args.use_local_viewer
    }
    
    # Create and run server
    server = CameraServer(config)
    return server.run()

if __name__ == "__main__":
    sys.exit(main())