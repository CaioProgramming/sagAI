# Pillar 3: Reviewer 🔍

**Function:** `reviewImagePrompt()`  
**File:** `ImagePrompts.kt`  
**Role:** Quality Assurance / Art Director Supervisor

---

## Purpose

The Reviewer acts as the **final safety net** before image generation, validating that the Artist's
prompt:

1. Successfully captures all **cinematography** from the Director
2. Properly adheres to **art style** rules and requirements
3. Includes **accent color** and **rich backgrounds**
4. Contains NO **technical jargon** or **banned terms**

---

## Input

The Reviewer receives:

1. **Visual Direction** (optional) - Cinematography specs from Director
2. **Art Style Validation Rules** - Genre-specific requirements
3. **Reviewer Strictness Level** - How harsh to be
4. **Final Prompt** - The Artist's output to validate

---

## Validation Framework

### Section A: Cinematography Compliance (12 checks)

**Only performed if Visual Direction was provided**

#### A1: Angle (CRITICAL if missing/wrong)

✓ Verifies angle described visually (not with degrees)

- ❌ `"low-angle 45°"` → Technical jargon
- ✅ `"captured from below at ground level, looking upward"`

#### A2: Lens (MAJOR)

✓ Validates lens as perspective (not millimeters)

- ❌ `"20mm ultra-wide lens"` → Technical jargon
- ✅ `"dramatic exaggerated perspective with looming foreground"`

#### A3: Framing (CRITICAL)

✓ **THE #1 MOST CRITICAL CHECK**
✓ Validates framing matches Director specs
✓ Ensures NO body parts described outside camera frame

- Close-up: NO legs, feet, full outfit, stance
- Medium: NO legs, feet
- Full body: All allowed

**Example Violation:**

- Director: `"CU head-shoulders"`
- Artist describes: `"athletic legs in combat boots"`
- Reviewer: **CRITICAL VIOLATION** - removes leg description

#### A4: Placement (MAJOR)

✓ Subject positioning specified (thirds, centered, anchored)

#### A5: DOF (MAJOR)

✓ Depth of field visual (not f-stops)
✓ **SKIP if style is flat/cartoon** (no bokeh in cel-shaded art)

- ❌ `"f/1.4 shallow depth of field"` → Technical jargon
- ✅ `"dreamy soft background blur, subject in sharp focus"`

#### A6: Lighting (CRITICAL if wrong)

✓ Direction and quality described (not Kelvin)

- ❌ `"5500K cool daylight"` → Technical jargon
- ✅ `"cool blue-tinted lighting"`

#### A7: Color (MAJOR)

✓ Color described as mood, not temperature

#### A8: Atmosphere + Emotion (MAJOR)

✓ Atmospheric conditions and mood present

#### A9: Environment (MAJOR)

✓ Environment with **3+ specific objects** named

- ❌ `"urban setting"` → Too vague
- ✅ `"urban alley with graffiti walls, dumpsters, fire escapes"` → 3 objects

#### A10: Perspective (MAJOR for dramatic)

✓ Perspective distortion described if critical

#### A11: Signature Detail (MAJOR if defines uniqueness)

✓ Unique unforgettable detail included

#### A12: NO Tech Jargon (MAJOR)

✓ **CRITICAL CHECK:** No f-stops, degrees, mm, or Kelvin values

- If found: **MAJOR VIOLATION** - must translate to visual language

---

### Section B: Art Style Compliance (8 checks)

#### B1: No Banned Terms (MAJOR)

✓ Genre forbidden terminology not used

- Fantasy: NO "anime", "photorealistic"
- Cyberpunk: NO "3D CGI", "modern anime"
- Horror: NO "colorful", "vibrant"

#### B2: Required Elements (CRITICAL if mandatory)

✓ Art style mandatory elements present

- Technique/medium mentioned
- Specific style characteristics included

#### B3: Anatomy Matches Style (MAJOR)

✓ Character anatomy follows style rules, not default realism

- Gorillaz style: Exaggerated proportions, noodle limbs
- Chibi style: Oversized head, stubby limbs
- Realistic style: Normal human proportions

**Example Violation:**

- Style: "Abstract dot eyes, no realistic eye details"
- Artist describes: "piercing blue eyes with detailed iris"
- Reviewer: **MAJOR VIOLATION** - corrects to abstract eyes

#### B4: Background DETAILED & ORGANIC (CRITICAL)

✓ **NO empty/plain/gradient backgrounds allowed**
✓ Environment must be rich and detailed

- ❌ `"plain grey background"` → **CRITICAL VIOLATION**
- ❌ `"simple gradient backdrop"` → **CRITICAL VIOLATION**
- ✅ `"ancient stone courtyard with moss-covered pillars, flickering torches, distant mountains"`

#### B5: Environment Has 3+ Objects (CRITICAL)

✓ At least 3 specific environmental elements named

- ❌ `"forest setting"` → Too vague
- ✅ `"dense forest with ancient gnarled trees, moss-covered stones, hanging vines"` → 3 objects

#### B6: ACCENT COLOR Strategically Used (CRITICAL)

✓ **Genre's signature color must be present**
✓ Integrated organically (lighting/environment/atmosphere)

- Fantasy: Ember gold/fiery orange glow
- Cyberpunk: Deep purple haze
- Crime: Hot pink neon reflections

#### B7: Accent Color Integrated Organically (MAJOR)

✓ Not randomly applied to character skin/hair
✓ Feels natural through environmental integration

#### B8: No Style Contradictions (MAJOR)

✓ Prompt doesn't contradict itself or art style rules

---

## Strictness Levels

### BALANCED (Default)

```
"Validates cinematography fidelity and art style compliance. 
Corrects major violations while preserving creative intent."
```

