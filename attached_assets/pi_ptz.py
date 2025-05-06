#!/usr/bin/env python3
"""
Raspberry Pi PTZ Camera Control Script
for controlling a PTZ camera using the Pelco-D protocol via CH341 USB-to-RS485 adapter

This script builds on the existing ptz_control.py implementation and adds more functionality
based on the camera's documentation.
"""

import serial
import time
import argparse
import sys
import os
import curses

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
        print("Stopped movement")

    def pan_left(self, speed=0x20):
        """Pan left at specified speed (00-3F)"""
        self._send_pelco_command(0x00, 0x04, speed, 0x00)
        print(f"Panning left at speed: {speed}")

    def pan_right(self, speed=0x20):
        """Pan right at specified speed (00-3F)"""
        self._send_pelco_command(0x00, 0x02, speed, 0x00)
        print(f"Panning right at speed: {speed}")

    def tilt_up(self, speed=0x20):
        """Tilt up at specified speed (00-3F)"""
        self._send_pelco_command(0x00, 0x08, 0x00, speed)
        print(f"Tilting up at speed: {speed}")

    def tilt_down(self, speed=0x20):
        """Tilt down at specified speed (00-3F)"""
        self._send_pelco_command(0x00, 0x10, 0x00, speed)
        print(f"Tilting down at speed: {speed}")

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
        print(f"Moving diagonally: {pan_direction}/{tilt_direction} at speed: {pan_speed}/{tilt_speed}")

    # Zoom control
    def zoom_in(self):
        """Zoom in"""
        self._send_pelco_command(0x00, 0x20)
        print("Zooming in")

    def zoom_out(self):
        """Zoom out"""
        self._send_pelco_command(0x00, 0x40)
        print("Zooming out")

    # Preset position control
    def goto_preset(self, preset_num):
        """Go to preset position (1-255)"""
        if 1 <= preset_num <= 255:
            self._send_pelco_command(0x00, 0x07, preset_num)
            print(f"Moving to preset position: {preset_num}")

    def set_preset(self, preset_num):
        """Set preset position (1-255)"""
        if 1 <= preset_num <= 255:
            self._send_pelco_command(0x00, 0x03, preset_num)
            print(f"Setting preset position: {preset_num}")

    # Special functions from the camera manual
    def set_scan_left_limit(self):
        """Set left limit for scan (preset 50)"""
        self.set_preset(50)
        print("Setting scan left limit (start point)")
        
    def set_scan_right_limit(self):
        """Set right limit for scan (preset 51)"""
        self.set_preset(51)
        print("Setting scan right limit (end point)")
        
    def start_scan(self):
        """Start horizontal scanning between limits"""
        self.goto_preset(51)
        print("Starting horizontal scan")
        
    def stop_scan(self):
        """Stop horizontal scanning"""
        self.goto_preset(50)
        print("Stopping horizontal scan")
        
    def toggle_backlight_compensation(self, enable=True):
        """Toggle backlight compensation (preset 55)"""
        if enable:
            self.goto_preset(55)
            print("Enabling backlight compensation")
        else:
            self.set_preset(55)
            print("Disabling backlight compensation")
            
    def toggle_digital_zoom(self, enable=True):
        """Toggle digital zoom (preset 58)"""
        if enable:
            self.goto_preset(58)
            print("Enabling digital zoom")
        else:
            self.set_preset(58)
            print("Disabling digital zoom")
            
    def toggle_auto_focus(self, auto=True):
        """Toggle between auto and manual focus (preset 59)"""
        if auto:
            self.goto_preset(59)
            print("Enabling auto focus")
        else:
            self.set_preset(59)
            print("Switching to manual focus")
            
    def toggle_auto_iris(self, auto=True):
        """Toggle between auto and manual iris (preset 60)"""
        if auto:
            self.goto_preset(60)
            print("Enabling auto iris")
        else:
            self.set_preset(60)
            print("Switching to manual iris")
            
    def toggle_day_night_mode(self, day_mode=True):
        """Toggle between day (color) and night (B&W) modes (preset 67)"""
        if day_mode:
            self.goto_preset(67)
            print("Switching to day mode (color)")
        else:
            self.set_preset(67)
            print("Switching to night mode (B&W)")
            
    def reboot_camera(self):
        """Reboot the camera (preset 54)"""
        self.goto_preset(54)
        print("Rebooting camera")
            
    def run_self_test(self):
        """Run camera self-test (preset 81)"""
        self.goto_preset(81)
        print("Running camera self-test")
        
    def toggle_auto_return(self, enable=True, timeout_mins=5):
        """Enable/disable auto return to home position after inactivity"""
        if enable:
            self.goto_preset(90)
            print(f"Enabling auto return to home after {timeout_mins} minutes of inactivity")
        else:
            self.set_preset(90)
            print("Disabling auto return")
            
    def toggle_long_focus_speed_limit(self, enable=True):
        """Enable/disable long focus speed limiting"""
        if enable:
            self.goto_preset(210)
            print("Enabling long focus speed limit")
        else:
            self.goto_preset(211)
            print("Disabling long focus speed limit")
            
    def toggle_power_memory(self, enable=True):
        """Enable/disable power memory (position after power cycle)"""
        if enable:
            self.goto_preset(221)
            print("Enabling power cycle memory")
        else:
            self.goto_preset(220)
            print("Disabling power cycle memory")

    def close(self):
        """Close the serial connection"""
        if self.ser and self.ser.is_open:
            self.ser.close()
            print("Connection closed")


