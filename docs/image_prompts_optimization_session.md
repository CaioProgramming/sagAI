# Image Prompts Optimization Session

**Date:** January 13, 2026  
**Status:** IN PROGRESS

## Overview

This document tracks the optimization work done on `ImagePrompts.kt` and related prompt engineering
for image generation.

---

## Changes Completed

### 9. NARRATIVE DNA - Story/Emotion Extraction ✅ (NEW)

**Problem:** Artworks were technically correct but lacked "soul" - characters were just
standing/posing instead of telling a story. Reference images have dynamic actions and emotional
intent that wasn't being captured.

**Example Issue:**

- Reference: Character pointing gun at viewer with aggressive confrontation
- Final Art: Character just standing in alley with hands together - technically correct framing but
  ZERO narrative

**Solution:** Added 3 new NARRATIVE DNA parameters to `extractComposition()`:

**New Parameters (11-13):**

```
11. ACTION - What is the subject DOING?
    - Specific action/gesture/movement
    - Objects being interacted with (gun, sword, cigarette)
    - Hand positions and what they're doing
    Examples: 'Pointing gun at viewer', 'Lighting cigarette', 'Mid-stride running'

12. EMOTIONAL_BEAT - The micro-story/feeling of this moment
    - Emotion being PROJECTED (not just felt)
    - Power, vulnerability, tension, anticipation?
    Examples: 'Aggressive confrontation', 'Quiet melancholy', 'Predatory anticipation'

13. TENSION_SOURCE - What creates visual/narrative tension?
    - What draws the eye and creates interest?
    - What makes it DYNAMIC vs static?
    Examples: 'Gun barrel pointed at camera', 'Contrast between pose and eyes', 'Clenched fist vs calm face'
```

**Artist Updates (artComposition):**

- Added NARRATIVE DNA section explaining how to ADAPT (not copy) reference actions
- Emphasis on preserving ENERGY and INTENT while adapting to our character's world
- Examples of action adaptation across genres

**Reviewer Updates (reviewImagePrompt):**

- New validation rule for NARRATIVE DNA (3 components)
- New violation types: `STATIC_SUBJECT_VIOLATION`, `MISSING_NARRATIVE_INTENT`
- Auto-fix patterns to inject action/emotion/tension when missing

**Prompt Structure Updated:**

```
[1] OPENING - Art style + rendering rules
[2] SUBJECTS - Character description with traits
[3] ACTION & NARRATIVE - What they're DOING (NEW - CRITICAL)
[4] FRAMING - Camera framing and visibility
[5] EXPRESSION & EMOTIONAL BEAT - Emotion being projected (ENHANCED)
[6] ENVIRONMENT - Location context
[7] LIGHTING - Direction, quality, color
[8] COMPOSITION - Placement, orientation, depth
[9] TENSION ELEMENT - Visual interest source (NEW)
[10] DETAIL - Texture, genre compliance
```

**Quality Checklist Added:**

```
✓ ACTION: Character is DOING something specific (not just standing/posing)
✓ EMOTIONAL BEAT: Clear emotional projection drives expression and body language
✓ TENSION SOURCE: Visual interest element present (weapon, gesture, contradiction)
✓ STORY MOMENT: Feels like a frame from a movie, not a static portrait
✓ ADAPTATION: Reference action/energy adapted to our character's world, not copied
```

---

### 10. 1980s OVA Art Style Enforcement ✅ (NEW)

### 11. 1980s OVA ANATOMY & LIGHTING Rules ✅ (NEW)

**Problem:** While art style was being enforced, the specific ANATOMY (eyes, face, body, hair) and
LIGHTING techniques that define authentic 1980s anime OVA were not being validated. This led to
images with correct overall style but incorrect anatomical rendering or modern lighting techniques.

**Reference Analysis (Ghost in the Shell style):**

- **EYES:** Large expressive with SHARP star-shaped catchlights, flat iris colors with 2-3 bands,
  mechanical elements visible in cyber eyes
- **FACE:** Angular jawline with hard shadows, simplified lips, sharp nose defined by simple line
- **BODY:** Matte flat-shaded skin with distinct shadow steps, aggressive flesh-chrome contrast at
  cyberware
- **HAIR:** Volumetric clumps with hard-edged highlight bands, NOT individual strands
- **LIGHTING:** High-contrast Rembrandt with HARD rim light, 2-3 shadow steps, colored shadows (
  purple/slate blue)

**Solution:** Added comprehensive ANATOMY and LIGHTING validation rules for CYBERPUNK:

**New Art Style Section Added:**

```
**1980s OVA ANATOMY RULES (CRITICAL - NON-NEGOTIABLE):**

⚠️ EYES - THE SOUL OF 80s ANIME (HIGHEST PRIORITY):
  • SHAPE: Large, expressive eyes with SIMPLIFIED geometry
  • CATCHLIGHTS: MANDATORY sharp star-shaped or geometric specular highlights (2-3 distinct white shapes)
  • IRIS: Flat color with 2-3 color bands maximum, NOT gradient blending
  • CYBER EYES: Must show VISIBLE MECHANICAL ELEMENTS—rotating lens arrays, LED grids, scanner reticles
  • BANNED: Realistic iris texture, soft pupil gradients, 'piercing eyes', 'deep brown eyes'

FACE & HEAD ANATOMY:
  • JAW: Angular, defined jawline with HARD shadow edge underneath
  • NOSE: Sharp angular nose with minimal detail, defined by 1-2 lines
  • LIPS: Simplified shape with flat color, single highlight line
  • CHEEKBONES: Defined by HARD cel-shaded shadows

BODY ANATOMY (80s OVA STYLE):
  • SKIN: MATTE flat-shaded with 2-3 distinct color steps
  • MUSCLES: Suggested through HARD-EDGED shadow shapes
  • FLESH-CHROME INTERFACE: AGGRESSIVE contrast—hard edge between matte skin and gleaming metal

HAIR ANATOMY (80s OVA SIGNATURE):
  • STRUCTURE: Hair rendered as VOLUMETRIC CLUMPS and SHAPES
  • HIGHLIGHTS: Simple highlight BANDS (1-2 per clump) with HARD EDGES
  • BANNED: 'flowing individual strands', 'realistic hair texture', 'soft hair gradients'
```

