# Depth Effect & Vertical UI Refactor

## Goal
Implement a visual "Depth Effect" similar to the iOS lockscreen, where text (titles, names) can be layered between a subject (foreground) and the background. This will be achieved using MLKit's Subject Segmentation to separate the foreground subject from the image.

## Scope
1.  **Saga Header (ChatView)**: Apply depth effect to the saga header image, allowing the Saga Title to sit behind the main subject but in front of the background.
2.  **Saga Icon (SagaDetailsView)**: Apply depth effect to the Saga Icon.
3.  **Character Details (CharactersDetailView)**: Integrate the character name into the art using the same depth technique.
4.  **Vertical Structure Refactor**: Refactor the UI to support a more vertical, immersive structure that complements this visual effect.

## Technical Approach

### 1. MLKit Integration
*   Use **MLKit Subject Segmentation API**.
*   Create a helper/manager class to handle image processing:
    *   Input: `Bitmap` (from URL or resource).
    *   Output: `Foreground Bitmap` (Subject), `Background Bitmap` (Original minus subject, or just original layered behind).
    *   *Note*: We might need to cache these segmented bitmaps to avoid re-processing on every view load.

### 2. UI Components
*   **DepthLayout (Composable)**:
    *   A custom Composable that takes:
        *   `originalImage`: The full image.
        *   `segmentedImage`: The subject mask/bitmap.
        *   `content`: The Composable (Text/Title) to place *between* the layers.
    *   Logic:
        1.  Render `originalImage` (Background).
        2.  Render `content` (Title/Text).
        3.  Render `segmentedImage` (Foreground) on top, perfectly aligned with the background.

### 3. View Updates
*   **ChatView**: Update the top bar/header area to use `DepthLayout`.
*   **SagaDetailsView**: Update the icon display.
*   **CharactersDetailView**: Redesign to allow the name to interact with the character image.

### 4. Refactoring
*   **Vertical Structure**: Review current layouts and adjust for a more vertical flow where the image takes up more vertical space to allow for the depth effect to shine.

## Tasks
- [ ] Research MLKit Subject Segmentation dependencies and setup.
- [ ] Create `ImageSegmentationHelper` to handle MLKit processing.
- [ ] Create `DepthLayout` Composable.
- [ ] Refactor `ChatView` header.
- [ ] Refactor `SagaDetailsView` icon.
- [ ] Refactor `CharactersDetailView`.
- [ ] Optimize performance (caching segmented images).
