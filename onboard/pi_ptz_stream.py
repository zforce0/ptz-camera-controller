#!/usr/bin/env python3
"""
Raspberry Pi PTZ Camera Control & Streaming Script
for controlling a PTZ camera using the Pelco-D protocol via CH341 USB-to-RS485 adapter
while simultaneously streaming and recording video.
"""

import serial
import time
import argparse
import sys
import os
import curses
import cv2
import threading
import datetime
import queue
import numpy as np
from pathlib import Path

class PTZController:
    """Pelco-D PTZ camera controller using RS485 via CH341 USB adapter"""

    def __init__(self, port='/dev/ttyUSB0', baudrate=9600, address=0x01):
        """
        Initialize the PTZ controller

        Args:
            port (str): Serial port path for the CH341 adapter
            baudrate (int): Communication baudrate (2400, 4800, 9600, or 38400)
            address (int): Camera address (1-255)
        """
        self.port = port
        self.baudrate = baudrate
        self.address = address
        self.ser = None
        self.connect()

    def connect(self):
        """Establish a serial connection to the PTZ camera"""
        try:
            self.ser = serial.Serial(
                port=self.port,
                baudrate=self.baudrate,
                bytesize=serial.EIGHTBITS,
                parity=serial.PARITY_NONE,
                stopbits=serial.STOPBITS_ONE,
                timeout=1
            )
            print(f"Connected to {self.port} at {self.baudrate} baud")
        except serial.SerialException as e:
            print(f"Error connecting to {self.port}: {e}")
            raise

    def _calculate_checksum(self, data):
        """Calculate Pelco-D checksum (sum of bytes mod 256)"""
        return sum(data) % 256

    def _send_pelco_command(self, command_1, command_2, data_1=0, data_2=0):
        """
        Send Pelco-D formatted command

        Args:
            command_1 (int): First command byte
            command_2 (int): Second command byte
            data_1 (int): Data byte 1 (pan speed, 0-63)
            data_2 (int): Data byte 2 (tilt speed, 0-63)
        """
        message = [
            0xFF,       # Start byte
            self.address,
            command_1,
            command_2,
            data_1,     # Pan speed (00-3F)
            data_2      # Tilt speed (00-3F)
        ]
        checksum = self._calculate_checksum(message[1:])  # Exclude start byte
        message.append(checksum)

        try:
            self.ser.write(bytes(message))
            time.sleep(0.1)  # Adjust based on device response
        except serial.SerialException as e:
            print(f"Communication error: {e}")

    # Basic movement commands
    def stop(self):
        """Stop all movement"""
        self._send_pelco_command(0x00, 0x00)
        return "Stopped movement"

    def pan_left(self, speed=0x20):
        """Pan left at specified speed (00-3F)"""
        self._send_pelco_command(0x00, 0x04, speed, 0x00)
        return f"Panning left at speed: {speed}"

    def pan_right(self, speed=0x20):
        """Pan right at specified speed (00-3F)"""
        self._send_pelco_command(0x00, 0x02, speed, 0x00)
        return f"Panning right at speed: {speed}"

    def tilt_up(self, speed=0x20):
        """Tilt up at specified speed (00-3F)"""
        self._send_pelco_command(0x00, 0x08, 0x00, speed)
        return f"Tilting up at speed: {speed}"

    def tilt_down(self, speed=0x20):
        """Tilt down at specified speed (00-3F)"""
        self._send_pelco_command(0x00, 0x10, 0x00, speed)
        return f"Tilting down at speed: {speed}"

    def move_diagonal(self, pan_direction, tilt_direction, pan_speed=0x20, tilt_speed=0x20):
        """Move diagonally by combining pan and tilt commands"""
        command_byte = 0x00

        # Set pan direction bits
        if pan_direction == "left":
            command_byte |= 0x04
        elif pan_direction == "right":
            command_byte |= 0x02

        # Set tilt direction bits
        if tilt_direction == "up":
            command_byte |= 0x08
        elif tilt_direction == "down":
            command_byte |= 0x10

        self._send_pelco_command(0x00, command_byte, pan_speed, tilt_speed)
        return f"Moving diagonally: {pan_direction}/{tilt_direction} at speed: {pan_speed}/{tilt_speed}"

    # Zoom control
    def zoom_in(self):
        """Zoom in"""
        self._send_pelco_command(0x00, 0x20)
        return "Zooming in"

    def zoom_out(self):
        """Zoom out"""
        self._send_pelco_command(0x00, 0x40)
        return "Zooming out"

    # Preset position control
    def goto_preset(self, preset_num):
        """Go to preset position (1-255)"""
        if 1 <= preset_num <= 255:
            self._send_pelco_command(0x00, 0x07, preset_num)
            return f"Moving to preset position: {preset_num}"
        return "Invalid preset number"

    def set_preset(self, preset_num):
        """Set preset position (1-255)"""
        if 1 <= preset_num <= 255:
            self._send_pelco_command(0x00, 0x03, preset_num)
            return f"Setting preset position: {preset_num}"
        return "Invalid preset number"

    # Special functions from the camera manual
    def set_scan_left_limit(self):
        """Set left limit for scan (preset 50)"""
        self.set_preset(50)
        return "Setting scan left limit (start point)"

    def set_scan_right_limit(self):
        """Set right limit for scan (preset 51)"""
        self.set_preset(51)
        return "Setting scan right limit (end point)"

    def start_scan(self):
        """Start horizontal scanning between limits"""
        self.goto_preset(51)
        return "Starting horizontal scan"

    def stop_scan(self):
        """Stop horizontal scanning"""
        self.goto_preset(50)
        return "Stopping horizontal scan"

    def toggle_backlight_compensation(self, enable=True):
        """Toggle backlight compensation (preset 55)"""
        if enable:
            self.goto_preset(55)
            return "Enabling backlight compensation"
        else:
            self.set_preset(55)
            return "Disabling backlight compensation"

    def toggle_digital_zoom(self, enable=True):
        """Toggle digital zoom (preset 58)"""
        if enable:
            self.goto_preset(58)
            return "Enabling digital zoom"
        else:
            self.set_preset(58)
            return "Disabling digital zoom"

    def toggle_auto_focus(self, auto=True):
        """Toggle between auto and manual focus (preset 59)"""
        if auto:
            self.goto_preset(59)
            return "Enabling auto focus"
        else:
            self.set_preset(59)
            return "Switching to manual focus"

    def toggle_auto_iris(self, auto=True):
        """Toggle between auto and manual iris (preset 60)"""
        if auto:
            self.goto_preset(60)
            return "Enabling auto iris"
        else:
            self.set_preset(60)
            return "Switching to manual iris"

    def toggle_day_night_mode(self, day_mode=True):
        """Toggle between day (color) and night (B&W) modes (preset 67)"""
        if day_mode:
            self.goto_preset(67)
            return "Switching to day mode (color)"
        else:
            self.set_preset(67)
            return "Switching to night mode (B&W)"

    def reboot_camera(self):
        """Reboot the camera (preset 54)"""
        self.goto_preset(54)
        return "Rebooting camera"

    def run_self_test(self):
        """Run camera self-test (preset 81)"""
        self.goto_preset(81)
        return "Running camera self-test"

    def toggle_auto_return(self, enable=True):
        """Enable/disable auto return to home position after inactivity"""
        if enable:
            self.goto_preset(90)
            return "Enabling auto return to home after inactivity"
        else:
            self.set_preset(90)
            return "Disabling auto return"

    def toggle_long_focus_speed_limit(self, enable=True):
        """Enable/disable long focus speed limiting"""
        if enable:
            self.goto_preset(210)
            return "Enabling long focus speed limit"
        else:
            self.goto_preset(211)
            return "Disabling long focus speed limit"

    def toggle_power_memory(self, enable=True):
        """Enable/disable power memory (position after power cycle)"""
        if enable:
            self.goto_preset(221)
            return "Enabling power cycle memory"
        else:
            self.goto_preset(220)
            return "Disabling power cycle memory"

    def close(self):
        """Close the serial connection"""
        if self.ser and self.ser.is_open:
            self.ser.close()
            return "Connection closed"


