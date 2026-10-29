#!/usr/bin/env bash
set -euo pipefail

# promote-remote-config.sh
# Copies the Remote Config template from the homologation Firebase project to production.
#
# Usage:
#   scripts/promote-remote-config.sh --hmg <hmg-project-id> [--prd sagai-ilustris] [--apply]
#
# Without --apply it only shows a diff (dry run). Requires: firebase CLI (logged in), jq.
# The hmg template's etag is replaced with prd's current etag so the publish is accepted.

HMG=""
PRD="sagai-ilustris"
APPLY=0

while [[ $# -gt 0 ]]; do
  case "$1" in
    --hmg) HMG="$2"; shift 2 ;;
    --prd) PRD="$2"; shift 2 ;;
    --apply) APPLY=1; shift ;;
    *) echo "Unknown option: $1" >&2; exit 1 ;;
  esac
done

[ -n "$HMG" ] || { echo "Missing --hmg <project-id>" >&2; exit 1; }
command -v firebase >/dev/null || { echo "firebase CLI not found" >&2; exit 1; }
command -v jq >/dev/null || { echo "jq not found" >&2; exit 1; }

WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

firebase remoteconfig:get --project "$HMG" -o "$WORK/hmg.json" >/dev/null
firebase remoteconfig:get --project "$PRD" -o "$WORK/prd.json" >/dev/null

# Compare only what we control (ignore etag/version metadata).
strip() { jq -S 'del(.etag, .version)' "$1"; }
strip "$WORK/hmg.json" > "$WORK/hmg.cmp.json"
strip "$WORK/prd.json" > "$WORK/prd.cmp.json"

if diff -u --label "prd ($PRD)" --label "hmg ($HMG)" "$WORK/prd.cmp.json" "$WORK/hmg.cmp.json"; then
  echo "Templates are identical. Nothing to promote."
  exit 0
fi

if [ "$APPLY" -ne 1 ]; then
  echo
  echo "Dry run. Re-run with --apply to publish the hmg template to $PRD."
  exit 0
fi

read -r -p "Publish hmg template to PRODUCTION ($PRD)? [y/N] " ANSWER
[ "$ANSWER" = "y" ] || { echo "Aborted."; exit 1; }

PRD_ETAG="$(jq -r '.etag' "$WORK/prd.json")"
mkdir -p "$WORK/deploy"
jq --arg etag "$PRD_ETAG" '.etag = $etag' "$WORK/hmg.json" > "$WORK/deploy/remoteconfig.template.json"
echo '{"remoteconfig":{"template":"remoteconfig.template.json"}}' > "$WORK/deploy/firebase.json"

# Keep a local backup of what prd had before the publish (rollback).
BACKUP="remoteconfig.prd.backup.$(date +%Y%m%d-%H%M%S).json"
cp "$WORK/prd.json" "$BACKUP"
echo "Previous prd template saved to $BACKUP"

(cd "$WORK/deploy" && firebase deploy --only remoteconfig --project "$PRD")
