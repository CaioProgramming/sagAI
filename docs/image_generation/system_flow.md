# System Flow: Complete Workflow

**Last Updated:** December 16, 2025

---

## High-Level Overview

```
User Creates Saga/Character
         ↓
Reference Image (optional)
         ↓
    [DIRECTOR]
    Extracts Cinematography
         ↓
    [ARTIST]
    Creates Artistic Prompt
         ↓
    [REVIEWER]
    Validates & Corrects
         ↓
   Final Prompt
         ↓
 AI Image Generation
         ↓
   Generated Artwork
```

---

## Detailed Flow

### Step 1: Input Collection

**Entry Points:**

- `CreateSagaIconUseCase` - Saga icon generation
- `CreateCharacterImageUseCase` - Character portrait generation

**Inputs Gathered:**

- Genre (Fantasy, Cyberpunk, Horror, etc.)
- Character data (personality, traits, appearance)
- Saga context (setting, mood, narrative)
- Reference image (optional - for composition extraction)
- Character hex color (for accent color)

---

### Step 2: Director Phase 🎬

**Function:** `ImagePrompts.extractComposition()`

**IF reference image provided:**

```
Reference Image
     ↓
[AI Vision Model analyzes image]
     ↓
Extracts 15 cinematography parameters:
  • TIER 1: Angle, Lens, Framing, Placement
  • TIER 2: Lighting, Color, Environment, Mood
  • TIER 3: DOF, Atmosphere, Perspective, Texture, Time, Signature
     ↓
Technical Output (with degrees, mm, f-stops, Kelvin)
```

**IF no reference image:**

- Skip Director phase
- Artist works with genre defaults only

**Output Example:**

```
1. ANGLE: low 45°
2. LENS: 24-35mm wide
3. FRAMING: FS full-body
4. PLACEMENT: H: center, V: lower third
5. LIGHTING: side hard
6. COLOR: cool 5500K, blue-grey tones
7. ENVIRONMENT: Urban rooftop, cityscape below
8. MOOD: Epic and solitary
9. DOF: moderate f/4
10. ATMOSPHERE: clear with light haze
11. PERSPECTIVE: slight upward convergence
12. TEXTURE: digital-clean with subtle grain
13. TIME: blue-hour
14. SIGNATURE: dramatic silhouette against glowing cityscape
```

---

### Step 3: Artist Phase 🎨

**Function:** `SagaPrompts.iconDescription()`

**Receives:**

1. Genre Art Style Mandate (from `GenrePrompts.artStyle()`)
2. Visual Direction (optional - from Director)
3. Creative Brief (character/saga data)
4. Character hex color (for accent color)

**Processing:**

#### 3.1: Art Style Integration

```
GenrePrompts.artStyle(genre)
     ↓
Loads genre-specific rules:
  • Technique (oil painting, anime cel, pixel art)
  • Color palette + accent color
  • Lighting style
  • Anatomy rules
  • Background requirements
  • Banned terminology
```

#### 3.2: Translation Layer (if Visual Direction present)

```
Technical specs → Visual language

"low 45°" → "captured from below at ground level, looking upward"
"20mm" → "dramatic exaggerated perspective"
"f/2.8" → "soft dreamy background blur"
"5500K" → "cool blue-tinted lighting"
```

#### 3.3: Framing-Aware Filtering

```
Director says: "CU head-shoulders"
     ↓
Character brief has: "athletic legs, combat boots"
     ↓
Artist filters OUT: legs, boots (not visible in close-up)
     ↓
Artist focuses ON: face, expression, hair, neck, shoulders
```

#### 3.4: Three-Part Assembly

**Part 1: Art Style Statement** (1 sentence)

```
"A gritty Gorillaz-style urban illustration with bold black ink
outlines and flat cel shading."
```

**Part 2: Cinematography Framework** (2-3 sentences)

