# Reviewer Enhancement Summary — Cinematography Validation

## Overview

The **Image Prompt Reviewer** has been massively enhanced to act as a comprehensive **Quality
Assurance system** that validates both **Cinematography** (from Visual Direction) and **Art Style
** (from style rules) before image generation.

This ensures that the artist agent successfully captures ALL aspects of the reference image
composition, preventing static, generic artworks.

---

## What Was Changed

### 1. **Expanded Validation from 3 Checks to 16 Comprehensive Checks**

#### **SECTION A: CINEMATOGRAPHY COMPLIANCE (11 Checks)**

Previously only checked basic framing. Now validates ALL 15 cinematographic parameters:

**A1. Camera Angle & Height (CRITICAL)**

- ✓ Verifies exact angle is mentioned (e.g., "low-angle 45°", "eye-level", "high-angle 30°")
- ✓ Checks for dutch angle/tilt specifications
- ✓ Ensures perspective matches (upward for low-angle, downward for high-angle)
- ✓ **CRITICAL violation** if angle missing or contradictory

**A2. Focal Length & Perspective (CRITICAL)**

- ✓ Validates lens characteristics are captured
- ✓ Wide-angle: Checks for "distortion", "converging lines", "exaggerated depth"
- ✓ Telephoto: Checks for "compression", "flattened space", "isolation"
- ✓ **MAJOR violation** if perspective doesn't match lens type

**A3. Framing & Shot Size (CRITICAL — NON-NEGOTIABLE)**

- ✓ The #1 most common failure point
- ✓ Validates exact shot type (ECU/CU/MCU/MS/MWS/FS/WS/EWS)
- ✓ Removes body part descriptions outside camera frame:
    - Close-up: NO legs, feet, full outfit
    - Medium: NO legs, feet
    - Full body: ALL allowed
- ✓ Ensures explicit framing instruction present
- ✓ **CRITICAL violation** if framing wrong

**A4. Subject Placement & Composition**

- ✓ Validates horizontal/vertical positioning (centered, thirds, anchored)
- ✓ Checks rule of thirds compliance
- ✓ **MAJOR violation** if placement critical to composition

**A5. Depth of Field**

- ✓ Shallow DOF: Validates bokeh, soft background mentions
- ✓ Deep DOF: Validates sharp environment, detailed background
- ✓ **MAJOR violation** if DOF critical to style

**A6. Lighting Direction & Quality (CRITICAL)**

- ✓ Validates light source direction (front, side, back, top, under, ambient)
- ✓ Checks quality (hard = sharp shadows, soft = diffused)
- ✓ **CRITICAL** if contradicts Visual Direction
- ✓ **MAJOR** if direction missing when specified

**A7. Color Temperature & Palette**

- ✓ Validates color mood (cool/warm/neutral)
- ✓ Checks palette matches Visual Direction
- ✓ **MAJOR violation** if color mood wrong/missing

**A8. Atmosphere & Environmental Mood**

- ✓ Validates atmospheric quality (clear, hazy, foggy, dusty, smoky)
- ✓ Checks emotional tone (epic, intimate, menacing, nostalgic, etc.)
- ✓ **MAJOR violation** if mood critical to genre

**A9. Environmental Context**

- ✓ Validates physical setting matches Visual Direction
- ✓ Checks architectural elements mentioned
- ✓ **MAJOR violation** if environment specified but missing

**A10. Perspective Distortion & Geometry**

- ✓ Validates converging lines for low-angle + wide-lens
- ✓ Checks foreshortening, barrel distortion
- ✓ **MAJOR violation** for dramatic angles

**A11. Signature Visual Detail**

- ✓ Validates unique element captured (rim light, aggressive scale, etc.)
- ✓ **MAJOR violation** if defines visual uniqueness

#### **SECTION B: ART STYLE COMPLIANCE (5 Checks)**

Enhanced from previous checks:

**B1. Banned Terminology Check**

- Same as before but integrated with cinematography checks
- **MAJOR violation**

**B2. Required Elements Check**

- Enhanced to cross-reference with Visual Direction environment
- **CRITICAL** if style mandates, **MAJOR** otherwise

**B3. Anatomy Terminology Check**

- Validates style-appropriate anatomy descriptions
- **MAJOR violation**

**B4. Background Validation (Enhanced)**

- Now adapts to framing:
    - Close-up: Tight-cropped backgrounds (wall, neon glow)
    - Wide: Full environmental context
- Must match Visual Direction environment
- **CRITICAL** if backgrounds mandatory

