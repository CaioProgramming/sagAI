# Image Prompt Reviewer Agent

**Status:** ✅ Implemented
**Date:** December 16, 2025

## Overview

The Image Prompt Reviewer is a quality assurance agent that validates and corrects AI-generated
image prompts before they are sent to the image generation model. This prevents common failures
like:

- Wrong framing (describing legs in a portrait shot)
- Banned terminology (using "brown eyes" in PUNK_ROCK style which requires "simple black dot eyes")
- Missing mandatory elements (no background when the art style requires one)
- Art style contradictions (using "soft lighting" in a cel-shaded style)

## Architecture

### Flow

```
Character/Saga Context 
    ↓
Art Director Prompt (iconDescription)
    ↓
Generated Image Description
    ↓
[NEW] Reviewer Agent ← (Visual Direction + Art Style Rules + Strictness Level)
    ↓
Corrected Description + Analytics
    ↓
Image Generation Model
```

### Components

#### 1. Data Models (`ImageReference.kt`)

**ReviewerStrictness Enum**

- `LENIENT`: Only fix critical violations. For flexible styles like oil paintings.
- `CONSERVATIVE`: Fix clear violations, preserve artistic intent. Default for most styles.
- `STRICT`: Enforce all rules precisely. For highly stylized art like cartoons.

Each level has a `description` field that explains to the AI what its role is.

**ViolationType Enum**

- `FRAMING_VIOLATION`: Describing body parts outside camera view
- `BANNED_TERMINOLOGY`: Using forbidden words from art style
- `MISSING_ELEMENTS`: Background/environment not described when required
- `ANATOMY_MISMATCH`: Realistic terms when style requires stylized
- `STYLE_CONTRADICTION`: Techniques that contradict the medium

**ViolationSeverity Enum**

- `CRITICAL`: Breaks image generation or produces completely wrong output
- `MAJOR`: Significantly degrades quality
- `MINOR`: Small improvement opportunity

**ImagePromptReview Data Class**

```kotlin
data class ImagePromptReview(
    val originalPrompt: String,
    val correctedPrompt: String,
    val violations: List<PromptViolation>,
    val changesApplied: List<String>,
    val wasModified: Boolean
) {
    val isCompletelyWrong: Boolean // Has CRITICAL violations?
    val violationsBySeverity: Map<ViolationSeverity, Int>
    val violationsByType: Map<ViolationType, Int>
}
```

#### 2. Validation Rules (`GenrePrompts.kt`)

**`reviewerStrictness(genre: Genre)`**
Returns the appropriate strictness level for each genre:

- **STRICT**: PUNK_ROCK, HORROR (highly stylized with specific requirements)
- **CONSERVATIVE**: CYBERPUNK, HEROES, SPACE_OPERA, SHINOBI (balanced)
- **LENIENT**: FANTASY, CRIME, COWBOY (traditional art with flexibility)

**`validationRules(genre: Genre)`**
Returns genre-specific rules as a string, including:

- **Banned Terms**: Words/phrases that must not appear
- **Required Elements**: What must be present (e.g., background objects)
- **Anatomy Rules**: How to describe bodies/faces for that style
- **Framing Adaptations**: How backgrounds work with different shot types

Example for PUNK_ROCK:

```
BANNED TERMS (ZERO TOLERANCE):
- Eye colors: 'brown eyes', 'blue eyes', etc.
- Realistic anatomy: 'realistic proportions', 'soft skin'
- Soft lighting: 'soft lighting', 'gentle gradient'
- Empty backgrounds: 'plain background', 'isolated portrait'

REQUIRED ELEMENTS:
- Eyes MUST be: 'simple black dot eyes', 'hollow circular eyes'
- Background MUST include 3+ specific objects
- Proportions MUST be: 'cartoon proportions', 'exaggerated limbs'
```

#### 3. Reviewer Prompt (`ImagePrompts.kt`)

**`reviewImagePrompt(visualDirection, artStyleValidationRules, strictness, finalPrompt)`**

Creates a structured prompt that:

1. Defines the reviewer's role using the strictness description
2. Checks framing compliance (extracts shot type from visual direction)
3. Validates art style compliance (scans for banned terms, checks required elements)
4. Validates background requirements
5. Returns JSON with corrections and analytics

Output format:

```json
{
  "correctedPrompt": "...",
  "violations": [
    {
      "type": "BANNED_TERMINOLOGY",
      "severity": "MAJOR",
      "description": "Used 'brown eyes' when style requires dot eyes",
      "example": "cast brown eyes"
    }
  ],
  "changesApplied": [
    "Replaced 'brown eyes' with 'simple black dot eyes'",
    "Added background details: graffiti wall, amp stack"
  ],
  "wasModified": true
}
```

