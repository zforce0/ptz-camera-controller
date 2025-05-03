#!/bin/bash
# test_build_environment.sh - Verifies the build environment for Android development

# Function to print section headers
print_header() {
    echo "============================================"
    echo " $1"
    echo "============================================"
}

# Check local.properties
print_header "Local Properties Check"
if [ -f "./local.properties" ]; then
    echo "local.properties exists:"
    cat ./local.properties
else
    echo "local.properties file not found!"
    echo "Running setup_android_sdk.sh to create it..."
    ./setup_android_sdk.sh
fi

# Check Android SDK structure
print_header "Android SDK Structure Check"
SDK_DIR=$(grep "sdk.dir" ./local.properties | cut -d'=' -f2)
echo "Checking SDK directory: $SDK_DIR"

if [ -d "$SDK_DIR" ]; then
    echo "SDK directory exists."
    
    # Check essential directories
    essential_dirs=("platforms" "build-tools" "platform-tools")
    for dir in "${essential_dirs[@]}"; do
        if [ -d "$SDK_DIR/$dir" ]; then
            echo "✓ $dir directory exists"
        else
            echo "✗ $dir directory missing"
        fi
    done
    
    # Check license acceptance
    if [ -d "$SDK_DIR/licenses" ]; then
        echo "✓ Licenses directory exists"
    else
        echo "✗ Licenses directory missing"
    fi
else
    echo "SDK directory not found at: $SDK_DIR"
    echo "Please run setup_android_sdk.sh to create the SDK structure."
fi

# Check Gradle wrapper
print_header "Gradle Wrapper Check"
if [ -f "./gradlew" ]; then
    echo "✓ Gradle wrapper exists"
    chmod +x ./gradlew
else
    echo "✗ Gradle wrapper missing"
fi

# Check build.gradle files
print_header "Build Files Check"
if [ -f "./build.gradle" ]; then
    echo "✓ Root build.gradle exists"
else
    echo "✗ Root build.gradle missing"
fi

if [ -f "./app/build.gradle" ]; then
    echo "✓ App build.gradle exists"
else
    echo "✗ App build.gradle missing"
fi

# Check for Android SDK environment variable
print_header "Environment Variables"
if [ -n "$ANDROID_HOME" ]; then
    echo "✓ ANDROID_HOME is set to: $ANDROID_HOME"
else
    echo "✗ ANDROID_HOME environment variable not set"
    echo "  Setting it temporarily to: $SDK_DIR"
    export ANDROID_HOME="$SDK_DIR"
fi

print_header "Gradle Version"
if command -v ./gradlew &> /dev/null; then
    ./gradlew --version | grep -E "^(Gradle|JVM)"
else
    echo "Cannot execute gradlew. Please make sure it's executable (chmod +x ./gradlew)"
fi

print_header "Next Steps"
echo "If all checks pass, try building with: ./gradlew assembleDebug"
echo "For more detailed SDK setup, run: ./setup_android_sdk.sh"
echo "For JDK verification, run: ./verify_jdk.sh"