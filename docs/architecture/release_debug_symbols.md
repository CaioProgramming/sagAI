# Release debug symbols (Play Console + Crashlytics + local retrace)

After `./gradlew :app:bundleRelease`:

## 1. Mapping file (Java/Kotlin / R8)

Path: `app/build/outputs/mapping/release/mapping.txt`

- **Crashlytics:** uploaded automatically by `uploadCrashlyticsMappingFileRelease` during the release build. Stacks in Firebase should deobfuscate after a few minutes.
- **Play Console:** App bundle explorer → Downloads → **Deobfuscation file** (optional if Crashlytics is enough for you).

## 2. Deobfuscate a stack trace locally (recommended while testing sideload)

Copy logcat or Crashlytics text, then:

```bash
chmod +x scripts/deobfuscate-stacktrace.sh

# App-only frames (default) — hides Android/Kotlin/coroutines noise
pbpaste | ./scripts/deobfuscate-stacktrace.sh

# Full deobfuscated trace
pbpaste | ./scripts/deobfuscate-stacktrace.sh --full

# Directly from device logcat, app-only
./scripts/deobfuscate-stacktrace.sh --logcat
```

Requires `ANDROID_HOME` (or `~/Library/Android/sdk`) with **cmdline-tools** (`retrace`).

**Important:** use the `mapping.txt` from the **same** `bundleRelease` you installed. Each build produces a new mapping.

## 3. Native crashes (NDK)

`app/build.gradle.kts` sets `ndk.debugSymbolLevel = SYMBOL_TABLE` on `release`.

## 4. AI failures in release

`GemmaClient.generate` records every failure to Crashlytics (`ai_data_type`, `ai_model`, `ai_attempt`) and sets `GemmaClient.lastGenerateFailure` for the generic UI error message.

Filter Crashlytics non-fatals by `GemmaClient` or custom keys above.
