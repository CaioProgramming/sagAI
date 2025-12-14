---
description: Automate the full release process (Version Bump -> Notes -> Build)
---

This workflow automates the entire release cycle, from determining the version number to building
the APK.

1. **Smart Commit Check**:
    - Run the `.agent/workflows/smart_commit.md` workflow to ensure all changes are committed and summarized.

2. **Sync with Develop**:
    - Ensure we are on the develop branch: `git checkout develop`
    - Pull the latest changes: `git pull origin develop`

3. **Check for Open PRs**:
    - Run `gh pr list --base develop --state open` to see any pending PRs targeting develop.
    - If there are open PRs:
        - List them to the user (Number and Title).
        - Ask: "Do you want to merge any of these before releasing? (Reply with PR number or 'No')"
        - If user replies with a number:
            - Run `gh pr merge [PR_NUMBER] --merge --delete-branch`.
            - Pull changes again: `git pull origin develop`.
            - Repeat the check until user says "No" or no PRs remain.

4. **Analyze & Determine Version**:
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

5. **Update Version**:
    - Update `version.properties` with the new values.
    - *Note*: `app/build.gradle.kts` automatically reads from this file, so no manual Gradle edit is
      needed.

6. **Draft Release Notes**:
    - Follow the style guide from `.agent/workflows/create_release_notes.md`.
    - Create `docs/release_notes/release_[new_version].md`.
    - Include the "What's New" and "Bug Fixes" sections based on the git log analysis.

7. **Build Debug & Release**:
    - **Debug Build**: Run `./gradlew assembleDebug` for Firebase distribution.
    - **Release Build**: Run `./gradlew assembleRelease` for Google Play Console.
    - *Note*: This might take a few minutes.

8. **Distribute to Firebase**:
    - **Prepare Release Notes**: Read the content from `docs/release_notes/release_[new_version].md`.
    - **Distribute**: Run `firebase appdistribution:distribute app/build/outputs/apk/debug/app-debug.apk --app [FIREBASE_APP_ID] --groups "alpha-testers" --release-notes-file docs/release_notes/release_[new_version].md`
    - *Note*: This distributes the debug build to alpha-testers. The release build will be uploaded to Google Play Console manually.
    - **Confirm**: Notify the user that the debug build has been distributed to alpha-testers.

9. **Finalize**:
    - Notify the user that Release **[new_version]** is ready.
    - Run `open app/build/outputs/apk/release/` to show the APK in Finder.
    - Provide the path to the APK: `app/build/outputs/apk/release/app-release.apk`.
    - Ask the user to review the generated release notes.

10. **Create Pull Request**:
    - **Identify Core Features**: Use the git log analysis from Step 1 to identify 1-2 core features
      or major improvements.
    - **Construct Title**: `✦ Release [Version] - [Core Feature 1] & [Core Feature 2]`
    - **Construct Body**:
        ```markdown
        ## 🚀 Release [Version]
        
        ### ✨ New Features
        - [Feature 1]
        - [Feature 2]
        
        ### 🛠 Improvements
        - [Improvement 1]
        
        ### 🐛 Bug Fixes
        - [Fix 1]
        ```
    - **Execute**: Run `gh pr create --base main --head develop --title "[Title]" --body "[Body]"`
    - **Open PR**: Run `gh pr view --web` to open the PR in the browser.
    - **Notify**: Confirm the PR has been created and provide the link.
