#!/bin/bash
# check_android_project.sh - A tool to verify Android project structure
# This script doesn't use Gradle, just checks files and directories

echo "==== Android Project Structure Check ===="
echo ""

# Check basic project files
echo "Checking essential project files..."
ESSENTIAL_FILES=("settings.gradle" "build.gradle" "gradle.properties" "local.properties" "gradlew" "gradlew.bat")
for file in "${ESSENTIAL_FILES[@]}"; do
  if [ -f "./$file" ]; then
    echo "✓ Found $file"
  else
    echo "✗ Missing $file"
  fi
done
echo ""

# Check app directory structure
echo "Checking app directory structure..."
if [ -d "./app" ]; then
  echo "✓ Found app directory"
  
  # Check app build.gradle
  if [ -f "./app/build.gradle" ]; then
    echo "✓ Found app/build.gradle"
  else
    echo "✗ Missing app/build.gradle"
  fi
  
  # Check source directory
  if [ -d "./app/src" ]; then
    echo "✓ Found app/src directory"
    
    # Check main directory
    if [ -d "./app/src/main" ]; then
      echo "✓ Found app/src/main directory"
      
      # Check AndroidManifest.xml
      if [ -f "./app/src/main/AndroidManifest.xml" ]; then
        echo "✓ Found AndroidManifest.xml"
      else
        echo "✗ Missing AndroidManifest.xml"
      fi
      
      # Check java directory
      if [ -d "./app/src/main/java" ]; then
        echo "✓ Found java directory"
        
        # Check package directories
        PACKAGE_DIR=$(grep -o 'namespace ['"'"'"][^'"'"'"]*['"'"'"]' ./app/build.gradle | sed 's/namespace ['"'"'"]//g' | sed 's/['"'"'"]//g' | tr '.' '/')
        if [ -n "$PACKAGE_DIR" ] && [ -d "./app/src/main/java/$PACKAGE_DIR" ]; then
          echo "✓ Found package directory structure: $PACKAGE_DIR"
          
          # Count Kotlin files
          KOTLIN_COUNT=$(find "./app/src/main/java/$PACKAGE_DIR" -name "*.kt" | wc -l)
          echo "  Found $KOTLIN_COUNT Kotlin files"
        else
          echo "✗ Missing or incomplete package directory structure"
          echo "  Searched for: $PACKAGE_DIR"
          
          # Try to find any Kotlin files
          KOTLIN_FILES=$(find "./app/src/main/java" -name "*.kt" | wc -l)
          echo "  Found $KOTLIN_FILES Kotlin files in java directory"
        fi
      else
        echo "✗ Missing java directory"
      fi
      
      # Check resources
      if [ -d "./app/src/main/res" ]; then
        echo "✓ Found resources directory"
        
        # Count resource files by type
        LAYOUT_COUNT=$(find "./app/src/main/res" -path "*/layout/*.xml" | wc -l)
        VALUES_COUNT=$(find "./app/src/main/res" -path "*/values/*.xml" | wc -l)
        DRAWABLE_COUNT=$(find "./app/src/main/res" -path "*/drawable*/*" | wc -l)
        
        echo "  Found $LAYOUT_COUNT layout files"
        echo "  Found $VALUES_COUNT values files"
        echo "  Found $DRAWABLE_COUNT drawable resources"
      else
        echo "✗ Missing resources directory"
      fi
    else
      echo "✗ Missing app/src/main directory"
    fi
  else
    echo "✗ Missing app/src directory"
  fi
else
  echo "✗ Missing app directory!"
fi
echo ""

# Check Android SDK setup
echo "Checking Android SDK setup..."
ANDROID_SDK_DIR="/home/runner/Android/Sdk"
if [ -d "$ANDROID_SDK_DIR" ]; then
  echo "✓ Found Android SDK directory at $ANDROID_SDK_DIR"
  
  # Check important SDK components
  if [ -d "$ANDROID_SDK_DIR/platforms" ]; then
    echo "✓ Found platforms directory"
    PLATFORM_COUNT=$(ls -la "$ANDROID_SDK_DIR/platforms" | grep -c "android-")
    echo "  Found $PLATFORM_COUNT platform versions"
  else
    echo "✗ Missing platforms directory"
  fi
  
  if [ -d "$ANDROID_SDK_DIR/build-tools" ]; then
    echo "✓ Found build-tools directory"
    BUILDTOOLS_COUNT=$(ls -la "$ANDROID_SDK_DIR/build-tools" | grep -v "^\." | wc -l)
    echo "  Found $((BUILDTOOLS_COUNT-2)) build tools versions"
  else
    echo "✗ Missing build-tools directory"
  fi
  
  if [ -d "$ANDROID_SDK_DIR/platform-tools" ]; then
    echo "✓ Found platform-tools directory"
  else
    echo "✗ Missing platform-tools directory"
  fi
  
  if [ -d "$ANDROID_SDK_DIR/licenses" ]; then
    echo "✓ Found licenses directory"
    LICENSE_COUNT=$(ls -la "$ANDROID_SDK_DIR/licenses" | grep -v "^\." | wc -l)
    echo "  Found $((LICENSE_COUNT-2)) license files"
  else
    echo "✗ Missing licenses directory"
  fi
else
  echo "✗ Android SDK directory not found at $ANDROID_SDK_DIR"
fi
echo ""

# Check environment variables
echo "Checking environment variables..."
if [ -n "$ANDROID_HOME" ]; then
  echo "✓ ANDROID_HOME is set to: $ANDROID_HOME"
else
  echo "✗ ANDROID_HOME is not set"
fi

if [ -n "$JAVA_HOME" ]; then
  echo "✓ JAVA_HOME is set to: $JAVA_HOME"
  java -version 2>&1 | head -n 1
else
  echo "✗ JAVA_HOME is not set"
  
  # Try to run java anyway
  if command -v java &> /dev/null; then
    echo "  But java is available in PATH:"
    java -version 2>&1 | head -n 1
  else
    echo "  And java is not available in PATH"
  fi
fi
echo ""

echo "==== Check Complete ===="