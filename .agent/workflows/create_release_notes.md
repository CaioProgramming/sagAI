---
description: Create release notes for Google Play Store
---

This workflow guides you through creating release notes for the Google Play Store, maintaining the
app's friendly and engaging voice.

1. **Smart Commit Check**:
    - Run the `.agent/workflows/smart_commit.md` workflow to ensure all changes are committed and
      summarized.

2. **Gather Context from Git**:
    - **Analyze Commits**: Run `git log -n 15 --oneline develop` to see the latest ~15 commits on
      the develop branch.
    - **Analyze Merges**: Run `git log --merges -n 2 --oneline develop` to see the latest 2 merges.
    - **Identify Changes**: Extract a list of **New Features** and **Bug Fixes** from these logs.
    - **Version**: Read `version.properties` to construct the version number (format:
      `MAJOR.MINOR.PATCH`).
    - **Theme**: Infer a "theme" for the update if possible, or ask the user.

2. **Drafting the Release Notes**:
    - Create a new markdown file (e.g., `docs/release_notes/release_[version].md`).
   - **Tone**: Exciting, concise, and slightly humorous. Use a "catchy" voice that feels like a
     friend sharing cool news. Don't be afraid of a random joke or pun if it fits.
    - **Structure**:
        - **Title**: "✦ What's New in Sagas [Version] ✨"
        - **English Version 🇺🇸**:
            - **Intro**: A quick, punchy sentence about the update. Feel free to add a small joke or
              witty remark here.
            - **Feature List**: Bullet points with emojis. Focus on the *benefit* to the user, not
              just the technical change.
                - *Bad*: "Added new Auteur logic."
                - *Good*: "🎭 **Smarter Characters**: NPCs now have deeper emotions and agency thanks
                  to our new narrative engine!"
            - **Fixes**: A quick mention of polish/fixes.
            - **Call to Action**: "Update now and start your new saga!"
        - **Separator**: `---`
        - **Portuguese Version 🇧🇷**:
            - Translate the English version, adapting the tone to be natural and exciting in
              Portuguese.

3. **Review**:
    - Present the draft to the user using `notify_user`.
    - Ask for confirmation.

4. **Finalize**:
    - Remind the user to copy/paste these into the Google Play Console.
