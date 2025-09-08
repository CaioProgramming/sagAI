#!/usr/bin/env bash
set -euo pipefail

# prepare-config.sh
# Creates app/config.properties with:
#   GEMINI_KEY = "XXXXX"
#
# Inputs (priority order):
#   1) --gemini-key "value"
#   2) ENV var GEMINI_KEY
#   3) --gemini-key-b64 "value"
#   4) ENV var GEMINI_KEY_B64
#
# Options:
#   --no-assets    Do not mirror into app/src/main/assets/config.properties
#   --quiet        Reduce non-essential logs
#
# Exit codes:
#   2 - Missing key

QUIET=0
MIRROR_ASSETS=1
RAW_KEY=""
B64_KEY=""

log() { [ "$QUIET" -eq 1 ] || echo "[prepare-config] $*"; }
err() { echo "[prepare-config][ERROR] $*" >&2; }

while [[ $# -gt 0 ]]; do
  case "$1" in
    --gemini-key)
      RAW_KEY="${2:-}"; shift 2;;
    --gemini-key-b64)
      B64_KEY="${2:-}"; shift 2;;
    --no-assets)
      MIRROR_ASSETS=0; shift;;
    --quiet)
      QUIET=1; shift;;
    -h|--help)
      cat <<USAGE
Usage: $0 [--gemini-key KEY | --gemini-key-b64 KEY_B64] [--no-assets] [--quiet]

Creates app/config.properties with the exact format required by Gradle:
  GEMINI_KEY = "XXXXX"

Inputs priority: --gemini-key > GEMINI_KEY env > --gemini-key-b64 > GEMINI_KEY_B64 env
USAGE
      exit 0;;
    -*)
      err "Unknown option: $1"; exit 2;;
    *)
      shift;;
  esac
done

# Fallbacks from environment
if [[ -z "${RAW_KEY}" ]]; then
  RAW_KEY="${GEMINI_KEY:-}"
fi
if [[ -z "${B64_KEY}" ]]; then
  B64_KEY="${GEMINI_KEY_B64:-}"
fi

# Prefer raw when available; otherwise decode base64
if [[ -z "${RAW_KEY}" && -n "${B64_KEY}" ]]; then
  if ! RAW_KEY="$(printf '%s' "${B64_KEY}" | base64 -d 2>/dev/null)"; then
    err "Failed to decode GEMINI_KEY_B64. Ensure it is valid base64."; exit 2
  fi
fi

if [[ -z "${RAW_KEY}" ]]; then
  err "GEMINI_KEY is empty. Provide via --gemini-key, GEMINI_KEY env, --gemini-key-b64, or GEMINI_KEY_B64 env.";
  exit 2
fi

mkdir -p app
CONFIG_FILE="app/config.properties"
{
  echo "GEMINI_KEY = \"${RAW_KEY}\"";
} > "${CONFIG_FILE}"
chmod 600 "${CONFIG_FILE}"
log "Wrote ${CONFIG_FILE}"
log "Path: $(ls -l "${CONFIG_FILE}")"

if [[ "${MIRROR_ASSETS}" -eq 1 ]]; then
  mkdir -p app/src/main/assets
  cp "${CONFIG_FILE}" app/src/main/assets/config.properties
  chmod 600 app/src/main/assets/config.properties || true
  log "Mirrored to app/src/main/assets/config.properties"
fi

# Sanitized preview (no secrets)
log "Sanitized preview:"
awk -F'=' '{ gsub(/ /,"" ); key=$1; if (length(key)>0) print key " = \"***\"" }' "${CONFIG_FILE}" || true

# Extra listing for debugging
log "app folder listing:"
ls -la app || true