#### 4. ImagenClient Integration

**`reviewAndCorrectPrompt()` method**

- Calls Gemma with the reviewer prompt
- Parses JSON response into `ImagePromptReview`
- Handles errors gracefully (returns original on parse failure)
- Logs detailed analytics:
    - Total violations
    - Violations by severity
    - Changes applied
    - Whether prompt was "completely wrong" (has CRITICAL violations)

#### 5. Integration Points

The reviewer is integrated into all image generation flows:

**CharacterUseCaseImpl.generateCharacterImage()**

```kotlin
val translatedDescription = gemmaClient.generate(...)
val reviewedPrompt = imagenClient.reviewAndCorrectPrompt(...)
val finalPrompt = reviewedPrompt?.correctedPrompt ?: translatedDescription
val image = imagenClient.generateImage(finalPrompt)
```

**SagaRepositoryImpl.generateSagaIcon()**
**ChapterUseCaseImpl.generateChapterCover()**
Same pattern in both.

## Analytics & Metrics

### What Makes a Prompt "Completely Wrong"?

A prompt is considered **completely wrong** if it has any `CRITICAL` violations:

- Wrong framing (full body description when portrait requested)
- Missing mandatory backgrounds when art style requires them
- Severe art style violations that would break rendering

Otherwise, it's just an **improvement** (MAJOR or MINOR violations).

### Tracking

The system automatically logs:

1. **Modification Rate**: How often prompts are modified
2. **Violation Distribution**: Most common violation types per genre
3. **Severity Distribution**: How many CRITICAL vs MAJOR vs MINOR issues
4. **Changes Applied**: What specific fixes were made

Example log output:

```
✏️ Prompt was modified by reviewer
Violations detected: 3
  - CRITICAL: 0
  - MAJOR: 2
  - MINOR: 1
Changes applied:
  • Replaced 'brown eyes' with 'simple black dot eyes'
  • Removed description of legs (not visible in portrait framing)
  • Added background details: brick wall, neon signs
```

## Usage

The reviewer runs automatically - no code changes needed when generating images. However, you can:

### Adjust Strictness for a Genre

Edit `GenrePrompts.reviewerStrictness()`:

```kotlin
CYBERPUNK -> ReviewerStrictness.STRICT // Make more strict
FANTASY -> ReviewerStrictness.CONSERVATIVE // Make less lenient
```

### Add New Validation Rules

Edit `GenrePrompts.validationRules()` for the specific genre:

```kotlin
SHINOBI -> """
    BANNED TERMS:
    - 'modern clothing', 'sneakers'
    
    REQUIRED ELEMENTS:
    - Traditional Japanese garments
    - Period-appropriate setting
"""
```

### Disable Reviewer (Emergency Fallback)

If the reviewer causes issues, the system automatically falls back to the original description if:

- Review returns null
- JSON parsing fails
- Network error during review

## Performance

- **Cost**: Free (using Gemma models)
- **Latency**: +1-2 seconds per image generation
- **Rate Limits**: Adds 1 extra Gemma call per image (total: 3 calls)
    1. Extract composition from reference
    2. Generate description
    3. **[NEW]** Review & correct description
    4. Generate image

## Future Improvements

1. **Caching**: Cache validation rules per genre to avoid re-parsing
2. **Learning**: Track which violations are most common to improve base prompts
3. **A/B Testing**: Compare images with/without reviewer to measure quality impact
4. **Auto-tune**: Adjust strictness based on failure rates
5. **Multi-language**: Extend to handle non-English validation rules

## Testing

To test the reviewer:

1. **Generate a character in PUNK_ROCK genre** (strict)
    - Check logs for violations detected
    - Verify no "brown eyes" in final prompt
    - Verify background is described

2. **Generate a character in FANTASY genre** (lenient)
    - Should have fewer corrections
    - More artistic freedom preserved

3. **Force violations** by manually editing `iconDescription()` output
    - Add "brown eyes" for PUNK_ROCK
    - Add "plain background" for PUNK_ROCK
    - Verify reviewer catches and fixes them

## Troubleshooting

**Reviewer fails to parse JSON**

- Check Gemma response in logs
- Ensure JSON format in prompt is correct
- System falls back to original prompt automatically

**Too many corrections / too strict**

- Adjust genre strictness level
- Refine validation rules to be more lenient

**Missing violations**

- Add specific banned terms to validation rules
- Increase strictness level for that genre

**Performance issues**

- Reviewer adds ~1-2 seconds per image
- Consider caching or disabling for free tier users

