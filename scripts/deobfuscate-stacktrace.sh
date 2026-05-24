#!/usr/bin/env bash
# Deobfuscate a release stack trace using the R8 mapping from the last bundleRelease build.
#
# Usage:
#   ./scripts/deobfuscate-stacktrace.sh [options] [mapping.txt] < stacktrace.txt
#   ./scripts/deobfuscate-stacktrace.sh --logcat
#   pbpaste | ./scripts/deobfuscate-stacktrace.sh
#
# Options:
#   --app-only   Show only com.ilustris.sagai frames (+ exception header). Default.
#   --full       Show the full deobfuscated stack trace.
#   --logcat     Read from `adb logcat -d`, extract System.err / AndroidRuntime stacks.
#
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
APP_PACKAGE="com.ilustris.sagai"
FILTER_MODE="app-only"
MAPPING=""
USE_LOGCAT=0

while [[ $# -gt 0 ]]; do
  case "$1" in
    --app-only)
      FILTER_MODE="app-only"
      shift
      ;;
    --full)
      FILTER_MODE="full"
      shift
      ;;
    --logcat)
      USE_LOGCAT=1
      shift
      ;;
    -h | --help)
      sed -n '2,15p' "$0"
      exit 0
      ;;
    *)
      MAPPING="$1"
      shift
      ;;
  esac
done

MAPPING="${MAPPING:-$ROOT/app/build/outputs/mapping/release/mapping.txt}"

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

# Pull stack-trace shaped lines from raw adb logcat (retrace cannot read log prefixes).
extract_logcat_stacks() {
  adb logcat -d 2>/dev/null | grep -E 'System\.err|AndroidRuntime' | sed -E \
    -e 's/^.*System\.err[^:]*:[[:space:]]*//' \
    -e 's/^.*AndroidRuntime[^:]*:[[:space:]]*//'
}

read_input() {
  if [[ "$USE_LOGCAT" -eq 1 ]]; then
    local extracted
    extracted="$(extract_logcat_stacks)"
    if [[ -z "$extracted" ]]; then
      echo "No System.err / AndroidRuntime stack traces in logcat." >&2
      echo "Tip: reproduce the error, then run again immediately (logcat rotates)." >&2
      return 1
    fi
    echo "$extracted"
  else
    cat
  fi
}

filter_app_only() {
  awk -v pkg="$APP_PACKAGE" '
    function is_app_frame(line) {
      return index(line, "at " pkg ".") || index(line, "at " pkg "$")
    }
    function is_exception_header(line) {
      return line ~ /^[a-zA-Z][a-zA-Z0-9.$]*Exception/ ||
        line ~ /^[a-zA-Z][a-zA-Z0-9.$]*Error/ ||
        line ~ /^Caused by:/
    }
    {
      if (is_exception_header($0)) {
        print
        next
      }
      if ($0 ~ /^[[:space:]]*at / && is_app_frame($0)) {
        print
      }
    }
  '
}

echo "Using mapping: $MAPPING (filter: $FILTER_MODE)" >&2

RAW_INPUT="$(read_input)" || exit 1

# Keep only lines retrace understands (exception headers + stack frames).
# Logcat strips the tag prefix but often leaves "at ..." without leading whitespace.
STACK_INPUT="$(printf '%s\n' "$RAW_INPUT" | awk '
  function is_stack_frame(line) {
    return line ~ /^[[:space:]]*at /
  }
  /^[a-zA-Z][a-zA-Z0-9.$]*(Exception|Error):/ { print; in_trace=1; next }
  /^Caused by:/ { print; in_trace=1; next }
  in_trace && is_stack_frame($0) { print; next }
  in_trace && /^[[:space:]]+\.\.\. / { print; next }
  in_trace && $0 !~ /^[[:space:]]/ && !is_stack_frame($0) { in_trace=0 }
')"

if [[ -z "$STACK_INPUT" ]]; then
  echo "No stack-trace lines found after parsing input." >&2
  echo "--- raw excerpt ---" >&2
  printf '%s\n' "$RAW_INPUT" | tail -20 >&2
  exit 1
fi

DEOBFUSCATED="$(printf '%s\n' "$STACK_INPUT" | "$RETRACE" "$MAPPING")"

if [[ "$FILTER_MODE" == "app-only" ]]; then
  FILTERED="$(printf '%s\n' "$DEOBFUSCATED" | filter_app_only)"
  if [[ -z "$FILTERED" ]]; then
    echo "No $APP_PACKAGE frames after deobfuscation. Showing full deobfuscated trace:" >&2
    printf '%s\n' "$DEOBFUSCATED"
    exit 0
  fi
  printf '%s\n' "$FILTERED"
else
  printf '%s\n' "$DEOBFUSCATED"
fi
