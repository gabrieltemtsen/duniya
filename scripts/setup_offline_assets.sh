#!/usr/bin/env bash
# =============================================================================
# DUNIYA ⛺ — Offline Asset, Sparse MoE GGUF & Knowledge Pack Provisioner
# =============================================================================
# Because Duniya enforces a strict kernel-level airgap on Android/GrapheneOS
# (android.permission.INTERNET is stripped from AndroidManifest.xml and process
# lacks GID 3003 AID_INET), optional multi-gigabyte Sparse MoE GGUF models and
# extended Wikipedia/ArXiv packs (up to the 50 GB budget) are fetched on a host
# computer and pushed over USB via `adb push`, or selected from local SD/USB-C
# storage via the in-app Storage Access Framework picker.
#
# NOTE: Duniya works out-of-the-box immediately after APK installation with ZERO
# external downloads required (core 64-expert Sparse MoE + 65K Engram table +
# Multi-Domain Graduate Encyclopedia are generated/bundled natively).
# =============================================================================

set -euo pipefail

MODE="${1:---verify}"
APP_ID="com.example.duniya"
DEVICE_PACK_DIR="/sdcard/Android/data/${APP_ID}/files/packs"
LOCAL_CACHE_DIR="./offline_packs_cache"

echo "======================================================================"
echo " DUNIYA ⛺ Offline AI Research Engine — Asset & Pack Provisioner"
echo "======================================================================"

mkdir -p "${LOCAL_CACHE_DIR}"

case "${MODE}" in
  --verify)
    echo "[1/3] Checking connected Android / GrapheneOS device via adb..."
    if command -v adb >/dev/null 2>&1; then
      adb devices -l
      echo "[2/3] Verifying APK zero-network-permission airgap on device..."
      if adb shell pm list packages | grep -q "${APP_ID}"; then
        NET_PERMS=$(adb shell dumpsys package "${APP_ID}" | grep -E "android.permission.INTERNET|android.permission.ACCESS_NETWORK_STATE" || true)
        if [ -z "${NET_PERMS}" ]; then
          echo "✅ PASS: ${APP_ID} has ZERO network permissions (Kernel Airgap Verified)."
        else
          echo "⚠️ Found permissions: ${NET_PERMS}"
        fi
      else
        echo "ℹ️ ${APP_ID} not yet installed. Run ./gradlew installDebug first."
      fi
    else
      echo "ℹ️ adb not in PATH. Add Android SDK platform-tools to PATH."
    fi
    ;;

  --olmoe-7b)
    echo "Downloading OLMoE-1B-7B-0924-Instruct-Q4_K_M.gguf (4.2 GB Sparse MoE, 1B active / 7B total)..."
    MODEL_URL="https://huggingface.co/allenai/OLMoE-1B-7B-0924-Instruct-GGUF/resolve/main/olmoe-1b-7b-0924-instruct-q4_k_m.gguf"
    DEST="${LOCAL_CACHE_DIR}/olmoe-1b-7b-0924-instruct-q4_k_m.gguf"
    curl -L --progress-bar -o "${DEST}" "${MODEL_URL}"
    echo "Pushing to device ${DEVICE_PACK_DIR}..."
    adb shell mkdir -p "${DEVICE_PACK_DIR}"
    adb push "${DEST}" "${DEVICE_PACK_DIR}/"
    echo "✅ OLMoE-1B-7B-Q4_K_M installed for offline mmap streaming!"
    ;;

  --qwen3-moe-30b)
    echo "Downloading Qwen3-30B-A3B-Q4_K_M.gguf (17.8 GB Sparse MoE, 3B active / 30B total)..."
    MODEL_URL="https://huggingface.co/Qwen/Qwen3-30B-A3B-GGUF/resolve/main/qwen3-30b-a3b-q4_k_m.gguf"
    DEST="${LOCAL_CACHE_DIR}/qwen3-30b-a3b-q4_k_m.gguf"
    curl -L --progress-bar -o "${DEST}" "${MODEL_URL}"
    echo "Pushing to device ${DEVICE_PACK_DIR}..."
    adb shell mkdir -p "${DEVICE_PACK_DIR}"
    adb push "${DEST}" "${DEVICE_PACK_DIR}/"
    echo "✅ Qwen3-30B-A3B-Q4_K_M installed for offline mmap streaming!"
    ;;

  *)
    echo "Usage:"
    echo "  ./scripts/setup_offline_assets.sh --verify         Verify device & kernel airgap"
    echo "  ./scripts/setup_offline_assets.sh --olmoe-7b       Fetch & adb push OLMoE-1B-7B GGUF (4.2 GB)"
    echo "  ./scripts/setup_offline_assets.sh --qwen3-moe-30b  Fetch & adb push Qwen3-30B-A3B GGUF (17.8 GB)"
    ;;
esac
