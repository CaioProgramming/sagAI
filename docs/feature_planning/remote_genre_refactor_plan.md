# Remote Genre Refactor - Expert Implementation Plan

**Agent Persona**: You are an Expert Android Developer specializing in clean architecture and
scalable systems. You pay extreme attention to detail, especially regarding "highly used fields" (
properties of `Genre` that are accessed frequently throughout the codebase). Your goal is to execute
a safe, destructive refactor that moves the `Genre` system from a hardcoded Enum to a purely
remote-driven configuration.

## 1. High-Level Strategy

We are migrating the `Genre` system to be fully driven by a remote JSON configuration. This
involves:

1. **Modeling**: Creating a JSON schema that covers all aspects of a Genre (visuals, prompts,
   logic).
2. **Repository**: Building a `GenreRepository` to fetch and expose this config.
3. **Destructive Refactor**: Stripping the `Genre` enum of its properties and forcing all usage
   sites to look up data from the Repository.
4. **No Fallbacks**: The app will rely entirely on the remote config.

## 2. Refactoring Plan

### Step 2.1: Data Modeling (`core/remote/model`)

Create the `RemoteGenreConfig.kt` file defining the JSON structure.

```kotlin
@Serializable
data class AppGenreConfig(
    val genres: List<GenreConfig>
)

@Serializable
data class GenreConfig(
    val id: String, // Matches Genre.name (e.g., "FANTASY")
    val titleKey: String? = null, // e.g. "genre_fantasy"
    val colorHex: String, // "#8B2635"
    val iconColorHex: String,
    val backgroundUrl: String, // Remote URL required
    val bubbleStyle: String, // e.g. "CYBERPUNK", "ROUND", "PIXEL" - maps to local shapes
    val referenceKeys: ReferenceKeysConfig, // New config for reference helper keys
    val prompts: GenrePromptConfig,
    val visuals: GenreVisualConfig
)

@Serializable
data class ReferenceKeysConfig(
    val artReferencesKey: String, // Single source for composition/icon/portrait references
)



@Serializable
data class GenreVisualConfig(
    val selectiveHighlight: SelectiveColorConfig,
    val colorPalette: List<String>,
    val vibrationPattern: List<Long>,
    val headerImageUrl: String? = null // Remote URL support
)

@Serializable
data class SelectiveColorConfig(
    val targetColorHex: String,
    val hueTolerance: Float,
    val saturationThreshold: Float,
    val lightnessThreshold: Float,
    val highlightSaturationBoost: Float,
    val desaturationFactorNonTarget: Float
)

@Serializable
data class GenrePromptConfig(
    val artStyle: String, // The FULL raw text string from GenrePrompts.kt
    val conversationDirective: String,
    val appearanceGuidelines: String? = null,
    val nameDirectives: String? = null,
    val validationRules: String? = null,
    val reviewerStrictness: String? = null // "STRICT", "CONSERVATIVE", "LENIENT"
)
```

### Step 2.2: Repository & State

Create `GenreRepository` (Singleton).

- `val genres: StateFlow<List<GenreConfig>>`
- `fun loadConfig()`
- `fun getGenreById(id: String): GenreConfig?`

### Step 2.3: Logic & Reference Refactor

- **`GenreReferenceHelper`**: Update to use `genreConfig.referenceKeys.artReferencesKey` instead of
  constructing keys.
- **`GenreBubble`**: Refactor `getBubbleShape` to use `genreConfig.bubbleStyle` as the primary
  lookup key. **Fallback Strategy**: If `bubbleStyle` is missing or invalid, use the `genre.id` (
  e.g., "FANTASY") to look up the shape. This allows `bubbleStyle` to just be the genre ID in most
  cases, but overrides are possible (e.g. "ROUND").
- **`GenrePrompts`**:
    - Refactor `GenrePrompts` object to receive `GenreConfig` as a parameter instead of `Genre` enum
      for its methods.
    - Example: `fun artStyle(config: GenreConfig): String = config.prompts.artStyle`
    - This effectively delegates all prompt logic to the JSON config.

### Step 2.4: Destructive Change (The "High Stakes" Part)

1. **Modify `Genre.kt`**: Remove ALL properties from the constructor. It becomes a simple list of
   keys.
   ```kotlin
   enum class Genre { FANTASY, CYBERPUNK, HORROR, HEROES, CRIME, SHINOBI, SPACE_OPERA, COWBOY, PUNK_ROCK }
   ```
2. **Refactor Usage Sites**: This will break the build. Methodically fix every error by
   injecting/accessing `GenreRepository`.
    - **Pattern**: logic accessing `genre.color` -> `repo.getGenreById(genre.name).colorHex`
    - **Pattern**: UI accessing `genre.background` -> `AsyncImage(model = config.backgroundUrl)`
    - **Specific Files**:
        - `Filters.kt`: Update `effectForGenre` to take `GenreConfig` (or just the needed visual
          config) instead of `Genre`.
        - `Type.kt`: If fonts are genre-dependent, map them using `genre.id` string.
        - `SagaPrompts.kt`: Ensure `iconDescription` uses `GenrePrompts.artStyle(config)` and
          `conversationDirective(config)` by looking up the config from the repo using `saga.genre`.
        - `ColorTones` logic: Move `Genre.colorTones()` logic into the repository or a utility that
          maps the `genre.id` string to the `ColorTone` object, or even better, fully define the
          color tone properties in the JSON `visuals` block if possible (leaving as code-mapping for
          now to reduce scope creep is acceptable).

## 3. Deliverables

### A. `genres_config.json`

Generate this file containing the configuration for ALL existing genres using the new schema. Use
placeholders for URLs (e.g., `https://example.com/fantasy_bg.jpg`).

### B. `genre_template.json`

Create a template file explaining how to add a new genre.

```json
{
  "_comment": "To add a new genre, copy this object into the 'genres' array in genres_config.json",
  "id": "NEW_GENRE_NAME",
  "titleKey": "genre_string_key",
  "colorHex": "#RRGGBB",
  "iconColorHex": "#FFFFFF",
  "backgroundUrl": "https://...",
  "bubbleStyle": "ROUND",
  "referenceKeys": {
    "artReferencesKey": "new_genre_references"
  },
  "prompts": {
    "artStyle": "Enter full prompt text here...",
    "conversationDirective": "Directive for chat persona...",
    "appearanceGuidelines": "Optional visual guidelines...",
    "nameDirectives": "Naming conventions...",
    "validationRules": "Validation logic...",
    "reviewerStrictness": "STRICT" 
  },
  "visuals": {
    "colorPalette": ["#HEX1", "#HEX2"],
    "vibrationPattern": [0, 100, 50],
    "selectiveHighlight": {
      "targetColorHex": "#TARGET",
      "hueTolerance": 0.1,
      "saturationThreshold": 0.5,
      "lightnessThreshold": 0.5,
      "highlightSaturationBoost": 1.5,
      "desaturationFactorNonTarget": 0.5
    }
  }
}
```

## 4. Execution Rules

1. **Prioritize Safety**: Ensure the Repository handles "loading" states gracefully in the UI logic
   before the destructive change breaks everything.
2. **Clean Code**: Avoid "quick fix" extension functions that rely on global mutable state if
   possible. Use Dependency Injection.
3. **Strict JSON**: Ensure the generated JSON matches the Kotlin classes perfectly.
