# Testing Checklist — Enhanced Reviewer & Composition Extraction

## Quick Testing Guide

Use this checklist to validate that the cinematography extraction and reviewer enhancements are
working correctly.

---

## ✅ Test 1: Low-Angle Hero Shot (Gorillaz-style)

**Reference Image:** Urban low-angle shot with towering architecture

### Expected Extract Composition Output:

```
✓ CAMERA ANGLE: Extreme low-angle (40-45° looking up)
✓ FOCAL LENGTH: Wide-angle 20-28mm with perspective distortion
✓ FRAMING: Full Shot or Medium Wide - body visible
✓ PLACEMENT: Anchored at bottom third
✓ PERSPECTIVE: Strong vertical convergence, upward lean
✓ LIGHTING: Cool ambient with warm accent from below
✓ ENVIRONMENT: Urban with tall architecture
✓ MOOD: Epic, heroic, confrontational
```

### What Reviewer Should Catch:

- ❌ If prompt says "eye-level" → **CAMERA_ANGLE_WRONG** (CRITICAL)
- ❌ If no angle mentioned → **CAMERA_ANGLE_MISSING** (CRITICAL)
- ❌ If no perspective distortion → **FOCAL_LENGTH_MISMATCH** (MAJOR)
- ❌ If no "towering" or "upward" → **PERSPECTIVE_MISSING** (MAJOR)

### Test Prompt:

```
[Use Gorillaz album art or similar low-angle urban shot]
```

**Pass Criteria:**

- cinematographyScore > 85
- overallReadiness: READY
- Corrected prompt includes "low-angle", "looking up", "vertical convergence"

---

## ✅ Test 2: Portrait Framing Violation

**Reference Image:** Close-up headshot/portrait

### Expected Extract Composition Output:

```
✓ FRAMING: Close-Up / Portrait - face and upper shoulders ONLY
✓ CAMERA HEIGHT: Eye-level or slightly below
✓ DEPTH OF FIELD: Shallow f/2.8, subject isolation
```

### What Reviewer Should Catch:

- ❌ If prompt mentions "legs" → **FRAMING_VIOLATION** (CRITICAL) → REMOVE
- ❌ If prompt mentions "feet" → **FRAMING_VIOLATION** (CRITICAL) → REMOVE
- ❌ If prompt mentions "standing pose" → **FRAMING_VIOLATION** (CRITICAL) → REMOVE
- ❌ If prompt mentions "full outfit from head to toe" → **FRAMING_VIOLATION** (CRITICAL) → REMOVE

### Test Bad Prompt:

```
"Close-up portrait of character wearing boots and standing with arms crossed, 
full body visible from head to toe"
```

**Expected Fix:**

```
"Close-up portrait of character with arms visible in frame, 
upper body composition"
```

**Pass Criteria:**

- Reviewer detects FRAMING_VIOLATION
- Removes all "legs", "boots", "full body", "toe" mentions
- Keeps upper body descriptions

---

## ✅ Test 3: Dramatic Lighting Capture

**Reference Image:** Hard side-lighting with sharp shadows

### Expected Extract Composition Output:

```
✓ LIGHTING: Hard right-side light at 90°, sharp shadow falloff
✓ COLOR: High contrast with deep blacks
✓ ATMOSPHERE: Dramatic, film-noir mood
```

### What Reviewer Should Catch:

- ❌ If prompt says "soft lighting" → **LIGHTING_WRONG** (CRITICAL) → REPLACE with "hard"
- ❌ If no direction specified → **LIGHTING_MISSING** (MAJOR) → ADD "side light"
- ❌ If says "even illumination" → **LIGHTING_WRONG** (CRITICAL) → REPLACE

### Test Bad Prompt:

```
"Character in soft, even lighting with gentle shadows"
```

**Expected Fix:**

```
"Character lit with hard side-lighting from the right, creating sharp shadow 
falloff and high contrast dramatic atmosphere"
```

**Pass Criteria:**

- Reviewer detects LIGHTING_WRONG
- Corrects "soft" to "hard"
- Adds direction ("right-side")

---

## ✅ Test 4: Wide-Angle Perspective

**Reference Image:** Ultra-wide shot with visible distortion

### Expected Extract Composition Output:

```
✓ FOCAL LENGTH: Ultra-wide 16-20mm
✓ PERSPECTIVE: Extreme distortion, stretched edges, converging lines
✓ DEPTH: Exaggerated spatial depth, large foreground, small background
```

### What Reviewer Should Catch:

- ❌ If no distortion mentioned → **FOCAL_LENGTH_MISMATCH** (MAJOR)
- ❌ If says "telephoto compression" → **FOCAL_LENGTH_MISMATCH** (CRITICAL)
- ❌ If no converging lines → **PERSPECTIVE_MISSING** (MAJOR)

### Test Bad Prompt:

```
"Character in normal perspective with natural proportions"
```

**Expected Fix:**

```
"Character captured with ultra-wide 18mm lens, creating dramatic perspective 
distortion with exaggerated spatial depth and converging lines"
```

**Pass Criteria:**

- Reviewer adds lens perspective characteristics
- Mentions "distortion", "wide-angle", or "converging"

---

## ✅ Test 5: Color Temperature Capture

**Reference Image:** Cool blue night scene

### Expected Extract Composition Output:

```
✓ COLOR: Cool blue-teal palette, 5500K+
✓ LIGHTING TEMP: Cold, digital, nocturnal
✓ TIME: Night with artificial lighting
```

### What Reviewer Should Catch:

