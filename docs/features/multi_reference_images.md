# Multi-Reference Image Setup Guide

## Overview

The app now supports **multiple reference images** for character avatar generation. Instead of using
a single fixed reference image, you can provide an array of reference URLs, and the app will
randomly select one for each generation. This creates more compositional variety while maintaining
quality.

## Backward Compatibility

✅ **Fully backward compatible** - older app versions will continue to use the single-reference
flag (`portrait_reference`)

## Firebase Remote Config Setup

### 1. Create the New Flag

In Firebase Console → Remote Config, create a new parameter:

**Parameter Name:** `portrait_references` (note the plural!)

**Type:** String

**Value Format:** JSON Object with "references" array

```json
{
  "references": [
    "https://example.com/portrait1.jpg",
    "https://example.com/portrait2.jpg",
    "https://example.com/portrait3.jpg",
    "https://example.com/portrait4.jpg"
  ]
}
```

### 2. Keep the Existing Flag

**Do NOT delete or modify** the existing `portrait_reference` flag (singular). It serves as:

- Fallback for the new multi-reference system
- Support for older app versions

### 3. Example Setup

```json
// NEW FLAG: portrait_references
{
  "references": [
    "https://storage.googleapis.com/your-bucket/references/intense_portrait.jpg",
    "https://storage.googleapis.com/your-bucket/references/dramatic_angle.jpg",
    "https://storage.googleapis.com/your-bucket/references/soft_lighting.jpg",
    "https://storage.googleapis.com/your-bucket/references/editorial_style.jpg"
  ]
}

// OLD FLAG: portrait_reference (keep as fallback)
"https://storage.googleapis.com/your-bucket/references/default_portrait.jpg"
```

## How It Works

### Selection Logic

1. **Try Multi-Reference First**
    - Fetches `portrait_references` flag
    - Parses the JSON array
    - Randomly selects one URL using `Random.nextInt()`
    - Downloads and uses that image

2. **Fallback to Single Reference**
    - If `portrait_references` doesn't exist → uses `portrait_reference`
    - If JSON parsing fails → uses `portrait_reference`
    - If array is empty → uses `portrait_reference`
    - If download fails → uses `portrait_reference`

### Code Flow

```kotlin
// App calls this for character generation:
genreReferenceHelper.getRandomPortraitReference()

// Under the hood:
1. Fetch portrait_references flag
2. Parse as ReferenceCollection model (JSON deserialization)
3. Call collection.getRandomReference() -> uses Kotlin's random()
4. Download image at that URL
5. Return bitmap

// If any step fails:
→ Call getPortraitReference() as fallback
```

## Benefits

### ✅ Compositional Variety

- Different character generations use different reference compositions
- Reduces repetitive visual patterns
- Makes each avatar feel more unique

### ✅ Quality Testing

- Easily test multiple reference styles without changing configs constantly
- Keep all your "working" references active simultaneously
- Remove underperforming references from the array

### ✅ Safe Experimentation

- Add new reference images to the array
- Monitor analytics/quality
- Keep or remove based on results

## Usage Examples

### Character Avatar Generation

```kotlin
// CharacterUseCaseImpl.kt
val portraitReference = genreReferenceHelper.getRandomPortraitReference()
    .getSuccess()?.let {
        ImageReference(it, ImagePrompts.extractComposition())
    }
```

Each time a character avatar is generated, a **different reference image** may be selected, leading
to varied compositions.

### Extending to Other Features

You can apply this pattern to other reference types:

```kotlin
// For genre-specific references
suspend fun getRandomIconReference(genre: Genre): RequestResult<Bitmap>

// For cover art
suspend fun getRandomCoverReference(genre: Genre): RequestResult<Bitmap>
```

Just create new Remote Config flags following the pattern:

- `fantasy_icon_references` (multi)
- `fantasy_icon_reference` (single, fallback)

## Monitoring

### Logs to Watch

```
D/GenreReferenceHelper: getRandomPortraitReference: attempting to fetch multi-reference flag portrait_references
D/GenreReferenceHelper: getRandomPortraitReference: selected URL 2 of 4 -> https://...
```

Or on fallback:

```
W/GenreReferenceHelper: getRandomPortraitReference: failed to parse multi-reference JSON, falling back to single reference
```

### Analytics

Track which reference was used by adding the selected URL to your image generation analytics.

## Best Practices

### 🎯 Reference Selection

- **Quality over quantity**: 3-5 high-quality references is better than 20 mediocre ones
- **Consistent style**: All references should work well with your genre/theme
- **Tested variety**: Each reference should produce different but equally good results

### 📐 Compositional Diversity

Choose references with varied:

- **Angles**: Low-angle, high-angle, dutch-tilt
- **Lighting**: Side, back, dramatic, soft
- **Framing**: Close-up, medium close-up, medium shot
- **Mood**: Intense, calm, mysterious, confident

### 🔄 Update Strategy

1. Start with your current best reference in the array
2. Add 1-2 new references
3. Monitor results for a few days
4. Keep what works, remove what doesn't
5. Iterate

## Troubleshooting

### Issue: All generations use the same reference

**Cause:** `portrait_references` flag not found, falling back to single reference
**Solution:** Verify the flag name is exactly `portrait_references` (plural) in Firebase Console

### Issue: JSON parsing error

**Cause:** Malformed JSON in Remote Config value
**Solution:** Validate your JSON object syntax:

```json
{"references": ["url1", "url2", "url3"]}  ✅ Correct
{'references': ['url1', 'url2', 'url3']}  ❌ Single quotes invalid
{"references": ["url1" "url2"]}           ❌ Missing commas
["url1", "url2"]                          ❌ Missing "references" key
```

### Issue: Empty array error

**Cause:** `portrait_references` value has empty "references" array
**Solution:** Add at least one URL to the "references" array or remove the flag to use
single-reference fallback

```json
{"references": []} ❌ Empty
{"references": ["https://..."]} ✅ Valid
```

## Migration from Single to Multi-Reference

### Step 1: Test Locally

Before updating Remote Config, test multiple references work correctly.

### Step 2: Add to Remote Config

Create `portrait_references` with your current best reference + 1-2 new ones:

```json
{
  "references": [
    "https://your-current-reference.jpg",
    "https://new-reference-1.jpg"
  ]
}
```

### Step 3: Monitor

Watch logs and user feedback for 24-48 hours.

### Step 4: Expand

Gradually add more references to the array as you find good ones.

### Step 5: Maintain Fallback

Always keep `portrait_reference` (singular) updated with your best reference for older app versions.

## Future Extensions

### Genre-Specific Multi-References

```kotlin
suspend fun getRandomIconReference(genre: Genre): RequestResult<Bitmap> {
    val multiFlag = "${genre.name.lowercase()}_icon_references"
    val singleFlag = "${genre.name.lowercase()}_icon_reference"
    // ... similar logic
}
```

### Weighted Selection

Instead of pure random, implement weighted probabilities:

```json
{
  "references": [
    {"url": "...", "weight": 0.5},
    {"url": "...", "weight": 0.3},
    {"url": "...", "weight": 0.2}
  ]
}
```

### A/B Testing Integration

Track which references produce the best results through analytics and automatically adjust
selection.
