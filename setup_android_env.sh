#!/bin/bash
# Set up Android environment variables
export ANDROID_HOME=/home/runner/Android/Sdk
export PATH=$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools:$PATH
echo "Android environment variables set up:"
echo "ANDROID_HOME=$ANDROID_HOME"
