---
description: Create a new Genre/Theme for the application
---

This workflow guides you through the process of adding a new theme (Genre) to the SagAI application.

1. **Analyze the Request**:
    - Identify the name of the new theme (e.g., "Cyberpunk", "Noir", "Fantasy").
    - Ask the user for any specific requirements (colors, mood, specific inspirations) if not
      provided.

2. **Create Implementation Plan**:
    - Create a new file `docs/[theme_name]_theme_plan.md`.
    - Use the structure of `docs/cowboys_theme_plan.md` as a template.
    - The plan MUST include:
        - **Core Logic**:
            - Modify `app/src/main/java/com/ilustris/sagai/features/newsaga/data/model/Genre.kt`:
              Add enum entry, properties (title, color, iconColor, background), and extensions (
              selectiveHighlight, defaultHeaderImage, colorPalette).
        - **AI & Prompts**:
            - Modify `app/src/main/java/com/ilustris/sagai/core/ai/prompts/GenrePrompts.kt`: Add
              prompts for Art Style, Cinematography, Names, and Conversation.
        - **Visual Effects**:
            - Modify `app/src/main/java/com/ilustris/sagai/ui/theme/filters/Filters.kt`: Define
              shader parameters (grain, contrast, etc.).
            - Modify `app/src/main/java/com/ilustris/sagai/ui/theme/filters/ColorTones.kt`: Add a
              new ColorTones object for the theme.
        - **Typography**:
            - Modify `app/src/main/java/com/ilustris/sagai/ui/theme/Type.kt`: Select fonts (or
              placeholders).
        - **Resources**:
            - Modify `app/src/main/res/values/strings.xml`: Add the genre title string.
            - **New Assets**: Plan for `[theme_name].png` (background) and `[theme_name]_card.png` (
              card image).

3. **Review Plan**:
    - Present the plan to the user using `notify_user`.
    - Ask for confirmation before proceeding.

4. **Execute Changes**:
    - **Code**: Apply the changes to the Kotlin files and XML as defined in the plan.
    - **Assets**: Use the `generate_image` tool to create the background and card images.
        - *Prompt for Background*: "Mobile app background for [Theme Name]
          theme, [Style Description], high quality, vertical aspect ratio."
        - *Prompt for Card*: "Card illustration for [Theme Name] theme, [Style Description], dynamic
          composition."
        - Save images to `app/src/main/res/drawable/`.

5. **Verification**:
    - Build the app (`./gradlew assembleDebug`) to ensure no compilation errors.
    - Instruct the user to verify the new theme in the "Create New Saga" screen.