```
"Captured from below at ground level, the figure looms overhead.
Full body composition anchored at bottom edge. Harsh neon purple
lighting strikes from above casting angular shadows. Set in rain-
slicked cyberpunk alley with holographic ads, data cables, mega-towers."
```

**Part 3: Character Description** (remaining)

```
"Young woman with stylized angular features and abstract eyes—white
circles with black pupils. Exaggerated lanky proportions. Weathered
leather jacket with tech-mod patches. Cocky shrug with hand on hip,
weight shifted confidently. Sly smirk, gaze off to side with defiance.
Deep purple accent bathes scene through neon reflections and atmospheric
haze swirling around silhouette."
```

**Output:** Complete artistic prompt (300-500 tokens)

---

### Step 4: Reviewer Phase 🔍

**Function:** `ImagePrompts.reviewImagePrompt()`

**Receives:**

1. Visual Direction (optional - Director output)
2. Art Style Validation Rules (from `GenrePrompts.validationRules()`)
3. Strictness Level (BALANCED/STRICT/LENIENT)
4. Final Prompt (Artist output)

**Validation Process:**

#### 4.1: Cinematography Checks (if Visual Direction present)

```
For each of 12 parameters:
  ✓ A1: Angle described visually? (no degrees)
  ✓ A2: Lens as perspective? (no mm)
  ✓ A3: Framing matches? (no body parts outside frame)
  ✓ A4: Placement specified?
  ✓ A5: DOF visual? (skip if flat style)
  ✓ A6: Lighting direction+quality? (no Kelvin)
  ✓ A7: Color as mood?
  ✓ A8: Atmosphere+emotion?
  ✓ A9: Environment w/3+ objects?
  ✓ A10: Perspective distortion?
  ✓ A11: Signature detail?
  ✓ A12: NO tech jargon?
     ↓
Calculate cinematographyScore (0-100)
```

#### 4.2: Art Style Checks

```
✓ B1: No banned terms?
✓ B2: Required elements present?
✓ B3: Anatomy matches style?
✓ B4: Background DETAILED & ORGANIC?
✓ B5: Environment has 3+ objects?
✓ B6: ACCENT COLOR strategically used?
✓ B7: Accent color organic?
✓ B8: No contradictions?
     ↓
Calculate artStyleScore (0-100)
```

#### 4.3: Violation Handling

```
For each violation found:
     ↓
  Severity?
     ↓
  CRITICAL → Must fix
  MAJOR → Should fix
  MINOR → Nice to fix
     ↓
Apply automatic corrections
     ↓
Log to violations array
```

#### 4.4: Overall Readiness

```
cinematographyScore + artStyleScore + violations
     ↓
Determine readiness:
  • READY (scores 70+, no critical violations)
  • NEEDS_REVIEW (scores 50-70)
  • CRITICAL_ISSUES (scores <50 or critical violations)
```

**Output:** JSON with corrected prompt, violations, scores

---

### Step 5: Final Prompt Assembly

**Decision Point:**

```
reviewResult.wasModified?
     ↓           ↓
    YES         NO
     ↓           ↓
Use corrected  Use original
    prompt       prompt
     ↓           ↓
     └─────┬─────┘
           ↓
    Final Prompt
```

---

### Step 6: Image Generation

**Final Prompt sent to:**

- OpenAI DALL-E 3 (or other image generation API)
- Includes all validated elements:
    - ✓ Art style statement
    - ✓ Cinematography (if reference used)
    - ✓ Character description (expressive, framing-aware)
    - ✓ Rich background (3+ objects)
    - ✓ Accent color integration
    - ✓ NO technical jargon

**Result:** Generated artwork image

---

### Step 7: Post-Processing

- Image saved to device/cloud
- Metadata stored (prompt, genre, character)
- Displayed to user in app

---

## Example: Complete Flow

### Scenario

User creates **Cyberpunk saga** with character **"Nova - rebellious hacker"**

### Step-by-Step:

#### 1. Input

- Genre: CYBERPUNK
- Character: Nova (cocky, tech-savvy, street-smart)
- Reference image: Urban portrait, low angle, dramatic lighting
- Hex color: `#9C27B0` (deep purple)

