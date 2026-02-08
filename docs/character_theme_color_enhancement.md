# Character Theme Color Enhancement

**Date:** January 15, 2026  
**Feature:** hexColor as Visual Identity Guide

## Overview

Enhanced the character generation system to use the `hexColor` field not just as a UI identifier in
the chat, but as a visual theme that influences the character's appearance during generation.

## Implementation

### 1. **Color Generation Flow**

- The `hexColor` is generated randomly using `getRandomColorHex()` **before** character generation
- This ensures the color is available to guide the AI during character creation
- The same color is stored in the Character entity for UI consistency

### 2. **Prompt Enhancement**

Updated `CharacterPrompts.characterGeneration()` to accept an optional `themeColor` parameter that
adds a new section to the prompt:

```
## 🎨 CHARACTER THEME COLOR: #hexValue 🎨
This character has a signature theme color that should influence their visual identity.
**USE THIS COLOR AS A GUIDE** for impactful details in their appearance:
  • Hair color or highlights
  • Eye color or unique iris features
  • Dominant outfit color or accent pieces
  • Accessories (scarves, jewelry, tech devices)
  • Carried items or weapons
  • Cyberware LED lights or accents (for cyberpunk)

**This is NOT mandatory for skin tone** - skin should reflect their ethnicity naturally.
**This is a THEME, not an obligation** - use it where it makes sense, creating visual cohesion.
If the color doesn't fit the character's aesthetic or story, you can use it subtly or as accent.
```

### 3. **Code Changes**

#### CharacterUseCaseImpl.kt

```kotlin
override suspend fun generateCharacter(
    sagaContent: SagaContent,
    description: String,
): RequestResult<Character> = executeRequest {
    val bannedNames = repository.getAllCharacterNames()
    // Generate theme color first to pass to AI for appearance guidance
    val themeColor = getRandomColorHex()
    val prompt = CharacterPrompts.characterGeneration(
        sagaContent,
        description,
        bannedNames,
        themeColor, // ← Now passed to prompt
    )
    Log.d(
        javaClass.simpleName,
        "generateCharacter: Starting character generation with theme color $themeColor..."
    )
    // ... rest of generation
    insertCharacter(
        newCharacter.copy(
            hexColor = themeColor, // ← Uses the same generated color
            // ...
        ),
    )
}
```

## Benefits

### ✅ Visual Cohesion

- Characters now have a signature color that makes them instantly recognizable
- The theme color creates visual consistency between the UI (chat bubbles) and the character's
  appearance

### ✅ Character Uniqueness

- Adds another layer of distinctiveness to each character
- The color becomes part of their visual identity (like iconic characters in media)

### ✅ Flexible Implementation

- The color is a **guide, not a mandate** - AI can use it subtly or prominently
- Works naturally with different genres and character types
- Respects character aesthetics (e.g., won't force a bright pink on a gritty mercenary unless it
  fits)

### ✅ Examples of Usage

- **Hair:** A character with #8B2635 (deep ruby red) might have crimson highlights or full red hair
- **Eyes:** #0081A7 (space teal) could translate to teal eyes or cybernetic eye glow
- **Outfit:** #E91E63 (hot pink) might appear as a signature pink jacket or accessories
- **Cyberware:** For cyberpunk characters, LED accents can match the theme color
- **Weapons/Items:** A character's signature weapon or tool can feature the color

## Genre Integration

### Cyberpunk

- Theme color can be used for cyberware LED lights, holographic displays, or neon accents
- Works particularly well for eye implants and interface colors

### Fantasy

- Can represent magical auras, enchanted items, or heraldic colors
- Natural for hair/eye colors in fantasy settings

### Medieval/Renaissance

- Heraldic colors for noble characters
- Signature clothing or accessory colors

### Modern/Realistic

- Fashion choices, favorite colors, signature items
- More subtle application but still creates visual identity

## Technical Details

- **Color Generation:** Uses `getRandomColorHex()` which ensures colors are:
    - Vibrant and visible (not too dark or too light)
    - Avoid pure black/white
    - Have good contrast for UI elements

- **AI Flexibility:** The prompt explicitly states this is a guide, allowing the AI to:
    - Use it subtly if the character's aesthetic demands it
    - Apply it prominently if it fits naturally
    - Ignore it for skin tone (respects natural ethnicity)

## Future Enhancements

- Could allow users to manually specify theme colors during character creation
- Could analyze existing characters to ensure color diversity across the cast
- Could use complementary colors for related characters (families, factions)
- Could tie theme colors to character personality or role

## Related Files

- `/app/src/main/java/com/ilustris/sagai/features/characters/data/usecase/CharacterUseCaseImpl.kt`
- `/app/src/main/java/com/ilustris/sagai/core/ai/prompts/CharacterPrompts.kt`
- `/app/src/main/java/com/ilustris/sagai/ui/theme/utils/ColorUtils.kt`
- `/app/src/main/java/com/ilustris/sagai/features/characters/data/model/Character.kt`

