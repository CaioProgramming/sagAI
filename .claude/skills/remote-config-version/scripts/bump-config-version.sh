#!/usr/bin/env bash
set -euo pipefail

# bump-config-version.sh <firebase-project> [template.json]
#
# Prints the next Remote Config template version for <firebase-project>: MAJOR.MINOR of the app
# (version.properties) plus a patch that counts publishes. While the project's published
# `config_version` still carries the app's MAJOR.MINOR the patch goes up by one; on a new release
# line it starts over at 1.
#
# With a template.json it also writes that version into the file's `config_version` parameter, so
# the template about to be deployed already carries it. Nothing is published here.

PROJECT="${1:-}"
TEMPLATE="${2:-}"
[ -n "$PROJECT" ] || { echo "usage: $0 <firebase-project> [template.json]" >&2; exit 1; }
command -v firebase >/dev/null || { echo "firebase CLI not found" >&2; exit 1; }
command -v jq >/dev/null || { echo "jq not found" >&2; exit 1; }

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../.." && pwd)"
prop() { sed -n "s/^$1=//p" "$ROOT/version.properties" | tr -d '[:space:]'; }
LINE="$(prop MAJOR).$(prop MINOR)"

WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT
firebase remoteconfig:get --project "$PROJECT" -o "$WORK/current.json" >/dev/null
CURRENT="$(jq -r '.parameters.config_version.defaultValue.value // empty' "$WORK/current.json")"

if [[ "$CURRENT" == "$LINE".* ]]; then
  NEXT="$LINE.$(( ${CURRENT##*.} + 1 ))"
else
  NEXT="$LINE.1"
fi

if [ -n "$TEMPLATE" ]; then
  jq --arg v "$NEXT" '.parameters.config_version = {
    defaultValue: {value: $v},
    description: "Version of this template: app MAJOR.MINOR plus a patch that counts publishes. Bumped by the remote-config-version skill.",
    valueType: "STRING"
  }' "$TEMPLATE" > "$WORK/out.json"
  mv "$WORK/out.json" "$TEMPLATE"
fi

echo "$PROJECT: ${CURRENT:-<none>} -> $NEXT" >&2
echo "$NEXT"