- ❌ If says "warm golden tones" → **COLOR_PALETTE_WRONG** (MAJOR)
- ❌ If no color temp mentioned → **ATMOSPHERE_MISSING** (MAJOR)

### Test Bad Prompt:

```
"Character in warm, sunset lighting with golden hour glow"
```

**Expected Fix:**

```
"Character in cool blue-teal nocturnal lighting with digital coldness"
```

**Pass Criteria:**

- Reviewer detects COLOR_PALETTE_WRONG
- Replaces "warm/golden" with "cool/blue"

---

## ✅ Test 6: Environment Validation

**Reference Image:** Urban street with specific architecture

### Expected Extract Composition Output:

```
✓ ENVIRONMENT: Urban street with brutalist architecture, neon signs
✓ SETTING: Night cityscape, industrial concrete
```

### What Reviewer Should Catch:

- ❌ If says "plain background" → **ENVIRONMENT_MISSING** (MAJOR)
- ❌ If says "studio backdrop" when urban specified → **ENVIRONMENT_MISSING** (MAJOR)

### Test Bad Prompt:

```
"Character against plain gradient background"
```

**Expected Fix:**

```
"Character in urban street environment with brutalist concrete architecture 
and neon signage visible in background"
```

**Pass Criteria:**

- Reviewer detects ENVIRONMENT_MISSING
- Adds specific environmental context

---

## 📊 Scoring Validation

For each test, verify JSON output includes:

```json
{
  "cinematographyScore": 0-100,
  "artStyleScore": 0-100,
  "overallReadiness": "READY|NEEDS_REVIEW|CRITICAL_ISSUES"
}
```

### Expected Score Ranges:

| Scenario               | Cinematography | Art Style | Readiness       |
|------------------------|----------------|-----------|-----------------|
| **Perfect capture**    | 90-100         | 90-100    | READY           |
| **Minor issues fixed** | 75-89          | 80-89     | READY           |
| **Major issues fixed** | 50-74          | 60-79     | NEEDS_REVIEW    |
| **Critical unfixable** | 0-49           | 0-59      | CRITICAL_ISSUES |

---

## 🐛 Common Issues to Watch For

### Issue 1: Generic Eye-Level Default

**Symptom:** All shots defaulting to eye-level despite reference angle  
**Check:** CAMERA_ANGLE_MISSING or CAMERA_ANGLE_WRONG violations  
**Expected Fix:** Reviewer adds explicit angle specification

### Issue 2: Framing Leakage

**Symptom:** Portrait shots mentioning full body elements  
**Check:** FRAMING_VIOLATION detected  
**Expected Fix:** Body part descriptions removed

### Issue 3: Flat Lighting

**Symptom:** "Normal lighting" or "even illumination" when dramatic specified  
**Check:** LIGHTING_WRONG or LIGHTING_MISSING  
**Expected Fix:** Specific direction and quality added

### Issue 4: No Perspective

**Symptom:** Missing wide-angle distortion or convergence  
**Check:** FOCAL_LENGTH_MISMATCH or PERSPECTIVE_MISSING  
**Expected Fix:** Lens characteristics added

### Issue 5: Wrong Color Mood

**Symptom:** Warm when cool specified (or vice versa)  
**Check:** COLOR_PALETTE_WRONG  
**Expected Fix:** Temperature corrected

---

## 🎯 Success Criteria Summary

A successful implementation should:

✅ **Extract Composition:**

- Outputs all 15 technical points with specific values
- Uses millimeter focal lengths (e.g., "24mm")
- Specifies angle degrees (e.g., "45° low-angle")
- Includes lighting direction (e.g., "right-side 90°")

✅ **Reviewer:**

- Catches ALL 6 test scenarios
- Provides detailed violation breakdown
- Outputs corrected prompt ready for generation
- Scores accurately (>85 for good captures)

✅ **Integration:**

- Composition extraction → Artist prompt → Reviewer validation → Image gen
- Each step preserves cinematographic intent
- Final artwork matches reference essence

---

## 📝 Quick Test Commands

### Test Extract Composition:

```kotlin
val compositionPrompt = ImagePrompts.extractComposition()
// Feed reference image + composition prompt to AI
// Verify output has all 15 points
```

### Test Reviewer:

```kotlin
val reviewerPrompt = ImagePrompts.reviewImagePrompt(
    visualDirection = extractedComposition,
    artStyleValidationRules = genreStyle,
    strictness = ReviewerStrictness.CONSERVATIVE,
    finalPrompt = artistGeneratedPrompt
)
// Verify catches violations and provides scores
```

---

## 🚀 Next Steps After Testing

1. **Log Metrics:**
    - Track violation frequency by type
    - Monitor cinematography scores over time
    - Identify most common failure patterns

2. **Iterate on Weak Points:**
    - If CAMERA_ANGLE_MISSING is frequent → Enhance artist prompt
    - If FRAMING_VIOLATION common → Strengthen framing instructions
    - If scores consistently low → Adjust reviewer strictness

3. **Build Reference Library:**
    - Save validated composition extractions
    - Tag by genre, angle, lighting
    - Quick lookup for common setups

4. **A/B Testing:**
    - Compare reviewer vs no reviewer quality
    - Test LENIENT vs CONSERVATIVE vs STRICT
    - Measure generation success rate improvement

---

## 📚 Related Documentation

- `/docs/reviewer_enhancement_summary.md` — Full technical documentation
- `/docs/feature_planning/smart_zoom_instructions.md` — Framing system details
- `ImagePrompts.kt` — Source code with all validation logic
- `ImageReference.kt` — Violation type definitions

---

**Ready to test! 🎬**

