# Best Practices: Image Generation System

**Last Updated:** December 16, 2025

---

## General Guidelines

### 1. Always Use Reference Images When Possible

**Why:** Reference images provide precise cinematographic direction
**Impact:** 40-60% improvement in composition quality

**Good Practice:**

```kotlin
// With reference for better composition control
val visualDirection = extractComposition(referenceImage)
val prompt = iconDescription(genre, context, visualDirection, hexColor)
```

**Acceptable:**

```kotlin
// Without reference - uses genre defaults
val prompt = iconDescription(genre, context, null, hexColor)
```

---

### 2. Choose Reference Images Carefully

**Good References:**
✅ Clear subject with defined framing
✅ Interesting camera angle (not just straight-on)
✅ Dramatic lighting with visible direction
✅ Rich environmental context
✅ Matches desired mood/emotion

**Poor References:**
❌ Cluttered or confusing composition
❌ Multiple subjects (unclear focus)
❌ Flat, boring angles
❌ Overly dark or overexposed
❌ Empty backgrounds

---

### 3. Match Reference Style to Genre

**Don't:** Use a realistic photo reference for pixel art Horror genre
**Do:** Use stylistically compatible references or let genre defaults handle it

**Example:**

- Cyberpunk: Urban anime/illustration references work great
- Fantasy: Oil painting or fantasy art references
- Horror: Retro game screenshots or pixel art
- Crime: Fashion photography or luxury lifestyle shots

---

## Working with the Director

### Understanding Tier Priorities

**TIER 1 (Critical) - Never Compromise:**

- Angle
- Lens/perspective
- Framing
- Placement

These define the **essence** of the shot. Get these wrong and the entire composition fails.

**TIER 2 (Important) - Prioritize:**

- Lighting
- Color
- Environment
- Mood

These create the **atmosphere** and emotional impact.

**TIER 3 (Refinement) - Nice to Have:**

- DOF
- Atmosphere
- Perspective distortion
- Texture
- Time of day
- Signature detail

These add **polish** but aren't essential to composition.

---

### Director Output Interpretation

When you see Director output like:

```
FRAMING: CU head-shoulders
```

This is **NON-NEGOTIABLE**. The Artist MUST:

- Only describe face, hair, neck, shoulders
- NOT describe legs, full outfit, stance, feet

**Remember:** Director sees through a camera lens. Trust the framing.

---

## Working with the Artist

### Accent Color Integration

**Strategic Application Methods (in order of effectiveness):**

1. **Lighting (Most Effective)**
    - Ambient glow
    - Rim light
    - Atmospheric haze
    - Light reflections on surfaces

2. **Environment (Very Effective)**
    - Background elements (neon signs, sunset, fires)
    - Distant light sources
    - Natural phenomena

3. **Atmospheric (Effective)**
    - Fog/mist tint
    - Volumetric rays
    - Particle effects

4. **Surfaces (Subtle)**
    - Reflections on wet ground/metal/glass
    - Material highlights

5. **Details (Use Sparingly)**
    - Small props
    - Fabric accents
    - Magical effects

**Anti-Pattern:**
❌ Randomly painting character's hair/skin with accent color
✅ Bathing entire scene in accent-colored atmospheric lighting

---

### Background Richness

**Formula for Rich Backgrounds:**

```
Location Type + 3+ Specific Objects + Atmospheric Quality
```

**Examples:**

**Bad (Too Vague):**

- ❌ "urban setting"
- ❌ "forest background"
- ❌ "indoor scene"

**Good (Specific & Rich):**

- ✅ "Gritty urban alley with spray-painted graffiti walls, overturned dumpsters, rusty fire escape
  ladders, atmospheric steam rising"
- ✅ "Ancient forest with massive gnarled oak trees, moss-covered stone ruins, hanging vines, dappled
  sunlight filtering through canopy"
