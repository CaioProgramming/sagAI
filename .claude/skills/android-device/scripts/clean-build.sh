#!/bin/bash
# Frees the build output that breaks or slows builds, keeping the Gradle caches that make the next build fast.
set -e
cd "$(git rev-parse --show-toplevel)"
before=$(du -sk app/build 2>/dev/null | cut -f1)
rm -rf app/build/generated/ksp app/build/kspCaches app/build/kotlin
find app/build -name "*.apk" -delete 2>/dev/null || true
if [ "$1" = "--deep" ]; then rm -rf app/build; fi
after=$(du -sk app/build 2>/dev/null | cut -f1)
echo "app/build: ${before:-0} KB -> ${after:-0} KB"
