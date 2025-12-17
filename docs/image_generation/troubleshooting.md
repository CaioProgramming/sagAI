# Troubleshooting: Image Generation Issues

**Last Updated:** December 16, 2025

---

## Quick Diagnostic Questions

Before diving into specific issues, ask:

1. **Is the problem in the prompt or the generated image?**
    - Prompt issue → Director/Artist/Reviewer problem
    - Image issue → Generation API or prompt quality

2. **Does it happen with all genres or just one?**
    - All genres → System-level issue
    - One genre → Genre-specific art style problem

3. **Does it happen with or without reference images?**
    - With reference → Director extraction issue
    - Without reference → Artist default issue
    - Both → Reviewer or core system issue

4. **What's the reviewer score?**
    - High score (70+), bad image → Generation API issue
    - Low score → Validation catching real problems

---

## Common Issues & Solutions

### Issue 1: Generated Image Has Wrong Framing

**Symptoms:**

- Director says "close-up" but image shows full body
- Director says "full body" but image is cropped at waist

**Root Cause:** Framing instruction not explicit enough in final prompt

**Check:**

1. Review Director output - is framing specified clearly?
2. Check Artist output - does Part 2 explicitly state framing?
3. Check Reviewer output - did it validate framing (A3 check)?

**Solution:**

```kotlin
// In Artist prompt, make framing MORE explicit:
"Extreme close-up portrait filling the entire frame from forehead to chin, 
NO body visible below neck"

// Or for full body:
"Full body composition with complete figure from head to feet visible, 
anchored at bottom edge of frame"
```

**Prevention:** Reviewer A3 check should flag vague framing

---

### Issue 2: Image Has Empty/Plain Background

**Symptoms:**

- Character on solid color background
- Gradient backdrop instead of environment
- Floating in void

**Root Cause:** Background enforcement not working

**Check:**

1. Review Artist output - does it describe environment with 3+ objects?
2. Check Reviewer violations - did B4/B5 flag this?
3. Check genre art style - does it have background requirements?

**Solution:**

```kotlin
// Reviewer should have flagged this as CRITICAL
// If not, strengthen background checks in reviewImagePrompt()

// In Artist output, ensure explicit environment:
"Set in gritty urban alley with spray-painted graffiti walls, 
overturned dumpsters, rusty fire escape ladders"
// Note: 3 specific objects named
```

**Prevention:**

- B4 check: Background DETAILED & ORGANIC - CRITICAL
- B5 check: Environment has 3+ objects - CRITICAL

---

### Issue 3: Accent Color Missing or Wrong

**Symptoms:**

- Genre signature color not visible
- Wrong color used
- Color randomly painted on character instead of environment

**Root Cause:** Accent color not integrated organically

**Check:**

1. Is `characterHexColor` being passed to `iconDescription()`?
2. Review Artist output - is accent color mentioned?
3. Check Reviewer - did B6/B7 validate accent color?

**Solution:**

```kotlin
// Ensure hex color passed:
val prompt = iconDescription(
    genre = genre,
    context = context,
    visualDirection = direction,
    characterHexColor = "#9C27B0" // Don't forget this!
)

// In prompt, accent color should appear like:
"Deep purple accent bathes the scene through neon reflections on 
wet pavement and atmospheric haze swirling around her silhouette"
```

**Prevention:**

- B6 check: ACCENT COLOR strategically used - CRITICAL
- B7 check: Accent color integrated organically - MAJOR

---

### Issue 4: Technical Jargon in Generated Images

**Symptoms:**

- Image generation fails
- Or image has text like "f/2.8" or "45°" overlaid
- Or image style doesn't match because AI interpreted jargon literally

**Root Cause:** Technical specs not translated to visual language

**Check:**

1. Review Artist output - does it contain "mm", "f/", "°", "K"?
2. Check Reviewer violations - did A12 flag jargon?
3. Check Translation Layer application

**Solution:**

```kotlin
// Reviewer A12 should catch this automatically
// If not, strengthen jargon detection regex

// Good translation:
"captured from below looking upward" 
// NOT: "shot from low-angle 45°"

"dramatic exaggerated perspective"
// NOT: "ultra-wide 20mm lens"

"soft dreamy background blur"
// NOT: "shallow f/1.4 depth of field"
```

**Prevention:**

- A12 check: NO tech jargon (f/°/mm/K) - MAJOR
- Translation Guide in Artist prompt

---

### Issue 5: Character Anatomy Doesn't Match Art Style

**Symptoms:**

- Gorillaz-style genre but character has realistic proportions
- Chibi genre but character is normal-sized
- Pixel art genre but character has realistic detail

**Root Cause:** Artist defaulting to realistic anatomy

**Check:**

1. Review genre art style - does it specify anatomy rules?
2. Check Artist output - does it describe stylized proportions?
3. Check Reviewer - did B3 validate anatomy?

**Solution:**