class CameraStream:
    """Class to handle camera streaming and recording"""

    def __init__(self, ip="192.168.1.108", username="admin", password="abcd1234", port=554):
        """
        Initialize the camera stream

        Args:
            ip (str): Camera IP address
            username (str): Camera login username
            password (str): Camera login password
            port (int): RTSP port
        """
        self.ip = ip
        self.username = username
        self.password = password
        self.port = port

        # RTSP URL templates
        self.rtsp_templates = {
            "main": f"rtsp://{username}:{password}@{ip}:{port}/h264/ch1/main/av_stream",
            "sub": f"rtsp://{username}:{password}@{ip}:{port}/h264/ch1/sub/av_stream"
        }

        self.stream_url = None
        self.cap = None
        self.recording = False
        self.paused = False
        self.displaying = False
        self.stream_thread = None
        self.recording_thread = None
        self.out = None
        self.frame_queue = queue.Queue(maxsize=30)  # Buffer for frames
        self.current_frame = None
        self.resolution = None
        self.fps = 0
        self.stream_type = "main"
        self.recordings_dir = Path("./recordings")
        self.recordings_dir.mkdir(exist_ok=True)
        self.playback_file = None
        self.playback_cap = None

    def connect(self, stream_type="main"):
        """Connect to the camera stream"""
        self.stream_type = stream_type
        self.stream_url = self.rtsp_templates[stream_type]

        # Release previous capture if any
        if self.cap and self.cap.isOpened():
            self.cap.release()

        # Connect to camera
        self.cap = cv2.VideoCapture(self.stream_url)

        if not self.cap.isOpened():
            return f"Error connecting to {self.stream_url}"

        # Get stream properties
        self.resolution = (
            int(self.cap.get(cv2.CAP_PROP_FRAME_WIDTH)),
            int(self.cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
        )
        self.fps = self.cap.get(cv2.CAP_PROP_FPS)

        return f"Connected to {self.stream_type} stream: {self.resolution[0]}x{self.resolution[1]} at {self.fps} FPS"

    def switch_stream(self, stream_type):
        """Switch between main and sub streams"""
        if stream_type not in self.rtsp_templates:
            return f"Invalid stream type: {stream_type}"

        if stream_type == self.stream_type:
            return f"Already using {stream_type} stream"

        # Stop current operations
        self.stop_stream()

        # Connect to new stream
        result = self.connect(stream_type)

        # Resume operations if needed
        if self.displaying:
            self.start_stream()

        return result

    def start_stream(self):
        """Start streaming thread"""
        if self.stream_thread and self.stream_thread.is_alive():
            return "Stream already running"

        self.displaying = True
        self.paused = False
        self.stream_thread = threading.Thread(target=self._stream_worker, daemon=True)
        self.stream_thread.start()
        return f"Started {self.stream_type} stream"

    def stop_stream(self):
        """Stop streaming"""
        self.displaying = False
        if self.stream_thread:
            self.stream_thread.join(timeout=1.0)
        cv2.destroyAllWindows()
        return "Stream stopped"

    def pause_stream(self):
        """Pause/unpause streaming"""
        self.paused = not self.paused
        return "Stream paused" if self.paused else "Stream resumed"

    def start_recording(self):
        """Start video recording"""
        if self.recording:
            return "Already recording"

        if not self.cap or not self.cap.isOpened():
            return "Cannot record: No active stream"

        # Create a filename with timestamp
        timestamp = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
        filename = self.recordings_dir / f"recording_{timestamp}_{self.stream_type}.mp4"

        # Create video writer
        fourcc = cv2.VideoWriter_fourcc(*'mp4v')
        self.out = cv2.VideoWriter(
            str(filename),
            fourcc,
            self.fps,
            self.resolution
        )

        self.recording = True

        # Start recording thread if not streaming
        if not self.displaying:
            self.recording_thread = threading.Thread(target=self._recording_worker, daemon=True)
            self.recording_thread.start()

        return f"Started recording to {filename}"

    def stop_recording(self):
        """Stop video recording"""
        if not self.recording:
            return "Not recording"

        self.recording = False

        if self.recording_thread:
            self.recording_thread.join(timeout=1.0)

        if self.out:
            self.out.release()
            self.out = None

        return "Recording stopped"

    def list_recordings(self):
        """List all available recordings"""
        recordings = list(self.recordings_dir.glob("*.mp4"))
        if not recordings:
            return "No recordings found"

        recordings_list = "\n".join([f"{i+1}. {rec.name}" for i, rec in enumerate(recordings)])
        return f"Available recordings:\n{recordings_list}"

    def start_playback(self, filename):
        """Start playback of a recorded video"""
        # Stop current operations
        self.stop_stream()
        self.stop_recording()

        # Check if file exists
        playback_path = self.recordings_dir / filename
        if not playback_path.exists():
            # Try with raw filename
            recordings = list(self.recordings_dir.glob("*.mp4"))
            matches = [rec for rec in recordings if filename in rec.name]

            if not matches:
                return f"Recording '{filename}' not found"

            playback_path = matches[0]

        self.playback_file = str(playback_path)
        self.playback_cap = cv2.VideoCapture(self.playback_file)

        if not self.playback_cap.isOpened():
            return f"Error opening recording {playback_path}"

        # Start playback thread
        self.playback_thread = threading.Thread(target=self._playback_worker, daemon=True)
        self.playback_thread.start()

        return f"Playing {playback_path.name}"

    def stop_playback(self):
        """Stop playback"""
        if not self.playback_cap:
            return "No active playback"

        if self.playback_cap:
            self.playback_cap.release()
            self.playback_cap = None
            self.playback_file = None

        cv2.destroyAllWindows()
        return "Playback stopped"

    def take_snapshot(self):
        """Take a snapshot of the current frame"""
        if not self.current_frame is not None:
            return "No frame available for snapshot"

        # Create a filename with timestamp
        timestamp = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
        filename = self.recordings_dir / f"snapshot_{timestamp}.jpg"

        # Save snapshot
        cv2.imwrite(str(filename), self.current_frame)
        return f"Snapshot saved to {filename}"

    def _stream_worker(self):
        """Worker thread for streaming"""
        try:
            while self.displaying:
                if not self.cap or not self.cap.isOpened():
                    time.sleep(0.5)
                    continue

                if not self.paused:
                    ret, frame = self.cap.read()

                    if not ret:
                        print("Failed to receive frame. Reconnecting...")
                        self.cap.release()
                        time.sleep(1)
                        self.cap = cv2.VideoCapture(self.stream_url)
                        continue

                    # Store current frame
                    self.current_frame = frame

                    # Put frame in queue for recording thread
                    if self.recording and not self.frame_queue.full():
                        self.frame_queue.put(frame, block=False)

                    # Add status text to display frame
                    display_frame = frame.copy()
                    status_text = f"{self.stream_type.upper()} {self.resolution[0]}x{self.resolution[1]}"
                    if self.recording:
                        status_text += " | RECORDING"
                        # Add red circle indicator
                        cv2.circle(display_frame, (20, 20), 10, (0, 0, 255), -1)

                    cv2.putText(display_frame, status_text, (10, 30),
                                cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 0), 2)

                    # Display frame
                    cv2.imshow('Camera Stream', display_frame)

                # Process key events (separate from curses keyboard input)
                key = cv2.waitKey(1) & 0xFF
                if key == 27:  # ESC key - emergency exit
                    self.displaying = False
                    break

        except Exception as e:
            print(f"Streaming error: {e}")
        finally:
            cv2.destroyAllWindows()

    def _recording_worker(self):
        """Worker thread for recording without display"""
        try:
            while self.recording:
                if not self.cap or not self.cap.isOpened():
                    time.sleep(0.5)
                    continue

                ret, frame = self.cap.read()

                if not ret:
                    print("Failed to receive frame for recording. Reconnecting...")
                    self.cap.release()
                    time.sleep(1)
                    self.cap = cv2.VideoCapture(self.stream_url)
                    continue

                # Store current frame
                self.current_frame = frame

                # Write frame to video file
                if self.out:
                    self.out.write(frame)

        except Exception as e:
            print(f"Recording error: {e}")

    def _playback_worker(self):
        """Worker thread for playback"""
        try:
            # Get video properties
            fps = self.playback_cap.get(cv2.CAP_PROP_FPS)
            frame_time = 1000 / fps if fps > 0 else 30

            while self.playback_cap and self.playback_cap.isOpened():
                ret, frame = self.playback_cap.read()

                if not ret:
                    # End of video
                    break

                # Add playback status text
                cv2.putText(frame, "PLAYBACK", (10, 30),
                            cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 0), 2)

                # Display frame
                cv2.imshow('Playback', frame)

                # Control playback speed
                key = cv2.waitKey(int(frame_time)) & 0xFF
                if key == 27:  # ESC key - stop playback
                    break

        except Exception as e:
            print(f"Playback error: {e}")
        finally:
            if self.playback_cap:
                self.playback_cap.release()
            cv2.destroyAllWindows()

    def close(self):
        """Close all connections and resources"""
        self.stop_playback()
        self.stop_recording()
        self.stop_stream()

        if self.cap and self.cap.isOpened():
            self.cap.release()

        return "Camera stream closed"


