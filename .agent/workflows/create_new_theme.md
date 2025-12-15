---
description: Create a new Genre/Theme for the application
---

This workflow guides you through the process of adding a new theme (Genre) to the SagAI application.

## Pre-Flight Checklist

1. **Create Feature Branch**:
    - Create a new Git branch: `git checkout -b feature/genre/{genre_name}`
    - Example: `feature/genre/punk_rock`
    - All changes for this theme should be committed to this branch.

2. **Analyze the Request**:
    - Identify the name of the new theme (e.g., "Cyberpunk", "Noir", "Fantasy").
    - Ask the user for any specific requirements (colors, mood, specific inspirations) if not provided.

3. **Study Existing Genre Structure**:
    - Review `app/src/main/java/com/ilustris/sagai/features/newsaga/data/model/Genre.kt` to understand the current enum structure.
    - **CRITICAL**: All colors MUST use Pantone Color Codes (e.g., `Pantone 208 C - Deep Ruby Red`).
    - Ensure new theme follows the EXACT SAME STRUCTURE:
      * Primary color (Pantone code with hex equivalent)
      * Icon color (MUST be readable/legible when displayed over the primary color - typically `Color.White`, but consider contrast ratio)
      * Background drawable resource (⚠️ SEE NOTE BELOW)
      * Selective color highlights parameters
      * Default header image (⚠️ SEE NOTE BELOW)
      * Color palette (3-4 secondary colors with Pantone codes)
      * Shimmer colors
    - Document all Pantone colors with their names and hex equivalents.

**⚠️ IMPORTANT NOTE - GRAPHIC ASSETS**:
    - Graphic resources (background drawable, header image, card image) are to be added **AFTER** the initial implementation.
    - For now, use placeholder resource names following the naming convention: `R.drawable.[theme_name]`, `R.drawable.[theme_name]_card`, etc.
    - These will be generated/added in a subsequent phase once the theme structure is finalized.
    - Do NOT block implementation waiting for graphic assets.

2. **Create Implementation Plan**:
    - Create a new file `docs/[theme_name]_theme_plan.md`.
    - Use the structure of `docs/cowboys_theme_plan.md` as a template.
    - The plan MUST include:
        - **Core Logic**:
            - Modify `app/src/main/java/com/ilustris/sagai/features/newsaga/data/model/Genre.kt`:
              Add enum entry with properties (title, color, iconColor, background), and extensions (
              selectiveHighlight, defaultHeaderImage, colorPalette).
              - **COLOR STANDARD**: Define primary color using Pantone code with hex equivalent
                Example: `Color(0xFF8B2635), // Pantone 208 C - Deep Ruby Red`
              - **PALETTE STANDARD**: Include 3-4 secondary colors, all with Pantone codes
              - **SELECTIVEHIGHLIGHT**: Define tone-appropriate parameters for color filtering
        - **AI & Prompts**:
            - Modify `app/src/main/java/com/ilustris/sagai/core/ai/prompts/GenrePrompts.kt`: Add:
              - `artStyle()`: Detailed art technique, lighting, texture, color palette with Pantone codes, mood, ambience
              - `appearanceGuidelines()`: Character outfit and styling directives
              - `nameDirectives()`: Name inspiration and examples
              - `conversationDirective()`: Dialogue tone, vocabulary, narrative voice
        - **Visual Effects**:
            - Modify `app/src/main/java/com/ilustris/sagai/ui/theme/filters/Filters.kt`: Define
              shader parameters (grain, contrast, saturation, etc.) appropriate to the art style.
            - Modify `app/src/main/java/com/ilustris/sagai/ui/theme/filters/ColorTones.kt`: Add a
              new ColorTones object for the theme with theme-appropriate color grading.
        - **Typography**:
            - Modify `app/src/main/java/com/ilustris/sagai/ui/theme/Type.kt`: Select fonts
              (if needed for specific theme identity).
        - **Resources**:
            - Modify `app/src/main/res/values/strings.xml`: Add the genre title string.
              Example: `<string name="genre_punk_rock">Punk Rock</string>`
            - **New Assets (⚠️ TO BE ADDED LATER)**: 
              - Plan placeholder resource names for: `[theme_name].png` (background drawable)
              - Plan placeholder resource names for: `[theme_name]_card.png` (card illustration)
              - These graphic assets will be generated/added in a subsequent phase.
              - Do NOT delay code implementation waiting for these assets.

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

---

## Pantone Color Reference Guide

When defining colors for the new theme, follow the Pantone standard as established in existing themes:

### Color Format
- Always use: `Color(0xHEXVALUE), // Pantone XXX C - Color Name`
- Example: `Color(0xFF8B2635), // Pantone 208 C - Deep Ruby Red`

### IconColor Readability
- The `iconColor` property MUST be legible when displayed over the primary `color`.
- **Current Standard**: Most themes use `Color.White` because primary colors are dark.
- **Contrast Ratio**: Ensure WCAG AA compliance (minimum 4.5:1 contrast ratio between iconColor and primary color).
- If primary color is light, use `Color.Black` or dark variant instead of white.
- Always test visual contrast before finalizing.

### Existing Theme Pantone Colors (Reference)
- **FANTASY**: Pantone 208 C (Deep Ruby Red - 0xFF8B2635), Pantone 871 C (Mystical Gold), Pantone 18-1664 TPX (Dark Red), Pantone 17-1462 TPX (Flame Orange)
- **CYBERPUNK**: Pantone 5255 C (Dark Purple - 0xFF2E294E), Pantone Neon Blue (0xFF00F5FF), Pantone Neon Pink (0xFFFF1493), Pantone Neon Green (0xFF39FF14)
- **HORROR**: Pantone 533 C (Dark Navy - 0xFF1C2541), Pantone Black 7 C (0xFF2F2F2F), Pantone Cool Gray 9 C (0xFF708090), Pantone 268 C (Deep Violet - 0xFF4B0082)
- **HEROES**: Pantone 286 C (Classic Hero Blue - 0xFF003F88), Pantone 18-1664 TPX (Hero Red - 0xFFDC143C), Pantone 116 C (Hero Gold - 0xFFFFD700), Pantone Process Blue C (0xFF00BFFF)
- **CRIME**: Pantone 213 C (Hot Pink - 0xFFE91E63), Pantone 319 C (Miami Turquoise - 0xFF00CED1), Pantone 812 C (Vice Pink - 0xFFFF69B4), Pantone 144 C (Sunset Orange - 0xFFFFA500)
- **SHINOBI**: Pantone 518 C (Deep Plum - 0xFF5C2751), Pantone 18-1664 TPX (Blood Red - 0xFF8B0000), Pantone 5467 C (Shadow Gray - 0xFF2F4F4F), Pantone 7562 C (Ancient Gold - 0xFFB8860B)
- **SPACE_OPERA**: Pantone 3145 C (Space Teal - 0xFF0081A7), Pantone 286 C (Royal Blue - 0xFF4169E1), Pantone 354 C (Cosmic Green - 0xFF00FA9A), Pantone 17-1463 TPX (Rocket Orange - 0xFFFF4500)
- **COWBOY**: Pantone 4695 C (Saddle Brown - 0xFF8B4513), Pantone 4695 C (Desert Sand - 0xFFD2691E), Pantone 4665 C (Prairie Tan - 0xFFCD853F), Pantone 18-1142 TPX (Maroon - 0xFF800000)

