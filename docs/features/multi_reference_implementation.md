# Multi-Reference Implementation Summary

## Overview

Implemented a structured approach to multi-reference image support using the `ReferenceCollection`
data model.

## Changes Made

### 1. Created Data Model

**File:** `/app/src/main/java/com/ilustris/sagai/core/file/model/ReferenceCollection.kt`

```kotlin
@Serializable
data class ReferenceCollection(
    val references: List<String>,
) {
    fun getRandomReference(): String {
        require(references.isNotEmpty()) { "Reference collection is empty" }
        return references.random()
    }
    
    val size: Int get() = references.size
}
```

**Benefits:**

- ✅ Type-safe JSON parsing with kotlinx.serialization
- ✅ Clean API: `collection.getRandomReference()`
- ✅ Extensible for future features (weights, metadata, etc.)

### 2. Updated GenreReferenceHelper

**File:** `/app/src/main/java/com/ilustris/sagai/core/file/GenreReferenceHelper.kt`

**Added:**

- Json parser instance with lenient configuration
- Updated `getRandomPortraitReference()` to use `ReferenceCollection`

**Parsing Flow:**

```kotlin
// 1. Fetch from Remote Config
val flagValue = firebaseRemoteConfig.getString("portrait_references")

// 2. Deserialize to model
val collection = json.decodeFromString<ReferenceCollection>(flagValue)

// 3. Get random reference
val selectedUrl = collection.getRandomReference()

// 4. Download image
// ... Coil download logic
```

### 3. Updated Documentation

**File:** `/docs/features/multi_reference_images.md`

- Updated all JSON examples to show new structure
- Updated code flow descriptions
- Updated troubleshooting section
- Updated migration guide

## JSON Structure

### Firebase Remote Config Format

```json
{
  "references": [
    "https://i.pinimg.com/736x/92/da/84/92da844f0a88588a8a3ac9bc46d55dfc.jpg",
    "https://i.pinimg.com/736x/52/07/3f/52073f96dff75cdda3d6a4df347ffa8d.jpg",
    "https://i.pinimg.com/736x/f6/19/8f/f6198f97d5d2b9fbff2b2359c4cc3057.jpg",
    "https://i.pinimg.com/736x/9d/9b/82/9d9b8272da8094eaba0a507eb9f4e849.jpg",
    "https://i.pinimg.com/736x/cb/45/d3/cb45d322c90e47951f5009e83b2cb562.jpg",
    "https://i.pinimg.com/1200x/a1/2b/6f/a12b6fd691b7ee1d824979664561cd45.jpg"
  ]
}
```

### Why This Structure?

1. **Extensibility** - Easy to add new fields later:
   ```json
   {
     "references": ["..."],
     "weights": [0.3, 0.2, 0.2, 0.1, 0.1, 0.1],
     "metadata": {...}
   }
   ```

2. **Clarity** - Clear separation of data
3. **Type Safety** - Proper Kotlin data class mapping
4. **Validation** - Model can enforce constraints

## Usage

### In Firebase Remote Config

1. Go to Firebase Console → Remote Config
2. Create parameter: `portrait_references`
3. Paste your JSON:
   ```json
   {
     "references": [
       "url1",
       "url2",
       "url3"
     ]
   }
   ```
4. Publish changes

### In Code

```kotlin
// Character generation automatically uses random reference
genreReferenceHelper.getRandomPortraitReference()

// Logs will show:
// D/GenreReferenceHelper: selected 1 of 6 references -> https://...
```

## Backward Compatibility

✅ **Complete backward compatibility:**

- Older app versions ignore `portrait_references` and use `portrait_reference`
- New app versions try `portrait_references` first, fall back to `portrait_reference`
- If JSON parsing fails → fallback
- If download fails → fallback
- If array is empty → fallback

## Future Enhancements

### Weighted Selection

```kotlin
@Serializable
data class ReferenceCollection(
    val references: List<String>,
    val weights: List<Float>? = null
) {
    fun getWeightedReference(): String {
        // Implement weighted random selection
    }
}
```

### Genre-Specific Collections

```kotlin
suspend fun getRandomIconReference(genre: Genre): RequestResult<Bitmap> {
    val flag = "${genre.name.lowercase()}_icon_references"
    // Same logic but genre-specific
}
```

### A/B Testing Integration

```kotlin
@Serializable
data class ReferenceCollection(
    val references: List<ReferenceWithMetadata>
)

@Serializable
data class ReferenceWithMetadata(
    val url: String,
    val variant: String,
    val tags: List<String>
)
```

## Testing Checklist

- [ ] Create `portrait_references` flag in Firebase Remote Config
- [ ] Paste your 6 Pinterest URLs in the JSON structure
- [ ] Generate a character avatar
- [ ] Check logs for: `"selected 1 of 6 references"`
- [ ] Generate multiple avatars to see variety
- [ ] Verify fallback works (temporarily break JSON to test)

## Logs to Monitor

```
✅ Success:
D/GenreReferenceHelper: getRandomPortraitReference: selected 1 of 6 references -> https://i.pinimg.com/...

⚠️ Fallback:
W/GenreReferenceHelper: failed to parse ReferenceCollection JSON, falling back to single reference

❌ Error:
E/GenreReferenceHelper: failed to load multi-reference, attempting single reference fallback
```
