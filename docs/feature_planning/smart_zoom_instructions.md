# specialized_agent_instructions.md

**Role:** You are an Expert Android Developer specializing in Kotlin, Jetpack Compose, and Room
Database.
**Task:** Implement "Subject-Based Smart Zoom with Persistent Metadata" for the SagAI application.

## Context

We need to improve the avatar display in `CharacterAvatar` by automatically zooming in on subjects
that have too much negative space, WHILE skipping zoom for subjects that are already close-ups. We
will persist these zoom calculations (Metadata) in the Room database to avoid recalculating them.

## Implementation Steps

### 1. Data Layer Modifications

**File:** `app/src/main/java/com/ilustris/sagai/features/characters/data/model/Character.kt`

- Define a `SmartZoom` data class (can be embedded or a serialized JSON string, or simple columns):
  ```kotlin
  data class SmartZoom(
      val scale: Float = 1f,
      val translationX: Float = 0f,
      val translationY: Float = 0f,
      val needsZoom: Boolean = false
  )
  ```
- Update the `Character` entity to include `smartZoom` (e.g.,
  `@Embedded(prefix = "zoom_") val smartZoom: SmartZoom? = null`).

**File:** `app/src/main/java/com/ilustris/sagai/core/database/SagaDatabase.kt`

- Increment the database version `version`.
- Provide a migration strategy (AutoMigration should work since we are just adding columns).

### 2. Core Logic Implementation

**File:** `app/src/main/java/com/ilustris/sagai/core/segmentation/ImageSegmentationHelper.kt`

- Extend this helper to include a method `calculateSubjectZoomRegion(bitmap: Bitmap): SmartZoom`.
- **Logic:**
    1. Process the bitmap (or re-use segmentation result) to find the subject's bounding box.
    2. Calculate the ratio of the subject to the frame.
    3. **Threshold:** If the subject occupies > 65% of the frame (width or height), `needsZoom` is
       `false`.
    4. If < 65%, calculate a `scale` (e.g., 1.5f) and `translationX/Y` to center the subject.
    5. Return the `SmartZoom` object.

### 3. UseCase Logic

**File:**
`app/src/main/java/com/ilustris/sagai/features/characters/data/usecase/CharacterUseCase.kt`

- Add function `checkAndGenerateZoom(character: Character)`:
    - Check if `character.smartZoom` is already populated. If yes, return to avoid redundant calls (
      crucial for lists/bubbles).
    - If null, use `ImageSegmentationHelper` to process the image URL.
    - Get the `SmartZoom` result.
    - Update the `Character` in the database with the new `smartZoom` data.
- **Optimization:** Trigger this logic during Character Creation if the image is available, to
  pre-populate the DB.
- **Optimization:** For `CharacterGalleryView` or `ChatBubble`, ensure we check
  `character.smartZoom != null` before queuing any background work to prevent mass-processing on
  scroll.

### 4. UI Updates

**File:** `app/src/main/java/com/ilustris/sagai/features/characters/ui/CharacterAvatar.kt`

- Update the Composable to observe or receive `character.smartZoom`.
- **Animation:** use `animateFloatAsState` for `scaleX`, `scaleY`, `translationX`, `translationY` to
  ensure smooth visual updates if the zoom loads in later.
- **Trigger:** In `AsyncImage`'s `onSuccess` callback, if `character.smartZoom` is null, trigger the
  generation logic (via a ViewModel, SideEffect, or Controller singleton depending on arch).
- Apply the zoom using `.graphicsLayer`:
  ```kotlin
  .graphicsLayer {
      scaleX = animatedScale
      scaleY = animatedScale
      translationX = animatedTx
      translationY = animatedTy
  }
  ```

### 5. Character Details Animation

**File:** `app/src/main/java/com/ilustris/sagai/features/characters/ui/CharacterDetailsView.kt`

- **Initial State:** When opening, if `smartZoom` exists, initialize the image state *with the zoom
  applied* (Scale > 1f).
- **Transition:** After a delay (e.g., 2 seconds) or upon user interaction, animate the
  scale/translation back to **1.0f (Identity)**.
- **Effect:** This creates a "reveal" effect, starting focused on the face (avatar style) and then
  expanding to show the full costume/context.
- **Text Reveal:** The Character Name should be hidden (alpha 0f) initially. Use
  `animateFloatAsState` to fade it in (alpha 1f) *only after* the zoom-out animation completes or
  starts. This ensures the user focuses on the reveal first.
  ```

## Verification

1. **DB Migration:** Ensure the app doesn't crash on boot and the schema is updated.
2. **already-zoomed:** Open a character like "Julie" (large portrait). Verify NO zoom jump occurs.
3. **needs-zoom:** Open a character with negative space. Verify it smoothly zooms in.
4. **Persistence:** Kill the app and reopen. The zoom should be applied instantly without
   recalculation.
