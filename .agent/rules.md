# SagAI Project Principles & Workflows

## 🛠️ Testing & Deployment Workflow

When asked to test, build, or verify a feature:

1. **Summarize FIRST**: Provide a bulleted summary of current changes or implementations.
2. **Auto-Run Build & Logs**: Execute the following command to build, install, start, and open a
   clean, stable Logcat window (Android Studio style):
   `./gradlew installDebug && adb shell am start -n com.ilustris.sagai/.MainActivity && sleep 2 && osascript -e "tell app \"Terminal\" to do script \"adb logcat --pid=$(adb shell pidof -s com.ilustris.sagai) | grep -vE 'XGL|GraphicsEnvironment|vulkan|HWUI|ActivityThread|ApplicationLoaders|nativeloader|DecorView|InsetsController|ViewRootImpl|VRI|IDS_TAG|SessionLifecycleClient|CompatChangeReporter|LifecycleServiceBinder'\"" && osascript -e "tell app \"Terminal\" to activate"`

## 💻 Coding Standards

- **Clean Summary**: Always explain what was changed and why before performing critical actions like
  building or committing.
- **Dependency Management**: When adding new features, verify if dependencies are already present in
  `libs.versions.toml` or `build.gradle.kts`.
- **Absolute Paths**: Always use absolute paths when interacting with the file system.
