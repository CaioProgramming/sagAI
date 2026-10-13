---
name: android-device
description: Build, install and inspect the Sagas Android app on Caio's physical phone from the terminal, without Android Studio. Use whenever the task needs to connect the device, install the debug build, open a screen by deep link, read logcat, take a screenshot, pull the saga database, or free memory/disk before a heavy Gradle build. Also use before any Gradle build to check the Mac is not out of memory.
---

# Android device from the terminal

Android Studio is **not needed**. `adb` talks to the phone and Gradle builds and installs. Studio only adds a big JVM and indexing, which is what starves the machine during builds, so keep it closed while building here.

## Before any heavy build

Run `.claude/skills/android-device/scripts/preflight.sh`. It prints memory and swap pressure, how many Gradle clients are running, and warns when a build would thrash. **One Gradle invocation at a time**: a second `./gradlew` waits on the same daemon, doubles the load and times out. If the swap is full, quit heavy apps first (`osascript -e 'quit app "Android Studio"'`).

## Connect the phone

- Check: `adb devices` (the phone shows as `adb-<serial>._adb-tls-connect._tcp` over Wi-Fi).
- Not listed: on the phone, Developer options > Wireless debugging. Pair once with `adb pair <ip>:<pairing-port>` (code shown on the phone), then `adb connect <ip>:<port>`.
- Restart a stuck server: `adb kill-server && adb start-server`.
- Two adb binaries exist (`/opt/homebrew/bin/adb` and the SDK one in `~/Library/Android/sdk/platform-tools/adb`, the one Gradle uses). Use the SDK one so the server Gradle talks to is the one that is paired.
- The wireless debugging **port changes** every time the setting is toggled, so `adb connect <ip>:<port>` needs the number currently shown on the phone. Quitting Android Studio also stops the adb server it started; after that the phone may need `adb pair` again (phone: Wireless debugging > Pair device with pairing code gives `ip:port` and a 6-digit code). Plain `failed to connect` with an open port usually means the host is no longer paired.
- A USB cable is the fallback: `adb devices` lists it at once, no pairing needed.

## Build and install

- `./gradlew :app:installDebug --console=plain -q` (prints `Installed on 1 device.`).
- Compile only, to check errors: `./gradlew :app:compileDebugKotlin --console=plain 2>&1 | grep -E "^e:|BUILD"`.
- In this project a build can take 5 to 12 minutes on a loaded Mac. Run it in the background and wait on the log instead of re-launching it.
- Unit tests of the levels feature: `./gradlew :app:testDebugUnitTest --tests '*progression*'`.

## Open a screen on the phone

The app has no URL intent filters; deep links go through an extra on `MainActivity`:

`adb shell am start -n com.ilustris.sagai/.MainActivity --es deepLink "saga://character_detail/<characterId>"`

Other targets: `saga://chat/<sagaId>/false`, `saga://saga_detail/<id>`, `saga://saga_characters/<id>`, `saga://saga_chapters/<sagaId>/<chapterId>`, `saga://saga_events/<sagaId>/<eventId>`.

If the phone is locked or Caio is using it, **do not** launch anything on it (screenshots come back black and it interrupts him). Ask him for a screenshot instead.

## Logs, screenshots, data

- Logcat: `adb logcat -d -t 20000 | grep -iE "GlobalShellService|CharacterProgression|RemoteConfigService"` (the buffer is short; reproduce, then read right away).
- Screenshot: `adb exec-out screencap -p > /tmp/x.png` (then `sips -s format jpeg -Z 900` to read it).
- Saga database (about 500 MB): `adb exec-out run-as com.ilustris.sagai cat databases/SagaDatabase > SagaDatabase`, and the `-wal` and `-shm` files next to it. There is no `sqlite3` on the phone; query the copy on the Mac. Put it in the scratchpad, never in the repo.
- Never tap through the UI with guessed coordinates; use `uiautomator dump` when a tap is unavoidable.

## Clean the build folders

`.claude/skills/android-device/scripts/clean-build.sh` removes the things that actually break builds (stale KSP output, Kotlin caches, old APKs) and keeps the Gradle caches that make the next build fast. Add `--deep` for a full `app/build` wipe when something is truly stuck; the next build then starts from scratch.

## Quit Android Studio

`osascript -e 'quit app "Android Studio"'` asks it to quit and save. Then `./gradlew --stop` if the daemons are still holding memory.
