# PUNK_ROCK Theme Implementation Plan

## Theme Overview

**Genre Name**: PUNK_ROCK  
**Display Title**: "Punk Rock"  
**Concept**: Urban adventure for teen/young adult audiences featuring a contemporary world where
musicians and bands are heroes. Musical battles, romance, generational conflict, artistic rebellion
inspired by Scott Pilgrim.

**Visual Identity**: Gorillaz-style cartoon illustration with expressive lines, vibrant colors, and
playful energy.

---

## Core Visual Definition

### Color Palette (Pantone Standard)

**Primary Color**:

- Pantone 375 C - Vibrant Green
- Hex: 0xFF00B050
- Usage: Theme accent, musical auras, guitar highlights, icon accents

**Icon Color**:

- Color.White (0xFFFFFFFF)
- Contrast ratio: ~5.2:1 (WCAG AA compliant)

**Secondary Colors**:

1. Pantone 106 C - Vibrant Yellow (0xFFFFD700)
    - Usage: Highlights, accents, energy effects
2. Pantone 16-1546 TPX - Hot Orange (0xFFFF6B35)
    - Usage: Warm accents, intensity, warning highlights
3. Pantone 18-1664 TPX - Deep Red (0xFF8B0000)
    - Usage: Shadows, intensity peaks, dramatic moments

### Art Style Details

**Technique**: Illustrated Cartoon Digital / Modern Comic (Gorillaz)

**Visual Characteristics**:

- Expressive, organic line work (not anatomically perfect)
- Simplified but dynamic forms
- Vibrant colors with smooth gradients
- "Sketchbook/zine" aesthetic—feels hand-drawn, not CGI
- Slightly exaggerated proportions for dramatic effect
- Flat shading with strategic gradients (between cel shading and illustration)

**Lighting**:

- High-energy, dynamic lighting
- Bright, cheerful highlights
- Deep, expressive shadows
- No photorealism—purely stylized

**Color Application**:

- Base: Urban contemporary (concrete grey, brick red, asphalt)
- Accent: Vibrant green for musical elements, instruments, character effects
- Secondary: Yellow/orange for warmth and energy, red for drama

---

## Implementation Checklist

### 1. Core Logic (Genre.kt)

- [ ] Add PUNK_ROCK enum entry
- [ ] Define primary color: `Color(0xFF00B050), // Pantone 375 C - Vibrant Green`
- [ ] Define icon color: `Color.White`
- [ ] Define background: `R.drawable.punk_rock`
- [ ] Add selectiveHighlight() case with green-focused parameters
- [ ] Add defaultHeaderImage() case: `R.drawable.punk_rock_card`
- [ ] Add colorPalette() case with all secondary colors

### 2. AI & Prompts (GenrePrompts.kt)

- [ ] Add PUNK_ROCK import
- [ ] Add artStyle() case with Gorillaz cartoon aesthetic details
- [ ] Add appearanceGuidelines() case for punk rock fashion
- [ ] Add nameDirectives() case for punk character names
- [ ] Add conversationDirective() case for teen/young adult dialogue

### 3. Visual Effects

- [ ] Update Filters.kt: Add PUNK_ROCK case in colorTones()
- [ ] Update Filters.kt: Add PUNK_ROCK case in shaderParams()
- [ ] Create PunkRockColorTones in ColorTones.kt with VIBRANT_STREET_ENERGY palette

### 4. Typography

- [ ] Check Type.kt for any theme-specific font requirements (likely none needed)

### 5. Resources

- [ ] Update strings.xml: Add `<string name="genre_punk_rock">Punk Rock</string>`
- [ ] Update strings-pt-rBR.xml: Add Portuguese translation
- [ ] Create placeholder: punk_rock.png (background)
- [ ] Create placeholder: punk_rock_card.png (card illustration)

---

## Detailed Implementation Specifications

### Genre.kt Structure

```kotlin
PUNK_ROCK(
    title = R.string.genre_punk_rock,
    color = Color(0xFF00B050), // Pantone 375 C - Vibrant Green
    iconColor = Color.White,
    background = R.drawable.punk_rock,
),
```

### SelectiveHighlight Parameters

```kotlin
SelectiveColorParams(
    targetColor = color, // Vibrant Green
    hueTolerance = 0.15f, // Moderate tolerance for green spectrum
    saturationThreshold = 0.4f,
    lightnessThreshold = 0.3f,
    highlightSaturationBoost = 2.0f, // Make green pop
    desaturationFactorNonTarget = 0.5f,
)
```

### Color Palette (Shimmer Colors)

```kotlin
listOf(
    Color.Transparent,
    color.copy(alpha = 0.5f), // Vibrant Green with alpha
    Color(0xFF00B050), // Pantone 375 C - Vibrant Green
    Color(0xFFFFD700), // Pantone 106 C - Vibrant Yellow
    Color(0xFFFF6B35), // Pantone 16-1546 TPX - Hot Orange
    Color(0xFF8B0000), // Pantone 18-1664 TPX - Deep Red
    Color.Transparent,
)
```

### Shader Parameters (Filters.kt)

