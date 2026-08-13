# Character Generation Technical Implementation 🛠️

This document outlines the technical architecture, state management, and data flow for the Character Creation feature within the Saga Creation flow.

---

## 1. Architecture Overview

The creation flow uses a **Unified ViewModel with Dual State Managers** pattern to separate concerns while maintaining a cohesive user experience.

### Core Components

*   **ViewModel:** `NewSagaViewModel`
    *   Acts as the orchestrator.
    *   Exposes unified state to the UI.
    *   Handles high-level actions (Save, Retry, Navigation).
*   **Managers:**
    *   `SagaStateManager`: Handles Saga-specific logic (Title, Description, Genre).
    *   `CharacterStateManager`: Handles Character-specific logic (Name, Backstory, Appearance).
*   **Use Cases:**
    *   `NewSagaUseCase`: AI logic for Saga generation.
    *   `NewCharacterUseCase`: AI logic for Character generation.

---

## 2. State Management

The `NewSagaViewModel` combines state from both managers to drive the UI.

### Key StateFlows
*   `sagaReady` & `characterReady`: Boolean flags indicating if minimum data requirements are met.
*   `isReadyToSave`: Computed as `sagaReady && characterReady`.
*   `currentGenre`: Shared state that affects the visual theme of both pages.
*   `formState`: Holds the current draft data (`SagaDraft` / `Character`).

### Initialization
*   **Saga Chat:** Initialized via `LaunchedEffect(Unit)` when the screen loads.
*   **Character Chat:** Initialized via `LaunchedEffect(pagerState.currentPage)` when the user swipes to the Character page (Page 1).

---

## 3. Data Flow & AI Interaction

We moved from a multi-step prompt chain to a **Single Conversational Prompt** for better performance and natural interaction.

### The Flow
1.  **User Input:** User types a message (e.g., "She's a cyberpunk hacker").
2.  **State Manager:** Sends input to UseCase.
3.  **UseCase (AI):**
    *   Calls `gemmaClient.generate` with `conversationalCharacterReply`.
    *   **Extracts & Enhances:** The AI parses the input *and* suggests improvements (e.g., specific cyberware, detailed backstory).
    *   **Returns Callback:** A `CharacterCreationGen` object containing the `Character` draft and an `action`.
4.  **Callback Handling:**
    *   `UPDATE_DATA`: Updates the form but keeps `characterReady = false`.
    *   `CONTENT_READY`: Signals the character is complete enough to save. Sets `characterReady = true`.
5.  **UI Update:** The UI reflects the new data and shows the "Continue" button if ready.

### Prompts (`CharacterPrompts.kt`)
*   `characterIntroPrompt()`: Generates the initial welcome message based on the Saga context.
*   `conversationalCharacterReply()`: The main driver. It instructs the AI to be a "friendly co-author" and extract data into the `Character` model.

---

## 4. UI Implementation

### `NewSagaView.kt`
*   **Pager Layout:** Uses `HorizontalPager` with 2 pages.
    *   Page 0: Saga Creation.
    *   Page 1: Character Creation.
*   **Animated Transitions:** Background color transitions smoothly between `genre.color` (Saga) and `genre.colorPalette().last()` (Character).
*   **Smart Navigation:**
    *   "Continue to Saga" button appears when Character is ready.
    *   Auto-save triggers when both Saga and Character are ready.

### `CharacterCreationView.kt`
*   **Design:** Minimalist, chat-forward interface.
*   **Features:**
    *   Typewriter text effect for AI responses.
    *   Suggestion chips for quick inputs.
    *   Hidden chat history (context window limited to last 10 messages).

---

## 5. Saving & Persistence

When `saveSaga()` is called:
1.  **Prepare Data:** `SagaStateManager` and `CharacterStateManager` finalize their drafts.
2.  **Link Entities:** The Character is assigned to the new Saga's ID.
3.  **Generate Assets:** Saga Icon and Character Portrait are generated using the `ImageGeneration` pipeline.
4.  **Persist:** Data is saved to the local database via `SagaRepository`.
5.  **Navigate:** User is taken to the new Saga detail screen.
