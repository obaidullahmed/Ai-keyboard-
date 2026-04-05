#!/bin/bash

# Fix Android Build Environment Script
# This script normalizes permissions and verifies wrapper files for Linux CI/dev environments.

set -e

echo "Fixing Android build environment..."

# Normalize permissions
chmod +x gradlew
chmod +x "$0"

# Check if wrapper files exist
if [ ! -f "gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "ERROR: gradle-wrapper.jar is missing. Please regenerate the Gradle wrapper."
    exit 1
fi

if [ ! -f "gradle/wrapper/gradle-wrapper.properties" ]; then
    echo "ERROR: gradle-wrapper.properties is missing."
    exit 1
fi

# Print Java and Gradle info
echo "Java version:"
java -version
echo ""
echo "Gradle wrapper version:"
./gradlew --version | head -5

echo "Build environment is ready."