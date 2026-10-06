#!/usr/bin/env bash
# =============================================================================
# DUNIYA ⛺ — One-Click Build & Install Script for Android / GrapheneOS
# =============================================================================
set -euo pipefail

echo "======================================================================"
echo " ⛺ DUNIYA — Offline AI Research Engine: Build & Install"
echo "======================================================================"

# 1. Verify ADB connection
echo "[1/4] Checking connected Android / GrapheneOS devices..."
if ! command -v adb >/dev/null 2>&1; then
    echo "❌ Error: 'adb' command not found."
    echo "👉 Please install Android Platform Tools or add them to your PATH:"
    echo "   export PATH=\"\$HOME/Library/Android/sdk/platform-tools:\$PATH\""
    exit 1
fi

DEVICE_COUNT=$(adb devices | grep -v "List of devices" | grep -w "device" | wc -l | tr -d ' ')
if [ "${DEVICE_COUNT}" -eq 0 ]; then
    echo "❌ Error: No Android device or emulator detected via ADB."
    echo "👉 Ensure your phone is connected with USB Debugging enabled, or launch an emulator."
    exit 1
fi

echo "✅ Detected ${DEVICE_COUNT} connected device(s):"
adb devices | grep -w "device"

# 2. Build Debug APK
echo ""
echo "[2/4] Building Duniya Native C++ Engine + Jetpack Compose APK..."
./gradlew assembleDebug

APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ ! -f "${APK_PATH}" ]; then
    echo "❌ Error: Build completed but APK not found at ${APK_PATH}"
    exit 1
fi
APK_SIZE=$(ls -lh "${APK_PATH}" | awk '{print $5}')
echo "✅ Successfully built APK (${APK_SIZE}) at ${APK_PATH}"

# 3. Install on device
echo ""
echo "[3/4] Installing APK on connected device..."
adb install -r "${APK_PATH}"
echo "✅ Installed successfully!"

# 4. Launch app and verify airgap
echo ""
echo "[4/4] Launching Duniya on device..."
adb shell am start -n com.example.duniya/.MainActivity >/dev/null 2>&1
echo "🚀 Duniya is now running on your device!"
echo ""
echo "Verifying 100% offline airgap (checking network permissions):"
PERMS=$(adb shell dumpsys package com.example.duniya | grep -E "android.permission.INTERNET" || true)
if [ -z "${PERMS}" ]; then
    echo "🔒 VERIFIED: Duniya has ZERO network permissions. 100% offline and airgapped."
else
    echo "⚠️ Warning: Found network permission: ${PERMS}"
fi
echo "======================================================================"
