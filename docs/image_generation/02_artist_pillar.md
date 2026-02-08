# Pillar 2: Artist 🎨

**Function:** `iconDescription()`  
**File:** `SagaPrompts.kt`  
**Role:** World-Class Art Director & Concept Artist

---

## Purpose

The Artist transforms technical cinematography specs from the Director into a compelling, artistic
image description that honors:

1. The **Art Style** (genre-specific aesthetic)
2. The **Cinematography** (translated from technical specs)
3. The **Character** (personality-driven, expressive, framing-aware)

---

## Input Sources

The Artist receives:

### 1. Art Style Mandate (from GenrePrompts)

- Genre-specific technique (oil painting, anime cel, pixel art, etc.)
- Color palette and mandatory accent color
- Lighting style and mood
- Rendering constraints (what's allowed/forbidden)
- Background requirements

### 2. Visual Direction (from Director)

- 15 cinematography parameters in technical language
- Camera angle (degrees), lens (mm), lighting (Kelvin), etc.

### 3. Creative Brief

- Character information (identity, personality, traits)
- Saga context and narrative elements
- Visual references (cast/costume department)

---

## The Translation Layer

**Critical Component:** The Artist MUST NOT use technical jargon in final output.

### Translation Examples

#### Camera Angle

❌ **Technical:** `Shot from low-angle 45° with camera tilted`  
✅ **Visual:**
`Captured from below at ground level, looking upward, character towers overhead with commanding presence`

#### Focal Length

❌ **Technical:** `Ultra-wide 20mm lens with barrel distortion`  
✅ **Visual:**
`Dramatic exaggerated perspective with foreground elements looming large, space stretched between near and far`

❌ **Technical:** `Telephoto 200mm compression`  
✅ **Visual:** `Flattened layers stacked together, background brought close to subject`

#### Lighting

❌ **Technical:** `Hard top-right 45° side light at 5500K cool temperature`  
✅ **Visual:**
`Harsh lighting striking from above and right, casting sharp angular shadows with cool blue-tinted ambience`

#### Depth of Field

⚠️ **Important:** Only apply if art style supports depth/blur (skip for flat/cartoon styles)

❌ **Technical:** `Shallow f/2.8 with subject isolation`  
✅ **Visual:** `Subject in sharp focus with background softly blurred, creating dreamy separation`

#### Environment

❌ **Vague:** `Urban setting with architecture`  
✅ **Specific:**
`Gritty urban alley cluttered with spray-painted graffiti walls, overflowing dumpsters, and rusty fire escape ladders`

**Rule:** Name at least 3 SPECIFIC objects/elements

---

## Three-Part Output Structure

The Artist creates prompts following this **MANDATORY** structure:

### PART 1: Art Style Statement (1 sentence)

Establishes the visual language immediately.

**Examples:**

- `A gritty Gorillaz-style urban illustration with bold black ink outlines and flat cel shading.`
- `A vibrant 1980s anime cel with retro color palette and hard-edged shading.`
- `A moody charcoal sketch with heavy cross-hatching and dramatic contrast.`

---

### PART 2: Cinematography Framework (2-3 sentences)

**⚠️ MANDATORY if Visual Direction provided**

Explicitly covers:

1. Camera angle/position (translated from degrees)
2. Framing type (must match Director specs)
3. Lighting direction & quality (translated from Kelvin/f-stops)
4. Environmental setting with 3+ specific objects

**Example:**

```
Captured from below at ground level, looking upward at the character who 
towers overhead with commanding presence. Full body composition with 
subject anchored at the bottom edge of frame. Harsh cool-toned neon 
lighting strikes from above, casting sharp angular shadows across the 
scene. Set in a gritty urban alley cluttered with spray-painted graffiti 
walls, overturned trash cans, and tangled electrical wires.
```

**Requirements:**
✓ These 2-3 sentences must be SEPARATE and PROMINENT  
✓ Use Translation Guide - NO technical jargon (degrees, mm, f-stops)  
✓ Describe visual effects, not camera settings  
✓ Name at least 3 specific environmental objects  
✓ Skip DOF if art style is flat/cartoon (no bokeh in cel-shaded art)

---

### PART 3: Character Description (remaining content)

Describe the character with rich detail, filtered through:

- **Art Style compliance** (anatomy matches genre style)
- **Framing awareness** (only describe what's visible)
- **Personality expression** (no static, neutral poses)

#### Must Include:

✓ **Physical Features** (filtered by camera framing)

- Portrait/Close-up: Face, eyes, hair, expression, neck only
- Medium shot: Add torso, arms, hands, upper body
- Full body: Complete body, outfit, stance, all limbs

✓ **Facial Expression** (matching personality + moment)

- NOT generic stoic stare
- Show tangible emotion: sly smirk, grim scowl, raised eyebrow sneer

✓ **Body Language/Posture** (revealing character traits)

- NOT standing like a mannequin
- Chest out (confident), hunched (insecure), coiled tension (aggressive)

✓ **Hand Position/Gesture** (adding life)

- NOT arms hanging limply
- Hands in pockets (casual), arms crossed (defensive), pointing (commanding)

✓ **Gaze Direction** (conveying intention)

- At viewer: confrontational, challenging
- Away/aside: contemplative, evasive
- Down: thoughtful, defeated
- Up: defiant, hopeful

---

## Critical Requirements

### 1. Framing-Aware Filtering

**ONLY describe what the camera sees!**

❌ **Close-up frame** describing: `long athletic legs in combat boots`  
✅ **Close-up frame** describing:
`intense eyes narrowed with determination, sharp jawline, windswept dark hair`

If the Director specified "CU head-shoulders", legs/feet/stance are INVISIBLE. Don't describe them.

---

### 2. Anatomy Compliance

**Follow the art style's anatomical rules!**

If style has:

- Abstract eyes (dots, shapes): Describe ONLY abstract eyes, NO realistic details
- Exaggerated proportions: Describe exaggerated proportions, NOT realistic anatomy
- Stylized features: Embrace the style's "imperfections"

**Example:**

- Character brief: `Tall, athletic build with piercing blue eyes`
- If Realistic style: Describe as written
- If Gorillaz style:
  `Impossibly long noodle-like limbs, lanky frame, stylized angular face with bold abstract eyes`
- If Chibi style: `Oversized head on tiny body, stubby limbs, huge expressive eyes`

---

### 3. Background Compliance (CRITICAL)

**⛔ Empty/plain backgrounds are STRICTLY FORBIDDEN**

Every artwork MUST have:
✓ Rich, detailed, organic environment  
✓ 3+ specific named objects/elements  
✓ Genre-appropriate atmospheric elements  
✓ Environmental storytelling

**Banned Forever:**
❌ 'plain background' / 'empty background' / 'solid color background'  
❌ 'gradient background' / 'simple backdrop' / 'neutral background'  
❌ 'isolated figure' / 'white void' / 'blank space'

---

### 4. Accent Color Compliance (CRITICAL)

**The signature final touch that makes genre artwork unique**

Must strategically integrate genre accent color through:

1. LIGHTING: Ambient glow, rim light, atmospheric haze
2. ENVIRONMENT: Background elements, distant lights, neon signs
3. ATMOSPHERIC: Fog tint, volumetric rays, particle effects
4. SURFACES: Reflections on wet ground/metal/glass
5. SUBTLE DETAILS: Small props, fabric accents, magical effects

**Integration Rules:**
✓ Must feel ORGANIC and naturally woven  
✓ Enhance mood and atmosphere cohesively  
✓ AVOID: Randomly painted on character  
✓ PREFER: Environmental integration that bathes scene in signature mood

**Example Accent Colors:**

- Fantasy: Ember Gold/Fiery Orange
- Cyberpunk: Deep Purple
- Crime: Hot Pink
- Heroes: Subtle Electric Blue

---

## Personality-Driven Expressiveness

**Characters must NEVER be static, neutral, or soulless.**

### Expression Matching Personality

- Confident trickster: Sly smirk, knowing grin
- Battle-hardened warrior: Grim scowl, thousand-yard stare
- Rebellious punk: Defiant sneer, raised eyebrow
- Anxious scholar: Bitten lip, furrowed brow

### Dynamic Energy

Characters should feel ALIVE even in still image:

- Weight shifted to one leg (contrapposto)
- Mid-gesture or mid-action
- Clothing/hair responding to movement/wind
- Asymmetric poses suggesting motion

**Mandatory:** Character must look CAUGHT in a moment, not posed for a portrait.

---

## Final Output Example

```
A gritty Gorillaz-style urban illustration with bold black ink outlines 
and flat cel shading. Captured from below at ground level, the figure 
looms overhead with towering presence. Medium shot framing shows head to 
waist. Harsh neon purple lighting strikes from above, casting angular 
shadows. Set in a rain-slicked cyberpunk alley with flickering holographic 
ads, scattered data cables, and distant mega-towers.

The character is a young woman with stylized angular features and bold 
abstract eyes—simple white circles with black pupils, no iris detail. Her 
lanky frame has exaggerated proportions with impossibly long limbs. She 
wears a weathered leather jacket with tech-mod patches, one shoulder 
raised in a cocky shrug. Her hand rests casually on her hip, weight 
shifted to one leg in a confident stance. A sly smirk plays across her 
face as she gazes off to the side with knowing defiance. The deep purple 
accent color bathes the scene through neon reflections on wet pavement 
and atmospheric haze swirling around her silhouette.
```

---

## Strengths

✅ **Translation expertise** - Converts technical to visual language  
✅ **Art style mastery** - Honors genre-specific aesthetics  
✅ **Character expressiveness** - Personality-driven, never static  
✅ **Framing intelligence** - Only describes what's visible  
✅ **Environmental storytelling** - Rich, detailed backgrounds

---

## Code Location

**Function:** `SagaPrompts.iconDescription()`  
**File:** `/app/src/main/java/com/ilustris/sagai/core/ai/prompts/SagaPrompts.kt`  
**Lines:** ~100-550 (extensive prompt)

---

## Related Documentation

- [Director Pillar](./01_director_pillar.md) - Provides cinematography specs
- [Reviewer Pillar](./03_reviewer_pillar.md) - Validates Artist output
- [System Flow](./system_flow.md) - Complete workflow

---

**Key Takeaway:** The Artist is the creative translator who bridges technical cinematography, genre
art styles, and character personality into a compelling unified prompt—with NO technical jargon in
the final output.