```kotlin
// In GenrePrompts.artStyle(), ensure anatomy specified:
"""
ANATOMY MANDATE:
- Exaggerated proportions: impossibly long noodle-like limbs
- Oversized hands and feet
- Angular, geometric facial features
- Stylized abstract eyes (white circles, NO iris detail)
"""

// Artist should translate to:
"lanky frame with exaggerated height, impossibly long noodle-like limbs, 
stylized angular face with bold abstract eyes—simple white circles"
```

**Prevention:**

- B3 check: Anatomy matches style - MAJOR
- Anatomy Mandate section in Artist prompt

---

### Issue 6: Character Looks Static/Boring

**Symptoms:**

- Character standing straight like mannequin
- Neutral expression, no emotion
- Arms hanging at sides
- Looking directly at camera with no personality

**Root Cause:** Not following personality-driven expressiveness guidelines

**Check:**

1. Review character personality traits - are they interesting?
2. Check Artist output - does it show body language, expression, gesture?
3. Is character too generic in source data?

**Solution:**

```kotlin
// In character description, add personality context:
"Cocky, street-smart, rebellious hacker who doesn't trust authority"

// Artist should translate to:
"cocky shrug with hand on hip, weight shifted to one leg, 
sly smirk, one eyebrow raised in challenge, gaze off to side 
with knowing defiance"
```

**Prevention:**

- Character Expressiveness section in Artist prompt
- Body Language/Posture requirements
- Hand Position/Gesture requirements
- Gaze Direction requirements

---

### Issue 7: Wrong Camera Angle

**Symptoms:**

- Reference shows low-angle but image is eye-level
- Reference shows dramatic tilt but image is straight

**Root Cause:** Director extraction inaccurate or Artist not following

**Check:**

1. Review Director output - is angle correct?
2. Check Artist Part 2 - is angle described?
3. Check Reviewer A1 - validated angle?

**Solution:**

```kotlin
// If Director wrong, improve extractComposition() prompt
// If Artist ignoring, strengthen Part 2 requirements
// If Reviewer missing, fix A1 validation

// Good angle description in Artist:
"Captured from below at ground level, camera tilted upward at dramatic angle, 
character towers overhead with commanding presence looming against sky"
```

**Prevention:**

- Director: ANGLE is TIER 1 CRITICAL
- Artist: Part 2 explicitly covers angle
- Reviewer: A1 check - CRITICAL if missing/wrong

---

### Issue 8: Lighting Doesn't Match Reference

**Symptoms:**

- Reference has dramatic side lighting
- Generated image has flat front lighting

**Root Cause:** Lighting direction not explicit enough

**Check:**

1. Director output - lighting specified (direction + quality)?
2. Artist Part 2 - lighting translated properly?
3. Reviewer A6 - validated lighting?

**Solution:**

```kotlin
// Director should output:
"LIGHTING: side hard / sharp cast shadows from right"

// Artist should translate:
"harsh lighting striking from the right, casting sharp angular shadows 
across the face and body"

// NOT just:
"dramatic lighting" (too vague)
```

**Prevention:**

- A6 check: Lighting direction+quality - CRITICAL if wrong

---

### Issue 9: Genre Art Style Not Applied

**Symptoms:**

- Requested Cyberpunk anime style
- Got generic digital art instead

**Root Cause:** Art style statement not strong enough

**Check:**

1. Review Artist Part 1 - is art style clearly stated?
2. Check genre art style - is it detailed enough?
3. Review generation API - does it support that style?

**Solution:**

```kotlin
// Strengthen Part 1 art style statement:
"A vintage 1980s anime OVA cel animation with flat cel shading, 
hard-edged shadows, delicate sketchy ink lines, and muted blue tones 
with analog noise and film grain"

// Much more specific than:
"Anime style art" (too generic)
```

**Prevention:**

- Part 1 must be first sentence (establishes visual language)
- Genre art styles should be highly detailed
- B2 check: Required elements - CRITICAL if mandatory

---

### Issue 10: Low Reviewer Scores

**Symptoms:**

- CinematographyScore: 45
- ArtStyleScore: 52
- OverallReadiness: CRITICAL_ISSUES

**Root Cause:** Artist output has multiple violations

**Check Violations Array:**

```json
"violations": [
  {"type": "A3_FRAMING", "severity": "CRITICAL"},
  {"type": "A12_JARGON", "severity": "MAJOR"},
  {"type": "B4_BACKGROUND", "severity": "CRITICAL"}
]
```

**Solution:**

1. Review each violation
2. Check if Reviewer corrected them (wasModified: true)
3. If corrected and score still low, investigate Reviewer logic
4. If not corrected, strengthen Reviewer correction logic

**Investigation Steps:**

```kotlin
// Log Reviewer output
Log.d("Reviewer", "Cinematography: ${result.cinematographyScore}")
Log.d("Reviewer", "ArtStyle: ${result.artStyleScore}")
Log.d("Reviewer", "Violations: ${result.violations}")
Log.d("Reviewer", "Was Modified: ${result.wasModified}")
Log.d("Reviewer", "Changes: ${result.changesApplied}")

// Check if corrections actually fixed issues
// If not, improve Reviewer correction logic
```

