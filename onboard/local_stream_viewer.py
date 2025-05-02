#!/usr/bin/env python3
"""
Local Stream Viewer for PTZ Camera System.
This module provides a way to view camera streams on the onboard computer 
for debugging purposes when a monitor is connected.
"""

import os
import cv2
import time
import argparse
import logging
import threading
import socket
import json

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger('local_stream_viewer')

class LocalStreamViewer:
    """Displays camera streams on the local machine for debugging"""
    
    def __init__(self, camera_mode="rgb", stream_quality="main", port=8000):
        """Initialize the local stream viewer
        
        Args:
            camera_mode (str): "rgb" or "ir" camera mode
            stream_quality (str): "main" (high) or "sub" (low) quality
            port (int): Port for obtaining stream URLs from the server
        """
        self.camera_mode = camera_mode
        self.stream_quality = stream_quality
        self.port = port
        self.running = False
        self.viewer_thread = None
        self.stream_url = None
        
        logger.info(f"Local stream viewer initialized for {camera_mode} camera, {stream_quality} quality")
    
    def get_stream_urls(self):
        """Get stream URLs from the server"""
        try:
            # Try to get stream URLs via HTTP request to the server
            import requests
            response = requests.get(f"http://localhost:{self.port}/streams")
            if response.status_code == 200:
                return response.json()
            
            logger.warning(f"Failed to get stream URLs via HTTP: {response.status_code}")
            return None
        except Exception as e:
            logger.warning(f"Could not get stream URLs via HTTP: {e}")
            
            # Fallback to get stream URLs from files
            try:
                if os.path.exists("/tmp/camera_sim/stream_urls.txt"):
                    with open("/tmp/camera_sim/stream_urls.txt", "r") as f:
                        lines = f.readlines()
                        urls = {}
                        for line in lines:
                            if "RGB Stream URL:" in line:
                                urls["rgb"] = line.split("RGB Stream URL:")[1].strip()
                            elif "IR Stream URL:" in line:
                                urls["ir"] = line.split("IR Stream URL:")[1].strip()
                        
                        if urls:
                            return urls
            except Exception as inner_e:
                logger.warning(f"Could not get stream URLs from file: {inner_e}")
            
            # Return hardcoded fallback URLs as a last resort
            return {
                "rgb": "rtsp://demo.ptz-camera.com:8554/live/rgb-stream",
                "ir": "rtsp://demo.ptz-camera.com:8555/live/ir-stream"
            }
    
    def start(self):
        """Start the local stream viewer"""
        if self.running:
            logger.warning("Local stream viewer is already running")
            return
        
        self.running = True
        self.viewer_thread = threading.Thread(target=self._view_stream)
        self.viewer_thread.daemon = True
        self.viewer_thread.start()
        logger.info("Local stream viewer started")
    
    def stop(self):
        """Stop the local stream viewer"""
        if not self.running:
            logger.warning("Local stream viewer is not running")
            return
        
        self.running = False
        if self.viewer_thread:
            self.viewer_thread.join(timeout=2.0)
            self.viewer_thread = None
        logger.info("Local stream viewer stopped")
    
    def _view_stream(self):
        """View the stream in a window on the local machine"""
        logger.info("Starting stream viewing...")
        
        # Get stream URLs
        urls = self.get_stream_urls()
        if not urls:
            logger.error("Failed to get stream URLs")
            self.running = False
            return
        
        # Select URL based on camera mode
        if self.camera_mode not in urls:
            logger.warning(f"Unknown camera mode: {self.camera_mode}, using rgb")
            self.camera_mode = "rgb"
        
        stream_url = urls[self.camera_mode]
        logger.info(f"Using stream URL: {stream_url}")
        
        # Open the stream
        window_name = f"PTZ Camera - {self.camera_mode.upper()} Stream ({self.stream_quality})"
        cap = None
        
        try:
            # Try to open the actual stream
            cap = cv2.VideoCapture(stream_url)
            
            # Simple simulation for demo purposes if no real stream is available
            if not cap.isOpened():
                logger.warning("Could not open real stream, using simulation")
                self._simulate_stream(window_name)
                return
            
            # Main streaming loop
            frames_count = 0
            start_time = time.time()
            
            while self.running:
                ret, frame = cap.read()
                
                if not ret:
                    logger.warning("Failed to receive frame, trying to reconnect...")
                    cap.release()
                    time.sleep(1)
                    cap = cv2.VideoCapture(stream_url)
                    continue
                
                # Calculate FPS
                frames_count += 1
                elapsed_time = time.time() - start_time
                if elapsed_time >= 5:  # Update FPS every 5 seconds
                    fps = frames_count / elapsed_time
                    logger.info(f"Stream FPS: {fps:.2f}")
                    frames_count = 0
                    start_time = time.time()
                
                # Add info overlay
                height, width = frame.shape[:2]
                info_text = f"{self.camera_mode.upper()} - {self.stream_quality.upper()}"
                cv2.putText(frame, info_text, (10, 30), 
                           cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 255, 0), 2)
                
                # Display the frame
                cv2.imshow(window_name, frame)
                
                if cv2.waitKey(1) & 0xFF == ord('q'):
                    logger.info("User pressed 'q', stopping stream")
                    break
        
        except KeyboardInterrupt:
            logger.info("Stream viewing interrupted by user")
        except Exception as e:
            logger.error(f"Error viewing stream: {e}")
        finally:
            if cap:
                cap.release()
            cv2.destroyAllWindows()
            self.running = False
            logger.info("Stream viewing ended")
    
    def _simulate_stream(self, window_name):
        """Simulate a stream for demonstration purposes"""
        logger.info("Starting simulated stream for demonstration")
        
        # Import numpy here to avoid issues if it's not available
        try:
            import numpy as np
        except ImportError:
            logger.error("NumPy is required for simulation but not available")
            return
            
        # Create a black canvas
        height, width = 480, 640
        
        # Define colors based on camera mode
        if self.camera_mode == "ir":
            # Use a grayscale animation for IR mode
            is_grayscale = True
            overlay_color = (255, 255, 255)  # White text for IR mode
            base_intensity = 128  # Mid-gray base for IR
        else:
            # Use a colored animation for RGB mode
            is_grayscale = False
            overlay_color = (0, 255, 0)  # Green text for RGB mode
            # BGR format for OpenCV
            base_color_b = 128  # Blue component
            base_color_g = 0    # Green component
            base_color_r = 0    # Red component
        
        start_time = time.time()
        frame_count = 0
        
        try:
            while self.running:
                # Calculate pulsing effect
                pulse_factor = 1 + time.time() % 2  # Value between 1-3
                
                # Create the base frame
                if is_grayscale:
                    # Grayscale simulation for IR
                    intensity = int(base_intensity + 50 * pulse_factor)  # Pulsing effect
                    # Ensure we don't exceed valid pixel values
                    intensity = min(255, intensity)
                    frame = np.ones((height, width), dtype=np.uint8) * intensity
                else:
                    # Color simulation for RGB
                    r_val = int(base_color_r + 50 * pulse_factor)
                    g_val = base_color_g
                    b_val = base_color_b
                    # Ensure we don't exceed valid pixel values
                    r_val = min(255, r_val)
                    
                    frame = np.zeros((height, width, 3), dtype=np.uint8)
                    frame[:, :, 0] = b_val  # Blue channel
                    frame[:, :, 1] = g_val  # Green channel
                    frame[:, :, 2] = r_val  # Red channel
                
                # Add timestamp
                timestamp = time.strftime("%Y-%m-%d %H:%M:%S")
                cv2.putText(frame, timestamp, (width - 200, height - 20), 
                           cv2.FONT_HERSHEY_SIMPLEX, 0.5, overlay_color, 1)
                
                # Add stream info
                info_text = f"SIMULATED {self.camera_mode.upper()} - {self.stream_quality.upper()}"
                cv2.putText(frame, info_text, (10, 30), 
                           cv2.FONT_HERSHEY_SIMPLEX, 0.8, overlay_color, 2)
                
                # Add artificial center target
                cv2.circle(frame, (width // 2, height // 2), 20, overlay_color, 1)
                cv2.line(frame, (width // 2 - 30, height // 2), (width // 2 + 30, height // 2), overlay_color, 1)
                cv2.line(frame, (width // 2, height // 2 - 30), (width // 2, height // 2 + 30), overlay_color, 1)
                
                # Display the frame
                cv2.imshow(window_name, frame)
                
                # Calculate FPS
                frame_count += 1
                elapsed_time = time.time() - start_time
                if elapsed_time >= 5:  # Update FPS every 5 seconds
                    fps = frame_count / elapsed_time
                    logger.info(f"Simulation FPS: {fps:.2f}")
                    frame_count = 0
                    start_time = time.time()
                
                # Check for exit key
                if cv2.waitKey(33) & 0xFF == ord('q'):
                    logger.info("User pressed 'q', stopping simulation")
                    break
                
                # Limit simulation to ~30 FPS
                time.sleep(0.033)
                
        except KeyboardInterrupt:
            logger.info("Simulation interrupted by user")
        except Exception as e:
            logger.error(f"Error in simulation: {e}")
        finally:
            cv2.destroyAllWindows()
            self.running = False
            logger.info("Simulation ended")

def parse_arguments():
    """Parse command line arguments"""
    parser = argparse.ArgumentParser(description='Local Stream Viewer for PTZ Camera')
    
    parser.add_argument('--mode', type=str, default='rgb',
                        choices=['rgb', 'ir'],
                        help='Camera mode: "rgb" or "ir/thermal"')
    
    parser.add_argument('--quality', type=str, default='main',
                        choices=['main', 'sub'],
                        help='Stream quality: "main" (high) or "sub" (low)')
    
    parser.add_argument('--port', type=int, default=8000,
                        help='Port for obtaining stream URLs from the server')
    
    return parser.parse_args()

def main():
    """Main function"""
    args = parse_arguments()
    
    try:
        # Import NumPy here to avoid issues if it's not available
        global np
        import numpy as np
        
        viewer = LocalStreamViewer(
            camera_mode=args.mode,
            stream_quality=args.quality,
            port=args.port
        )
        viewer.start()
        
        # Keep the main thread alive
        try:
            while viewer.running:
                time.sleep(1)
        except KeyboardInterrupt:
            logger.info("Interrupted by user")
        finally:
            viewer.stop()
            
    except ImportError as e:
        logger.error(f"Required package not found: {e}")
        logger.error("Please install missing packages: pip install numpy opencv-python")
    except Exception as e:
        logger.error(f"Error in main: {e}")

if __name__ == "__main__":
    main()