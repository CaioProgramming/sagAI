# Add Cowboys Theme ![Status: Completed](https://img.shields.io/badge/Status-Completed-success)

This plan outlines the steps to add a new "Cowboys" genre to the application, enabling users to
create sagas with a Western theme.

## User Review Required

> [!IMPORTANT]
> **Assets & Fonts**:
> - We need to confirm if `R.font.broadway` and `R.font.fondamento_regular` are suitable
    placeholders, or if we should import a specific Western font (e.g., Rye, Sancreek).
> - Placeholder images for `cowboys.png` and `cowboys_card.png` will be generated.

## Proposed Changes

### Core Logic

#### [MODIFY] [Genre.kt](file:///Users/caioferreira/AndroidStudioProjects/sagAI/app/src/main/java/com/ilustris/sagai/features/newsaga/data/model/Genre.kt)

- Add `COWBOYS` enum entry.
- **Properties**:
    - `title`: `R.string.genre_cowboys`
    - `color`: `Color(0xFF8D6E63)` (Brown 400)
    - `iconColor`: `Color.White`
    - `background`: `R.drawable.cowboys`
- **Extensions**:
    - `selectiveHighlight`: Target brown, high hue tolerance (.1f), saturation boost (1.5f).
    - `defaultHeaderImage`: `R.drawable.cowboys_card`.
    - `colorPalette`: `[color, Orange300, Brown300, DeepOrange200]`.

### AI & Prompts

#### [MODIFY] [GenrePrompts.kt](file:///Users/caioferreira/AndroidStudioProjects/sagAI/app/src/main/java/com/ilustris/sagai/core/ai/prompts/GenrePrompts.kt)

- **Art Style**: Classic Western Oil Painting (Remington/Russell style). Visible brushstrokes, warm
  golden hour lighting.
    - *Palette*: Earthy tones (Sienna, Ochre), Desert Sky Blue. Accent: Burnt Orange/Sunset Gold.
- **Cinematography**: Emphasize vastness/isolation (wide shots), low angles for heroism, focus on
  texture (grit, leather).
- **Names**: Rugged, biblical, nicknames (e.g., Jed, Silas, "Tex").
- **Conversation**: Laconic, stoic, western slang ("reckon", "howdy"), implied drawl.

### Visual Effects & Theme

#### [MODIFY] [Filters.kt](file:///Users/caioferreira/AndroidStudioProjects/sagAI/app/src/main/java/com/ilustris/sagai/ui/theme/filters/Filters.kt)

- **Shader Params**:
    - `grainIntensity`: 0.3f (Heavy grain for old canvas look)
    - `contrast`: 1.4f
    - `colorTemperature`: 0.2f (Warm)
    - `vignetteStrength`: 0.4f
- **Color Tones**: Use `CowboyColorTones.DESERT_SUNSET`.

#### [MODIFY] [ColorTones.kt](file:///Users/caioferreira/AndroidStudioProjects/sagAI/app/src/main/java/com/ilustris/sagai/ui/theme/filters/ColorTones.kt)

- Add `CowboyColorTones` object.
- `DESERT_SUNSET`: Highlight (Warm orange/gold), Shadow (Deep warm brown).

#### [MODIFY] [Type.kt](file:///Users/caioferreira/AndroidStudioProjects/sagAI/app/src/main/java/com/ilustris/sagai/ui/theme/Type.kt)

- **Header Font**: `R.font.broadway` (Placeholder - consider changing)
- **Body Font**: `R.font.fondamento_regular` (Placeholder - consider changing)

### Resources

#### [MODIFY] [strings.xml](file:///Users/caioferreira/AndroidStudioProjects/sagAI/app/src/main/res/values/strings.xml)

- Add `<string name="genre_cowboys">Cowboys</string>`

#### [NEW] [cowboys.png](file:///Users/caioferreira/AndroidStudioProjects/sagAI/app/src/main/res/drawable/cowboys.png)

- Background image (Desert landscape, sunset).

#### [NEW] [cowboys_card.png](file:///Users/caioferreira/AndroidStudioProjects/sagAI/app/src/main/res/drawable/cowboys_card.png)

- Card image (Cowboy on horse, dynamic).

## Verification Plan

### Automated Tests

- Run `./gradlew assembleDebug` to ensure no compilation errors.

### Manual Verification

1. Launch the app.
2. Go to "Create New Saga".
3. Select "Cowboys" genre.
4. **Verify Visuals**:
    - Check if the card image loads.
    - Verify the "Desert Sunset" color theme (warm browns/oranges).
    - Check the background image.
5. **Verify AI Context**:
    - Create a saga.
    - Check if the generated title/description feels "Western".
    - Start a chat and verify the narrator/NPC tone matches the "Laconic/Western" directive.
