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
import json
from enum import Enum

logger = logging.getLogger('video_streamer')

class StreamQuality(Enum):
    """Enum for stream quality"""
    LOW = 0
    MEDIUM = 1
    HIGH = 2

class VideoStreamer:
    """Handles video streaming from RGB and IR cameras"""
    
    def __init__(self, camera_controller, port=8554):
        """Initialize the video streamer
        
        Args:
            camera_controller: CameraController instance
            port: RTSP server port (default: 8554)
        """
        self.camera_controller = camera_controller
        self.port = port
        self.running = False
        self.stream_process = None
        self.monitoring_thread = None
        self.quality_stats = {"timestamp": time.time(), "quality": "good", "dropped_frames": 0}
        self.status_report_callback = None
        
    def start(self):
        """Start the video streamer"""
        if self.running:
            logger.warning("Video streamer is already running")
            return
            
        self.running = True
        self._start_stream()
        
        # Start monitoring thread for stream quality
        self.monitoring_thread = threading.Thread(target=self._monitor_stream)
        self.monitoring_thread.daemon = True
        self.monitoring_thread.start()
        
        logger.info(f"Video streamer started on port {self.port}")
        
    def stop(self):
        """Stop the video streamer"""
        if not self.running:
            logger.warning("Video streamer is not running")
            return
            
        self.running = False
        
        # Stop the stream process
        self._stop_stream()
        
        # Stop monitoring thread
        if self.monitoring_thread:
            self.monitoring_thread.join(timeout=2.0)
            
        logger.info("Video streamer stopped")
        
    def restart(self):
        """Restart the video streamer"""
        logger.info("Restarting video streamer")
        self.stop()
        time.sleep(1)
        self.start()
        
    def set_quality(self, quality):
        """Set stream quality
        
        Args:
            quality (StreamQuality or int): Stream quality level
        """
        if isinstance(quality, int):
            quality = StreamQuality(quality)
            
        logger.info(f"Setting stream quality to {quality.name}")
        # Restart stream with new quality settings
        self.restart()
        
    def get_stream_url(self):
        """Get the RTSP stream URL for the current camera"""
        # Get local IP address
        hostname = socket.gethostname()
        ip_address = socket.gethostbyname(hostname)
        
        camera_type = "rgb" if self.camera_controller.get_camera_mode() == 0 else "ir"
        return f"rtsp://{ip_address}:{self.port}/{camera_type}"
        
    def set_status_report_callback(self, callback):
        """Set callback function for stream status reports
        
        Args:
            callback: Function to call with status data
        """
        self.status_report_callback = callback
        
    def get_quality_report(self):
        """Get the current stream quality report"""
        return self.quality_stats.copy()
        
    def _start_stream(self):
        """Start the RTSP stream process"""
        # Get current camera device
        device = self.camera_controller.get_current_camera_device()
        camera_type = "rgb" if self.camera_controller.get_camera_mode() == 0 else "ir"
        
        logger.info(f"Starting {camera_type.upper()} stream from {device} on port {self.port}")
        
        # Build stream command based on platform and camera type
        # This example uses GStreamer pipeline to create an RTSP server
        try:
            # This is a simplified example - real implementation would have more robust error handling
            # and would use the actual video device capabilities
            
            # Raspberry Pi example using v4l2src
            cmd = [
                "gst-launch-1.0", "-v",
                "v4l2src", f"device={device}", "!", 
                "video/x-raw,width=640,height=480,framerate=30/1", "!",
                "videoconvert", "!",
                "x264enc", "tune=zerolatency", "bitrate=500", "speed-preset=superfast", "!",
                "rtph264pay", "name=pay0", "pt=96", "!",
                "udpsink", f"host=0.0.0.0", f"port={self.port}"
            ]
            
            # For NVIDIA Jetson, nvv4l2src might be used instead
            if "jetson" in os.uname().machine:
                cmd[1] = "nvv4l2src"  # Use NVIDIA's optimized source
                
            # Start process
            self.stream_process = subprocess.Popen(
                cmd,
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE
            )
            
            # Wait briefly to ensure process starts
            time.sleep(2)
            
            # Check if process is still running
            if self.stream_process.poll() is not None:
                stderr = self.stream_process.stderr.read().decode('utf-8')
                raise Exception(f"Stream process failed to start: {stderr}")
                
        except Exception as e:
            logger.error(f"Error starting stream: {e}")
            self.running = False
            return False
            
        return True
        
    def _stop_stream(self):
        """Stop the RTSP stream process"""
        if self.stream_process:
            try:
                # Try to terminate gracefully
                self.stream_process.terminate()
                self.stream_process.wait(timeout=5)
            except subprocess.TimeoutExpired:
                # Force kill if not terminated after timeout
                self.stream_process.kill()
                
            self.stream_process = None
            
    def _monitor_stream(self):
        """Monitor stream quality and report status"""
        logger.info("Stream quality monitoring started")
        
        last_check = time.time()
        check_interval = 5  # Check every 5 seconds
        
        while self.running:
            current_time = time.time()
            
            # Check stream quality periodically
            if current_time - last_check >= check_interval:
                self._check_stream_quality()
                last_check = current_time
                
            time.sleep(1)
            
        logger.info("Stream quality monitoring stopped")
        
    def _check_stream_quality(self):
        """Check stream quality and update stats"""
        # In a real implementation, this would check things like:
        # - Dropped frames
        # - Encoding/decoding latency
        # - Network bandwidth utilization
        # For this example, we'll simulate quality statistics
        
        # Get current timestamp
        current_time = time.time()
        
        # Simulate quality check
        try:
            # Check if stream process is still running
            if self.stream_process and self.stream_process.poll() is not None:
                quality = "bad"
                dropped_frames = 100  # Process died, so effectively all frames dropped
                
                # Restart the stream
                logger.warning("Stream process died, restarting...")
                self._start_stream()
            else:
                # Simulate some quality metrics
                # In reality, these would come from analyzing stream performance
                import random
                dropped_frames = random.randint(0, 10)
                quality = "good" if dropped_frames < 5 else "bad"
        except Exception as e:
            logger.error(f"Error checking stream quality: {e}")
            quality = "unknown"
            dropped_frames = 0
            
        # Update quality stats
        self.quality_stats = {
            "timestamp": current_time,
            "quality": quality,
            "dropped_frames": dropped_frames
        }
        
        # Send report via callback if configured
        if self.status_report_callback:
            try:
                self.status_report_callback(self.quality_stats)
            except Exception as e:
                logger.error(f"Error in status report callback: {e}")
                
        logger.debug(f"Stream quality check: {quality} (dropped frames: {dropped_frames})")

if __name__ == "__main__":
    # Set up logging for standalone testing
    logging.basicConfig(
        level=logging.DEBUG,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
    )
    
    # Mock camera controller for testing
    class MockCameraController:
        def get_current_camera_device(self):
            return "/dev/video0"
        def get_camera_mode(self):
            return 0  # RGB mode
    
    # Test the video streamer
    camera_controller = MockCameraController()
    streamer = VideoStreamer(camera_controller)
    
    def print_status(status):
        print(f"Stream status: {json.dumps(status)}")
    
    streamer.set_status_report_callback(print_status)
    streamer.start()
    
    try:
        print(f"Streaming at URL: {streamer.get_stream_url()}")
        print("Press Ctrl+C to stop...")
        
        # Keep running for testing
        while True:
            time.sleep(5)
            report = streamer.get_quality_report()
            print(f"Quality report: {json.dumps(report)}")
            
    except KeyboardInterrupt:
        print("Test interrupted")
    finally:
        streamer.stop()