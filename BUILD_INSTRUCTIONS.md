# Android App Build Instructions

This document provides instructions for building the Android app, particularly in CI environments or Replit where you might encounter SDK location issues.

## Prerequisites

- JDK 17 or higher
- Android SDK installed (or available in the environment)
- Gradle (the wrapper should handle this)

## Common Build Issues

### SDK Location Issues

If you encounter errors like:

```
Could not determine the dependencies of task ':app:compileDebugJavaWithJavac'.
> Could not determine the dependencies of null.
  > SDK location not found. Define a valid SDK location with an ANDROID_HOME environment variable or by setting the sdk.dir path in your project's local properties file at '/path/to/local.properties'.
```

Or:

```
Could not determine the dependencies of task ':app:compileDebugJavaWithJavac'.
> Could not determine the dependencies of null.
  > Failed to find target with hash string 'android-33' in: /path/to/Android/Sdk
```

### Solution

Run the SDK fix script:

```bash
./fix_android_sdk.sh
```

This script will:
1. Set up the correct environment variables
2. Create/update the `local.properties` file with the SDK path
3. Ensure platform source.properties files exist
4. Add necessary configuration to gradle.properties
5. Update the app's build.gradle if needed

## Build Commands

After running the fix script, you can build the app with:

```bash
./gradlew clean build
```

For a faster debug build:

```bash
./gradlew assembleDebug
```

## Build Environment Checks

To check your build environment:

```bash
./test_build_environment.sh
```

## Troubleshooting

If builds are still failing:

1. Confirm the ANDROID_HOME environment variable points to a valid SDK installation
2. Verify the platforms directory contains android-33
3. Ensure build-tools are installed (particularly version 33.0.1)
4. Check gradle.properties for any conflicting settings

## CI/Replit Specific Notes

In CI or Replit environments, we've added special configuration to optimize builds:
- Reduced resource usage
- Skipped non-essential validations
- Disabled lint checks
- Simplified build process

These optimizations are automatically applied when building in CI or Replit environments.