class PTZCameraSystem:
    """Combined PTZ control and camera streaming system"""

    def __init__(self, ptz_port='/dev/ttyUSB0', ptz_baudrate=9600, ptz_address=1,
                 camera_ip="192.168.1.108", camera_username="admin",
                 camera_password="abcd1234", camera_port=554):
        """Initialize the combined system"""
        self.ptz = PTZController(port=ptz_port, baudrate=ptz_baudrate, address=ptz_address)
        self.camera = CameraStream(
            ip=camera_ip,
            username=camera_username,
            password=camera_password,
            port=camera_port
        )
        self.status_message = "System initialized"
        self.running = False

    def start(self):
        """Start the system"""
        self.running = True
        # Connect to camera stream
        self.status_message = self.camera.connect()
        # Start streaming
        self.camera.start_stream()

    def stop(self):
        """Stop the system"""
        self.running = False
        self.ptz.stop()
        self.camera.close()
        self.ptz.close()

    def interactive_control(self):
        """
        Interactive control mode using keyboard
        """
        # Initialize curses
        screen = curses.initscr()
        curses.noecho()
        curses.cbreak()
        screen.keypad(True)

        try:
            screen.clear()
            screen.addstr(0, 0, "PTZ Camera Control & Streaming System")
            screen.addstr(2, 0, "Movement Controls:")
            screen.addstr(3, 0, "  Arrow keys: Pan/Tilt")
            screen.addstr(4, 0, "  Home/End/PgUp/PgDn: Diagonal movements")
            screen.addstr(5, 0, "  Space: Stop movement")
            screen.addstr(6, 0, "  +/-: Zoom in/out")
            screen.addstr(7, 0, "  1/2/3: Speed (Slow/Medium/Fast)")
            screen.addstr(9, 0, "Camera Controls:")
            screen.addstr(10, 0, "  p: Set preset position")
            screen.addstr(11, 0, "  g: Go to preset position")
            screen.addstr(12, 0, "  s: Control scan function")
            screen.addstr(13, 0, "  d/n: Toggle day/night mode")
            screen.addstr(14, 0, "  a: Toggle auto focus")
            screen.addstr(15, 0, "  b: Toggle backlight compensation")
            screen.addstr(17, 0, "Stream Controls:")
            screen.addstr(18, 0, "  m: Switch stream (main/sub)")
            screen.addstr(19, 0, "  r: Start/stop recording")
            screen.addstr(20, 0, "  c: Take snapshot")
            screen.addstr(21, 0, "  v: View recordings")
            screen.addstr(22, 0, "  z: Pause/resume stream")
            screen.addstr(24, 0, "  q: Quit")

            screen.refresh()

            scanning = False
            speed = 0x20  # Default speed (medium)

            # Start camera stream
            self.start()

            while self.running:
                # Display status message
                screen.addstr(26, 0, f"Status: {self.status_message[:60]}")
                screen.clrtoeol()
                screen.refresh()

                # Get keyboard input with timeout
                screen.timeout(100)  # 100ms timeout
                key = screen.getch()

                if key == -1:  # No key pressed
                    continue

                if key == ord('q'):
                    self.running = False
                    break

                # Movement controls
                elif key == curses.KEY_UP:
                    self.status_message = self.ptz.tilt_up(speed)
                elif key == curses.KEY_DOWN:
                    self.status_message = self.ptz.tilt_down(speed)
                elif key == curses.KEY_LEFT:
                    self.status_message = self.ptz.pan_left(speed)
                elif key == curses.KEY_RIGHT:
                    self.status_message = self.ptz.pan_right(speed)
                elif key == curses.KEY_HOME:
                    self.status_message = self.ptz.move_diagonal("left", "up", speed, speed)
                elif key == curses.KEY_END:
                    self.status_message = self.ptz.move_diagonal("left", "down", speed, speed)
                elif key == curses.KEY_PPAGE:  # Page Up
                    self.status_message = self.ptz.move_diagonal("right", "up", speed, speed)
                elif key == curses.KEY_NPAGE:  # Page Down
                    self.status_message = self.ptz.move_diagonal("right", "down", speed, speed)
                elif key == ord(' '):
                    self.status_message = self.ptz.stop()

                # Zoom controls
                elif key == ord('+'):
                    self.status_message = self.ptz.zoom_in()
                elif key == ord('-'):
                    self.status_message = self.ptz.zoom_out()

                # Speed adjustment
                elif key == ord('1'):
                    speed = 0x10  # Slow
                    screen.addstr(27, 0, "Speed: Slow    ")
                    screen.refresh()
                    self.status_message = "Speed set to Slow"
                elif key == ord('2'):
                    speed = 0x20  # Medium
                    screen.addstr(27, 0, "Speed: Medium  ")
                    screen.refresh()
                    self.status_message = "Speed set to Medium"
                elif key == ord('3'):
                    speed = 0x30  # Fast
                    screen.addstr(27, 0, "Speed: Fast    ")
                    screen.refresh()
                    self.status_message = "Speed set to Fast"

                # Preset position controls
                elif key == ord('p'):
                    screen.addstr(28, 0, "Enter preset number (1-255): ")
                    screen.clrtoeol()
                    curses.echo()
                    preset_str = screen.getstr(28, 30, 3).decode('utf-8')
                    curses.noecho()
                    try:
                        preset_num = int(preset_str)
                        if 1 <= preset_num <= 255:
                            self.status_message = self.ptz.set_preset(preset_num)
                        else:
                            self.status_message = "Invalid preset number! Must be 1-255"
                    except ValueError:
                        self.status_message = "Invalid input! Enter a number."
                    screen.refresh()

                elif key == ord('g'):
                    screen.addstr(28, 0, "Enter preset number (1-255): ")
                    screen.clrtoeol()
                    curses.echo()
                    preset_str = screen.getstr(28, 30, 3).decode('utf-8')
                    curses.noecho()
                    try:
                        preset_num = int(preset_str)
                        if 1 <= preset_num <= 255:
                            self.status_message = self.ptz.goto_preset(preset_num)
                        else:
                            self.status_message = "Invalid preset number! Must be 1-255"
                    except ValueError:
                        self.status_message = "Invalid input! Enter a number."
                    screen.refresh()

                # Scan controls
                elif key == ord('s'):
                    screen.addstr(28, 0, "Scan: L (left/start), R (right/end), S (start scan), X (stop scan): ")
                    screen.clrtoeol()
                    scan_key = screen.getch()
                    if scan_key == ord('l') or scan_key == ord('L'):
                        self.status_message = self.ptz.set_scan_left_limit()
                    elif scan_key == ord('r') or scan_key == ord('R'):
                        self.status_message = self.ptz.set_scan_right_limit()
                    elif scan_key == ord('s') or scan_key == ord('S'):
                        self.status_message = self.ptz.start_scan()
                        scanning = True
                    elif scan_key == ord('x') or scan_key == ord('X'):
                        self.status_message = self.ptz.stop_scan()
                        scanning = False
                    screen.refresh()

                # Day/night mode
                elif key == ord('d'):
                    self.status_message = self.ptz.toggle_day_night_mode(True)
                elif key == ord('n'):
                    self.status_message = self.ptz.toggle_day_night_mode(False)

                # Auto focus
                elif key == ord('a'):
                    screen.addstr(28, 0, "Auto focus: 1 (on) or 0 (off): ")
                    screen.clrtoeol()
                    af_key = screen.getch()
                    if af_key == ord('1'):
                        self.status_message = self.ptz.toggle_auto_focus(True)
                    elif af_key == ord('0'):
                        self.status_message = self.ptz.toggle_auto_focus(False)
                    screen.refresh()

                # Backlight compensation
                elif key == ord('b'):
                    screen.addstr(28, 0, "Backlight compensation: 1 (on) or 0 (off): ")
                    screen.clrtoeol()
                    blc_key = screen.getch()
                    if blc_key == ord('1'):
                        self.status_message = self.ptz.toggle_backlight_compensation(True)
                    elif blc_key == ord('0'):
                        self.status_message = self.ptz.toggle_backlight_compensation(False)
                    screen.refresh()

                # Stream quality/resolution switching
                elif key == ord('m'):
                    new_stream_type = "sub" if self.camera.stream_type == "main" else "main"
                    self.status_message = self.camera.switch_stream(new_stream_type)

                # Recording controls
                elif key == ord('r'):
                    if not self.camera.recording:
                        self.status_message = self.camera.start_recording()
                    else:
                        self.status_message = self.camera.stop_recording()

                # Snapshot
                elif key == ord('c'):
                    self.status_message = self.camera.take_snapshot()

                # Pause/resume stream
                elif key == ord('z'):
                    self.status_message = self.camera.pause_stream()

                # View recordings
                elif key == ord('v'):
                    # List recordings
                    recordings_list = self.camera.list_recordings()

                    # Clear area for list
                    for i in range(28, 35):
                        screen.addstr(i, 0, " " * 70)

                    # Display list with selection options
                    lines = recordings_list.split('\n')
                    for i, line in enumerate(lines):
                        screen.addstr(28 + i, 0, line)

                    # Prompt for playback
                    if "No recordings" not in recordings_list:
                        screen.addstr(28 + len(lines), 0, "Enter number to play or 'x' to cancel: ")
                        screen.clrtoeol()
                        curses.echo()
                        choice = screen.getstr(28 + len(lines), 40, 5).decode('utf-8')
                        curses.noecho()

                        if choice.lower() != 'x':
                            try:
                                # Check if input is a number for selection
                                if choice.isdigit():
                                    idx = int(choice) - 1
                                    if 0 <= idx < len(lines) - 1:  # -1 for the header line
                                        # Extract filename from the list
                                        filename = lines[idx + 1].split('. ')[1]
                                        self.status_message = self.camera.start_playback(filename)
                                    else:
                                        self.status_message = "Invalid selection"
                                else:
                                    # Treat as direct filename
                                    self.status_message = self.camera.start_playback(choice)
                            except Exception as e:
                                self.status_message = f"Playback error: {e}"

                    screen.clear()
                    screen.addstr(0, 0, "PTZ Camera Control & Streaming System")
                    screen.addstr(2, 0, "Movement Controls:")
                    screen.addstr(3, 0, "  Arrow keys: Pan/Tilt")
                    screen.addstr(4, 0, "  Home/End/PgUp/PgDn: Diagonal movements")
                    screen.addstr(5, 0, "  Space: Stop movement")
                    screen.addstr(6, 0, "  +/-: Zoom in/out")
                    screen.addstr(7, 0, "  1/2/3: Speed (Slow/Medium/Fast)")
                    screen.addstr(9, 0, "Camera Controls:")
                    screen.addstr(10, 0, "  p: Set preset position")
                    screen.addstr(11, 0, "  g: Go to preset position")
                    screen.addstr(12, 0, "  s: Control scan function")
                    screen.addstr(13, 0, "  d/n: Toggle day/night mode")
                    screen.addstr(14, 0, "  a: Toggle auto focus")
                    screen.addstr(15, 0, "  b: Toggle backlight compensation")
                    screen.addstr(17, 0, "Stream Controls:")
                    screen.addstr(18, 0, "  m: Switch stream (main/sub)")
                    screen.addstr(19, 0, "  r: Start/stop recording")
                    screen.addstr(20, 0, "  c: Take snapshot")
                    screen.addstr(21, 0, "  v: View recordings")
                    screen.addstr(22, 0, "  z: Pause/resume stream")
                    screen.addstr(24, 0, "  q: Quit")