# Installing the PTZ Camera Control App on Your Android Tablet

## Option 1: Build from Source

To install the PTZ Camera Control app on your Android tablet, follow these steps using Android Studio:

1. Clone the repository to your local machine:
   ```
   git clone [repository-url]
   ```

2. Open the project in Android Studio

3. Connect your Android tablet to your computer with a USB cable and enable USB debugging in Developer Options

4. Select "Run" → "Run 'app'" from the Android Studio menu, or click the green play button in the toolbar

5. Select your connected tablet from the device list and click OK

6. The app will be installed and launched on your tablet

## Option 2: Install via APK File (For Development Only)

If you prefer not to use Android Studio, you can build and install the APK directly:

1. Build the release APK using Gradle:
   ```
   ./gradlew assembleRelease
   ```

2. The APK will be generated at: `app/build/outputs/apk/release/app-release.apk`

3. Transfer the APK file to your Android tablet (via email, USB, cloud storage, etc.)

4. On your Android tablet, browse to the APK file and tap it to install
   - You may need to allow installation from unknown sources in Settings > Security

## Option 3: Use Google Play Store (Coming Soon)

In the future, the app will be available through the Google Play Store for easier installation.

## Connecting to the Onboard Computer

After installation, follow these steps to connect to the PTZ Camera System:

1. Ensure both your tablet and onboard computer (Raspberry Pi or Jetson) are on the same network

2. Launch the PTZ Camera Control app on your tablet

3. On the Connection screen, choose WiFi or Bluetooth connection method

4. For WiFi, enter the IP address of the onboard computer (default port is 8000)

5. For Bluetooth, select the "PTZCameraServer" device from the list of available Bluetooth devices

6. Once connected, you'll be able to view the camera stream and control PTZ functions

## Troubleshooting

If you encounter issues:

1. Ensure the PTZ Camera Server is running on the onboard computer
   ```
   python camera_server.py
   ```

2. Check that your tablet and onboard computer are on the same network (for WiFi connections)

3. Verify Bluetooth is enabled on both devices (for Bluetooth connections)

4. Check the logs on the onboard computer for any error messages

5. If streaming issues occur, try the lower quality stream through settings