**New Lighting Rules Added:**

```
**1980s OVA LIGHTING RULES (CRITICAL - DEFINES THE ERA):**

MANDATORY LIGHTING ELEMENTS:
  • RIM LIGHT (CRITICAL): Strong HARD rim light separating character from background
  • REMBRANDT LIGHTING: High-contrast facial lighting with clear shadow/light division
  • CAST SHADOWS: Hard-edged, clearly defined cast shadows with NO soft falloff
  • SHADOW STEPS: 2-3 distinct shadow color levels, NOT gradient transitions
  • SPECULAR HIGHLIGHTS: Sharp, geometric highlights on shiny surfaces

LIGHTING COLOR TEMPERATURE:
  • PRIMARY: Cool blue-white or warm amber (NOT neutral gray)
  • SHADOWS: Deep purple, slate blue, or warm brown—NEVER pure black or gray
  • RIM LIGHT: Contrasting temperature to main light
  • CYBERWARE GLOW: Amber, cold blue, or deep purple from implants

BANNED LIGHTING TECHNIQUES:
  ✗ Global illumination / radiosity
  ✗ Ambient occlusion
  ✗ Soft shadow falloff / penumbra
  ✗ Volumetric fog/god rays (unless stylized)
  ✗ Subsurface scattering light through skin
  ✗ HDR bloom / lens flare
```

**New Validation Rules Added:**

```
⚠️⚠️⚠️ 1980s OVA ANATOMY ENFORCEMENT (CRITICAL - ZERO TOLERANCE) ⚠️⚠️⚠️:

**EYES - HIGHEST PRIORITY VALIDATION:**
REQUIRED EYE ELEMENTS (verify ALL are present):
□ 'Large expressive eyes' or '80s anime eyes'
□ 'Star catchlights' or 'sharp specular highlights'
□ 'Flat iris color' or '2-3 color bands'
□ For cyber eyes: 'mechanical iris', 'LED array', 'scanner reticle'

BANNED EYE DESCRIPTIONS (CRITICAL VIOLATION: INVALID_80S_EYE_STYLE):
- 'realistic eyes', 'natural eye color', 'detailed iris texture'
- 'piercing blue eyes', 'deep brown eyes', 'emerald green eyes'

**FACE & HEAD ANATOMY VALIDATION:**
BANNED (ANATOMY_VIOLATION):
- 'realistic skin texture', 'pores', 'skin detail'
- 'soft facial contours', 'glossy lips'

**HAIR ANATOMY VALIDATION:**
BANNED (HAIR_VIOLATION):
- 'individual hair strands', 'realistic hair'
- 'soft hair gradients', 'flowing wispy hair'

⚠️⚠️⚠️ 1980s OVA LIGHTING ENFORCEMENT (CRITICAL) ⚠️⚠️⚠️:

**LIGHTING VALIDATION CHECKLIST:**
□ RIM LIGHT: 'Hard rim light' mentioned (MANDATORY)
□ SHADOW TYPE: 'Hard shadows' or 'cel-shaded shadows'
□ CONTRAST: 'High contrast' or 'Rembrandt lighting'

BANNED LIGHTING (LIGHTING_VIOLATION):
- 'soft shadows', 'gradient shadows', 'ambient occlusion'
- 'volumetric lighting', 'god rays', 'HDR bloom'
```

**New Violation Types:**

- `INVALID_80S_EYE_STYLE` - Eyes don't follow 80s anime conventions
- `ANATOMY_VIOLATION` - Face/body doesn't match vintage anime anatomy
- `HAIR_VIOLATION` - Hair rendered with modern techniques (strands, soft gradients)
- `LIGHTING_VIOLATION` - Lighting uses modern techniques (soft shadows, GI, AO)

**New Auto-Fix Patterns:**

```
⚠️ ANATOMY FIX PATTERNS (CRITICAL):

EYES (INVALID_80S_EYE_STYLE):
- 'realistic eyes' → 'large expressive 80s anime eyes with simplified iris and sharp star-shaped catchlights'
- 'piercing blue eyes' → 'large expressive eyes with flat blue iris, 2-3 color bands, and sharp geometric catchlights'
- 'soft eye highlights' → 'sharp star-shaped specular catchlights in eyes'

FACE (ANATOMY_VIOLATION):
- 'realistic skin texture' → 'matte flat-shaded skin with hard cel shadows'
- 'soft facial contours' → 'angular facial planes with hard-edged cel-shaded shadows'
- 'glossy lips' → 'simplified lips with flat color and single highlight line'

HAIR (HAIR_VIOLATION):
- 'individual strands' → 'hair rendered as volumetric clumps with simple hard-edged highlight bands'
- 'soft hair gradient' → 'chunky hair shapes with 2-3 distinct color steps and sharp highlight bands'

⚠️ LIGHTING FIX PATTERNS (CRITICAL):
- Missing rim light → ADD: 'strong hard rim light separating character from background'
- 'soft shadows' → 'hard-edged cast shadows with 2-3 distinct shadow steps'
- 'ambient occlusion' → 'high-contrast Rembrandt lighting with hard shadows'
- Gray/black shadows → 'deep purple shadows' or 'slate blue shadows'
- Missing color temperature → ADD: 'cool blue-white main light with warm amber rim'
```