**B5. Style Contradiction Check**

- Catches technique mismatches
- **MAJOR violation**

---

### 2. **Enhanced JSON Output Structure**

Added three new scoring fields:

```json
{
  "correctedPrompt": "...",
  "violations": [...],
  "changesApplied": [...],
  "wasModified": true,
  
  // NEW SCORING FIELDS:
  "cinematographyScore": 85,        // 0-100: How well camera/lighting/mood captured
  "artStyleScore": 95,               // 0-100: How compliant with art style rules
  "overallReadiness": "READY"        // READY | NEEDS_REVIEW | CRITICAL_ISSUES
}
```

**Scoring Guidance:**

- **cinematographyScore**: 100 = all 11 camera specs captured, 0 = none captured
- **artStyleScore**: 100 = zero violations, 0 = multiple critical violations
- **overallReadiness**:
    - `READY`: No critical issues, can generate immediately
    - `NEEDS_REVIEW`: Major issues corrected but may benefit from review
    - `CRITICAL_ISSUES`: Severe problems that may still cause failure

---

### 3. **Expanded Violation Types**

Added 13 new cinematography-specific violation types to `ViolationType` enum:

**New Types:**

- `CAMERA_ANGLE_MISSING`
- `CAMERA_ANGLE_WRONG`
- `FOCAL_LENGTH_MISMATCH`
- `PLACEMENT_MISSING`
- `DEPTH_OF_FIELD_MISSING`
- `LIGHTING_WRONG`
- `LIGHTING_MISSING`
- `COLOR_PALETTE_WRONG`
- `ATMOSPHERE_MISSING`
- `ENVIRONMENT_MISSING`
- `PERSPECTIVE_MISSING`
- `SIGNATURE_DETAIL_MISSING`

**Original Types (kept):**

- `FRAMING_VIOLATION`
- `BANNED_TERMINOLOGY`
- `MISSING_ELEMENTS`
- `ANATOMY_MISMATCH`
- `STYLE_CONTRADICTION`

This enables granular analytics and debugging.

---

### 4. **Clear Severity Classification**

Each validation point now has explicit severity guidance:

- **CRITICAL**: Breaks core functionality
    - Wrong framing (body parts outside camera view)
    - Contradictory camera angle
    - Missing mandatory elements

- **MAJOR**: Significantly degrades quality
    - Missing key cinematography (lighting, angle)
    - Banned terminology used
    - Wrong color mood

- **MINOR**: Small polish issues
    - Slight wording improvements
    - Optional enhancements

---

### 5. **Correction Principles**

Added 5 explicit principles for how to correct violations:

1. **PRESERVE** original personality and creative intent
2. **ONLY CHANGE** what violates rules — don't over-rewrite
3. **ADD** missing elements rather than removing (except framing violations)
4. **MERGE** cinematography and art style seamlessly
5. **ENSURE** correctedPrompt is immediately usable

---

## Real-World Example: Gorillaz-Style Reference

### Visual Direction Extracted (from improved `extractComposition()`):

```
CAMERA ANGLE: Extreme low-angle (45° looking up), level horizon
FOCAL LENGTH: Wide-angle 24mm with noticeable perspective distortion
FRAMING: Full Shot - entire bodies head-to-toe
PLACEMENT: Subjects centered horizontally, anchored at bottom edge
LIGHTING: Cool ambient night light with warm sodium vapor accents from below
COLOR: Cool blue-teal night sky with warm orange street lighting
ENVIRONMENT: Urban street with towering brutalist building, strong vertical lines converging upward
PERSPECTIVE: Strong vertical convergence creating heroic upward lean
MOOD: Epic, confrontational, towering presence with urban grit
SIGNATURE: Extreme low-angle creating heroic dominance with compressed group framing
```

### Bad Prompt (Before Reviewer):

```
Four characters standing in a casual group shot with neutral lighting and 
simple background. Eye-level perspective with realistic proportions.
```

### Reviewer Detection:

- ❌ **CAMERA_ANGLE_WRONG** (CRITICAL): Says "eye-level" when "extreme low-angle 45°" specified
- ❌ **FOCAL_LENGTH_MISMATCH** (MAJOR): No mention of wide-angle distortion or converging lines
- ❌ **LIGHTING_WRONG** (CRITICAL): "Neutral lighting" contradicts "cool night with warm accents from
  below"
- ❌ **COLOR_PALETTE_WRONG** (MAJOR): No blue-teal/orange split
- ❌ **ENVIRONMENT_MISSING** (MAJOR): "Simple background" vs "urban street with brutalist
  architecture"
