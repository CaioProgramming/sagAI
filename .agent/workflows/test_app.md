---
description: Builds and installs the debug version of the app to the connected device.
---

1. Run the following command to install the debug build:
   ```bash
   ./gradlew :app:installDebug
   ```
   **IMPORTANT**: Set `WaitMsBeforeAsync` to `2000` (2 seconds) and `SafeToAutoRun` to `true`.
   **CRITICAL**: Do NOT wait for the command to complete. Do NOT check `command_status`. Return
   immediately after sending the command to the background. The user will inform you if it fails.
