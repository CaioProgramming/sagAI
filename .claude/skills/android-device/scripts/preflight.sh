#!/bin/bash
# Is the Mac in shape to run a Gradle build? Memory, swap, load and stray Gradle clients.
swap=$(sysctl -n vm.swapusage)
load=$(uptime | sed 's/.*load averages*: //')
clients=$(pgrep -f GradleWrapperMain | wc -l | tr -d ' ')
studio=$(pgrep -f "Android Studio.app/Contents/MacOS/studio" | wc -l | tr -d ' ')
echo "swap:    $swap"
echo "load:    $load"
echo "gradle clients running: $clients"
echo "android studio running: $studio"
used=$(echo "$swap" | sed -E 's/.*used = ([0-9.]+)M.*/\1/')
total=$(echo "$swap" | sed -E 's/total = ([0-9.]+)M.*/\1/')
warn=0
if [ "$(echo "$used >= $total * 0.9" | bc -l 2>/dev/null)" = "1" ]; then echo "WARN: swap is nearly full; quit heavy apps before building"; warn=1; fi
if [ "$clients" -gt 0 ]; then echo "WARN: another Gradle invocation is running; wait for it instead of starting a second one"; warn=1; fi
if [ "$studio" -gt 0 ]; then echo "NOTE: Android Studio is open; closing it frees a lot of memory"; fi
[ "$warn" -eq 0 ] && echo "OK to build"
exit 0
