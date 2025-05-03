#!/bin/bash
# verify_jdk.sh - A script to verify JDK version and setup

# Function to print section headers
print_header() {
    echo "============================================"
    echo " $1"
    echo "============================================"
}

# Check JAVA_HOME
print_header "JAVA_HOME Check"
if [ -n "$JAVA_HOME" ]; then
    echo "JAVA_HOME is set to: $JAVA_HOME"
else
    echo "JAVA_HOME is not set!"
fi

# Check Java version
print_header "Java Version"
if command -v java &> /dev/null; then
    java -version
else
    echo "Java is not installed or not in PATH!"
fi

# Check Gradle compatibility
print_header "Gradle Version"
if [ -f "./gradlew" ]; then
    chmod +x ./gradlew
    ./gradlew --version | grep -E "^(Gradle|JVM)"
else
    echo "Gradle wrapper not found!"
fi

# Check Android SDK
print_header "Android SDK Check"
if [ -n "$ANDROID_HOME" ]; then
    echo "ANDROID_HOME is set to: $ANDROID_HOME"
    
    # Check for SDK components
    if [ -d "$ANDROID_HOME/platform-tools" ]; then
        echo "Found platform-tools"
    else
        echo "platform-tools not found"
    fi
    
    if [ -d "$ANDROID_HOME/build-tools" ]; then
        echo "Found build-tools: $(ls -1 $ANDROID_HOME/build-tools | tr '\n' ' ')"
    else
        echo "build-tools not found"
    fi
    
    if [ -d "$ANDROID_HOME/platforms" ]; then
        echo "Found platforms: $(ls -1 $ANDROID_HOME/platforms | tr '\n' ' ')"
    else
        echo "platforms not found"
    fi
else
    echo "ANDROID_HOME is not set!"
fi

# Check build configuration for JDK 17
print_header "Build Configuration Check"
grep -r "JavaVersion.VERSION_1[1-7]" --include="*.gradle" .
grep -r "jvmTarget" --include="*.gradle" .

# Print system information
print_header "System Information"
echo "OS: $(uname -a)"
echo "Total RAM: $(free -h | grep -i mem | awk '{print $2}')"
echo "Available Disk Space: $(df -h . | tail -1 | awk '{print $4}')"

print_header "Verification Complete"
echo "If you're using JDK 17, you should see 'version \"17.x.x\"' in the Java Version section."
echo "For any issues, refer to docs/release_notes_jdk17.md"