def interactive_control(ptz):
    """
    Interactive control mode using keyboard
    
    Args:
        ptz: PTZController instance
    """
    screen = curses.initscr()
    curses.noecho()
    curses.cbreak()
    screen.keypad(True)
    
    try:
        screen.clear()
        screen.addstr(0, 0, "PTZ Camera Control")
        screen.addstr(2, 0, "Arrow keys: Pan/Tilt")
        screen.addstr(3, 0, "+/-: Zoom in/out")
        screen.addstr(4, 0, "p: Set preset position")
        screen.addstr(5, 0, "g: Go to preset position")
        screen.addstr(6, 0, "s: Start/stop scan")
        screen.addstr(7, 0, "d/n: Toggle day/night mode")
        screen.addstr(8, 0, "a: Toggle auto focus")
        screen.addstr(9, 0, "b: Toggle backlight compensation")
        screen.addstr(10, 0, "q: Quit")
        
        screen.refresh()
        
        scanning = False
        speed = 0x20  # Default speed (medium)
        
        while True:
            key = screen.getch()
            
            if key == ord('q'):
                break
                
            # Movement controls
            elif key == curses.KEY_UP:
                ptz.tilt_up(speed)
            elif key == curses.KEY_DOWN:
                ptz.tilt_down(speed)
            elif key == curses.KEY_LEFT:
                ptz.pan_left(speed)
            elif key == curses.KEY_RIGHT:
                ptz.pan_right(speed)
            elif key == curses.KEY_HOME:
                ptz.move_diagonal("left", "up", speed, speed)
            elif key == curses.KEY_END:
                ptz.move_diagonal("left", "down", speed, speed)
            elif key == curses.KEY_PPAGE:  # Page Up
                ptz.move_diagonal("right", "up", speed, speed)
            elif key == curses.KEY_NPAGE:  # Page Down
                ptz.move_diagonal("right", "down", speed, speed)
            elif key == ord(' '):
                ptz.stop()
                
            # Zoom controls
            elif key == ord('+'):
                ptz.zoom_in()
            elif key == ord('-'):
                ptz.zoom_out()
                
            # Speed adjustment
            elif key == ord('1'):
                speed = 0x10  # Slow
                screen.addstr(12, 0, "Speed: Slow    ")
                screen.refresh()
            elif key == ord('2'):
                speed = 0x20  # Medium
                screen.addstr(12, 0, "Speed: Medium  ")
                screen.refresh()
            elif key == ord('3'):
                speed = 0x30  # Fast
                screen.addstr(12, 0, "Speed: Fast    ")
                screen.refresh()
                
            # Preset position controls
            elif key == ord('p'):
                screen.addstr(13, 0, "Enter preset number (1-255): ")
                curses.echo()
                preset_str = screen.getstr(13, 30, 3).decode('utf-8')
                curses.noecho()
                try:
                    preset_num = int(preset_str)
                    if 1 <= preset_num <= 255:
                        ptz.set_preset(preset_num)
                        screen.addstr(14, 0, f"Preset {preset_num} set successfully    ")
                    else:
                        screen.addstr(14, 0, "Invalid preset number! Must be 1-255")
                except ValueError:
                    screen.addstr(14, 0, "Invalid input! Enter a number.     ")
                screen.refresh()
                
            elif key == ord('g'):
                screen.addstr(13, 0, "Enter preset number (1-255): ")
                curses.echo()
                preset_str = screen.getstr(13, 30, 3).decode('utf-8')
                curses.noecho()
                try:
                    preset_num = int(preset_str)
                    if 1 <= preset_num <= 255:
                        ptz.goto_preset(preset_num)
                        screen.addstr(14, 0, f"Moving to preset {preset_num}           ")
                    else:
                        screen.addstr(14, 0, "Invalid preset number! Must be 1-255")
                except ValueError:
                    screen.addstr(14, 0, "Invalid input! Enter a number.     ")
                screen.refresh()
                
            # Scan controls
            elif key == ord('s'):
                if not scanning:
                    screen.addstr(15, 0, "Setting scan: L (left/start), R (right/end), or S (start scanning): ")
                    scan_key = screen.getch()
                    if scan_key == ord('l') or scan_key == ord('L'):
                        ptz.set_scan_left_limit()
                        screen.addstr(16, 0, "Left scan limit set                      ")
                    elif scan_key == ord('r') or scan_key == ord('R'):
                        ptz.set_scan_right_limit()
                        screen.addstr(16, 0, "Right scan limit set                     ")
                    elif scan_key == ord('s') or scan_key == ord('S'):
                        ptz.start_scan()
                        scanning = True
                        screen.addstr(16, 0, "Scan started                             ")
                else:
                    ptz.stop_scan()
                    scanning = False
                    screen.addstr(16, 0, "Scan stopped                             ")
                screen.refresh()
                
            # Day/night mode
            elif key == ord('d'):
                ptz.toggle_day_night_mode(True)
                screen.addstr(17, 0, "Day mode (color) enabled                  ")
                screen.refresh()
            elif key == ord('n'):
                ptz.toggle_day_night_mode(False)
                screen.addstr(17, 0, "Night mode (B&W) enabled                  ")
                screen.refresh()
                
            # Auto focus
            elif key == ord('a'):
                screen.addstr(18, 0, "Auto focus: 1 (on) or 0 (off): ")
                af_key = screen.getch()
                if af_key == ord('1'):
                    ptz.toggle_auto_focus(True)
                    screen.addstr(19, 0, "Auto focus enabled                        ")
                elif af_key == ord('0'):
                    ptz.toggle_auto_focus(False)
                    screen.addstr(19, 0, "Manual focus enabled                      ")
                screen.refresh()
                
            # Backlight compensation
            elif key == ord('b'):
                screen.addstr(18, 0, "Backlight compensation: 1 (on) or 0 (off): ")
                blc_key = screen.getch()
                if blc_key == ord('1'):
                    ptz.toggle_backlight_compensation(True)
                    screen.addstr(19, 0, "Backlight compensation enabled            ")
                elif blc_key == ord('0'):
                    ptz.toggle_backlight_compensation(False)
                    screen.addstr(19, 0, "Backlight compensation disabled           ")
                screen.refresh()
                
    finally:
        curses.nocbreak()
        screen.keypad(False)
        curses.echo()
        curses.endwin()


