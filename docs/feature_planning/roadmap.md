# Feature Implementation Roadmap

This document outlines the planned order of execution for upcoming features. Each feature has its
own dedicated folder containing detailed tasks and implementation plans.

## Implementation Order

### 1. Cowboys Theme 🤠

* **Status**: Completed ✅
* **Folder**: `cowboys/`
* **Plan**: `cowboys/task.md`
* **Description**: Implementation of the new "Cowboys" genre theme, including assets, colors, and
  styling.

### 2. Expressive Message Effects ✨

* **Status**: Completed ✅
* **Folder**: `expressive_messages/`
* **Plan**: `expressive_messages/task.md`
* **Description**: Adding iMessage-style animations and emotional tones to chat bubbles (Slam, Loud,
  Gentle, Invisible Ink).

### 3. Backup Extensions & Unified File 📦

* **Status**: Completed ✅
* **Folder**: `backup_extension/`
* **Plan**: `backup_extension/task.md`
* **Description**: Refactoring backup system to use custom `.saga` (single export) and `.sagas` (
  full backup) file extensions with Intent filters for easy importing.

### 4. Depth Effect & Vertical UI 🎨

* **Status**: Completed ✅
* **Folder**: `depth_effect/`
* **Plan**: `depth_effect/plan.md`
* **Description**: Implement iOS-style depth effects using MLKit Subject Segmentation for Saga
  headers, icons, and character details. Includes vertical UI refactoring.

### 5. Character Reveal "New Challenger" 🥊
 
 * **Status**: Completed ✅
 * **Folder**: `character_reveal/`
 * **Plan**: `character_reveal/plan.md`
 * **Description**: A dramatic "New Challenger Arrives" style overlay when a new character is created
   in chat. Refactors `CharacterShareView` to create a reusable `CharacterCard` component with glow
   effects and genre-based shaping.

### 6. Google Play Store Automation 🚀

* **Status**: Blocked (Deprioritized - Focus on Features First)
* **Folder**: `play_store_automation/`
* **Plan**: `play_store_automation/task.md`
* **Description**: Automate Google Play Console publishing via CLI using Gradle Play Publisher
  plugin. Enables automated releases to internal/alpha/beta/production tracks with release notes and
  metadata management.

### 7. Automated String Resources Agent 🌐

* **Status**: Completed ✅
* **Folder**: `string_resources_agent/`
* **Plan**: `string_resources_agent/task.md`
* **Description**: Automated agent that identifies hardcoded strings in Kotlin and XML files,
  extracts them to string resources (English and Portuguese), and refactors code to use resource
  references. Improves i18n efforts and ensures consistency in localization. Integrated into
  `/deliver_feature` workflow with `StringResourceHelper` singleton for ViewModels.

### 8. Conversation Snippet Sharing 💬

* **Status**: Completed ✅
* **Folder**: `conversation_share/`
* **Plan**: `conversation_share/plan.md`
* **Description**: Enable users to select and share beautiful, branded screenshots of conversation
  snippets (6-10 messages). Features multi-select UI, genre-styled share cards using existing
  ChatBubble components, and integration with the existing ShareSheet system. Provides organic
  user-generated marketing content optimized for social media.

### 9. Live Notification Media Player Revamp 🎵

* **Status**: In Progress 🚧
* **Folder**: `live_notification/`
* **Plan**: `live_notification/plan.md`
* **Description**: Replace the current media player with a live notification system inspired by
  Samsung SmartThings TV controls. Displays saga progress, timeline objectives, and provides
  Play/Pause controls.
---

## Usage

To start working on a feature:

1. Open the corresponding `task.md` or `plan.md` in the feature's folder.
2. Follow the agent-specific instructions within.

---

## Detailed Feature Descriptions

### Live Notification Media Player Revamp (Feature #9)

**Objective:** Replace the current media player with a live notification system inspired by Samsung
SmartThings TV controls. The notification will display the current saga progress with detailed
information about the timeline objective.

**Motivation:**

* Provide a more immersive and contextual playback experience.
* Allow users to track saga progress without opening the app.
* Leverage Android's live notification capabilities for a modern, system-integrated experience.
* Display rich context about the current narrative position (saga title, act, chapter).

**Scope:**

1. **Live Notification Implementation:**
    * Create a live notification that replaces the traditional media player interface.
    * Display the saga title as the main notification title.
    * Display the current timeline objective as the subtitle (similar to the in-app notification
      system).
    * Include playback controls (play/pause, skip, etc.) in the notification.

2. **ChatViewModel Integration:**
    * Detect saga flow updates in `ChatViewModel`.
    * Extract the current timeline objective from the saga state.
    * Update the live notification with the current objective information.

3. **Enhanced Title Formatting:**
    * Format the notification title to include detailed position information.
    * Example format: `"Cinnamon - Act I - Chapter II"` or
      `"[Saga Name] - Act [Roman Numeral] - Chapter [Roman Numeral]"`.
    * Ensure the title is concise enough to fit notification constraints while being informative.

4. **Notification Actions:**
    * Implement standard media controls (play, pause, next objective, previous objective).
    * Add a "Return to Saga" action that opens the app to the current saga.
    * Consider adding a "Dismiss" action for user control.

**Technical Considerations:**

* Use `NotificationCompat.Builder` with `MediaStyle` for media playback notifications.
* Implement a foreground service to maintain the notification during playback.
* Ensure proper lifecycle management to update the notification as the saga progresses.
* Handle notification permissions for Android 13+ (Tiramisu).
* Consider battery optimization and background execution limits.

**Acceptance Criteria:**

* Live notification displays current saga title and timeline objective.
* Notification updates automatically when the saga flow progresses.
* Title format includes saga name, act, and chapter information.
* Playback controls function correctly from the notification.
* Notification persists during active playback and dismisses appropriately.
* Tapping the notification navigates to the current saga in the app.

**Next Steps:**

* Design the notification layout and action buttons.
* Implement the foreground service for notification management.
* Create the integration points in `ChatViewModel` for saga flow detection.
* Develop the title formatting logic for act/chapter display.
* Test notification behavior across different Android versions and device manufacturers.
