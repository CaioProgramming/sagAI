#!/bin/bash
# Blocks until no other Gradle invocation is running, so two sessions never build at once.
# Usage: wait-for-gradle.sh [max-minutes, default 20]. Exit 0 = free to build, 1 = still busy.
max=${1:-20}
waited=0
while [ "$(pgrep -f GradleWrapperMain | wc -l | tr -d ' ')" -gt 0 ]; do
  if [ "$waited" -ge $((max * 60)) ]; then
    echo "Gradle still busy after ${max} min; not starting a second build."
    exit 1
  fi
  [ "$waited" -eq 0 ] && echo "Another Gradle build is running; waiting for it to finish..."
  sleep 15
  waited=$((waited + 15))
done
echo "Gradle is free (waited ${waited}s)."