---

**Problem:** Artworks were looking too modern/clean - more like modern digital illustrations than
authentic 1980s anime OVA frames. Missing the distinctive visual markers of the era.

**Example Issue:**

- Expected: Frame from AKIRA, GHOST IN THE SHELL (1995), BUBBLEGUM CRISIS
- Getting: Modern digital art with smooth gradients and clean lines

**Solution:** Massively strengthened 80s OVA art style requirements in GenrePrompts.kt:

**New Mandatory Visual Markers (at least 5 must be present):**

```
□ 'Cel shading' / 'flat color blocks' / 'hard-edged shadows'
□ 'Film grain' / 'analog noise' / 'laserdisc quality'
□ 'Thick ink lines' / 'hand-drawn line work' / 'sketchy lines with weight variation'
□ 'Muted colors' / 'desaturated palette' / 'washed out tones'
□ 'No gradients' / 'distinct shadow steps' / '2-3 color shadow levels'
□ 'Hair as clumps/shapes' / 'hair with highlight bands' (NOT individual strands)
□ 'Matte skin' / 'flat skin shading' (NOT realistic/glossy)
□ 'Sharp specular highlights' / 'star catchlights' (NOT soft glows)
□ '1980s OVA' / '1990s anime' / 'vintage anime aesthetic'
```

**New Forbidden Modern Techniques (CRITICAL violations):**

```
✗ Soft gradients, smooth transitions, gradient shading
✗ Ambient occlusion, global illumination, volumetric lighting
✗ Subsurface scattering, realistic skin, skin texture detail
✗ Individual hair strands, realistic hair, flowing hair detail
✗ Digital painting, modern illustration, clean digital art
✗ 3D render, CGI, Unreal Engine, photorealism
✗ Over-saturated, vibrant colors, high saturation
✗ Soft shadows, gradient shadows, smooth shadow falloff
```

**New Violation Type:**

- `MISSING_80S_OVA_STYLE` - Prompt lacks 80s anime markers

**Auto-Fix Pattern for 80s Style:**

```
If prompt lacks 80s markers → INJECT: 
'Authentic 1980s anime OVA cel animation style, with flat color blocks, 
hard-edged cel-shaded shadows in 2-3 distinct steps, thick hand-drawn ink 
lines with visible weight variation, visible film grain texture, muted 
desaturated color palette, hair rendered as volumetric clumps with simple 
highlight bands, matte flat-shaded skin, sharp star-shaped specular 
catchlights, vintage laserdisc quality aesthetic'
```

**Updated REQUIRED ELEMENTS:**

```
- Art style opener: '1980s Anime OVA', 'Cel Animation', 'vintage anime'
- Shading: 'flat color blocks', 'cel-shaded', 'hard-edged shadows', '2-3 shadow steps'
- Line work: 'thick ink lines', 'hand-drawn', 'sketchy lines with weight variation'
- Texture: 'film grain', 'analog noise', 'laserdisc quality'
- Colors: 'muted', 'desaturated', 'slate blue', 'washed out tones'
- Hair: 'hair as clumps/shapes', 'simple highlight bands', 'NOT individual strands'
- Skin: 'matte flat-shaded skin', 'NO realistic texture', 'cel-shaded skin'
- Highlights: 'sharp specular highlights', 'star catchlights'
```

---

### 1. Cyberpunk Heavy Cyberware Enforcement ✅

**Problem:** Characters were being generated with subtle/invisible augmentations (silver scars,
enhanced vision, circuit tattoos) instead of heavy visible cyberware.

**Solution:**

- Updated `GenrePrompts.kt` CYBERPUNK validation rules to MANDATE heavy cyberware
- Added `INSUFFICIENT_CYBERWARE` violation type
- Added cyberware intensity scale (INSUFFICIENT → MINIMUM ACCEPTABLE → IDEAL HEAVY)
- Added auto-fix upgrade patterns

**Files Modified:**

- `GenrePrompts.kt` - CYBERPUNK validation rules (~line 1622)
- `ImagePrompts.kt` - Added violation type and auto-fix patterns

**Key Changes:**

- "ARTIST MAY ADD" → "ARTIST MUST add"
- Minimum 3 MAJOR visible cyberware elements required
- Banned subtle terms: "silver scars", "enhanced vision", "circuit tattoos", "data bracelets"
- Auto-fix patterns to upgrade subtle → heavy cyberware

---

### 2. Token Optimization - extractComposition() ✅

**Problem:** Original extraction had 18 parameters with verbose instructions (~300+ lines)

**Solution:** Reduced to 10 essential photography parameters (~50 lines)

**Parameters KEPT (photography DNA):**

1. ANGLE
2. LENS
3. FRAMING
4. PLACEMENT
5. LIGHTING
6. DOF
7. PERSPECTIVE
8. SUBJECT_ORIENTATION
9. FORM_POSTURE
10. SCALE_ZOOM

**Parameters REMOVED (genre provides these):**

- COLOR (genre defines palette)
- MOOD (genre defines emotional tone)
- ATMOSPHERE (genre defines ambience)
- TEXTURE (genre defines rendering style)
- ENVIRONMENT (genre defines setting)
- SIGNATURE (removed - too vague)
- DEPTH_LAYERS (simplified into PERSPECTIVE)
- TIME (removed - not essential)

---

### 3. Token Optimization - reviewImagePrompt() ✅

**Problem:** Original reviewer was ~330 lines with verbose redundant instructions

**Solution:** Reduced to ~80 lines with compact validation rules

**Key optimizations:**

- Removed verbose explanations, kept actionable rules
- Condensed violation types to essential ones
- Genre rules come from `artStyleValidationRules` parameter, not repeated
- Kept mandatory feedback fields

