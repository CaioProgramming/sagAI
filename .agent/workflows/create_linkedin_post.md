---
description: Create a LinkedIn update post for Sagas
---

This workflow guides you through creating a LinkedIn update post for Sagas, following the personal
and engaging style of the "Dev Diary".

1. **Gather Context from Git**:
    - **Identify Branch**: Run `git branch --show-current` to know what we are working on.
    - **Analyze Commits**: Run `git log -n 15 --oneline` to see the latest ~15 commits on the
      current branch.
    - **Analyze Merges**: Run `git log --merges -n 2 --oneline` to see the latest 2 merges.
    - **Synthesize**: Based on these logs, identify the key features, "wins", and technical
      challenges to highlight.
    - **Ask (Optional)**: Only ask the user for clarification if the commit messages are unclear or
      if they want to highlight something specific not in the logs.
    - Ask if there are any specific screenshots or videos they plan to attach.

2. **Drafting the Post**:
    - Create a new markdown file (e.g., `docs/posts/linkedin_update_[date].md`).
    - **Tone**: Personal, enthusiastic, humble but proud. Use first-person ("I", "My"). Avoid
      corporate jargon.
    - **Structure**:
        - **Catchy Title**: E.g., "Sagas Dev Diary: [Topic] 🚀"
        - **English Version 🇺🇸**:
            - Hook: "Hey everyone! 👋"
            - Body Paragraph 1: Context/What I've been working on.
            - Body Paragraph 2: The specific challenge or goal.
            - Body Paragraph 3: The solution/result ("And honestly? It's getting there!").
            - Closing: Forward-looking statement.
            - Hashtags: #AndroidDev #JetpackCompose #GenAI #Gemini #GameDev #IndieDev #Kotlin
        - **Separator**: `---`
        - **Portuguese Version 🇧🇷**:
            - Translate the English version, maintaining the same casual and enthusiastic tone (not
              a literal robotic translation). Use terms like "E aí, pessoal!", "Tá ficando show",
              etc.

3. **Review**:
    - Present the draft to the user using `notify_user`.
    - Ask for feedback on the tone and content.

4. **Finalize**:
    - Make any requested edits.
    - Remind the user to attach the media when posting.
