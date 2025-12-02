# Character Reveal & Card Refactor 🥊

## Goal
Enhance the character creation experience in the chat by introducing a dramatic "New Challenger Arrives" style reveal. When a new character is generated, the chat will momentarily pause to display a stylized card of the new character, similar to Super Smash Bros or Marvel vs Capcom introduction screens.

## Scope
1.  **Component Refactor**: Extract the card UI from `CharacterShareView` into a reusable `CharacterCard` component.
2.  **New Feature**: Implement `CharacterRevealDialog` (or Overlay) in `ChatView`.
3.  **Visuals**: Add glow effects, drop shadows, and genre-based shaping to the card.

## Technical Approach

### 1. CharacterCard Component (Refactor)
*   Extract the core card layout from `CharacterShareView.kt` into a new Composable: `CharacterCard`.
*   **Parameters**:
    *   `character: Character`: The primary data model.
    *   `description: String?`: Optional text to display (e.g., bio or "New Challenger").
    *   `showWatermark: Boolean`: Toggle for the app logo/watermark (default `false` for chat, `true` for share).
    *   `caption: String?`: Optional caption text (for the Share view usage).
    *   `modifier: Modifier`: To allow external styling (like genre shapes).
*   **Logic**:
    *   If `description` is provided, it should be clipped/truncated (e.g., max 100 chars) to avoid overcrowding.
    *   Maintain the existing visual fidelity (images, text styles).

### 2. Character Reveal UI
*   Create a new overlay/dialog component: `CharacterRevealOverlay`.
*   **Visual Style**:
    *   "Super Smash Bros" / "Marvel vs Capcom" vibe.
    *   **Background**: Follow `StarryLoader` concepts - blur the background content (similar to `StarryLoader`'s blur effect).
    *   **Card Border**: Apply a **rotating gradient border** (like `StarryLoader`'s sweep gradient animation) using the saga's genre colors.
    *   **Shape**: Use `genre.shape()` to clip the card, integrating it with the current saga's theme.
    *   **Floating Animation**: Smooth, infinite floating animation (vertical oscillation) to make the card feel alive and suspended in air.
*   **Structure**:
    *   Reuse `StarryLoader` concepts for the overlay background/blocking interaction.
    *   Display the `CharacterCard` centrally.
*   **Behavior (Snackbar-like)**:
    *   **Auto-dismiss**: Display for **7 seconds**, then automatically dismiss.
    *   **User Dismissal**: If the user taps/clicks the card, dismiss immediately.
    *   **Non-blocking**: After dismissal, restore normal chat interaction.

### 3. Integration
*   **ChatView / ChatViewModel**:
    *   Detect when a new character is created.
    *   Trigger the `CharacterRevealOverlay`.
    *   Freeze/Pause chat interaction while the reveal is active.
    *   Auto-dismiss after 7 seconds or on user tap.

## Tasks
- [x] Refactor `CharacterShareView.kt`: Extract `CharacterCard` composable.
- [x] Update `CharacterShareView` to use the new `CharacterCard`.
- [x] Create `CharacterRevealOverlay` composable:
  - [x] Implement blur background (following `StarryLoader` pattern).
  - [x] Add rotating gradient border using genre colors (sweep gradient animation).
  - [x] Apply genre-based shape clipping to the card.
  - [x] Implement floating animation (smooth vertical oscillation).
  - [x] Add 7-second auto-dismiss timer.
  - [x] Implement tap-to-dismiss functionality.
- [x] Integrate `CharacterRevealOverlay` into `ChatView`.
- [x] Connect `ChatViewModel` state to trigger the reveal on new character creation.
- [x] Polish animations and visual effects ("New Challenger" vibe).
