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

* **Status**: Planning
* **Folder**: `character_reveal/`
* **Plan**: `character_reveal/plan.md`
* **Description**: A dramatic "New Challenger Arrives" style overlay when a new character is created
  in chat. Refactors `CharacterShareView` to create a reusable `CharacterCard` component with glow
  effects and genre-based shaping.

### 6. Google Play Store Automation 🚀

* **Status**: Refining
* **Folder**: `play_store_automation/`
* **Plan**: `play_store_automation/task.md`
* **Description**: Automate Google Play Console publishing via CLI using Gradle Play Publisher
  plugin. Enables automated releases to internal/alpha/beta/production tracks with release notes and
  metadata management.

---

## Usage

To start working on a feature:

1. Open the corresponding `task.md` in the feature's folder.
2. Follow the agent-specific instructions within.

### Automated Hardcoded String Extraction and Localization Agent

**Objective:** Develop an automated agent that identifies hardcoded strings in the application's
source code, extracts them, creates corresponding string resources (English and Portuguese), and
replaces the original hardcoded strings with resource references.

**Motivation:**

* Improve internationalization (i18n) efforts by centralizing all user-facing strings.
* Reduce manual effort in identifying and extracting hardcoded strings.
* Ensure consistency in string resource naming and usage.
* Facilitate easier translation management.

**Scope:**

1. **String Identification:** The agent should be able to scan Kotlin (`.kt`) and XML layout (
   `.xml`) files within the `app/src/main/` directory.
    * Identify literal strings used directly in `Text`, `Button`, `TextField` (and similar UI
      components in Compose/XML) or other user-facing contexts.
    * Intelligently filter out non-user-facing strings (e.g., debug messages, internal identifiers,
      code comments, variable names, file paths, URLs, `translatable="false"` attributes).
2. **Resource Generation:**
    * For each identified hardcoded string, propose a unique string resource name (e.g., based on
      file path and content).
    * Add the new string resource to `app/src/main/res/values/strings.xml` (English) and
      `app/src/main/res/values-pt-rBR/strings.xml` (Portuguese). The Portuguese translation can
      initially be a copy of the English string or a simple machine translation placeholder, with a
      clear indication that manual review is needed.
3. **Code Refactoring:**
    * Replace the original hardcoded string in the source file with a reference to the newly created
      string resource (e.g., `stringResource(R.string.new_string_id)` in Kotlin Compose, or
      `@string/new_string_id` in XML).
4. **Integration (Future Consideration):**
    * Potentially integrate this agent as a pre-commit hook or a step in the `deliver_feature` agent
      workflow to ensure new features adhere to localization best practices.

**Acceptance Criteria:**

* The agent can successfully identify a majority of user-facing hardcoded strings.
* The agent can generate valid `strings.xml` entries for both English and Portuguese.
* The agent can correctly replace hardcoded strings with resource references in the source code.
* The refactored code compiles and runs without errors.

**Next Steps:**

* Detailed design of the string identification heuristics.
* Implementation of a parsing and modification mechanism for Kotlin and XML files.
* Consideration of edge cases and false positives/negatives in string identification.

---

### Live Notification Media Player Revamp

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

---

### Conversation Snippet Sharing

**Objective:** Enable users to select and share beautiful, branded screenshots of conversation
snippets, similar to Spotify's lyrics sharing feature. Users can select 6-10 messages and generate a
shareable card optimized for social media.

**Motivation:**

* Allow users to share memorable moments from their sagas with friends.
* Generate user-created marketing content showcasing the app's conversations.
* Encourage users to revisit and highlight favorite dialogue.
* Provide simple, visual sharing without complex AI processing.
* Subtle app promotion through watermarked share cards.

**Scope:**

1. **Multi-Selection UI:**
    * Long press on a message to enter selection mode.
    * Tap messages to select/deselect with visual feedback (checkmarks/highlights).
    * Display selection counter (e.g., "3/10 messages selected").
    * Limit selection to 6-10 messages for optimal card layout.
    * Show "Share" button when at least 1 message is selected.

2. **Share Card Generation:**
    * Render selected messages as a branded card with saga-specific theme styling.
    * Apply genre colors, gradients, fonts, and bubble shapes.
    * Include saga title header and character avatars.
    * Add subtle "Created with Sagas" watermark.
    * Optimize for Instagram stories (1080x1920).

3. **ShareSheet Integration:**
    * Extend existing `ShareSheet.kt` with new "Share Conversation" mode.
    * Add to `ShareType` enum alongside existing share types.
    * Reuse existing share infrastructure (Android share intent).

4. **Rendering & Export:**
    * Capture composed card as bitmap.
    * Save to cache directory and trigger share intent.
    * Clean up cached images after sharing.

**Technical Considerations:**

* Use Compose's bitmap capture capabilities for rendering.
* Apply genre-specific styling from existing theme system.
* No AI required - purely visual presentation of user-selected content.
* Handle edge cases (long messages, special message types).

**Acceptance Criteria:**

* Users can select 6-10 messages via long press and tap.
* Share card renders with correct genre styling and branding.
* Card is optimized for Instagram stories dimensions.
* Share intent works correctly with generated image.
* Selection mode has clear visual feedback.

**Next Steps:**

* Add message selection state to `ChatViewModel`.
* Implement multi-selection UI in `ChatView` and `ChatBubble`.
* Design and implement `ConversationShareCard` composable.
* Create bitmap capture helper for rendering.
* Extend `ShareSheet` with conversation sharing option.
* Test with different message counts and genres.