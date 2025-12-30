# Image Prompt Reviewer - Implementation Summary

## What Was Implemented

A **conservative reviewer agent** that validates and corrects AI-generated image prompts before they
reach the image generation model. This acts as a quality assurance layer to catch violations of art
style rules, framing requirements, and composition guidelines.

## Key Features

### 1. **Genre-Specific Strictness Levels**

Each genre has an appropriate strictness level based on its requirements:

- **STRICT** (PUNK_ROCK, HORROR): Highly stylized with specific anatomical rules
- **CONSERVATIVE** (CYBERPUNK, HEROES, SPACE_OPERA, SHINOBI): Balanced approach
- **LENIENT** (FANTASY, CRIME, COWBOY): Traditional art with organic flexibility

Each strictness level has a description field that explains to the AI what its role is.

### 2. **Comprehensive Violation Tracking**

The system tracks:

- **Type**: Framing, banned terminology, missing elements, anatomy mismatch, style contradiction
- **Severity**: Critical (breaks generation), Major (degrades quality), Minor (small improvement)
- **Analytics**: Violation counts by type and severity, changes applied

### 3. **Measurable Metrics**

The system can distinguish between:

- **"Completely Wrong"**: Has CRITICAL violations (wrong framing, missing mandatory elements)
- **"Improved"**: Only MAJOR or MINOR violations (terminology tweaks, small additions)

### 4. **Automatic Fallback**

If the reviewer fails (parse error, network issue), the system automatically uses the original
prompt and logs the issue. Generation never blocks on review failures.

## Files Modified

### Created

- `ImageReference.kt`: Added `ReviewerStrictness`, `ViolationType`, `ViolationSeverity`,
  `PromptViolation`, `ImagePromptReview`

### Updated

- **GenrePrompts.kt**: Added `reviewerStrictness()` and `validationRules()` functions with detailed
  rules for each genre
- **ImagePrompts.kt**: Added `reviewImagePrompt()` function
- **ImagenClient.kt**: Added `reviewAndCorrectPrompt()` method with JSON parsing and logging
- **CharacterUseCaseImpl.kt**: Integrated reviewer into character image generation
- **SagaRepositoryImpl.kt**: Integrated reviewer into saga icon generation
- **ChapterUseCaseImpl.kt**: Integrated reviewer into chapter cover generation

## How It Works

```
1. Generate description (Art Director prompt)
2. Extract visual direction (framing, mood, lighting)
3. [NEW] Review description against art style rules
4. Get corrected description OR original if review fails
5. Generate image with final description
```

## Example: PUNK_ROCK Validation

### Before Reviewer

```
"Gizella's expression is defiant. Her eyes are cast brown, intense..."
```

### Violations Detected

- **MAJOR**: Banned terminology "cast brown" (eyes must be "simple black dots")
- **CRITICAL**: Missing background (style requires 3+ specific objects)

### After Reviewer

```
"Gizella's expression is defiant. Her eyes are simple black dots, 
intense and hollow. Behind her, a graffiti-covered brick wall with 
acid green neon signs and tattered band posters creates the gritty 
urban backdrop..."
```

### Log Output

```
✏️ Prompt was modified by reviewer
Violations detected: 2
  - CRITICAL: 1
  - MAJOR: 1
Changes applied:
  • Replaced 'cast brown eyes' with 'simple black dot eyes'
  • Added background details: brick wall, graffiti, neon signs
⚠️ CRITICAL violations found - prompt was completely wrong!
```

## Validation Rules Examples

### PUNK_ROCK (Strict)

```
BANNED: eye colors, realistic anatomy, soft lighting, plain backgrounds
REQUIRED: dot eyes, cartoon proportions, 3+ background objects
```

### CYBERPUNK (Conservative)

```
BANNED: 3D CGI, photorealism, soft gradients, bright neon
REQUIRED: 1980s anime style, cel-shading, hard edges, muted blue palette
```

### FANTASY (Lenient)

```
REQUIRED: oil painting technique, earthy palette, brushstrokes
ALLOWS: Artistic interpretation within traditional art boundaries
```

## Performance

- **Cost**: Free (Gemma models)
- **Latency**: +1-2 seconds per image
- **API Calls**: +1 per image generation (3 total: extract, describe, review, generate)

## Testing

1. **Generate PUNK_ROCK character** → Should catch eye colors and missing backgrounds
2. **Generate FANTASY character** → Should be more lenient with artistic choices
3. **Check logs** → See violations detected and changes applied

## Next Steps

### For You

1. Test with different genres to see strictness in action
2. Check logs to see what violations are caught
3. Adjust strictness levels if needed (`GenrePrompts.reviewerStrictness()`)
4. Add more banned terms to validation rules if you spot issues

### Future Enhancements

1. Cache validation rules per genre
2. Track violation patterns to improve base prompts
3. A/B test: images with vs without reviewer
4. Auto-adjust strictness based on failure rates

## Documentation

Full documentation available at:
`docs/feature_planning/image_composition/reviewer_agent.md`

## Summary

The reviewer agent is now **fully integrated** and will automatically validate all image prompts
before generation. It's conservative by default (only fixes clear violations), uses
genre-appropriate strictness levels, provides detailed analytics, and has automatic fallback to
ensure generation never fails due to review issues.

The system is ready to test! 🚀