---

### Issue 11: Generation API Fails

**Symptoms:**

- Prompt looks good (high reviewer scores)
- But generation API returns error
- Or returns inappropriate/filtered content

**Root Cause:** API content policy violation or service issue

**Common Triggers:**

- Violence described too explicitly
- Suggestive poses/clothing
- Copyrighted characters/brands
- Prompt too long (token limit exceeded)

**Solution:**

```kotlin
// Soften language:
❌ "blood dripping from wounds, gore"
✅ "weathered and battle-worn"

❌ "sexy outfit, revealing"
✅ "elegant attire"

❌ "Batman logo"
✅ "stylized bat symbol"

// Check prompt length:
if (prompt.length > 4000) {
    // Trim or summarize
}
```

**Prevention:**

- Add content policy check before generation
- Implement fallback/retry logic
- Log API errors for analysis

---

### Issue 12: Colors Look Wrong/Washed Out

**Symptoms:**

- Generated image colors don't match genre
- Accent color barely visible
- Overall image looks desaturated

**Root Cause:** Color instructions not strong enough

**Check:**

1. Artist output - is color palette mentioned?
2. Genre art style - does it specify colors?
3. Is accent color integrated subtly (too subtle)?

**Solution:**

```kotlin
// Strengthen color instructions in Artist:
"Vibrant electric blues and deep purples dominate the scene with 
high saturation. Cool-toned neon lighting bathes everything in 
intense cyan and magenta glow."

// Make accent color more prominent:
"Deep purple accent STRONGLY illuminates the scene through multiple 
neon signs, atmospheric haze, and reflections on every wet surface"
```

---

## Debugging Workflow

### Step 1: Identify Phase

```
Bad output → Where did it go wrong?
├── Director phase (if reference used)
│   └── Check: extractComposition output
├── Artist phase  
│   └── Check: iconDescription output
├── Reviewer phase
│   └── Check: reviewImagePrompt output
└── Generation phase
    └── Check: API response
```

### Step 2: Isolate Issue

```
Test each phase independently:
1. Run Director on reference → Check parameters
2. Run Artist with mocked Director output → Check prompt
3. Run Reviewer on Artist output → Check violations
4. Run Generation with validated prompt → Check image
```

### Step 3: Add Logging

```kotlin
// In use case:
Log.d("ImageGen", "=== DIRECTOR OUTPUT ===")
Log.d("ImageGen", directorOutput)

Log.d("ImageGen", "=== ARTIST OUTPUT ===")
Log.d("ImageGen", artistOutput)

Log.d("ImageGen", "=== REVIEWER OUTPUT ===")
Log.d("ImageGen", "Score: ${reviewer.cinematographyScore}/${reviewer.artStyleScore}")
Log.d("ImageGen", "Violations: ${reviewer.violations.size}")
Log.d("ImageGen", reviewerOutput)
```

### Step 4: Compare Expected vs Actual

```
For each phase output:
✓ Does it match expected format?
✓ Are all required elements present?
✓ Is there unexpected content?
✓ Are scores/validations correct?
```

### Step 5: Fix Root Cause

```
Don't patch symptoms, fix the cause:
❌ Manually editing final prompts
✅ Fixing prompt generation logic

❌ Bypassing Reviewer
✅ Improving Reviewer validation

❌ Ignoring low scores
✅ Understanding and fixing violations
```

---

## Performance Issues

### Issue: Generation Takes Too Long

**Symptoms:** 60+ seconds per image

**Causes:**

1. Multiple retries due to failures
2. Token limit causing delays
3. Network issues

**Solutions:**

```kotlin
// Implement timeout:
withTimeout(45000) { // 45 seconds
    generateImage(prompt)
}

// Add caching for genre styles:
val cachedStyle = styleCache[genre] ?: GenrePrompts.artStyle(genre)

// Optimize prompts (reduce tokens):
// Remove redundant explanations
// Use concise language
```

### Issue: High Token Usage

**Symptoms:** Hitting API token limits

**Solutions:**

- Cache genre art styles (static content)
- Reduce Artist prompt length (remove verbose examples)
- Simplify Reviewer prompt (focus on critical checks)
- Use token-efficient language

---

## When to Escalate

**Contact Development Team If:**

1. Issue persists after trying all solutions
2. Multiple genres affected
3. System-wide failure
4. API consistently failing
5. Reviewer scores don't match visual quality
6. Need to add new genre or major feature

**Provide:**

- Detailed reproduction steps
- All log outputs (Director, Artist, Reviewer)
- Genre and character data
- Reference image (if used)
- Expected vs actual results
- Reviewer scores and violations

---

## Related Documentation

- [Director Pillar](./01_director_pillar.md)
- [Artist Pillar](./02_artist_pillar.md)
- [Reviewer Pillar](./03_reviewer_pillar.md)
- [System Flow](./system_flow.md)
- [Best Practices](./best_practices.md)

---

**Remember:** Most issues have a root cause in one of the three pillars. Identify the phase, isolate
the problem, fix the source, not the symptom!

