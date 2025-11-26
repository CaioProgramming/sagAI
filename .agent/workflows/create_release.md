---
description: Automate the full release process (Version Bump -> Notes -> Build)
---

This workflow automates the entire release cycle, from determining the version number to building
the APK.

1. **Analyze & Determine Version**:
    - **Fetch History**: Run `git log -n 20 --oneline develop` and
      `git log --merges -n 3 --oneline develop`.
    - **Analyze Impact**:
        - **MAJOR**: Breaking changes, complete redesigns.
        - **MINOR**: New features, significant improvements (most common for feature releases).
        - **PATCH**: Bug fixes, internal refactoring, polish.
    - **Calculate Version**:
        - Read `version.properties`.
        - Increment the determined component (MAJOR, MINOR, or PATCH).
        - *Rule*: If MAJOR increments, reset MINOR and PATCH to 0. If MINOR increments, reset PATCH
          to 0.

2. **Update Version**:
    - Update `version.properties` with the new values.
    - *Note*: `app/build.gradle.kts` automatically reads from this file, so no manual Gradle edit is
      needed.

3. **Draft Release Notes**:
    - Follow the style guide from `.agent/workflows/create_release_notes.md`.
    - Create `docs/release_notes/release_[new_version].md`.
    - Include the "What's New" and "Bug Fixes" sections based on the git log analysis.

4. **Build Release**:
    - Run `./gradlew assembleRelease`.
    - *Note*: This might take a few minutes.

5. **Finalize**:
    - Notify the user that Release **[new_version]** is ready.
    - Provide the path to the APK: `app/build/outputs/apk/release/app-release.apk`.
    - Ask the user to review the generated release notes.
