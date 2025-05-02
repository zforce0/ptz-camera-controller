#!/usr/bin/env python3
"""
Video Streamer for PTZ Camera.
This module handles the video streaming from the cameras to the Android tablet.
It supports both RGB and IR/Thermal cameras and switches between them based on
the current mode.
"""

import os
import time
import logging
import threading
import subprocess
import signal
import socket

logger = logging.getLogger('video_streamer')

class VideoStreamer:
    """Streams video from PTZ cameras via RTSP"""
    
    def __init__(self, camera_controller, port=8554):
        """Initialize the video streamer"""
        self.camera_controller = camera_controller
        self.port = port
        
        self.rgb_process = None
        self.ir_process = None
        self.running = False
        
        # Lock for thread safety
        self.lock = threading.Lock()
        
        logger.info(f"Video streamer initialized on port {port}")
    
    def start(self):
        """Start the video streamer"""
        with self.lock:
            if self.running:
                logger.warning("Video streamer is already running")
                return
                
            self.running = True
            self._start_streaming()
            logger.info("Video streamer started")
    
    def stop(self):
        """Stop the video streamer"""
        with self.lock:
            if not self.running:
                logger.warning("Video streamer is not running")
                return
                
            self.running = False
            self._stop_streaming()
            logger.info("Video streamer stopped")
    
    def restart(self):
        """Restart the video streamer"""
        with self.lock:
            self._stop_streaming()
            if self.running:
                self._start_streaming()
                logger.info("Video streamer restarted")
    
    def _start_streaming(self):
        """Start both RGB and IR video streams"""
        # Start RGB stream
        self._start_rgb_stream()
        
        # Start IR stream
        self._start_ir_stream()
    
    def _stop_streaming(self):
        """Stop all video streams"""
        # Stop RGB stream
        if self.rgb_process:
            logger.info("Stopping RGB stream")
            try:
                self.rgb_process.send_signal(signal.SIGTERM)
                self.rgb_process.wait(timeout=5)
            except subprocess.TimeoutExpired:
                self.rgb_process.kill()
            except Exception as e:
                logger.error(f"Error stopping RGB stream: {e}")
            self.rgb_process = None
        
        # Stop IR stream
        if self.ir_process:
            logger.info("Stopping IR stream")
            try:
                self.ir_process.send_signal(signal.SIGTERM)
                self.ir_process.wait(timeout=5)
            except subprocess.TimeoutExpired:
                self.ir_process.kill()
            except Exception as e:
                logger.error(f"Error stopping IR stream: {e}")
            self.ir_process = None
    
    def _start_rgb_stream(self):
        """Start the RGB camera stream in simulation mode"""
        if self.rgb_process and self.rgb_process.poll() is None:
            logger.warning("RGB stream is already running")
            return
            
        rgb_device = self.camera_controller.rgb_device
        
        if not os.path.exists(rgb_device):
            logger.error(f"RGB camera device not found: {rgb_device}")
            return
            
        logger.info(f"Starting simulated RGB stream from {rgb_device}")
        
        try:
            # Create a simulation video stream status file
            stream_file = "/tmp/camera_sim/rgb_stream.txt"
            with open(stream_file, "w") as f:
                f.write(f"RGB Camera Stream Simulation\n")
                f.write(f"Status: Running\n")
                f.write(f"Port: {self.port}\n")
                f.write(f"Resolution: 1280x720\n")
                f.write(f"Framerate: 30fps\n")
                f.write(f"Started: {time.strftime('%Y-%m-%d %H:%M:%S')}\n")
                
            # In a real implementation, we would start GStreamer here
            # For simulation, we'll just create a dummy process that sleeps
            self.rgb_process = subprocess.Popen(
                ["sleep", "86400"],  # Sleep for 24 hours
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE
            )
            logger.info("Simulated RGB stream started")
        except Exception as e:
            logger.error(f"Failed to start RGB stream simulation: {e}")
            self.rgb_process = None
    
    def _start_ir_stream(self):
        """Start the IR/Thermal camera stream in simulation mode"""
        if self.ir_process and self.ir_process.poll() is None:
            logger.warning("IR stream is already running")
            return
            
        ir_device = self.camera_controller.ir_device
        
        if not os.path.exists(ir_device):
            logger.error(f"IR camera device not found: {ir_device}")
            return
            
        logger.info(f"Starting simulated IR stream from {ir_device}")
        
        try:
            # Create a simulation video stream status file
            stream_file = "/tmp/camera_sim/ir_stream.txt"
            with open(stream_file, "w") as f:
                f.write(f"IR/Thermal Camera Stream Simulation\n")
                f.write(f"Status: Running\n")
                f.write(f"Port: {self.port + 1}\n")
                f.write(f"Resolution: 640x480\n")
                f.write(f"Framerate: 15fps\n")
                f.write(f"Started: {time.strftime('%Y-%m-%d %H:%M:%S')}\n")
                
            # In a real implementation, we would start GStreamer here
            # For simulation, we'll just create a dummy process that sleeps
            self.ir_process = subprocess.Popen(
                ["sleep", "86400"],  # Sleep for 24 hours
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE
            )
            logger.info("Simulated IR stream started")
        except Exception as e:
            logger.error(f"Failed to start IR stream simulation: {e}")
            self.ir_process = None

    def get_stream_urls(self):
        """Get the URLs for the active streams"""
        # Get local IP address for proper streaming
        local_ip = self._get_local_ip_address()
            
        # Create stream URLs that can be accessed from external devices
        # In production with real cameras, these would point to the actual RTSP streams
        rgb_url = f"rtsp://{local_ip}:{self.port}/rgb"
        ir_url = f"rtsp://{local_ip}:{self.port + 1}/ir"
        
        # Log the URLs we're providing
        logger.info(f"Providing stream URLs - RGB: {rgb_url}, IR: {ir_url}")
        
        # Save the URLs to files for reference and for the local viewer to use
        try:
            with open("/tmp/camera_sim/stream_urls.txt", "w") as f:
                f.write(f"RGB Stream URL: {rgb_url}\n")
                f.write(f"IR Stream URL: {ir_url}\n")
                f.write(f"Generated: {time.strftime('%Y-%m-%d %H:%M:%S')}\n")
                if not self._check_for_real_cameras():
                    f.write("Note: These are simulation URLs and not actual streams.\n")
        except Exception as e:
            logger.error(f"Failed to save stream URLs: {e}")
        
        return {
            "rgb": rgb_url,
            "ir": ir_url
        }
        
    def _get_local_ip_address(self):
        """Get the local IP address for streaming"""
        try:
            # Create a socket and connect to an external server
            # This doesn't actually establish a connection but gives us the local IP
            s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            s.connect(("8.8.8.8", 80))
            local_ip = s.getsockname()[0]
            s.close()
            return local_ip
        except Exception as e:
            logger.warning(f"Could not determine local IP: {e}")
            # Fallback to localhost if we can't determine IP
            return "127.0.0.1"
    
    def _check_for_real_cameras(self):
        """Check if real camera devices are available"""
        # Common camera device paths to check
        camera_paths = [
            "/dev/video0",
            "/dev/video1",
            # Add more common paths as needed
        ]
        
        # Check if any of these exist and aren't our simulation files
        for path in camera_paths:
            if os.path.exists(path) and not path.startswith("/tmp/camera_sim"):
                return True
                
        return False
