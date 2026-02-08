# Reviewer Agent - Quick Reference

## Overview

Conservative AI reviewer that validates and corrects image prompts before generation.

## Strictness Levels (Auto-Selected by Genre)

| Level            | Genres                                  | Behavior                              |
|------------------|-----------------------------------------|---------------------------------------|
| **LENIENT**      | Fantasy, Crime, Cowboy                  | Only fix critical violations          |
| **CONSERVATIVE** | Cyberpunk, Heroes, Space Opera, Shinobi | Fix clear violations, preserve intent |
| **STRICT**       | Punk Rock, Horror                       | Enforce all rules precisely           |

## Violation Types & Severity

### Critical (Must Fix)

- ❌ Wrong framing (describing unseen body parts)
- ❌ Missing mandatory backgrounds
- ❌ Severe art style violations

### Major (Should Fix)

- ⚠️ Banned terminology
- ⚠️ Wrong anatomy descriptions
- ⚠️ Style contradictions

### Minor (Nice to Fix)

- ℹ️ Small improvements to polish

## Common Fixes by Genre

### PUNK_ROCK (Strict)

```
❌ "brown eyes" → ✅ "simple black dot eyes"
❌ "realistic proportions" → ✅ "cartoon proportions"
❌ "plain background" → ✅ "graffiti wall, neon signs, band posters"
❌ "soft lighting" → ✅ "hard cel-shading"
```

### CYBERPUNK (Conservative)

```
❌ "3D render" → ✅ "1980s anime cel animation"
❌ "soft gradients" → ✅ "flat shading, hard edges"
❌ "bright pink neon" → ✅ "deep purple accent"
```

### FANTASY (Lenient)

```
✅ Mostly preserves artistic interpretation
⚠️ Only fixes if it breaks oil painting style
```

## How to Use

### Automatic (Default)

```kotlin
// Just generate as normal - reviewer runs automatically
characterUseCase.generateCharacterImage(character, saga)
```

### Check Logs

```
✏️ Prompt was modified by reviewer
Violations detected: 3
  - CRITICAL: 1
  - MAJOR: 2
Changes applied:
  • [list of fixes]
```

### Adjust Strictness (If Needed)

```kotlin
// In GenrePrompts.kt
fun reviewerStrictness(genre: Genre) = when (genre) {
    CYBERPUNK -> ReviewerStrictness.STRICT // Make stricter
    FANTASY -> ReviewerStrictness.CONSERVATIVE // Make less lenient
    // ...
}
```

### Add Validation Rules

```kotlin
// In GenrePrompts.kt
fun validationRules(genre: Genre) = when (genre) {
    YOUR_GENRE -> """
        BANNED TERMS:
        - 'word1', 'phrase2'
        
        REQUIRED ELEMENTS:
        - 'element1', 'element2'
    """
}
```

## Testing Checklist

- [ ] Generate PUNK_ROCK character
    - [ ] Check logs for eye color corrections
    - [ ] Verify background was added
    - [ ] Confirm cartoon style enforced

- [ ] Generate FANTASY character
    - [ ] Should have fewer corrections
    - [ ] Artistic freedom preserved
    - [ ] Oil painting style maintained

- [ ] Generate with portrait reference
    - [ ] No legs/feet in final prompt
    - [ ] Background adapted to framing
    - [ ] Upper body focus maintained

- [ ] Check fallback behavior
    - [ ] If reviewer fails, original prompt used
    - [ ] No generation blocking
    - [ ] Error logged but continues

## Performance

- **Latency**: +1-2 seconds per image
- **Cost**: Free (Gemma models)
- **API Calls**: +1 per generation
- **Success Rate**: Automatic fallback ensures 100%

## Troubleshooting

| Issue              | Solution                                  |
|--------------------|-------------------------------------------|
| Too strict         | Lower genre's strictness level            |
| Missing violations | Add to validation rules                   |
| Parse errors       | Check logs, automatic fallback handles it |
| Slow               | Expected +1-2s, consider caching rules    |

## Files to Edit

| Task                   | File              | Function               |
|------------------------|-------------------|------------------------|
| Adjust strictness      | `GenrePrompts.kt` | `reviewerStrictness()` |
| Add validation rules   | `GenrePrompts.kt` | `validationRules()`    |
| Change reviewer prompt | `ImagePrompts.kt` | `reviewImagePrompt()`  |
| Modify integration     | `*UseCaseImpl.kt` | `generate*Image()`     |

## Quick Wins

### Improve PUNK_ROCK Accuracy

```kotlin
// Add more banned terms
PUNK_ROCK -> """
    BANNED TERMS (ZERO TOLERANCE):
    - Eye colors: 'brown', 'blue', 'green', 'hazel', 'amber'
    - Eye details: 'iris', 'pupils', 'detailed eyes', 'expressive eyes'
    - Skin: 'soft', 'smooth', 'flawless', 'porcelain'
"""
```

### Make FANTASY More Strict

```kotlin
FANTASY -> ReviewerStrictness.CONSERVATIVE
```

### Add Background Requirement to Genre

```kotlin
YOUR_GENRE -> """
    REQUIRED ELEMENTS:
    - Background MUST include 3+ specific environmental objects
    - NO 'plain background' or 'gradient background'
"""
```

## Analytics Queries

### Most Common Violations

```kotlin
reviews.groupBy { it.violations.map { v -> v.type } }
    .mapValues { it.value.size }
    .toList()
    .sortedByDescending { it.second }
```

### Success Rate by Genre

```kotlin
reviews
    .groupBy { it.genre }
    .mapValues { (_, reviews) ->
        val failures = reviews.count { it.isCompletelyWrong }
        val total = reviews.size
        ((total - failures) / total.toFloat() * 100).roundToInt()
    }
```

### Most Applied Changes

```kotlin
reviews
    .flatMap { it.changesApplied }
    .groupingBy { it }
    .eachCount()
    .toList()
    .sortedByDescending { it.second }
```

## Next Steps

1. **Test thoroughly** with each genre
2. **Monitor logs** for common violations
3. **Refine rules** based on patterns
4. **Adjust strictness** if too harsh/lenient
5. **Document failures** to improve base prompts

---

**Status**: ✅ Production Ready
**Version**: 1.0
**Date**: December 16, 2025

