# Revamp Saga Creation Flow

## Objective

To create a more user-friendly, magical, and less stressful saga creation experience by replacing
the chat-based interface with an AI-powered flip-card system.

## Key Features Implemented

### 1. Unified Flip Card Interface

- **Edit Mode (Front Face):**
    - Displays `EditCard` with a large text input area.
    - Integrates `StorySeedRow` for quick idea generation using AI suggestions.
    - Includes a "Generate ✨" button that triggers AI refinement.
- **Review Mode (Back Face):**
    - Displays `ReviewCard` showing the AI-generated title, subtitle, and description.
    - Automatically flips to this mode once content is generated.
    - Includes a "Continue ->" or "Create Saga 🚀" button based on the flow stage.

### 2. Reactive UI Components

- **Dynamic Button:** The main action button changes text, color, and behavior based on the current
  state (Edit vs. Review) and flow (Saga vs. Character).
- **Levitating Genre Cards:** In the theme selection sheet, the selected `GenreCard` now uses a
  `levitate` modifier to visually float, emphasizing the user's choice.
- **Shimmer Effects:**
    - The card shimmers during AI generation ("refining").
    - A full-screen `StarryLoader` overlay appears during the final save process.

### 3. State Management

- **Auto-Flip:** The card automatically flips to the review side when the `isReady` state becomes
  true (i.e., AI has finished generation).
- **Flow Control:**
    - `NewSagaViewModel` manages the state for both Saga and Character creation steps.
    - The UI seamlessly transitions between these steps using the same flip-card container logic.

## Technical Details

- **Composables:** `NewSagaView`, `EditCard`, `ReviewCard`, `FlipCard`.
- **Modifiers:** `levitate`, `reactiveShimmer`.
- **View Model:** `NewSagaViewModel` handles `refineDraft`, `saveSaga`, and state observation.

## Verification

- **Build:** Validated via `./gradlew installDebug`.
- **Flow:** Tested the complete path from Genre Selection -> Saga Input -> AI Generation -> Flip to
  Review -> Character Input -> AI Generation -> Flip to Review -> Save.