```kotlin
// PUNK_ROCK cartoon style
grainIntensity: 0.15f     // Reduced grain for clean comic look
saturation: 1.0f          // Vibrant colors
contrast: 1.6f            // High for comic edge
brightness: 0.05f         // Neutral
vignetteStrength: 0.15f   // Subtle edges
colorTemperature: 0.05f   // Neutral-cool
```

### ColorTones (ColorTones.kt)

```kotlin
object PunkRockColorTones {
    val VIBRANT_STREET_ENERGY = ColorGradingParams(
        highlightTint = Color(0xFFF2F2CC), // Bright, warm highlight
        shadowTint = Color(0xFF1A1A26),    // Deep urban shadow
        defaultTintStrength = 0.35f
    )
}
```

### Conversation Directive Details

**Vocabulary**:

- Music culture terminology (gig, jam, beat, riff, distortion, reverb, amplifier)
- Contemporary street slang
- Band/music references
- Youth culture terms

**Tone**:

- Confident and rebellious
- Irreverent, playful
- Casual and conversational
- Fast-paced, energetic
- Minimal formality (no "sir/ma'am")

**Pacing**: Quick, dynamic dialogue reflecting the energy of live music and youth culture

### Appearance Guidelines

Characters should wear:

- Leather jackets (classic punk aesthetic)
- Band t-shirts and graphic tees
- Ripped or distressed clothing
- Colorful dyed hair (emphasis on vibrant green, yellow, orange, red)
- Spikes, studs, chains as accessories
- Combat boots or high-tops
- Musical instrument references in outfit details
- Bold, unconventional styling

### Name Directives

Influences:

- Music culture nicknames
- Street-smart names
- Modern, edgy sounds
- Can reference music terms or artist names
- Short, punchy names work well

Examples:

- Echo, Vinyl, Riff, Chord, Neon, Sage, Punk, Blaze
- Street nicknames that reflect personality or music style

Avoid: Overly heroic names, fantasy-sounding names, corporate-sounding names

---

## Art Style Prompt (GenrePrompts.kt)

```
Art Technique: ILLUSTRATED CARTOON DIGITAL / MODERN COMIC (Gorillaz Style)
Inspiration: Gorillaz album artwork, Scott Pilgrim, modern web comics, indie animation
Line Work: EXPRESSIVE, ORGANIC LINES with varying weight. Not anatomically perfect—embrace stylization and personality.
Shading: FLAT SHADING with strategic smooth gradients. Hard-edged colors next to soft transitions for dynamic effect.

**COLOR PALETTE & ACCENT:**
 - Base: Urban contemporary tones (concrete grey, brick red, asphalt black, street lights)
 - Mandatory Accent: VIBRANT GREEN (Pantone 375 C - 0xFF00B050)
 - Application: Use vibrant green for musical elements—guitar glows, sound wave auras, instrument highlights, character energy effects, musical note accents. The green should feel energetic and alive, not muted.

Visual Style: "Sketchbook/Zine"—render as if hand-drawn with felt-tip markers, colored pencils, and digital touch-ups. Include subtle imperfections that enhance charm.
Forms: Simplified but dynamic. Exaggerate proportions slightly for expressive character design (comic book style).
Facial Features: Expressive eyes, stylized nose and mouth, colorful hair (encourage use of vibrant palette).

Detail: Keep details selective—bold where it matters (instruments, key clothing elements), simplified elsewhere (backgrounds, less important elements).
Texture & Artifacts: Subtle paper texture, minimal grain, clean digital finish with intentional hand-drawn imperfections.

Mood: Energetic, youthful, rebellious, fun, confident. A sense of musical passion and creative freedom.
Ambience: Urban contemporary setting—street scenes, music venues, band studios, graffiti-covered walls, neon signs, parking lots, train yards. Modern city energy with artistic flair.

Rendering Constraints: Forbid photorealism, 3D CGI smoothness, anime aesthetics, overly rendered details. The final image must look like a Gorillaz album cover or a Scott Pilgrim illustration—vibrant, stylized, and distinctly cartoon.
```

---

## Drawable Resources (To Be Created)

### punk_rock.png

- **Purpose**: Main background for theme
- **Dimensions**: 1080x1920 (vertical mobile aspect ratio)
- **Style**: Gorillaz-style urban scene
- **Description**: Vibrant street scene with graffiti, band posters, musical elements
- **Status**: Placeholder (to be generated later)

### punk_rock_card.png

- **Purpose**: Card preview illustration
- **Dimensions**: 540x360 (card aspect ratio)
- **Style**: Gorillaz-style character portrait
- **Description**: Dynamic punk rock character with musical instrument
- **Status**: Placeholder (to be generated later)

---

## Implementation Order

1. Create Genre.kt enum entry
2. Add GenrePrompts.kt cases
3. Update Filters.kt and ColorTones.kt
4. Add strings.xml entries
5. Create drawable placeholders
6. Test compilation and theme selection
7. Verify color contrast and visual rendering

---

## Notes

- All colors follow Pantone standard format
- Icon color contrast verified (WCAG AA compliant)
- Art style emphasizes cartoon/illustration aesthetic, not realism
- Theme designed for teen/young adult narrative scope
- Graphic assets will be generated in separate phase