---

### 4. Improved ANGLE Detection ✅

**Problem:** AI was incorrectly detecting "eye-level" when image was clearly low-angle

**Solution:** Added explicit visual cues for angle detection:

```
LOW-ANGLE (camera below, looking UP at subject):
- Shoulders appear prominent/closer to camera than face
- Can see under chin/jaw
- Subject appears dominant, imposing, powerful

HIGH-ANGLE (camera above, looking DOWN at subject):
- Top of head/hair prominent
- Subject appears smaller, vulnerable, submissive

EYE-LEVEL:
- Neutral perspective, no vertical distortion
- Eyes roughly at center of vertical frame

⚠️ TEST: Draw line from camera to subject's eyes - is it angled UP or DOWN?
```

---

### 5. Improved SUBJECT_ORIENTATION ✅

**Problem:** Ambiguous "3/4 left-right" didn't specify direction

**Solution:** Now requires specific direction:

```
• BODY AXIS: [Front / 3/4-left / 3/4-right / Profile-left / Profile-right / Back-facing]
• HEAD: [Front / 3/4-left / 3/4-right / Profile-left / Profile-right / Over-shoulder-left / Over-shoulder-right]
• GAZE: [Direct / Away-left / Away-right / Up / Down / Closed]
(left/right = direction subject is turned TOWARD)
```

---

### 6. Improved FORM_POSTURE ✅

**Problem:** AI was outputting vague descriptions like just "Relaxed"

**Solution:** Now requires detailed description:

```
ECU/CU: head tilt (direction), neck angle, shoulder position (level/raised/tension)
Example: 'Head tilted slight right, neck extended, left shoulder raised with tension'
```

---

### 7. Mandatory Feedback Fields ✅

**Problem:** `artistImprovementSuggestions` and `visualDirectorSuggestions` were sometimes null

**Solution:** Added explicit instructions that these fields are REQUIRED and NEVER NULL:

- `artistImprovementSuggestions`: Concrete feedback for prompt writer
- `visualDirectorSuggestions`: Feedback for extraction AI improvements

---

### 8. Rendering Style Enforcement ✅

**Problem:** Final art could deviate to modern anime or smooth rendering instead of genre-specific
style (e.g., 1980s OVA for Cyberpunk)

**Solution:** Added lean rendering enforcement (token-optimized):

**Artist (4 lines):**

```
⚠️ RENDERING STYLE ENFORCEMENT (CRITICAL):
Your prompt MUST explicitly include the rendering technique from the ART STYLE above.
ALWAYS SPECIFY: shading type, shadow style, line work, color palette, texture/grain.
DO NOT default to modern anime or smooth digital rendering unless the genre specifies it.
```

**Reviewer (3 lines):**

```
7. RENDERING STYLE (CRITICAL):
   Prompt MUST specify: shading, shadows, line work, colors, texture matching genre.
   RENDERING_VIOLATION if: wrong style (e.g., modern anime for vintage OVA genre) or missing rendering specs.
```

**Added RENDERING_VIOLATION** to violations list with auto-fix pattern.

---

## Current File Stats

**ImagePrompts.kt:**

- Lines: 478
- Characters: ~33,000

**Key Functions:**

- `criticalGenerationRule()` - Full-bleed art rules
- `artComposition()` - Artist prompt generator
- `extractComposition()` - Visual direction extractor (10 params)
- `reviewImagePrompt()` - Prompt validator/fixer

---

## Pending / Future Work

### To Discuss:

- [ ] Any additional genre-specific rendering rules needed?
- [ ] Further token optimization possible?
- [ ] Test results from current implementation?

---

## Quick Reference - Current Parameter Structure

### extractComposition() - 13 Parameters:

```
=== PHOTOGRAPHY DNA (10 params) ===
1. ANGLE: [high-angle/low-angle/eye-level/dutch-tilt] 
   PRIMARY TEST: Where is subject's face pointing?
   - Face tilted UP → HIGH-ANGLE (camera above)
   - Face tilted DOWN → LOW-ANGLE (camera below)
   - Face level → EYE-LEVEL
2. LENS: [ultra-wide 14-24mm / wide 24-35mm / normal 35-50mm / portrait 50-85mm / tele 85-200mm+]
   CUES: Tight head shots + blur → portrait/tele. Wide environment → wide/normal.
3. FRAMING: [ECU/CU/MCU/MS/MWS/FS/WS/EWS]
4. PLACEMENT: [H: left/center/right] [V: upper/center/lower]
5. LIGHTING: [direction] + [quality] + shadow description
6. DOF: [razor/shallow/moderate/deep/infinite]
7. PERSPECTIVE: [one-point/two-point/three-point/forced/foreshortening]
8. SUBJECT_ORIENTATION: BODY + HEAD + GAZE (specific directions)
9. FORM_POSTURE: Detailed stance description
10. SCALE_ZOOM: [Frame fill %] + [Distance descriptor]

=== NARRATIVE DNA (3 params - NEW) ===
11. ACTION: What subject is DOING (gun, gesture, movement, interaction)
12. EMOTIONAL_BEAT: Micro-story/feeling being projected (confrontation, melancholy, anticipation)
13. TENSION_SOURCE: What creates visual interest (weapon, contradiction, dramatic pose)
```

### reviewImagePrompt() - Validation Rules:

```
1. CINEMATOGRAPHY (10 params)
2. NARRATIVE DNA (3 params - action, emotional_beat, tension_source)
3. SUBJECT ORIENTATION (3 components)
4. VISIBILITY BY FRAMING
5. VISUAL DIRECTION enforcement
6. GENRE RULES
7. EXPRESSION specificity
8. RENDERING STYLE
9. ANATOMY (80s OVA) - Eyes, Face, Body, Hair (NEW)
10. LIGHTING (80s OVA) - Rim light, shadow steps, color temperature (NEW)
```

