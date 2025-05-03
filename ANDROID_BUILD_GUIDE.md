# Android Build Guide

## Resolving SDK Platform Detection Issues

If you encounter the error:

```
Failed to find target with hash string 'android-33' in: /home/runner/Android/Sdk
```

This is a common issue with Android SDK platform detection, particularly in CI environments or when SDKs are installed in non-standard ways.

## Quick Fix

Run the provided fix script:

```bash
./fix_android_sdk.sh
```

This script performs several important fixes:

1. Sets the `ANDROID_HOME` environment variable
2. Creates/updates `local.properties` with the correct SDK path
3. Ensures platform metadata files exist and are properly configured
4. Updates Gradle properties with configuration to bypass problematic checks

## Manual Fix Steps

If you prefer to fix manually, follow these steps:

1. Set the environment variable:
   ```bash
   export ANDROID_HOME=/home/runner/Android/Sdk
   ```

2. Create/update `local.properties`:
   ```
   sdk.dir=/home/runner/Android/Sdk
   ```

3. Add the SDK directory to `app/build.gradle`:
   ```gradle
   android {
       // ...
       sdkDirectory = new File("/home/runner/Android/Sdk")
       // ...
   }
   ```

4. Add to `gradle.properties`:
   ```
   android.dir.platformDirectory=/home/runner/Android/Sdk/platforms/android-33
   android.builder.sdkDownload=false
   android.sdk.repo.prefetched=true
   android.enableSdkLocationCheck=false
   ```

## Understanding the Error

This error occurs when the Android Gradle Plugin cannot properly detect the installed SDK platform. This can happen for several reasons:

1. The SDK platform is installed but missing required metadata files
2. The environment variable `ANDROID_HOME` is not set correctly
3. The SDK platform directory structure is not what the build system expects
4. Gradle's cache has outdated information about the SDK location

The fixes address these issues by explicitly telling the build system where to find the SDK and adding any missing metadata files.

## Prevention

To prevent this issue in future builds:

1. Always run the `./fix_android_sdk.sh` script before building
2. Consider adding it as a pre-build step in CI workflows
3. Make sure the Android SDK platform specified in `compileSdk` is actually installed