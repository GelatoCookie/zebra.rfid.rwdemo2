#!/bin/bash
# Automated script to build, deploy, launch, and execute the app on a connected device.

set -euo pipefail

WORKDIR="$(cd "$(dirname "$0")" && pwd)"
cd "$WORKDIR"

ensure_cmd() {
  local cmd="$1"
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "Required command not found: $cmd"
    exit 1
  fi
}

ensure_cmd adb

# 1. Build the APK
echo "Building APK..."

BUILD_CMD=("./gradlew" "assembleDebug")

# Recover from missing wrapper JAR by generating wrapper with system Gradle.
if [ ! -f "gradle/wrapper/gradle-wrapper.jar" ]; then
  echo "gradle-wrapper.jar is missing. Attempting to generate it using system Gradle..."
  ensure_cmd gradle
  gradle wrapper
fi

"${BUILD_CMD[@]}"

# 2. Find the APK path
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ ! -f "$APK_PATH" ]; then
  echo "APK not found at $APK_PATH"
  exit 1
fi

echo "APK built at $APK_PATH"


# 3. Find first connected online device (USB or TCP)
adb start-server >/dev/null
DEVICE=$(adb devices | awk 'NR>1 && $2=="device" {print $1; exit}')
if [ -z "$DEVICE" ]; then
  echo "No online ADB device found. Connect a device and ensure it shows as 'device' in 'adb devices'."
  exit 1
fi

PACKAGE_NAME="com.zebra.rfid.rwdemo2"

echo "Killing previous running app..."
adb -s "$DEVICE" shell am force-stop "$PACKAGE_NAME" || true

echo "Deleting (uninstalling) existing app..."
adb -s "$DEVICE" uninstall "$PACKAGE_NAME" || true

echo "Deploying APK to device $DEVICE..."
adb -s "$DEVICE" install -r "$APK_PATH"

echo "Launching app..."
adb -s "$DEVICE" shell monkey -p "$PACKAGE_NAME" -c android.intent.category.LAUNCHER 1

echo "App launched on $DEVICE."
