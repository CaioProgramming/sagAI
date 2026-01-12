---
description: Create a Pull Request for the current feature branch with a cool, tech-focused description.
---

This workflow automates the creation of a Pull Request (PR) for the current branch, focusing on
technical achievements and implementation details.
! IMPORTANT REMEMBER TO ADD docs folder to .gitignore and push to the branch before procceed
! Important validate if exist any uncommited changes before procceed if has summarize the changes
and
push to the branch
! Important remove any comments in code even if they are explanations

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

2. **Documentation Consolidation** (Clean Up docs/):
    - **Scan docs/ Folder**: List all files in the `docs/` directory.
    - **Identify Related Documents**: Look for files related to the current feature/implementation:
        - Group by feature name, topic, or common keywords (e.g., "orientation", "
          character_creation", "fantasy")
        - Identify planning documents, implementation summaries, quick references, and update logs
    - **Consolidate**: For each related group:
        - Merge related documents into a single comprehensive file with clear sections:
            - **Overview**: What the feature does
            - **Implementation Details**: Technical approach and key decisions
            - **Code Changes**: Main file changes and logic updates
            - **Testing**: How it was verified
            - **Quick Reference**: Key points for future reference
        - Use naming pattern: `<feature_name>_complete.md` or `<feature_name>_implementation.md`
        - Delete the individual fragmented files after consolidation
    - **Commit Changes**: `git commit -am "docs: consolidate documentation for [feature]"`
    - **Keep Consolidated Docs for PR**: These will be used in the next step to generate the PR
      description.

3. **Context Gathering**:
    - **Current Branch**: Run `git branch --show-current` to get the current branch name.
    - **Commits**: Run `git log --oneline develop..HEAD` to see the commits made in this feature
      branch.
    - **Diff Summary**: Run `git diff --stat develop..HEAD` to see which files were modified.
   - **Documentation Context**: Read the consolidated documentation files from step 2 to understand:
       - Feature purpose and goals
       - Technical implementation details
       - Key architectural decisions
       - Testing approach

4. **Generate PR Content**:
    - **Analyze**: Look at the commits, file changes, and consolidated documentation. Identify the
      core technical achievements (
      e.g., new patterns, refactors, AI integrations).
   - **Suggest Label**: Based on the branch name (e.g., `feature/genre/shinobi` -> `genre:shinobi`),
     suggest a feature label to the user.
    - **Draft Description**: Create a PR description with the following sections:
        - **Title**: A concise, imperative title (e.g., "✦ feat: Implement AI Narrative Engine").
      - **Summary**: A brief paragraph explaining *what* this feature does (use consolidated docs
        for accurate context).
        - **Tech Achievements 🚀**: A section highlighting the cool technical details. Use emojis. Be
          enthusiastic about the engineering.
            - Example: "Refactored the entire chat layer to use a unidirectional data flow."
            - Example: "Integrated Gemini 1.5 Flash for sub-second response times."
          - Pull key achievements from the consolidated documentation
      - **Implementation Details 🛠️**: Bullet points of specific file changes and logic updates (
        reference consolidated docs).
      - **Localization 🌐**: If strings were extracted, mention the localization improvements.
          - **Testing**: How was this verified? (include testing details from docs)


5. **Create Pull Request**:
    - Construct the `gh pr create` command.
    - Use the generated Title for the `--title` flag.
    - Use the generated Description for the `--body` flag.
   - **Labels**: Add the `gemini-assisted` label and the feature label from the previous step.
    - **Base Branch**: Target `develop`.
   - **Command**:
     `gh pr create --base develop --title "[Title]" --body "[Description]" --label "gemini-assisted" --label "[feature-label]"`
      the "coolness" factor is met.
   - **Open PR**: After creation, run `gh pr view --web` to open the PR in the browser.