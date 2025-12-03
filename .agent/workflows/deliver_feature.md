---
description: Create a Pull Request for the current feature branch with a cool, tech-focused description.
---

This workflow automates the creation of a Pull Request (PR) for the current branch, focusing on
technical achievements and implementation details.

1. **Extract Hardcoded Strings** (Localization Check):
    - **Scan Modified Files**: Run `git diff --name-only develop..HEAD` to get list of changed
      files.
    - **Filter Kotlin/XML Files**: Focus on `.kt` and `.xml` files that may contain hardcoded
      strings.
    - **Detect Hardcoded Strings**: Scan for:
        - Kotlin: `Text("...")`, `Toast.makeText(...)`, `AlertDialog` content, ViewModel state
          strings
        - XML: `android:text="..."`, `android:hint="..."` (not using `@string/`)
    - **Exclusions**: Skip Log statements, debug messages, technical identifiers, URLs
    - **Report Findings**: If hardcoded strings are found:
        - List each string with file location and line number
        - Propose string resource names following `<context>_<purpose>_<content>` pattern
        - Generate English and Portuguese translations
        - Ask user if they want to extract them now or proceed with PR
    - **Auto-Extract** (if user confirms):
        - Add strings to `values/strings.xml` and `values-pt-rBR/strings.xml`
        - Refactor code to use `stringResource()` or `StringResourceHelper`
        - Commit changes: `git commit -am "chore: extract hardcoded strings to resources"`

2. **Context Gathering**:
    - **Current Branch**: Run `git branch --show-current` to get the current branch name.
    - **Commits**: Run `git log --oneline develop..HEAD` to see the commits made in this feature
      branch.
    - **Diff Summary**: Run `git diff --stat develop..HEAD` to see which files were modified.

3. **Generate PR Content**:
    - **Analyze**: Look at the commits and file changes. Identify the core technical achievements (
      e.g., new patterns, refactors, AI integrations).
   - **Suggest Label**: Based on the branch name (e.g., `feature/genre/shinobi` -> `genre:shinobi`),
     suggest a feature label to the user.
    - **Draft Description**: Create a PR description with the following sections:
        - **Title**: A concise, imperative title (e.g., "✦ feat: Implement AI Narrative Engine").
        - **Summary**: A brief paragraph explaining *what* this feature does.
        - **Tech Achievements 🚀**: A section highlighting the cool technical details. Use emojis. Be
          enthusiastic about the engineering.
            - Example: "Refactored the entire chat layer to use a unidirectional data flow."
            - Example: "Integrated Gemini 1.5 Flash for sub-second response times."
        - **Implementation Details 🛠️**: Bullet points of specific file changes and logic updates.
      - **Localization 🌐**: If strings were extracted, mention the localization improvements.
        - **Testing**: How was this verified?

4. **Create Pull Request**:
    - Construct the `gh pr create` command.
    - Use the generated Title for the `--title` flag.
    - Use the generated Description for the `--body` flag.
   - **Labels**: Add the `gemini-assisted` label and the feature label from the previous step.
    - **Base Branch**: Target `develop`.
   - **Command**:
     `gh pr create --base develop --title "[Title]" --body "[Description]" --label "gemini-assisted" --label "[feature-label]"`
      the "coolness" factor is met.
   - **Open PR**: After creation, run `gh pr view --web` to open the PR in the browser.