- ❌ **PERSPECTIVE_MISSING** (MAJOR): No converging vertical lines
- ❌ **ATMOSPHERE_MISSING** (MAJOR): No "epic, confrontational" mood
- ❌ **SIGNATURE_DETAIL_MISSING** (MAJOR): No heroic dominance emphasis

**Scores:**

- cinematographyScore: 15/100
- artStyleScore: 80/100 (assuming style rules mostly followed)
- overallReadiness: CRITICAL_ISSUES

### Corrected Prompt (After Reviewer):

```
Four characters in FULL BODY shot captured from an EXTREME LOW-ANGLE (45° below, 
camera looking up), creating towering heroic presence with compressed group framing. 
Wide-angle 24mm lens creates strong vertical convergence of the brutalist urban 
architecture behind them, with building lines leaning upward dramatically. 

Characters anchored at bottom edge of frame against night urban street environment. 
Cool blue-teal night sky ambient lighting contrasted with warm orange sodium vapor 
street lights illuminating from below, creating dual-tone mood lighting. 

Epic, confrontational atmosphere with urban grit. Perspective emphasizes aggressive 
upward scale and heroic dominance. Sharp focus throughout with slight film grain texture.
```

**Scores:**

- cinematographyScore: 95/100
- artStyleScore: 95/100
- overallReadiness: READY

---

## Benefits

### 1. **Prevents Static, Generic Artworks**

- Forces unique camera angles instead of default eye-level
- Ensures dramatic lighting instead of flat illumination
- Captures environmental context instead of plain backgrounds

### 2. **Acts as Final Safety Lock**

- Catches ALL violations before expensive image generation
- Provides detailed diagnostics (16 validation points)
- Quantifies readiness with scores

### 3. **Ensures Artist-DP Alignment**

- Validates that artist agent captured ALL cinematography instructions
- Merges art style and camera work seamlessly
- Preserves creative intent while enforcing technical specs

### 4. **Better Analytics**

- 18 violation types for granular tracking
- Score metrics for quality measurement
- Identifies common failure patterns

### 5. **Reduces Generation Waste**

- Fixes issues BEFORE calling expensive Imagen API
- Provides corrected prompt ready for immediate use
- Reduces iteration cycles

---

## Next Steps (Optional Enhancements)

### 1. **Historical Violation Tracking**

- Log violations per genre to identify patterns
- Track which cinematography aspects are most commonly missed
- A/B test strictness levels

### 2. **Auto-Adjust Artist Prompt**

- Feed violation patterns back to artist instruction
- Emphasize commonly-missed aspects
- Dynamic prompt evolution

### 3. **Reference Image Library**

- Build database of validated composition extractions
- Tag by camera angle, lighting, mood
- Quick reference for common setups

### 4. **Visual Diff Tool**

- Show "before reviewer" vs "after reviewer" side-by-side
- Highlight specific changes with color coding
- Educational for understanding violations

---

## Files Modified

1. **`ImagePrompts.kt`**
    - `extractComposition()`: Expanded from 10 to 15 technical points (previously done)
    - `reviewImagePrompt()`: Expanded from 3 to 16 validation checks

2. **`ImageReference.kt`**
    - `ViolationType`: Expanded from 5 to 18 violation types
    - Added clear categorization (Cinematography vs Art Style)

---

## Testing Recommendations

### Manual Testing:

1. **Low-angle test**: Feed Gorillaz-style reference, generate character, validate angle captured
2. **Portrait framing test**: Feed headshot reference, ensure no leg descriptions leak in
3. **Lighting test**: Feed dramatic side-lit reference, validate hard light captured
4. **Color test**: Feed cool-tone reference, ensure no warm descriptions

### Automated Testing (Future):

- Unit tests for violation detection
- Regression tests for common failure patterns
- Score distribution analysis

---

## Conclusion

The reviewer is now a **comprehensive cinematography and art style validator** that acts as the
final quality gate before image generation.

It ensures that every prompt captures:

- ✅ Exact camera angle and rotation
- ✅ Proper focal length perspective
- ✅ Correct framing without body part leakage
- ✅ Precise lighting direction and quality
- ✅ Accurate color temperature and palette
- ✅ Environmental context and atmosphere
- ✅ Emotional mood and signature details
- ✅ Complete art style compliance

**Result:** Unique, dynamic artworks that match the reference composition essence, not static
generic outputs.