- ✅ "Corporate boardroom with floor-to-ceiling glass windows, holographic data displays, minimalist
  chrome furniture, cold blue lighting"

---

### Character Expressiveness

**Avoid These Patterns:**

- ❌ "standing confidently" (boring, static)
- ❌ "looking at viewer" (default AI behavior)
- ❌ "neutral expression" (soulless)

**Instead Use:**

- ✅ "cocky shrug with hand on hip, weight shifted to one leg"
- ✅ "gaze off to the side with knowing smirk"
- ✅ "eyes narrowed with determination, jaw set, fists clenched"

**Key Principle:** Show personality through **body language**, not just words.

---

## Working with the Reviewer

### Strictness Level Selection

**Use BALANCED for:** (Default)

- Standard saga/character generation
- Good balance of accuracy and creativity
- Most common use case

**Use STRICT for:**

- Testing reference image extraction accuracy
- When composition precision is critical
- Debugging cinematography issues
- Professional/commercial artwork

**Use LENIENT for:**

- Experimental or artistic freedom desired
- When reference is rough guidance only
- Quick iterations/prototyping
- Stylized interpretations preferred

---

### Understanding Violation Severities

**CRITICAL (Always Fix):**

- Framing mismatches
- Empty backgrounds
- Missing accent color
- Technical jargon in final prompt

These **break** the system's core requirements.

**MAJOR (Usually Fix):**

- Anatomy doesn't match style
- Banned terminology used
- Lighting poorly described
- Environment lacks 3+ objects

These **degrade** quality significantly.

**MINOR (Nice to Fix):**

- Wording improvements
- Polish suggestions
- Creative enhancements

These are **optional** quality improvements.

---

### Interpreting Scores

**Cinematography Score:**

- **90-100:** Excellent - Reference captured perfectly
- **70-89:** Good - Minor elements missed
- **50-69:** Fair - Several important elements missing
- **Below 50:** Poor - Major composition issues

**Art Style Score:**

- **90-100:** Excellent - Perfect genre adherence
- **70-89:** Good - Minor style deviations
- **50-69:** Fair - Some style violations
- **Below 50:** Poor - Doesn't match genre

**Action Based on Scores:**

- Both 70+: ✅ Proceed with generation
- One below 70: ⚠️ Review violations, consider regenerating
- Both below 70: ❌ Fix input issues, regenerate

---

## Genre-Specific Best Practices

### Fantasy

- **Accent Color:** Use ember gold/fiery orange for magical effects
- **Backgrounds:** Medieval architecture, natural wilderness
- **Mood:** Epic, mysterious, wistful
- **Avoid:** Modern elements, bright neon colors

### Cyberpunk

- **Accent Color:** Deep purple in shadows/atmospheric glow
- **Backgrounds:** Dense urban tech environments with neon
- **Mood:** Dystopian, melancholic, vast
- **Avoid:** Bright fluorescent colors, modern anime style

### Horror

- **Accent Color:** Ash grey/faded cerulean (subtle)
- **Backgrounds:** Abandoned/eerie locations with oppressive atmosphere
- **Mood:** Dread, uncanny, psychological unease
- **Avoid:** Vibrant colors, cheerful elements

### Heroes

- **Accent Color:** Subtle electric blue in sky/reflections
- **Backgrounds:** Vast urban cityscapes with vertical scale
- **Mood:** Heroic, expansive, dynamic
- **Avoid:** Confined spaces, dark claustrophobic settings

### Crime

- **Accent Color:** Hot pink through neon/beach sunset
- **Backgrounds:** Luxury beach environments, Miami vibes
- **Mood:** Elegant, divine, glamorous with vice undertones
- **Avoid:** Gritty ugliness, poverty imagery

---

## Common Pitfalls & Solutions

### Pitfall 1: Technical Jargon in Final Prompts

