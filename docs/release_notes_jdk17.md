# Release Notes: JDK 17 Upgrade

## Overview
The PTZ Camera Controller project has been upgraded to use JDK 17, replacing the previous JDK 11 implementation. This upgrade provides access to the latest Java language features and improves overall performance and security.

## Changes Made

1. **Updated Build Configuration**
   - Modified app/build.gradle to target JDK 17:
     ```gradle
     compileOptions {
         sourceCompatibility JavaVersion.VERSION_17
         targetCompatibility JavaVersion.VERSION_17
     }
     kotlinOptions {
         jvmTarget = '17'
     }
     ```
   
   - Updated root build.gradle to ensure all Java projects use JDK 17 toolchain:
     ```gradle
     plugins.withType(JavaPlugin).configureEach {
         java {
             toolchain {
                 languageVersion = JavaLanguageVersion.of(17)
             }
         }
     }
     ```

2. **GitHub Actions Configuration**
   - Verified GitHub workflow is already configured for JDK 17:
     ```yaml
     - name: Set up JDK 17
       uses: actions/setup-java@v3
       with:
         java-version: '17'
         distribution: 'temurin'
         cache: gradle
     ```

## Benefits of JDK 17

1. **Language Features**
   - Text blocks
   - Pattern matching for instanceof
   - Records
   - Sealed classes
   - Enhanced pseudo-random number generators
   - Enhanced NullPointerException messages

2. **Performance Improvements**
   - New garbage collector
   - Improved memory management
   - Better startup time

3. **Security Enhancements**
   - Modern cryptographic algorithms
   - Enhanced security libraries
   - Improved sandboxing capabilities

## Development Requirements

Developers working on this project now need:
- JDK 17 or higher installed locally
- Android Studio Arctic Fox (2021.3.1) or newer
- Gradle 7.3 or newer

## Compatibility Notes

This upgrade maintains compatibility with:
- Android 6.0 (API level 23) and higher
- All existing native libraries
- Current build pipeline configurations

## Troubleshooting

If you encounter build issues after the JDK 17 upgrade:

1. Ensure you have JDK 17 installed and properly configured
2. Update your IDE to the latest version
3. Run a clean build: `./gradlew clean build`
4. Delete .gradle directory and Gradle caches if necessary

## Future Considerations

1. Utilize JDK 17 language features in new code
2. Consider enabling preview features in development builds
3. Monitor performance improvements from JDK 17 runtime