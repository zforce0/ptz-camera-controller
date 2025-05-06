#!/usr/bin/env python3
"""
Local Stream Viewer for PTZ Camera System.
This module provides a way to view camera streams on the onboard computer 
for debugging purposes when a monitor is connected.
"""

import os
import cv2
import time
import logging
import threading
import socket
import json

# Configure logging
logger = logging.getLogger('local_stream_viewer')

class LocalStreamViewer:
    """Local viewer for displaying camera streams on a connected monitor"""
    
    def __init__(self, camera_controller, video_streamer, window_title="PTZ Camera Stream"):
        """Initialize the local stream viewer
        
        Args:
            camera_controller: CameraController instance
            video_streamer: VideoStreamer instance
            window_title: Title for the display window
        """
        self.camera_controller = camera_controller
        self.video_streamer = video_streamer
        self.window_title = window_title
        self.running = False
        self.viewer_thread = None
        
    def start(self):
        """Start the local stream viewer"""
        if self.running:
            logger.warning("Local stream viewer is already running")
            return
            
        self.running = True
        self.viewer_thread = threading.Thread(target=self._viewer_loop)
        self.viewer_thread.daemon = True
        self.viewer_thread.start()
        
        logger.info("Local stream viewer started")
        
    def stop(self):
        """Stop the local stream viewer"""
        if not self.running:
            logger.warning("Local stream viewer is not running")
            return
            
        self.running = False
        
        # Wait for viewer thread to end
        if self.viewer_thread:
            self.viewer_thread.join(timeout=2.0)
            
        # Close any open windows
        cv2.destroyAllWindows()
        
        logger.info("Local stream viewer stopped")
        
    def _viewer_loop(self):
        """Main viewer loop that displays the camera stream"""
        logger.info("Viewer loop started")
        
        # Try to get RTSP stream URL from the video streamer
        stream_url = self.video_streamer.get_stream_url()
        logger.info(f"Opening stream URL: {stream_url}")
        
        try:
            # Alternative 1: Try to connect to the RTSP stream
            cap = cv2.VideoCapture(stream_url)
            
            # If RTSP connection fails, try connecting directly to the camera device
            if not cap.isOpened():
                logger.warning(f"Failed to open RTSP stream, trying direct camera access")
                device_path = self.camera_controller.get_current_camera_device()
                
                # Try to open the camera device directly
                cap = cv2.VideoCapture(device_path)
                
                if not cap.isOpened():
                    logger.error(f"Failed to open camera device: {device_path}")
                    return
            
            # Get video properties
            frame_width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
            frame_height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
            fps = cap.get(cv2.CAP_PROP_FPS)
            
            logger.info(f"Stream properties: {frame_width}x{frame_height} at {fps} FPS")
            
            # Create window
            cv2.namedWindow(self.window_title, cv2.WINDOW_NORMAL)
            
            # Main display loop
            frames_count = 0
            start_time = time.time()
            
            while self.running:
                # Read a frame
                ret, frame = cap.read()
                
                if not ret:
                    logger.warning("Failed to read frame, attempting to reconnect...")
                    cap.release()
                    
                    # Try to reconnect
                    time.sleep(1)
                    
                    # Check if camera mode has changed
                    device_path = self.camera_controller.get_current_camera_device()
                    stream_url = self.video_streamer.get_stream_url()
                    
                    # Try RTSP first, then direct device
                    cap = cv2.VideoCapture(stream_url)
                    if not cap.isOpened():
                        cap = cv2.VideoCapture(device_path)
                        
                    continue
                
                # Calculate FPS
                frames_count += 1
                elapsed_time = time.time() - start_time
                if elapsed_time >= 5:  # Update FPS every 5 seconds
                    fps_actual = frames_count / elapsed_time
                    cv2.setWindowTitle(self.window_title, f"PTZ Camera Stream - {fps_actual:.2f} FPS")
                    frames_count = 0
                    start_time = time.time()
                
                # Add camera mode indicator
                camera_mode = "RGB" if self.camera_controller.get_camera_mode() == 0 else "IR/Thermal"
                cv2.putText(
                    frame, 
                    f"Mode: {camera_mode}", 
                    (10, 30), 
                    cv2.FONT_HERSHEY_SIMPLEX, 
                    1, 
                    (0, 255, 0), 
                    2
                )
                
                # Display the frame
                cv2.imshow(self.window_title, frame)
                
                # Check for key press (ESC or q to quit)
                key = cv2.waitKey(1) & 0xFF
                if key == 27 or key == ord('q'):
                    logger.info("User requested to close the viewer")
                    break
                    
                # Check for mode switch
                elif key == ord('m'):
                    new_mode = 1 if self.camera_controller.get_camera_mode() == 0 else 0
                    logger.info(f"User requested to switch camera mode to {new_mode}")
                    self.camera_controller.set_camera_mode(new_mode)
                    
                # Pan/tilt controls
                elif key == ord('w'):  # Tilt up
                    self.camera_controller.set_tilt(-50)
                elif key == ord('s'):  # Tilt down
                    self.camera_controller.set_tilt(50)
                elif key == ord('a'):  # Pan left
                    self.camera_controller.set_pan(-50)
                elif key == ord('d'):  # Pan right
                    self.camera_controller.set_pan(50)
                elif key == ord(' '):  # Stop movement
                    self.camera_controller.set_pan(0)
                    self.camera_controller.set_tilt(0)
                
                # Zoom controls
                elif key == ord('+') or key == ord('='):  # Zoom in
                    current_zoom = self.camera_controller.zoom_level
                    self.camera_controller.set_zoom(min(current_zoom + 10, 100))
                elif key == ord('-'):  # Zoom out
                    current_zoom = self.camera_controller.zoom_level
                    self.camera_controller.set_zoom(max(current_zoom - 10, 0))
                
        except Exception as e:
            logger.error(f"Error in viewer loop: {e}")
        finally:
            # Clean up
            try:
                cap.release()
                cv2.destroyAllWindows()
            except:
                pass
                
            logger.info("Viewer loop ended")

if __name__ == "__main__":
    # Set up logging for standalone testing
    logging.basicConfig(
        level=logging.DEBUG,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
    )
    
    # Mock classes for testing
    class MockCameraController:
        def __init__(self):
            self.zoom_level = 50
            self._mode = 0
            
        def get_current_camera_device(self):
            return "/dev/video0"
        def get_camera_mode(self):
            return self._mode
        def set_camera_mode(self, mode):
            print(f"Switching to mode: {mode}")
            self._mode = mode
        def set_pan(self, speed):
            print(f"Pan speed: {speed}")
        def set_tilt(self, speed):
            print(f"Tilt speed: {speed}")
        def set_zoom(self, level):
            self.zoom_level = level
            print(f"Zoom level: {level}")
    
    class MockVideoStreamer:
        def get_stream_url(self):
            return "/dev/video0"  # Use device path for testing
    
    print("Starting local stream viewer test")
    print("Controls:")
    print("  w/a/s/d - Pan/tilt")
    print("  +/- - Zoom in/out")
    print("  Space - Stop movement")
    print("  m - Switch camera mode")
    print("  q/ESC - Quit")
    
    # Test the local stream viewer
    camera_controller = MockCameraController()
    video_streamer = MockVideoStreamer()
    viewer = LocalStreamViewer(camera_controller, video_streamer)
    viewer.start()
    
    try:
        # Run until viewer thread ends
        while viewer.viewer_thread and viewer.viewer_thread.is_alive():
            time.sleep(0.1)
    except KeyboardInterrupt:
        print("Test interrupted")
    finally:
        viewer.stop()