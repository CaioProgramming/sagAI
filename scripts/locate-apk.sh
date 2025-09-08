#!/usr/bin/env bash
set -euo pipefail

# locate-apk.sh
# Locates the first APK for a given Android module and build variant.
# Outputs a single line to stdout in the form: apk=<absolute_or_relative_path>
# Use with GitHub Actions step outputs via: echo "apk=$APK_FILE" >> "$GITHUB_OUTPUT"
#
# Env vars (or flags) supported:
#   ANDROID_MODULE  - Gradle module that produces the APK (default: app)
#   BUILD_VARIANT   - Build variant (Debug/Release/etc). Case-insensitive (default: Debug)
#
# Flags (override envs):
#   --module <name>
#   --variant <name>
#   --quiet
#
# Exit codes:
#   1 - APK not found
#   2 - Bad usage

QUIET=0
MODULE="${ANDROID_MODULE:-app}"
VARIANT="${BUILD_VARIANT:-Debug}"

log(){ [ "$QUIET" -eq 1 ] || echo "[locate-apk] $*"; }
err(){ echo "[locate-apk][ERROR] $*" >&2; }

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

Searches for *.apk in <module>/build/outputs/apk/<variant_lower>/
Prints: apk=<path>
USAGE
      exit 0;;
    *)
      err "Unknown option: $1"; exit 2;;
  esac
done

VARIANT_LOWER=$(echo "$VARIANT" | tr '[:upper:]' '[:lower:]')
SEARCH_DIR="${MODULE}/build/outputs/apk/${VARIANT_LOWER}"
log "Module=${MODULE} Variant=${VARIANT} (lower=${VARIANT_LOWER})"
log "Searching in: ${SEARCH_DIR}"

shopt -s nullglob
APK_CANDIDATES=("${SEARCH_DIR}"/*.apk)
shopt -u nullglob

if [[ ${#APK_CANDIDATES[@]} -eq 0 ]]; then
  err "No APK found in ${SEARCH_DIR}"
  exit 1
fi

APK_FILE="${APK_CANDIDATES[0]}"
log "Found: ${APK_FILE}"

echo "apk=${APK_FILE}"
