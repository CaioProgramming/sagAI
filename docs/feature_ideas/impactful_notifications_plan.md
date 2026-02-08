# Impactful Notifications (Gamification) Plan

## Objective

Transform the user experience from a static "reading app" to a dynamic "motion game" by highlighting
significant narrative milestones with full-screen, animated overlays. This increases engagement and
provides a sense of reward and progression.

## Core Concept

Instead of relying solely on subtle Snackbars, we will introduce **"Saga Milestones"**. These are
full-screen, genre-styled animations that trigger when specific narrative events occur in real-time.

### Milestone Types

1. **New Event:** "New Timeline Event: [Title]"
2. **Chapter Completed:** "Chapter [Number] Finished: [Title]"
3. **Act Completed:** "Act [Number] Completed: [Title]"

## Technical Architecture

### 1. Data Modeling (`features/saga/chat/presentation/model/SagaMilestone.kt`)

Create a sealed class to represent the event type.

```kotlin
sealed class SagaMilestone(val title: String, val subtitle: String, val genre: Genre) {
    class NewEvent(title: String, genre: Genre) : SagaMilestone(title, "New Event Unlocked", genre)
    class ChapterFinished(title: String, number: Int, genre: Genre) : SagaMilestone(title, "Chapter $number Completed", genre)
    class ActFinished(title: String, number: Int, genre: Genre) : SagaMilestone(title, "Act $number Concluded", genre)
}
```

### 2. State Management (`ChatViewModel.kt`)

* **Tracking State:** The ViewModel needs to track the *previous* IDs of the Act, Chapter, and
  Timeline to detect changes during the session.
* **UI State:** Add `milestone: SagaMilestone?` to `ChatUiState`.
* **Logic (Overlap Prevention):**
    * We use a **Hierarchy of Importance** to prevent notification spam.
    * Inside `observeSaga()`:
        * Only trigger *after* the initial load (`loadFinished == true`).
        * **Priority 1 (High):** `currentSaga.currentAct.id` vs `lastActId`. If changed -> Trigger "
          Act Finished". (Implicitly covers lower milestones).
        * **Priority 2 (Medium):** Else if `currentSaga.currentChapter.id` vs `lastChapterId`. If
          changed -> Trigger "Chapter Finished".
        * **Priority 3 (Low):** Else if `currentSaga.latestEvent.id` vs `lastEventId`. If changed ->
          Trigger "New Event".
    * **Display Logic:**
        * Set `stateManager.updateMilestone(...)`.
        * Launch a coroutine: `delay(5.seconds)`.
        * Clear `milestone = null`.

### 3. UI Component (`features/saga/chat/ui/components/MilestoneOverlay.kt`)

A new Composable that renders the milestone.

* **Design:** Full-screen `Box`.
* **Background:** Heavily blurred version of the current saga background or a genre-specific
  gradient.
* **Animation:**
    * **Enter:** Scale Up + Fade In (Explosive).
    * **Exit:** Fade Out + Slide Up (Dismissive).
* **Typography:** Use the `Genre` specific headers and body fonts.

### 4. Integration (`ChatView.kt`)

* Add `MilestoneOverlay` to the root `Box` of `ChatView`, just below the `SagaSnackBar` but above
  the content.
* It should effectively block interaction for those 5 seconds (or allow tap to dismiss early).

## User Experience Flow

1. User sends a message that triggers a progression.
2. AI generates the new Act/Chapter/Event.
3. `SagaContentManager` updates the `content` flow.
4. `ChatViewModel` detects the ID change.
5. **Logic Check:** It sees Act ID changed. It ignores the Chapter ID change (implied).
6. **BOOM:** Screen blurs, "ACT 1 COMPLETED" animates in.
7. User goes "Whoa!".
8. Overlay fades out, revealing the new chat context.

## Future Enhancements: Audio Packages [TODO]

* **Concept:** To further enhance the "Game feel", we need genre-specific sound effects (SFX) for
  these milestones (e.g., a "Gong" for Shinobi, a "Synth Chord" for Cyberpunk).
* **Implementation Strategy:**
    * This requires a new feature in `SagaContentManager` to download a "Genre Audio Package" (
      containing Soundtrack + SFX) from a remote source upon loading a Saga.
    * *Current Status:* **Deferred.** We will implement the visual milestones first.
    * *Reference:* `SagaContentManager.kt` ambient music handling needs to be expanded to
      `SagaAudioManager` later.

## Implementation Steps

1. **Define `SagaMilestone`** sealed class.
2. **Create `MilestoneOverlay`** composable with animations.
3. **Update `ChatViewModel`** to track IDs and implement the priority logic.
4. **Integrate** into `ChatView`.