**Problem:** Artist outputs "shot with 20mm f/2.8 at 45°"
**Solution:** Reviewer catches this (A12 check) and translates to visual language
**Prevention:** Emphasize Translation Layer in Artist prompt

### Pitfall 2: Empty Backgrounds

**Problem:** Generated images have plain gradients
**Solution:** Reviewer enforces B4/B5 checks (3+ objects required)
**Prevention:** Strong background mandate in Artist prompt

### Pitfall 3: Framing Violations

**Problem:** Close-up prompt describes legs/feet
**Solution:** Reviewer's A3 check removes body parts outside frame
**Prevention:** Clear framing-aware filtering in Artist instructions

### Pitfall 4: Missing Accent Color

**Problem:** Genre's signature color not present
**Solution:** Reviewer's B6 check flags missing accent color
**Prevention:** Prominent accent color section in Artist prompt

### Pitfall 5: Generic Static Poses

**Problem:** Characters look like mannequins
**Solution:** Personality-driven expressiveness section in Artist
**Prevention:** Emphasize body language, gaze, gestures in prompts

---

## Performance Optimization

### Token Budget Management

**High Token Consumers:**

1. Artist prompt (~2000 tokens)
2. Reviewer prompt (~2500 tokens)
3. Genre art style descriptions (~300-800 tokens)

**Optimization Strategies:**

- Use concise language in prompts
- Avoid redundant explanations
- Rely on examples over lengthy descriptions
- Cache frequently used genre styles

### Caching Opportunities

**Cache These:**

- Genre art style descriptions (rarely change)
- Validation rules per genre (static)
- Translation guide (static)

**Don't Cache:**

- Character-specific descriptions
- Visual direction from references
- Final prompts (always unique)

---

## Testing & Validation

### Manual Testing Checklist

When testing a new feature or change:

1. ✓ Generate with reference image
2. ✓ Generate without reference image
3. ✓ Test all 9 genres
4. ✓ Try different strictness levels
5. ✓ Check close-up, medium, and full-body framings
6. ✓ Verify accent color appears in output
7. ✓ Confirm backgrounds are rich (3+ objects)
8. ✓ Check no technical jargon in final prompt
9. ✓ Review cinematography and art style scores

### A/B Testing Scenarios

**Test Accent Color Integration:**

- Generate same character with/without hex color
- Verify signature color visible and organic

**Test Reference Extraction:**

- Same genre, different reference images
- Compare framing/angle accuracy

**Test Background Enforcement:**

- Check all genres for rich environments
- Confirm no empty/plain backgrounds slip through

---

## Documentation Updates

**When You Change Something, Update:**

1. The relevant pillar documentation file
2. System flow if workflow changes
3. This best practices file if new patterns emerge
4. Code comments in the actual implementation

**Keep In Sync:**

- Documentation describes what code does
- Code implements what documentation promises
- Examples reflect current system behavior

---

## Quick Reference Dos & Don'ts

### ✅ DO

- Use reference images for better composition
- Trust the Director's framing decisions
- Integrate accent colors through lighting/environment
- Describe 3+ specific background objects
- Make characters expressive and personality-driven
- Translate technical specs to visual language
- Run Reviewer validation before generation

### ❌ DON'T

- Ignore framing (describe body parts outside camera)
- Use technical jargon in final prompts (mm, f-stops, degrees)
- Create empty/plain backgrounds
- Forget accent color integration
- Make characters static/neutral/boring
- Override art style anatomy rules with realism
- Skip validation to save tokens

---

## Related Documentation

- [Director Pillar](./01_director_pillar.md)
- [Artist Pillar](./02_artist_pillar.md)
- [Reviewer Pillar](./03_reviewer_pillar.md)
- [System Flow](./system_flow.md)
- [Troubleshooting](./troubleshooting.md)

---

**Remember:** The three-pillar system is designed to create unique, cinematographically-precise,
genre-authentic artwork. Trust the system, follow these best practices, and you'll get consistently
amazing results!