def main():
    parser = argparse.ArgumentParser(description="PTZ Camera Control for Raspberry Pi")
    parser.add_argument('-p', '--port', default='/dev/ttyUSB0', help="Serial port path (default: /dev/ttyUSB0)")
    parser.add_argument('-b', '--baudrate', type=int, default=9600, choices=[2400, 4800, 9600, 38400],
                        help="Communication baudrate (default: 9600)")
    parser.add_argument('-a', '--address', type=int, default=1, help="Camera address (1-255)")
    parser.add_argument('-i', '--interactive', action='store_true', help="Start interactive control mode")
    parser.add_argument('-c', '--command', choices=['pan_left', 'pan_right', 'tilt_up', 'tilt_down', 
                                                  'zoom_in', 'zoom_out', 'stop', 'goto_preset', 'set_preset',
                                                  'start_scan', 'stop_scan', 'day_mode', 'night_mode',
                                                  'auto_focus_on', 'auto_focus_off', 'reboot'],
                       help="Command to execute")
    parser.add_argument('-s', '--speed', type=int, default=32, help="Movement speed (1-63)")
    parser.add_argument('-n', '--preset', type=int, help="Preset number for goto_preset or set_preset commands")
    
    args = parser.parse_args()
    ptz = None
    
    # Validate speed range
    if not 1 <= args.speed <= 63:
        print("Error: Speed must be between 1 and 63")
        return 1
        
    # Validate address range
    if not 1 <= args.address <= 255:
        print("Error: Address must be between 1 and 255")
        return 1

    try:
        # Connect to the camera
        ptz = PTZController(port=args.port, baudrate=args.baudrate, address=args.address)
        
        # Execute command or enter interactive mode
        if args.interactive:
            interactive_control(ptz)
        elif args.command:
            # Validate preset number if needed
            if args.command in ['goto_preset', 'set_preset'] and (args.preset is None or not 1 <= args.preset <= 255):
                print("Error: Valid preset number (1-255) is required for goto_preset and set_preset commands")
                return 1
                
            # Execute the specified command
            if args.command == 'pan_left':
                ptz.pan_left(args.speed)
            elif args.command == 'pan_right':
                ptz.pan_right(args.speed)
            elif args.command == 'tilt_up':
                ptz.tilt_up(args.speed)
            elif args.command == 'tilt_down':
                ptz.tilt_down(args.speed)
            elif args.command == 'zoom_in':
                ptz.zoom_in()
            elif args.command == 'zoom_out':
                ptz.zoom_out()
            elif args.command == 'stop':
                ptz.stop()
            elif args.command == 'goto_preset':
                ptz.goto_preset(args.preset)
            elif args.command == 'set_preset':
                ptz.set_preset(args.preset)
            elif args.command == 'start_scan':
                ptz.start_scan()
            elif args.command == 'stop_scan':
                ptz.stop_scan()
            elif args.command == 'day_mode':
                ptz.toggle_day_night_mode(True)
            elif args.command == 'night_mode':
                ptz.toggle_day_night_mode(False)
            elif args.command == 'auto_focus_on':
                ptz.toggle_auto_focus(True)
            elif args.command == 'auto_focus_off':
                ptz.toggle_auto_focus(False)
            elif args.command == 'reboot':
                ptz.reboot_camera()
                
            # Sleep briefly to ensure command is processed
            time.sleep(0.5)
        else:
            print("No command specified. Use --interactive or --command options.")
            parser.print_help()
            
    except Exception as e:
        print(f"Error: {e}")
        return 1
    finally:
        if ptz:
            ptz.close()
            
    return 0

if __name__ == "__main__":
    sys.exit(main())