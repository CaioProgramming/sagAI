# Edit & Delete Messages Feature

## Overview

This feature allows users to maintain control over their saga's narrative by editing the most recent
message or deleting any message in the conversation history. It ensures a seamless experience within
the "Saga" genre aesthetic.

## "Why" - The Rationale

* **Narrative Control:** Users often make typos or want to rephrase their last action to better
  steer the story.
* **Cleanup:** Deleting accidental or irrelevant messages helps keep the saga clean.
* **Immersion:** By restricting edits to the *last* message, we preserve the cause-and-effect chain
  of the story, preventing "time travel" paradoxes where changing a past message invalidates
  subsequent AI responses.

## "How" - Implementation Details

### Core Logic (`ChatViewModel`)

* **Edit Restriction:** Logic ensures only the last message (User or AI) can be edited.
* **Safeguards:** Actions are disabled during:
    * Loading states (`isLoading`)
    * AI generation (`isGenerating`)
    * Ended sagas (`saga.isEnded`)
* **Post-Action Logic:**
    * **Edit User Message:** Automatically triggers AI regeneration to reflect the new context.
    * **Edit AI Message:** Updates the text in place.
    * **Delete AI Message:** Offers a "Regenerate" option via Snackbar.
    * **Delete User Message:** Simply removes the message; user can type a new one.

### UI Components

* **`MessageOptionsSheet`:** A genre-themed bottom sheet offering Edit (conditional), Delete, Copy,
  and Select actions.
* **`DeleteConfirmationDialog`:** A custom dialog styled as a "Narrator" bubble, making the deletion
  confirmation feel like a diegetic part of the story.
* **`ChatInputView`:** Updated to support an "Editing mode" with a visual indicator, "Save" (Check)
  button, and "Cancel" (X) button.

### Persistence

* Operations are performed directly on the `MessageUseCase`, which updates the local Room database.
  The `ChatViewModel` observes these changes via the `SagaContent` flow.

## "Where" - key Files

* **Logic:** `ChatViewModel.kt`, `ChatUiAction.kt`
* **UI:** `ChatView.kt`, `ChatInputView.kt`, `MessageOptionsSheet.kt`, `DeleteConfirmationDialog.kt`
* **State:** `ChatState.kt`

## Strings & Localization

* All user-facing strings are extracted to `strings.xml` and localized for English and Portuguese (
  pt-BR).
