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
        self.pan_speed = 0
        self.tilt_speed = 0
        self.zoom_level = 0
        self.running = False
        self.control_thread = None
        self._check_cameras()
        
    def _check_cameras(self):
        """Check if the camera devices exist"""
        rgb_exists = os.path.exists(self.rgb_device)
        ir_exists = os.path.exists(self.ir_device)
        
        if not rgb_exists:
            logger.warning(f"RGB camera device {self.rgb_device} not found")
        if not ir_exists:
            logger.warning(f"IR camera device {self.ir_device} not found")
            
        logger.info(f"Camera devices - RGB: {rgb_exists}, IR: {ir_exists}")
        
    def start(self):
        """Start the camera controller"""
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
        if not self.running:
            logger.warning("Camera controller is not running")
            return
            
        self.running = False
        if self.control_thread:
            self.control_thread.join(timeout=2.0)
        logger.info("Camera controller stopped")
        
    def set_pan(self, speed):
        """Set pan speed
        
        Args:
            speed (int): Speed value from -100 to 100, 0 is stopped
        """
        self.pan_speed = max(-100, min(100, speed))
        logger.debug(f"Pan speed set to {self.pan_speed}")
        
    def set_tilt(self, speed):
        """Set tilt speed
        
        Args:
            speed (int): Speed value from -100 to 100, 0 is stopped
        """
        self.tilt_speed = max(-100, min(100, speed))
        logger.debug(f"Tilt speed set to {self.tilt_speed}")
        
    def set_zoom(self, level):
        """Set zoom level
        
        Args:
            level (int): Zoom level from 0 to 100, 0 is wide angle
        """
        self.zoom_level = max(0, min(100, level))
        self._set_zoom(self.zoom_level)
        logger.debug(f"Zoom level set to {self.zoom_level}")
        
    def set_camera_mode(self, mode):
        """Set camera mode
        
        Args:
            mode (int or CameraMode): 0/RGB or 1/IR
        """
        if isinstance(mode, int):
            mode = CameraMode(mode)
            
        if mode == self.current_mode:
            logger.debug(f"Camera already in {mode.name} mode")
            return
            
        logger.info(f"Switching camera mode from {self.current_mode.name} to {mode.name}")
        self.current_mode = mode
        
        if mode == CameraMode.RGB:
            self._activate_rgb_camera()
        else:
            self._activate_ir_camera()
            
    def get_current_camera_device(self):
        """Get the current active camera device path"""
        return self.rgb_device if self.current_mode == CameraMode.RGB else self.ir_device
        
    def get_camera_mode(self):
        """Get the current camera mode"""
        return self.current_mode.value
        
    def process_command(self, command):
        """Process a command from the client
        
        Args:
            command (dict): Command dictionary with 'type' and 'value' keys
        """
        cmd_type = command.get('type', '').lower()
        value = command.get('value', 0)
        
        if cmd_type == 'pan':
            self.set_pan(value)
        elif cmd_type == 'tilt':
            self.set_tilt(value)
        elif cmd_type == 'zoom':
            self.set_zoom(value)
        elif cmd_type == 'mode':
            self.set_camera_mode(value)
        else:
            logger.warning(f"Unknown command type: {cmd_type}")
            
    def _control_loop(self):
        """Main control loop for camera movement"""
        logger.info("Control loop started")
        
        while self.running:
            # Send pan/tilt commands if there's any movement
            if self.pan_speed != 0 or self.tilt_speed != 0:
                self._move_camera(self.pan_speed, self.tilt_speed)
                
            time.sleep(0.05)  # 20Hz control rate
            
        logger.info("Control loop ended")
        
    def _move_camera(self, pan_speed, tilt_speed):
        """Send movement commands to the camera hardware"""
        # Convert -100 to 100 scale to hardware-specific values
        # This would call into pi_ptz.py functions to control movement
        # For simulation, just log the commands
        logger.debug(f"Move camera: pan={pan_speed}, tilt={tilt_speed}")
        
        # Actual hardware interface would go here
        # Example: call to external script or direct serial communication
        try:
            # Convert to 0-63 range for Pelco-D protocol
            pelco_pan_speed = abs(int(pan_speed * 0.63))
            pelco_tilt_speed = abs(int(tilt_speed * 0.63))
            
            # Set direction commands
            cmd_2 = 0
            if pan_speed < 0:
                cmd_2 |= 0x04  # Left
            elif pan_speed > 0:
                cmd_2 |= 0x02  # Right
                
            if tilt_speed < 0:
                cmd_2 |= 0x08  # Up
            elif tilt_speed > 0:
                cmd_2 |= 0x10  # Down
                
            # This would send Pelco-D command to the camera
            # Example: self._send_pelco_command(0x00, cmd_2, pelco_pan_speed, pelco_tilt_speed)
            
        except Exception as e:
            logger.error(f"Error moving camera: {e}")
        
    def _set_zoom(self, zoom_level):
        """Set the zoom level on the camera"""
        # Convert 0-100 scale to hardware-specific zoom command
        # This would call into pi_ptz.py functions to control zoom
        logger.debug(f"Set zoom level: {zoom_level}")
        
        # Example: If zoom level > 50, zoom in, else zoom out
        try:
            # This would send Pelco-D zoom command to the camera
            # Example: if zoom_level > self.current_zoom:
            #    self._send_pelco_command(0x00, 0x20)  # Zoom in
            # else:
            #    self._send_pelco_command(0x00, 0x40)  # Zoom out
            pass
        except Exception as e:
            logger.error(f"Error setting zoom: {e}")
            
    def _activate_rgb_camera(self):
        """Activate the RGB camera"""
        logger.info(f"Activating RGB camera: {self.rgb_device}")
        # Implementation would depend on hardware setup
        # This might involve switching video inputs or enabling specific camera
        
    def _activate_ir_camera(self):
        """Activate the IR/Thermal camera"""
        logger.info(f"Activating IR camera: {self.ir_device}")
        # Implementation would depend on hardware setup
        # This might involve switching video inputs or enabling specific camera

if __name__ == "__main__":
    # Set up logging for standalone testing
    logging.basicConfig(
        level=logging.DEBUG,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
    )
    
    # Test the camera controller
    controller = CameraController()
    controller.start()
    
    try:
        # Test pan/tilt
        controller.set_pan(50)  # Pan right at 50% speed
        time.sleep(2)
        controller.set_pan(0)   # Stop panning
        
        controller.set_tilt(-30)  # Tilt up at 30% speed
        time.sleep(2)
        controller.set_tilt(0)    # Stop tilting
        
        # Test zoom
        controller.set_zoom(75)  # Zoom to 75%
        time.sleep(2)
        
        # Test camera mode
        controller.set_camera_mode(CameraMode.IR)
        time.sleep(2)
        controller.set_camera_mode(CameraMode.RGB)
        
    except KeyboardInterrupt:
        print("Test interrupted")
    finally:
        controller.stop()