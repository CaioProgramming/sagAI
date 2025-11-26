---
description: Create a Pull Request for the current feature branch with a cool, tech-focused description.
---

This workflow automates the creation of a Pull Request (PR) for the current branch, focusing on
technical achievements and implementation details.

1. **Context Gathering**:
    - **Current Branch**: Run `git branch --show-current` to get the current branch name.
    - **Commits**: Run `git log --oneline develop..HEAD` to see the commits made in this feature
      branch.
    - **Diff Summary**: Run `git diff --stat develop..HEAD` to see which files were modified.

2. **Generate PR Content**:
    - **Analyze**: Look at the commits and file changes. Identify the core technical achievements (
      e.g., new patterns, refactors, AI integrations).
    - **Draft Description**: Create a PR description with the following sections:
        - **Title**: A concise, imperative title (e.g., "feat: Implement AI Narrative Engine").
        - **Summary**: A brief paragraph explaining *what* this feature does.
        - **Tech Achievements 🚀**: A section highlighting the cool technical details. Use emojis. Be
          enthusiastic about the engineering.
            - Example: "Refactored the entire chat layer to use a unidirectional data flow."
            - Example: "Integrated Gemini 1.5 Flash for sub-second response times."
        - **Implementation Details 🛠️**: Bullet points of specific file changes and logic updates.
        - **Testing**: How was this verified?

3. **Create Pull Request**:
    - Construct the `gh pr create` command.
    - Use the generated Title for the `--title` flag.
    - Use the generated Description for the `--body` flag.
    - **Base Branch**: Target `develop`.
    - **Command**: `gh pr create --base develop --title "[Title]" --body "[Description]"`
    - **Review**: Show the command (or the description) to the user before running it to ensure
      the "coolness" factor is met.