#### 2. Director Extracts

```
ANGLE: low 30° / looking upward
LENS: 35mm normal / slight wide perspective
FRAMING: MS head-waist / upper body visible
PLACEMENT: center, lower third
LIGHTING: side hard / sharp shadows from left
COLOR: cool 5800K / blue-purple tones
ENVIRONMENT: Urban tech district / neon signs, holo-displays
MOOD: Defiant and confident
DOF: shallow f/2 / background blur
ATMOSPHERE: light mist / neon glow
PERSPECTIVE: slight upward convergence
TEXTURE: digital-clean / crisp
TIME: night / neon-lit darkness
SIGNATURE: dramatic neon rim light
```

#### 3. Artist Creates

```
A vintage 1980s anime OVA cel animation with flat shading and
hard-edged shadows. Captured from below looking upward, Nova
looms with commanding presence. Medium shot framing from head
to waist. Harsh lighting strikes from left casting sharp shadows.
Set in neon-lit tech district with holographic ad displays, data
terminals, and towering mega-buildings.

Nova is a young woman with classic 1990s anime proportions—large
expressive eyes with prominent highlights, sharp nose. Stylized
angular features with matte skin. Cocky smirk plays across her face,
one eyebrow raised in challenge. Leather jacket with luminescent
circuit patterns. Arms crossed confidently, weight shifted to right
hip in defiant stance. Gaze locked on viewer with street-smart
intensity. Deep purple accent bathes the scene through neon
reflections on wet ground and atmospheric haze swirling around her.
```

#### 4. Reviewer Validates

```json
{
  "cinematographyScore": 95,
  "artStyleScore": 92,
  "violations": [
    {
      "type": "MINOR_WORDING",
      "severity": "MINOR",
      "description": "Enhanced environmental detail",
      "example": "Added specific neon sign types"
    }
  ],
  "wasModified": true,
  "overallReadiness": "READY"
}
```

#### 5. Generate

Final validated prompt → DALL-E 3 → Cyberpunk artwork of Nova

---

## Flow Variations

### Variation 1: No Reference Image

```
Skip Director phase → Artist uses genre defaults only → Reviewer validates art style only
```

### Variation 2: Strict Mode

```
Normal flow → Reviewer uses STRICT strictness → Fixes ALL violations (even minor)
```

### Variation 3: Lenient Mode

```
Normal flow → Reviewer uses LENIENT strictness → Fixes only CRITICAL violations
```

---

## Performance Considerations

### Token Usage

- **Director:** ~200 tokens (prompt) + 300 tokens (response) = 500 tokens
- **Artist:** ~2000 tokens (prompt) + 400 tokens (response) = 2400 tokens
- **Reviewer:** ~2500 tokens (prompt) + 500 tokens (response) = 3000 tokens
- **Total per generation:** ~5900 tokens

### Timing

- Director: 3-5 seconds
- Artist: 5-8 seconds
- Reviewer: 5-8 seconds
- Image generation: 15-30 seconds
- **Total:** 28-51 seconds

---

## Error Handling

### Director Phase Fails

- Fall back to genre defaults
- Continue with Artist phase
- Log warning

### Artist Phase Fails

- Retry with simplified prompt
- If still fails, use fallback template
- Alert user

### Reviewer Phase Fails

- Use Artist output directly (bypass review)
- Log error for investigation
- Continue generation

### Image Generation Fails

- Retry up to 3 times
- Show user-friendly error
- Suggest trying different reference or description

---

## Related Documentation

- [Director Pillar](./01_director_pillar.md)
- [Artist Pillar](./02_artist_pillar.md)
- [Reviewer Pillar](./03_reviewer_pillar.md)
- [Best Practices](./best_practices.md)

---

**Key Takeaway:** The system is a pipeline where each pillar has a specific role, with built-in
validation and error handling to ensure high-quality, genre-authentic artwork every time.
