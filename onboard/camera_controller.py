#!/usr/bin/env python3
"""
Camera Controller for PTZ cameras.
This module handles the communication with the physical cameras and controls
pan, tilt, zoom, and camera mode (RGB/IR) operations.
"""

import os
import time
import logging
import threading
import subprocess
from enum import Enum

logger = logging.getLogger('camera_controller')

class CameraMode(Enum):
    """Enum for camera modes"""
    RGB = 0
    IR = 1

class CameraController:
    """Controls PTZ camera movements and mode switching"""
    
    def __init__(self, rgb_device='/dev/video0', ir_device='/dev/video1'):
        """Initialize the camera controller"""
        self.rgb_device = rgb_device
        self.ir_device = ir_device
        self.current_mode = CameraMode.RGB
        
        # Movement parameters
        self.pan_speed = 0  # -100 to 100, 0 is stopped
        self.tilt_speed = 0  # -100 to 100, 0 is stopped
        self.zoom_level = 0  # 0 to 100, 0 is wide angle
        
        # Control thread
        self.running = False
        self.control_thread = None
        
        # Lock for thread safety
        self.lock = threading.Lock()
        
        logger.info("Camera controller initialized")
        
        # Check if cameras are available
        self._check_cameras()
        
    def _check_cameras(self):
        """Check if the camera devices exist"""
        # Just log messages regardless of device existence for demo
        logger.info(f"RGB camera configured as: {self.rgb_device}")
        logger.info(f"IR camera configured as: {self.ir_device}")
        
        # For simulation purposes
        if not os.path.exists("/tmp/camera_sim"):
            os.makedirs("/tmp/camera_sim", exist_ok=True)
            with open(f"/tmp/camera_sim/rgb_camera.txt", "w") as f:
                f.write("RGB Camera Simulation File")
            with open(f"/tmp/camera_sim/ir_camera.txt", "w") as f:
                f.write("IR Camera Simulation File")
            
        # Override actual devices with simulated ones for demo
        self.rgb_device = "/tmp/camera_sim/rgb_camera.txt"
        self.ir_device = "/tmp/camera_sim/ir_camera.txt"
        
        logger.info("Using simulated camera devices for demo")
    
    def start(self):
        """Start the camera controller"""
        with self.lock:
            if self.running:
                logger.warning("Camera controller is already running")
                return
                
            self.running = True
            self.control_thread = threading.Thread(target=self._control_loop)
            self.control_thread.daemon = True
            self.control_thread.start()
            logger.info("Camera controller started")
    
    def stop(self):
        """Stop the camera controller"""
        with self.lock:
            if not self.running:
                logger.warning("Camera controller is not running")
                return
                
            self.running = False
            if self.control_thread:
                self.control_thread.join(timeout=2.0)
                self.control_thread = None
            logger.info("Camera controller stopped")
    
    def set_pan(self, speed):
        """Set pan speed
        
        Args:
            speed (int): Speed value from -100 to 100, 0 is stopped
        """
        with self.lock:
            # Clamp speed value
            speed = max(-100, min(100, speed))
            self.pan_speed = speed
            logger.debug(f"Pan speed set to {speed}")
    
    def set_tilt(self, speed):
        """Set tilt speed
        
        Args:
            speed (int): Speed value from -100 to 100, 0 is stopped
        """
        with self.lock:
            # Clamp speed value
            speed = max(-100, min(100, speed))
            self.tilt_speed = speed
            logger.debug(f"Tilt speed set to {speed}")
    
    def set_zoom(self, level):
        """Set zoom level
        
        Args:
            level (int): Zoom level from 0 to 100, 0 is wide angle
        """
        with self.lock:
            # Clamp zoom level
            level = max(0, min(100, level))
            self.zoom_level = level
            logger.debug(f"Zoom level set to {level}")
    
    def set_camera_mode(self, mode):
        """Set camera mode
        
        Args:
            mode (int or CameraMode): 0/RGB or 1/IR
        """
        with self.lock:
            if isinstance(mode, int):
                mode = CameraMode.RGB if mode == 0 else CameraMode.IR
                
            if mode != self.current_mode:
                self.current_mode = mode
                logger.info(f"Camera mode changed to {mode.name}")
                
                # Perform any needed hardware switching here
                if mode == CameraMode.RGB:
                    self._activate_rgb_camera()
                else:
                    self._activate_ir_camera()
    
    def get_current_camera_device(self):
        """Get the current active camera device path"""
        return self.rgb_device if self.current_mode == CameraMode.RGB else self.ir_device
    
    def get_camera_mode(self):
        """Get the current camera mode"""
        return self.current_mode
    
    def process_command(self, command):
        """Process a command from the client
        
        Args:
            command (dict): Command dictionary with 'type' and 'value' keys
        """
        if not isinstance(command, dict):
            logger.error(f"Invalid command format: {command}")
            return False
            
        if 'type' not in command or 'value' not in command:
            logger.error(f"Missing command type or value: {command}")
            return False
            
        cmd_type = command['type']
        cmd_value = command['value']
        
        logger.debug(f"Processing command: {cmd_type}={cmd_value}")
        
        if cmd_type == 'pan':
            self.set_pan(cmd_value)
        elif cmd_type == 'tilt':
            self.set_tilt(cmd_value)
        elif cmd_type == 'zoom':
            self.set_zoom(cmd_value)
        elif cmd_type == 'mode':
            self.set_camera_mode(cmd_value)
        else:
            logger.warning(f"Unknown command type: {cmd_type}")
            return False
            
        return True
    
    def _control_loop(self):
        """Main control loop for camera movement"""
        logger.info("Control loop started")
        
        try:
            while self.running:
                with self.lock:
                    # Here we would send the actual commands to the PTZ hardware
                    # This is a placeholder for the real implementation
                    if self.pan_speed != 0 or self.tilt_speed != 0:
                        self._move_camera(self.pan_speed, self.tilt_speed)
                        
                    if self.zoom_level > 0:
                        self._set_zoom(self.zoom_level)
                
                # Sleep to prevent CPU hogging
                time.sleep(0.05)
                
        except Exception as e:
            logger.error(f"Error in control loop: {e}")
            self.running = False
            
        logger.info("Control loop ended")
    
    def _move_camera(self, pan_speed, tilt_speed):
        """Send movement commands to the camera hardware"""
        # Simulation only - in a real implementation, this would control actual hardware
        logger.info(f"Camera movement: pan={pan_speed}, tilt={tilt_speed}")
        
        # Update a file to simulate camera movement
        try:
            camera_file = self.get_current_camera_device()
            camera_mode = "RGB" if self.current_mode == CameraMode.RGB else "IR/Thermal"
            
            with open(camera_file, "w") as f:
                f.write(f"{camera_mode} Camera Simulation\n")
                f.write(f"Pan Speed: {pan_speed}\n")
                f.write(f"Tilt Speed: {tilt_speed}\n")
                f.write(f"Zoom Level: {self.zoom_level}\n")
                f.write(f"Last Updated: {time.strftime('%Y-%m-%d %H:%M:%S')}\n")
        except Exception as e:
            logger.error(f"Failed to update camera simulation: {e}")
    
    def _set_zoom(self, zoom_level):
        """Set the zoom level on the camera"""
        # Simulation only
        logger.info(f"Setting zoom level: {zoom_level}")
        
        # The actual zoom update is handled in _move_camera for simulation purposes
        try:
            # Update a status file for debugging
            with open("/tmp/camera_sim/zoom_status.txt", "w") as f:
                f.write(f"Camera: {self.current_mode.name}\n")
                f.write(f"Zoom Level: {zoom_level}\n")
                f.write(f"Last Updated: {time.strftime('%Y-%m-%d %H:%M:%S')}\n")
        except Exception as e:
            logger.error(f"Failed to update zoom simulation: {e}")
    
    def _activate_rgb_camera(self):
        """Activate the RGB camera"""
        # This is a placeholder for switching to RGB mode
        logger.info("Activating RGB camera")
        
        # In a real implementation, this might involve switching inputs
        # on a video multiplexer or activating/deactivating devices
    
    def _activate_ir_camera(self):
        """Activate the IR/Thermal camera"""
        # This is a placeholder for switching to IR mode
        logger.info("Activating IR/Thermal camera")
        
        # In a real implementation, this might involve switching inputs
        # on a video multiplexer or activating/deactivating devices
