# Android SDK Setup Guide

This guide provides detailed instructions for setting up the Android SDK for developing the PTZ Camera Controller application.

## Requirements

- JDK 17
- Android SDK with build tools version 34.0.0
- Gradle 8.2 or higher
- Git

## Quick Setup

For a quick setup, we provide these simple commands:

```bash
# Verify existing SDK setup
./simplified_build.sh sdk-check

# Generate QR code with SDK configuration details
./simplified_build.sh generate-qr
```

The QR code will contain all the necessary information for setting up your development environment.

## Manual SDK Setup

If you prefer to set up the Android SDK manually, follow these steps:

1. Download and install Android Studio (which includes the Android SDK)
2. Set the `ANDROID_HOME` environment variable to your Android SDK location
3. Create a `local.properties` file in the project root with the following content:
   ```
   sdk.dir=/path/to/your/Android/Sdk
   ```
4. Install the required SDK components through Android Studio's SDK Manager:
   - Android SDK Platform 34
   - Android SDK Build-Tools 34.0.0
   - Android SDK Platform-Tools
   - Android SDK Tools

## Simplified Build Script

We provide a simplified build script to handle common development tasks. This script automatically:

1. Verifies the Android SDK setup
2. Sets the appropriate environment variables
3. Executes Gradle commands with optimized settings

### Available Commands

```bash
# Verify SDK setup
./simplified_build.sh sdk-check

# Generate QR code with SDK configuration
./simplified_build.sh generate-qr

# Quick project check without running Gradle
./simplified_build.sh minimal-check

# Show project structure and tasks
./simplified_build.sh project-info

# Compile without building APK
./simplified_build.sh compile

# Build debug APK
./simplified_build.sh debug 

# Resolve dependencies
./simplified_build.sh dependencies

# Check project structure
./simplified_build.sh check-project
```

## SDK Location Resolution Process

The project uses the following resolution order to locate the Android SDK:

1. `ANDROID_HOME` environment variable (if set)
2. `sdk.dir` property in `local.properties` file
3. `/home/runner/Android/Sdk` (default location for CI/CD environments)

## Troubleshooting

### Common SDK Issues

1. **SDK Not Found**: 
   - Create or update `local.properties` with your SDK location
   - Run `./simplified_build.sh sdk-check` to verify the setup

2. **Build Tools Not Found**:
   - Install the required build tools version (34.0.0) through Android Studio's SDK Manager
   - Or run the setup script: `./setup_android_sdk.sh`

3. **JDK Version Mismatch**:
   - Ensure you have JDK 17 installed and configured
   - The project explicitly requires Java 17 compatibility

4. **Gradle Errors**:
   - Run `./simplified_build.sh version` to check Gradle and JDK versions
   - Run `./simplified_build.sh clean` to clear any corrupted build files

## CI/CD Environment Setup

For CI/CD environments like GitHub Actions, we provide automatic SDK setup:

1. Include setup in your workflow:
   ```yaml
   - name: Set up Android SDK
     run: ./setup_android_sdk.sh
     
   - name: Build APK
     run: ./simplified_build.sh debug
   ```

2. This will:
   - Create the necessary directory structure for Android SDK
   - Set up license agreements
   - Configure environment variables
   - Create a minimal SDK structure for building the app

## Further Resources

- [Android SDK Documentation](https://developer.android.com/studio/command-line/sdkmanager)
- [JDK 17 Setup Guide](docs/release_notes_jdk17.md)
- [Gradle Documentation](https://docs.gradle.org/current/userguide/userguide.html)