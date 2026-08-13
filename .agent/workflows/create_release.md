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

3. **Check for version changes on SagaDatabase**:
    - Run
      `git diff origin/main...develop -- app/src/main/java/com/example/app/data/saga/SagaDatabase.kt`
    - If there are changes:
    - **Add Docs to Gitignore**:
        - Check if `docs/` is in `.gitignore`.
        - If not, ask the user if they want to add it (or specific subdirectories like `docs/archive`).
        - *Recommendation*: Generally, keep documentation in version control, but ignore large assets or temporary files.


4. **Hardcoded String Resources Check**:
    - Diff the accumulated release scope for new/changed UI code:
      `git diff origin/main...develop -- '*.kt'`.
    - Scan the added lines for user-facing string literals that didn't go through
      `stringResource(...)`/`getString(...)` — `Text("...")`, `contentDescription = "..."`,
      `Toast.makeText(..., "...", ...)`, snackbar/dialog copy, etc.
    - Skip debug-only/preview code (`features/debug/**`, `DesignSystemPreviewView.kt`,
      `DesignSystemMocks.kt`) and non-user-facing strings (`Timber.*` log messages, analytics
      event names) — those don't need resources.
    - For every real hit:
        - Add a `snake_case`-named entry to `app/src/main/res/values/strings.xml` (EN).
        - Add the matching translated entry to `app/src/main/res/values-pt-rBR/strings.xml`
          (PT-BR) — same key, natural PT-BR copy, not a literal translation.
        - Update the call site to reference `R.string.xxx` / `stringResource(R.string.xxx)`
          instead of the literal.
    - This is a real recurring gap, not a hypothetical — release 1.14.0 shipped without this
      check having ever been formalized.

5. **Check for Open PRs**:
    - Run `gh pr list --base develop --state open` to see any pending PRs targeting develop.
    - If there are open PRs:
        - List them to the user (Number and Title).
        - Ask: "Do you want to merge any of these before releasing? (Reply with PR number or 'No')"
        - If user replies with a number:
            - Run `gh pr merge [PR_NUMBER] --merge --delete-branch`.
            - Pull changes again: `git pull origin develop`.
            - Repeat the check until user says "No" or no PRs remain.
        - *Note*: a merge can conflict with work already on `develop` (two PRs independently
          touching the same function, for example). Don't resolve a conflict there by blindly
          picking one side — read both versions, understand what each was actually trying to
          fix, and check whether one already subsumes the other before deciding. Verify with
          `git diff origin/develop -- <file>` that the resolved result matches the version you
          intended to keep.

6. **Analyze & Determine Version**:
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

7. **Update Version**:
    - Update `version.properties` with the new values.
    - *Note*: `app/build.gradle.kts` automatically reads from this file, so no manual Gradle edit is
      needed.

8. **Create Release Branch**:
    - Create and checkout a new branch: `git checkout -b release/[new_version]`
    - Example: `git checkout -b release/1.5.0`
    - Commit the version changes: `git add version.properties && git commit -m "chore: bump version to [new_version]"`

9. **Draft Release Notes**:
    - Follow the style guide from `.agent/workflows/create_release_notes.md`.
    - Create `docs/release_notes/release_[new_version].md`.
    - Include the "What's New" and "Bug Fixes" sections based on the git log analysis.

10. **Create Pull Request**:
    - **Identify Core Features**: Use the git log analysis from Step 6 to identify 1-2 core features
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
    - **Push the release branch**: Run `git push origin release/[new_version]`.
    - **Execute**: Run `gh pr create --base main --head release/[new_version] --title "[Title]" --body "[Body]"`.
      *Note*: The PR must come **from the release branch**, not from `develop`. This keeps the
      release scope frozen even if new commits land on `develop` while the PR is open. Merge
      `release/[new_version]` back into `develop` separately (fast-forward) to keep it in sync —
      that merge is not what gets PR'd to `main`.
    - **Open PR**: Run `gh pr view --web` to open the PR in the browser.
    - **Notify**: Confirm the PR has been created and provide the link.

11. **Proguard / R8 Keep Rules Check**:
    - This only bites in the *release* build — R8 obfuscation/shrinking doesn't run on debug, so
      a missing keep rule stays invisible through every debug test and only breaks in production
      (silently mangled/omitted fields, reflection failures on Gson parsing, etc.). Check this
      *before* burning a build in Step 12, not after.
    - Diff for new or changed data/model classes since the last release:
      `git diff origin/main...develop --name-only -- '*.kt' | grep -E '/(model|data)/'`
      — pay special attention to anything parsed from AI/LLM JSON output
      (`core/ai/model/**`) or any new Gson-annotated class, since those rely on reflection to
      populate fields and are exactly what R8 silently strips without a keep rule.
    - Check `app/proguard-rules.pro` — new classes are already covered for free if they land
      under one of the existing wildcarded packages:
      - `com.ilustris.sagai.core.ai.model.**`
      - `com.ilustris.sagai.features.**.data.model.**`
      - `com.ilustris.sagai.features.**.domain.model.**`
      - (any class with `@SerializedName` fields is covered by the blanket
        `-keepclassmembers` rule regardless of package)
    - If a new reflection-parsed class lives *outside* those package globs, either move it
      under a covered package or add an explicit `-keep class <fqcn> { *; }` rule.
    - If in doubt, a quick sanity check: `./gradlew assembleRelease` and exercise the
      AI-response-parsing path (or inspect `app/build/outputs/mapping/release/mapping.txt` for
      the class) before distributing.

12. **Build Debug & Release**:
    - **Debug Build**: Run `./gradlew assembleDebug` for Firebase distribution.
    - **Release Build**: Run `./gradlew assembleRelease` for Google Play Console.
    - *Note*: This might take a few minutes.

13. **Distribute to Firebase**:
    - **Prepare Release Notes**: Read the content from `docs/release_notes/release_[new_version].md`.
    - **Distribute**: Run `firebase appdistribution:distribute app/build/outputs/apk/debug/app-debug.apk --app [FIREBASE_APP_ID] --groups "alpha-testers" --release-notes-file docs/release_notes/release_[new_version].md`
    - *Note*: This distributes the debug build to alpha-testers. The release build will be uploaded to Google Play Console manually.
    - *Note*: Distributing to real alpha-testers is user-visible and not easily reversible —
      confirm with the user before running this, don't fire it automatically just because the
      rest of the workflow ran cleanly.
    - **Confirm**: Notify the user that the debug build has been distributed to alpha-testers.

14. **Update FAQ**:
    - **Analyze**: Using the features and changes identified in Step 6 and 9, determine what new
      questions users might have.
    - **Generate**: Create a draft list of new/updated FAQ items (Question & Answer) relevant to
      this release.
    - **Consult**: Ask the user to provide the path to the current FAQ JSON file (default:
      `docs/faq/faq_data_en.json`).
    - **Compare**: Read the specified file. Compare the generated draft with existing entries.
    - **Refine**: Present the user with a list of suggested additions, updates, or removals.
    - **Update PT-BR FAQ**:
        - Repeat the process for the Portuguese FAQ file (default: `docs/faq/faq_data_pt.json`).
        - Ensure translations capture the same tone and accuracy.
    - **Apply**: Ask the user if they want to update the FAQ files with these changes. If yes, write
      the updated JSON to the files.

15. **Finalize**:
    - Notify the user that Release **[new_version]** is ready.
    - Run `open app/build/outputs/apk/release/` to show the APK in Finder.
    - Provide the path to the APK: `app/build/outputs/apk/release/app-release.apk`.
    - Ask the user to review the generated release notes.