### Violations:

- VISIBILITY_VIOLATION
- SUBJECT_ORIENTATION_VIOLATION
- MISSING_SCALE_ZOOM
- RENDERING_VIOLATION
- MISSING_80S_OVA_STYLE (CYBERPUNK specific)
- INVALID_80S_EYE_STYLE (NEW - Eyes don't follow 80s anime conventions)
- ANATOMY_VIOLATION (NEW - Face/body doesn't match vintage anime anatomy)
- HAIR_VIOLATION (NEW - Hair rendered with modern techniques)
- LIGHTING_VIOLATION (NEW - Lighting uses modern techniques)
- STATIC_SUBJECT_VIOLATION
- MISSING_NARRATIVE_INTENT
- INSUFFICIENT_CYBERWARE (CYBERPUNK)
- GENRE_AURA_VIOLATION
- BANNED_TERMINOLOGY

---

### 12. IMPROVED ANGLE & LENS Detection ✅ (NEW - January 13, 2026)

**Problem:** AI was frequently misidentifying camera angles. In the test image (Renova Boy), the
camera is clearly HIGH-ANGLE (above looking down) but AI extracted "LOW-ANGLE". The subject's face
is tilted UP toward camera - the most reliable indicator - was being ignored.

**Root Cause Analysis:**
The previous angle detection focused on compositional feelings ("subject appears dominant") rather
than **concrete visual geometry**. The most reliable indicator - **where the subject's face is
pointing** - was not emphasized.

**Reference Image (Renova Boy):**

- Subject's face is TILTED UP toward camera → HIGH-ANGLE (camera above)
- Top of head/hair is prominent and closer to camera
- Tight portrait framing with telephoto compression
- AI incorrectly said: "LOW-ANGLE" and "normal 35-50mm"

**Solution - ANGLE Detection Overhaul:**

New primary test (most reliable indicator):

```
⚠️⚠️⚠️ PRIMARY TEST (DO THIS FIRST): WHERE IS THE SUBJECT'S FACE POINTING?
- If subject's FACE is TILTED UP toward camera → CAMERA IS ABOVE → HIGH-ANGLE
- If subject's FACE is TILTED DOWN toward camera → CAMERA IS BELOW → LOW-ANGLE  
- If subject's FACE is LEVEL/STRAIGHT → EYE-LEVEL
This is the MOST RELIABLE indicator. Trust the face orientation!
```

Improved HIGH-ANGLE cues:

```
HIGH-ANGLE (camera ABOVE, looking DOWN at subject):
- FACE IS TILTED UP toward camera (chin raised, eyes looking up)
- Top of head/hair PROMINENT and CLOSER to camera
- Forehead appears larger/closer than chin
- Background shows floor/ground/lower areas
- Subject may appear vulnerable, introspective, or intimate
- COMMON in portrait photography
```

Improved LOW-ANGLE cues:

```
LOW-ANGLE (camera BELOW, looking UP at subject):
- FACE IS TILTED DOWN toward camera (chin tucked, eyes looking down)
- Jaw/chin PROMINENT and CLOSER to camera
- Can see UNDER chin/nostrils
- Shoulders appear ABOVE eye line, looming over camera
- Background shows ceiling/sky/upper areas
- Subject appears dominant, imposing, powerful
```

Self-check added:

```
⚠️ SELF-CHECK: If you say 'LOW-ANGLE' but the subject's face is TILTED UP → YOU ARE WRONG. Switch to HIGH-ANGLE.
```

**Solution - LENS Detection Improved:**

Added visual cues for lens compression detection:

```
ULTRA-WIDE 14-24mm: Visible barrel distortion, exaggerated depth, close objects HUGE
WIDE 24-35mm: Environment prominent, slight distortion at edges, sense of space
NORMAL 35-50mm: Natural perspective, balanced subject/background, no obvious compression
PORTRAIT 50-85mm: Flattering compression, background slightly compressed, intimate feel
TELEPHOTO 85-200mm+: Strong compression, background flattened, very intimate/tight
⚠️ CUES: Tight head/face shots with blurred background → portrait/tele. Wide environmental context → wide/normal.
```

**Expected Results After Fix:**

Previous extraction (WRONG):

```
1. ANGLE: LOW-ANGLE ❌
2. LENS: normal 35-50mm ❌
```

Expected extraction (CORRECT):

```
1. ANGLE: HIGH-ANGLE (face tilted up toward camera, top of head prominent)
2. LENS: portrait 50-85mm or tele 85-200mm (tight compression, intimate feel, blurred background)
```

---

## Test Results

### Reference Image Test (Renova Boy - Cyberpunk Portrait - January 13, 2026):

**Original Extraction (INCORRECT):**

```
1. ANGLE: LOW-ANGLE ❌ (WRONG - camera is clearly ABOVE subject)
2. LENS: normal 35-50mm ❌ (WRONG - tight portrait/telephoto compression visible)
3. FRAMING: CU: Head-Shoulders ✓
4. PLACEMENT: H: center [V: upper] ✓
5. LIGHTING: side-left + hard + strong shadow on right side of face ✓
6. DOF: shallow ✓
7. PERSPECTIVE: one-point ✓
8. SUBJECT_ORIENTATION: BODY AXIS: 3/4-right; HEAD: 3/4-right; GAZE: Direct ✓
9. FORM_POSTURE: Head tilted slight right, neck neutral, shoulders level, slight tension in jaw ✓
10. SCALE_ZOOM: 70% + close ✓
11. ACTION: Wearing futuristic eyewear and cybernetic implants, looking directly at the viewer ⚠️ (static - not really an action)
12. EMOTIONAL_BEAT: Intense scrutiny - assessing the viewer with calculated precision ✓
13. TENSION_SOURCE: Direct gaze and futuristic tech create a sense of technological power and potential threat ✓
```

**Key Issues:**

- ANGLE completely wrong (said LOW when it's HIGH)
- LENS too generic (portrait photography typically uses 50-85mm+, not 35-50mm)
- ACTION is passive ("wearing", "looking") - not a dynamic action

**Expected After Fixes:**

```
1. ANGLE: HIGH-ANGLE (camera above, face tilted up toward camera, top of head/hair prominent)
2. LENS: portrait 50-85mm or tele 85-200mm (tight compression, intimate feel, flattened background)
3-10: [same as above]
11. ACTION: Subject posed with subtle head tilt, displaying cybernetic enhancements and tech eyewear (if static, acknowledge it)
```

---

### Reference Image Test (Cyberpunk character with mask):

**Original Extraction (INCORRECT):**

```
1. ANGLE: Eye-level ❌ (should be low-angle)
9. FORM_POSTURE: Relaxed ❌ (too vague)
8. SUBJECT_ORIENTATION: 3/4 left-right ❌ (ambiguous)
11. ACTION: [missing] ❌ (didn't capture "pointing gun")
12. EMOTIONAL_BEAT: [missing] ❌ (didn't capture "aggressive confrontation")
13. TENSION_SOURCE: [missing] ❌ (didn't capture "gun barrel aimed at viewer")
```

**Expected After Fixes:**

```
1. ANGLE: Low-angle (shoulders prominent, looking up at subject)
9. FORM_POSTURE: Head tilted slight left, neck extended, shoulders back with tension
8. SUBJECT_ORIENTATION: Body: 3/4-right, Head: 3/4-right, Gaze: Direct
11. ACTION: Pointing gun directly at viewer with extended arm, cybernetic fingers gripping pistol
12. EMOTIONAL_BEAT: Aggressive confrontation - daring the viewer to make a move, predatory confidence
13. TENSION_SOURCE: Gun barrel pointed directly at camera creates immediate threat, glowing eyes add menace
```

---

## Documentation Files Updated

- `docs/cyberpunk_heavy_augmentation_enhancement.md` - Updated to v2 with mandatory enforcement
- `docs/image_prompts_optimization_session.md` - This file (session tracker)

---

## How to Continue This Session

1. Open this file to see current state
2. Check `ImagePrompts.kt` for current implementation
3. Check `GenrePrompts.kt` for genre-specific rules
4. Review "Pending / Future Work" section for next steps
5. Run tests to validate changes work correctly

---

## Session Update - January 13, 2026 (Late Session)

### 12. GenrePrompts.kt Syntax Fixes ✅

**Problem:** The `validationRules()` function had critical syntax errors:

- CYBERPUNK branch closed prematurely at line 1617 with `.trimIndent()`
- ~100 lines of validation content was orphaned outside the string literal
- `when` expression was non-exhaustive (missing branches)
- Multiple "Too many characters in character literal" errors due to unescaped content

**Solution:**

- Removed premature `.trimIndent() }` closure in CYBERPUNK branch
- Connected all orphaned validation content back into the proper raw string
- Fixed string closure to be at the correct position (after all CYBERPUNK rules)

**Files Modified:**

- `GenrePrompts.kt` - Fixed CYBERPUNK branch structure (~lines 1617-1720)

---

### 13. Cyberware Philosophy Reverted to Heavy Dystopian ✅

**Problem:** Previous "Seamless Integration" philosophy made cyberware too subtle/elegant, losing
the dystopian feel. Characters looked too clean and polished.

**Previous (TOO SUBTLE):**

```
**CYBERWARE PHILOSOPHY - SEAMLESS INTEGRATION:**
Cyberware should feel NATURAL and INTEGRATED—part of the person, not overwhelming robot parts.

GOOD CYBERWARE (integrated, elegant):
□ Mechanical iris with subtle rotation/scanner overlay
□ Sleek neural ports flush at temples/neck
□ Chrome accents following bone structure
□ Thin glowing circuit traces under skin
```

**New (HEAVY DYSTOPIAN):**

```
**CYBERWARE PHILOSOPHY - AGGRESSIVE DYSTOPIAN AUGMENTATION:**
Cyberware should feel INVASIVE, BRUTAL, and INDUSTRIAL—humanity sacrificed for function.
This is a world where people REPLACE their flesh, not decorate it.

GOOD CYBERWARE (heavy, visible, dystopian):
□ Complete mechanical eye replacements with rotating multi-lens arrays, LED scanner grids
□ Chunky chrome neural ports with thick data cables and junction boxes
□ Massive bolted-on plates with visible screws, welds, and repair marks
□ Full limb replacements with exposed hydraulic pistons and mechanical joints
□ Aggressive flesh-chrome interfaces with scarring and visible integration hardware

BAD CYBERWARE (TOO SUBTLE - VIOLATIONS):
✗ 'Subtle implants', 'barely visible augmentation', 'seamless integration'
✗ 'Thin circuit traces', 'delicate enhancements', 'elegant chrome accents'
✗ 'Natural-looking prosthetics', 'skin-matched augments'
✗ 'Circuit tattoos', 'silver scars' (too subtle - need REAL hardware)
```

**Updated Banned Terms:**

- Changed from banning "overwhelming cyberware" to banning "subtle cyberware"
- Added: 'subtle implants', 'seamless integration', 'elegant augmentation', 'minimalist tech', '
  barely visible'

**Updated Cyberware Glow:**

- Changed from "subtle amber, dying embers" to "Strong amber, cold blue, deep purple glow from
  active systems"
- LEDs and status lights should be VISIBLE and functional-looking
- Eye augments MUST have visible scanner lines, targeting reticles, data overlays

---

### 14. Mature/Gritty Tone Enforcement for CYBERPUNK ✅

**Problem:** Artworks sometimes had childish or cute elements that broke the dystopian atmosphere.
Need consistently mature, gritty, melancholic tone.

**New Section Added - TONE & ATMOSPHERE:**

```
**TONE & ATMOSPHERE (CRITICAL - MATURE CONTENT ONLY):**
This is ADULT cyberpunk - gritty, melancholic, brutal, disruptive.
The world is HARSH, UNFORGIVING, and DEHUMANIZING.
Characters should feel WORN, HARDENED, and WORLD-WEARY.

REQUIRED MOOD: Melancholic, dystopian, oppressive, noir, cynical, weary
FORBIDDEN MOOD: Cute, playful, cheerful, innocent, bright, hopeful, whimsical
```

**New Childish/Cute Elements Ban (ABSOLUTE BAN):**

```
✗ Soft/rounded facial features suggesting youth or innocence
✗ Big sparkly eyes with innocent expressions
✗ Bright cheerful colors or lighting
✗ Playful poses or expressions
✗ Clean/pristine appearances
✗ Cute accessories, kawaii elements, mascots
✗ Soft pastel tones anywhere
✗ Warm fuzzy lighting
✗ Hopeful or optimistic framing
```

**Required Character Feel:**

```
□ Hardened expressions - cynical, weary, calculating, or dangerous
□ Signs of street life - scars, weathering, hard edges
□ Body language suggesting tension, alertness, or exhaustion
□ Eyes that have SEEN things - not innocent, not wide-eyed wonder
□ Mature facial structure - angular, defined, weathered
```

**New Banned Terms Added:**

```
Childish/Cute (ZERO TOLERANCE): 'cute', 'adorable', 'kawaii', 'innocent', 
'playful', 'cheerful', 'bright smile', 'sparkly eyes', 'soft features', 
'youthful glow', 'warm lighting', 'hopeful', 'optimistic', 'gentle', 
'sweet', 'precious', 'doll-like', 'baby face', 'round cheeks', 'soft skin', 'pastel'
```

**New Auto-Fix Patterns (MATURE TONE ENFORCEMENT - HIGHEST PRIORITY):**

```
- 'cute', 'adorable', 'kawaii', 'innocent' → 'hardened', 'weathered', 'world-weary', 'cynical'
- 'playful', 'cheerful', 'bright smile' → 'calculating gaze', 'grim expression', 'cold stare'
- 'soft features', 'round face', 'baby face' → 'angular features', 'sharp jawline', 'weathered face with hard edges'
- 'sparkly eyes', 'big innocent eyes' → 'cold calculating eyes', 'weary gaze', 'eyes that have seen too much'
- 'youthful', 'young-looking', 'fresh-faced' → 'mature', 'hardened by street life', 'aged beyond years'
- 'warm lighting', 'soft glow', 'gentle light' → 'harsh industrial lighting', 'cold neon glare', 'unforgiving shadows'
- 'hopeful', 'optimistic', 'bright future' → 'melancholic', 'dystopian despair', 'grim resignation'
- 'pastel', 'soft colors', 'warm tones' → 'cold steel blues', 'bruised purples', 'industrial grays'
- Too friendly/approachable → 'guarded expression', 'suspicious glare', 'thousand-yard stare'
```

**Updated Environment Section:**

```
**ENVIRONMENT (REQUIRED - OPPRESSIVE & HARSH):**
Must include cyberpunk setting with 3+ details that reinforce dystopian mood
Environments should feel: cramped, polluted, dangerous, decaying, industrial
REQUIRED: rain-slicked streets, neon in darkness, urban decay, corporate oppression signs
FORBIDDEN: Plain background, empty void, pristine/clean tech, bright daylight, pleasant scenery
```

---

## Current CYBERPUNK Validation Priority Order

1. **MATURE TONE ENFORCEMENT** - No cute/childish elements, gritty/melancholic feel
2. **80s OVA STYLE** - Cel shading, film grain, muted colors, thick ink lines
3. **CYBERWARE: CYBORGS PRETENDING TO BE HUMAN** - Real replacements that fit human form
4. **ANATOMY** - 80s anime eyes, angular faces, volumetric hair clumps
5. **LIGHTING** - Hard rim light, Rembrandt contrast, colored shadows
6. **ENVIRONMENT** - Oppressive cyberpunk settings, no empty backgrounds

---

### 15. Cyberware Philosophy Refined - "Cyborgs Pretending to Be Human" ✅

**Problem:** Previous approaches were too binary:

- "Seamless Integration" = too subtle, lost dystopian feel
- "Aggressive Dystopian" = too heavy, weird chunky robot parts

**The Sweet Spot - Cyborgs Pretending to Be Human:**
The horror isn't giant robot parts - it's that they gave up their humanity piece by piece.
Chrome shaped like flesh. Artificial eyes that fit in the socket. The uncanny valley.

**New Philosophy:**

```
**CYBERWARE PHILOSOPHY - CYBORGS PRETENDING TO BE HUMAN:**
People REPLACE their flesh with chrome - but the chrome is shaped like flesh.
This is the uncanny valley: clearly artificial, but designed to fit human form.
The horror is not giant robot parts - it's that they gave up their humanity piece by piece.
```

**Key Design Principles:**

1. **REPLACEMENTS, NOT ADDITIONS:**
    - Prosthetic limbs that REPLACE arms/legs - chrome shaped like muscle and bone
    - Artificial eyes that REPLACE organic ones - fits in eye socket, clearly electronic
    - Mechanical spines, chrome bones - internal replacements visible at seams

2. **INTEGRATED, NOT BOLTED-ON:**
    - Cyberware follows human anatomy and proportions
    - Chrome arms have fingers, wrists, elbows - just made of metal
    - Tech is BUILT INTO the body, not strapped on top

3. **FUNCTIONAL SMALL TECH:**
    - Ear comms = sleek integrated earpieces (NOT giant headsets bolted to skull)
    - Neural interfaces = subtle temple ports (NOT massive junction boxes)
    - Data displays = eye overlays (NOT bulky external monitors)

4. **THE UNCANNY VALLEY FEEL:**
    - First glance: looks human
    - Second glance: something is WRONG
    - Close look: that's CHROME, not skin

**Good Examples:**

- Full chrome arm - shaped like human arm, metallic with visible joints
- Artificial eyes - electronic iris rings, faint scanner lines, fits in socket
- In-ear comm implants - like high-tech earbuds fused to ear
- Mechanical spine - chrome vertebrae visible at back of neck

**Bad - TOO SUBTLE (still violations):**

- Circuit tattoos, silver scars (not REPLACEMENTS)
- Enhanced vision with no visible modification
- Invisible augmentation

**Bad - TOO HEAVY (now also violations):**

- Giant protruding lens arrays
- Massive chunky boxes bolted to body
- Cables and wires everywhere
- More machinery than body

**Updated Fix Patterns:**

- Insufficient → Upgrade to actual replacement parts (prosthetics, artificial eyes)
- Excessive → Scale down to fit human anatomy (eye in socket, not lens array)
- Small tech → Make functional and integrated (earpieces not headsets)

---

### 16. Mandatory Cyberware Enforcement ✅

**Problem:** Cyberware was being treated as optional. Artists and character generation were creating
fully organic characters in a cyberpunk world where EVERYONE should have chrome.

**Solution:** Made cyberware MANDATORY at every level of the pipeline:

**1. GenrePrompts.kt - appearanceGuidelines:**
Added at top: "CYBERPUNK MANDATORY RULE: In this world, EVERYONE has cyberware. It's not optional -
it's survival."

**2. GenrePrompts.kt - validationRules:**
Added highest-priority rule: "A fully organic character is a CRITICAL VIOLATION in this genre."

**3. GenrePrompts.kt - AUTONOMOUS FIX PATTERNS:**
New fix: If NO cyberware → MUST ADD 2-3 augmentations (prosthetic arm, artificial eyes, neural port,
etc.)

**4. CharacterPrompts.kt - characterIntroPrompt:**
When CYBERPUNK: "Every character suggestion MUST include visible cybernetic augmentations"

**5. CharacterPrompts.kt - generateCharacterQuestionPrompt (APPEARANCE):**
CYBERPUNK examples now show cyberware: "Chrome left arm, artificial eyes with amber glow, neural
ports"

**6. CharacterPrompts.kt - characterGeneration:**
Full mandatory cyberware section for auto-generated characters from narrative

**7. CharacterPrompts.kt - extractCharacterDataPrompt:**
Suggests adding cyberware when user hasn't mentioned any

**Result:** Cyberware enforced at ALL levels - character creation, appearance questions, data
extraction, auto-generation, image validation, and auto-fixing.

---

### 17. Token Optimization - Genre Critical Rules at TOP ✅

**Problem:** Genre-specific rules were buried deep in prompts, risking being missed due to token
limits.

**Solution:** Added two new functions that inject genre-critical rules at the TOP of prompts:

**1. `genreCriticalRules(genre)` - For Artist (artComposition):**
Defines the SOUL of each genre - the elements that make it unmistakably that style.

**2. `genreCriticalValidation(genre)` - For Reviewer (reviewImagePrompt):**
Critical checks to validate the most important genre rules FIRST.

**All 9 Genres Now Have Critical Rules:**

| Genre           | Critical Rules (Soul)                                                                         |
|-----------------|-----------------------------------------------------------------------------------------------|
| **CYBERPUNK**   | Mandatory cyberware (2-3), mature tone, 80s OVA style                                         |
| **PUNK_ROCK**   | Cartoon style (Gorillaz), 3 eye options only, mandatory background, flat rendering            |
| **HORROR**      | 32-bit pixel art, dark blue palette, eerie environment, psychological horror                  |
| **FANTASY**     | Classical oil painting, crimson red dominance, classical anatomy, Renaissance environment     |
| **HEROES**      | Comic book style, urban verticality, electric blue accent, heroic posing                      |
| **CRIME**       | Renaissance masterpiece, divine perfection, tropical paradise, hot pink accent, candid posing |
| **SHINOBI**     | Sumi-e ink wash, black/white only + crimson accent, negative space, rice paper texture        |
| **SPACE_OPERA** | 1950s atomic age illustration, cosmic environments, cherry red accent, optimistic tone        |
| **COWBOY**      | Western oil painting, expressive brushwork, warm earthy palette, golden hour lighting         |

**Implementation:**

- `artComposition()` now calls `genreCriticalRules(genre)` at the TOP before art style
- `reviewImagePrompt()` now calls `genreCriticalValidation(genre)` at the TOP before validation
  rules
- Both functions added `genre` parameter to receive the current genre

---

## Files Modified This Session

- `GenrePrompts.kt` - Syntax fixes + Cyberware philosophy + Mature tone + Mandatory cyberware
- `CharacterPrompts.kt` - Mandatory cyberware guidance at multiple touch points
- `ImagePrompts.kt` - Added genreCriticalRules() and genreCriticalValidation() for ALL genres
- `ImagenClient.kt` - Pass genre to reviewImagePrompt
- `image_prompts_optimization_session.md` - This session tracker update

