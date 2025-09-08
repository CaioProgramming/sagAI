#!/usr/bin/env bash
set -euo pipefail

# build-apk.sh
# Builds an Android APK for the given module and variant using Gradle,
# then prints a single output line: apk=<path>
# If running inside GitHub Actions, it will also append this to $GITHUB_OUTPUT.
#
# Env vars (overridable via flags):
#   ANDROID_MODULE  - Gradle module (default: app)
#   BUILD_VARIANT   - Variant name like Debug or Release (default: Debug)
#
# Flags:
#   --module <name>
#   --variant <name>
#   --quiet
#   -h|--help
#
# Exit codes:
#   1 - Build failed or APK not found
#   2 - Bad usage

QUIET=0
MODULE="${ANDROID_MODULE:-app}"
VARIANT="${BUILD_VARIANT:-Debug}"

log(){ [ "$QUIET" -eq 1 ] || echo "[build-apk] $*"; }
err(){ echo "[build-apk][ERROR] $*" >&2; }

while [[ $# -gt 0 ]]; do
  case "$1" in
    --module)
      MODULE="${2:-}"; shift 2;;
    --variant)
      VARIANT="${2:-}"; shift 2;;
    --quiet)
      QUIET=1; shift;;
    -h|--help)
      cat <<USAGE
Usage: $0 [--module app] [--variant Debug] [--quiet]

Builds the Gradle task :<module>:assemble<Variant> and outputs: apk=<path>
USAGE
      exit 0;;
    *)
      err "Unknown option: $1"; exit 2;;
  esac
done

# Normalize variant for Gradle task: capitalize first letter only if simple (e.g., Debug/Release)
# If user passes something like freeDebug, we leave as-is to support flavors.
# To be safe, when the variant is all-lowercase (debug/release), capitalize first letter.
TASK_VARIANT="$VARIANT"
if [[ "$TASK_VARIANT" =~ ^[a-z]+$ ]]; then
  FIRST_UPPER="$(printf '%s' "${TASK_VARIANT:0:1}" | tr '[:lower:]' '[:upper:]')${TASK_VARIANT:1}"
  TASK_VARIANT="$FIRST_UPPER"
fi

log "Module=${MODULE} Variant=${VARIANT} (task variant=${TASK_VARIANT})"

# Ensure gradlew is executable
if [[ -f ./gradlew ]]; then
  chmod +x ./gradlew || true
fi

# Build
log "Running: ./gradlew :${MODULE}:assemble${TASK_VARIANT}"
./gradlew ":${MODULE}:assemble${TASK_VARIANT}" --stacktrace --no-daemon

# Locate the APK using the existing helper
log "Locating APK..."
# Use bash to avoid relying on executable bit of the helper script
APK_LINE="$(bash scripts/locate-apk.sh --module "${MODULE}" --variant "${VARIANT}" --quiet)" || {
  err "APK not found after build"; exit 1;
}

# Expect APK_LINE like: apk=<path>
if [[ ! "$APK_LINE" =~ ^apk=.+ ]]; then
  err "Unexpected output from locate-apk.sh: '$APK_LINE'"; exit 1
fi

APK_PATH="${APK_LINE#apk=}"
log "APK path: ${APK_PATH}"

# Emit to stdout
echo "apk=${APK_PATH}"

# Emit to GITHUB_OUTPUT if available
if [[ -n "${GITHUB_OUTPUT:-}" ]]; then
  echo "apk=${APK_PATH}" >> "$GITHUB_OUTPUT"
fi