- Fixes critical issues
- Allows minor creative deviations
- Maintains artistic expression

### STRICT

```
"Enforces all rules with zero tolerance. Any deviation from 
visual direction or art style rules will be corrected."
```

- Fixes all violations
- No creative leeway
- Maximum technical accuracy

### LENIENT

```
"Reviews for critical violations only. Allows artistic 
interpretation as long as the core identity is preserved."
```

- Fixes only critical issues
- Maximum creative freedom
- Preserves artistic vision

---

## Output Format

The Reviewer returns JSON:

```json
{
  "correctedPrompt": "The validated/corrected prompt...",
  "violations": [
    {
      "type": "A3_FRAMING_VIOLATION",
      "severity": "CRITICAL",
      "description": "Close-up framing specified but prompt describes legs/feet",
      "example": "Removed: 'athletic legs in combat boots'"
    },
    {
      "type": "B4_BACKGROUND_EMPTY",
      "severity": "CRITICAL", 
      "description": "Background was plain/empty",
      "example": "Added: Urban alley with graffiti, dumpsters, fire escapes"
    }
  ],
  "changesApplied": [
    "Removed body parts outside camera frame",
    "Added detailed environmental background",
    "Integrated accent color through neon lighting",
    "Translated 'f/2.8' to 'soft background blur'"
  ],
  "wasModified": true,
  "cinematographyScore": 92,
  "artStyleScore": 88,
  "overallReadiness": "READY"
}
```

---

## Scoring System

### Cinematography Score (0-100)

Validates all 12 cinematography checks:

- **90-100:** Excellent - All elements captured accurately
- **70-89:** Good - Minor issues, mostly correct
- **50-69:** Fair - Several elements missing/incorrect
- **Below 50:** Poor - Major violations

### Art Style Score (0-100)

Validates all 8 art style checks:

- **90-100:** Excellent - Perfect style adherence
- **70-89:** Good - Minor style deviations
- **50-69:** Fair - Some style violations
- **Below 50:** Poor - Major style violations

### Overall Readiness

- **READY:** Scores above 70, no critical violations
- **NEEDS_REVIEW:** Scores 50-70, moderate issues
- **CRITICAL_ISSUES:** Scores below 50 or critical violations present

---

## Violation Types & Severities

### CRITICAL Violations (Must Fix)

- Framing mismatch (body parts outside frame)
- Empty/plain backgrounds
- Missing mandatory art style elements
- Missing accent color
- Wrong camera angle that changes composition

### MAJOR Violations (Should Fix)

- Technical jargon present (mm, f-stops, degrees, Kelvin)
- Banned terminology used
- Anatomy doesn't match style
- Lighting/perspective poorly described
- Accent color not organically integrated

### MINOR Violations (Nice to Fix)

- Creative deviations that don't break rules
- Wording improvements
- Polish suggestions

---

## Common Corrections

### 1. Framing Violations

**Before:**

```
Close-up portrait. Her athletic build and long legs are visible 
in combat boots...
```

**After:**

```
Close-up portrait filling the frame from forehead to shoulders. 
Her intense eyes narrow with determination, sharp jawline set, 
windswept dark hair...
```

### 2. Technical Jargon

**Before:**

```
Shot from low-angle 45° with 20mm ultra-wide lens, f/2.8 shallow 
DOF, lit with 5500K daylight...
```

**After:**

```
Captured from below at ground level looking upward, dramatic 
exaggerated perspective with looming foreground, subject in sharp 
focus with dreamy blurred background, cool blue-tinted lighting...
```

### 3. Empty Backgrounds

**Before:**

```
Character stands against a plain grey gradient background...
```

**After:**

```
Character positioned in a gritty urban alley cluttered with 
spray-painted graffiti walls, overturned dumpsters, tangled 
electrical wires hanging overhead...
```

### 4. Missing Accent Color

**Before:**

```
Dark cyberpunk street scene with neon signs and rain...
```

**After:**

```
Dark cyberpunk street scene bathed in deep purple atmospheric 
haze, neon signs casting violet reflections on rain-slicked 
pavement...
```

---

## Strengths

✅ **Comprehensive validation** - 20 total checks (12 cinematography + 8 art style)  
✅ **Severity-aware** - Differentiates CRITICAL vs MAJOR vs MINOR  
✅ **Automatic corrections** - Fixes violations, not just reports them  
✅ **Scoring system** - Quantifies quality (0-100)  
✅ **Detailed reporting** - Explains what was changed and why

---

## Integration with System

```
[ARTIST] creates prompt
    ↓
[REVIEWER] validates with 20 checks
    ↓
  Violations found?
    ↓           ↓
   YES         NO
    ↓           ↓
Apply      Pass through
corrections   as-is
    ↓           ↓
    └─────┬─────┘
          ↓
   Final validated prompt
          ↓
   Image generation
```

---

## Code Location

**Function:** `ImagePrompts.reviewImagePrompt()`  
**File:** `/app/src/main/java/com/ilustris/sagai/core/ai/prompts/ImagePrompts.kt`  
**Lines:** ~71-136

---

## Usage Example

```kotlin
val reviewedPrompt = reviewImagePrompt(
    visualDirection = directorOutput,
    artStyleValidationRules = GenrePrompts.validationRules(genre),
    strictness = ReviewerStrictness.BALANCED,
    finalPrompt = artistOutput
)
```

---

## Related Documentation

- [Director Pillar](./01_director_pillar.md) - Provides cinematography specs
- [Artist Pillar](./02_artist_pillar.md) - Creates prompts to validate
- [System Flow](./system_flow.md) - Complete workflow

---

**Key Takeaway:** The Reviewer is the quality gatekeeper that ensures every prompt meets all
cinematography, art style, accent color, and background requirements before image
generation—automatically fixing violations with detailed reporting.

