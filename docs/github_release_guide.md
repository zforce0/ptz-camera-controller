# GitHub Release Guide for PTZ Camera Controller

This guide explains how to create and manage GitHub releases for the PTZ Camera Controller project, making it easy to distribute the Android APK to users.

## Setting Up Your GitHub Repository

1. Create a new repository on GitHub:
   - Go to https://github.com/new
   - Repository name: `ptz-camera-controller`
   - Description: "Android tablet application for controlling PTZ cameras connected to Raspberry Pi/Jetson"
   - Set as Public
   - Initialize with a README
   - Choose MIT License
   - Click "Create repository"

2. Clone the repository to your local machine:
   ```
   git clone https://github.com/YOUR_USERNAME/ptz-camera-controller.git
   cd ptz-camera-controller
   ```

3. Copy all project files to the repository:
   ```
   # Copy all project files to the repository
   cp -r /path/to/project/* .
   
   # Add files to git
   git add .
   
   # Commit the changes
   git commit -m "Initial commit"
   
   # Push to GitHub
   git push origin main
   ```

## Creating Your First Release

1. Build the APK:
   - Open the project in Android Studio
   - Select Build > Generate Signed Bundle/APK
   - Choose APK
   - Create or select a keystore
   - Fill in the key details
   - Choose release build variant
   - Click "Finish"
   - The APK will be generated in `app/build/outputs/apk/release/app-release.apk`

2. Generate a QR code for the APK:
   - Install qrencode: `sudo apt-get install qrencode` (Ubuntu/Debian)
   - Generate QR code:
   ```
   qrencode -o qrcode.png "https://github.com/YOUR_USERNAME/ptz-camera-controller/releases/latest/download/ptz-camera-controller.apk"
   ```

3. Create a GitHub release:
   - On your repository page, click "Releases" in the right sidebar
   - Click "Create a new release"
   - Tag version: v1.0.0
   - Release title: "PTZ Camera Controller v1.0.0"
   - Description: Add detailed release notes
   - Attach the APK file by dragging it into the assets area
   - Attach the QR code image
   - Check "This is a pre-release" if it's not ready for production
   - Click "Publish release"

## Using GitHub Actions for Automated Releases

We've included a GitHub Actions workflow file in your project at `.github/workflows/build-android-app.yml` that automates:

1. Building the APK when you push a new tag
2. Generating a QR code
3. Creating a GitHub release with both the APK and QR code

To use this:

1. Set up repository secrets:
   - Go to your repository Settings > Secrets and variables > Actions
   - Add the following secrets:
     - KEYSTORE_FILE: Base64-encoded keystore file
     - KEYSTORE_PASSWORD: Your keystore password
     - KEY_PASSWORD: Your key password
     - KEY_ALIAS: Your key alias

2. To create a new release:
   ```
   # Update version in app/build.gradle
   # Commit the changes
   git commit -am "Bump version to x.y.z"
   
   # Create and push a tag
   git tag -a vx.y.z -m "Version x.y.z"
   git push origin vx.y.z
   ```

3. GitHub Actions will automatically:
   - Build the APK
   - Generate a QR code
   - Create a release with proper version information
   - Attach the APK and QR code to the release

## Sharing the App

Once you've created a release, you can share the application in several ways:

1. Share the QR code - users can scan it to download the APK
2. Share the direct link: `https://github.com/YOUR_USERNAME/ptz-camera-controller/releases/latest/download/ptz-camera-controller.apk`
3. Share the releases page: `https://github.com/YOUR_USERNAME/ptz-camera-controller/releases`

## Update Process

When you want to release updates:

1. Make your code changes
2. Update the version number in app/build.gradle
3. Create a new tag with the version number
4. Push the tag to trigger the GitHub Actions workflow
5. Users can download the update using the same QR code or link

## Best Practices

1. Follow semantic versioning (MAJOR.MINOR.PATCH)
2. Provide detailed release notes
3. Tag all releases in git
4. Keep the main branch stable
5. Test APK before releasing