#!/usr/bin/env bash
# Deobfuscate a release stack trace using the R8 mapping from the last bundleRelease build.
#
# Usage:
#   ./scripts/deobfuscate-stacktrace.sh [mapping.txt] < stacktrace.txt
#   adb logcat -d | ./scripts/deobfuscate-stacktrace.sh
#   pbpaste | ./scripts/deobfuscate-stacktrace.sh
#
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
MAPPING="${1:-$ROOT/app/build/outputs/mapping/release/mapping.txt}"

if [[ ! -f "$MAPPING" ]]; then
  echo "Mapping not found: $MAPPING" >&2
  echo "Run: ./gradlew :app:bundleRelease" >&2
  exit 1
fi

RETRACE="${ANDROID_HOME:-$HOME/Library/Android/sdk}/cmdline-tools/latest/bin/retrace"
if [[ ! -x "$RETRACE" ]]; then
  RETRACE="${ANDROID_HOME:-$HOME/Library/Android/sdk}/tools/proguard/bin/retrace.sh"
fi
if [[ ! -x "$RETRACE" ]]; then
  echo "retrace not found. Set ANDROID_HOME or install Android cmdline-tools." >&2
  exit 1
fi

echo "Using mapping: $MAPPING" >&2
"$RETRACE" "$MAPPING"
