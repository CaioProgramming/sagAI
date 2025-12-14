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

* **Status**: Completed ✅
* **Folder**: `live_notification/`
* **Plan**: `live_notification/plan.md`
* **Description**: Enhanced the media notification to display saga progress with Act/Chapter
  information and timeline objectives. Uses MediaStyle for persistent status bar chip. Includes
  progress bar showing saga completion percentage and localized text formatting.

### 10. Stories Feature 📖

* **Status**: Planning
* **Folder**: `stories/`
* **Plan**: `stories/plan.md`
* **Description**: Add an engaging "Stories" row to HomeView displaying active sagas (not ended, > 1
  chapter). Tapping a story opens a full-screen bottom sheet with two pages: "Previously
  on [Saga Title]" (AI-generated summary) and "The history continues" (AI-generated hook). Uses
  GemmaClient for cost-effective single-shot generation of both summary and hook via
  `StoryDailyBriefing` model. Generation triggered on-demand when user opens the story to avoid rate
  limits and unnecessary API calls.

### 11. Global Notification System 🔔

* **Status**: Planning
* **Folder**: `global_notifications/`
* **Plan**: `global_notifications/plan.md`
* **Description**: Enhance the existing notification system to provide comprehensive updates for
  all                                                    
  major saga events when the app is in the background. Extends beyond just new messages to
  include                                                      
  new chapters, acts, characters, timeline events, and other significant story
  progressions.                                                            
  Creates a more engaging user experience by keeping players informed of story developments
  even                                                        
  when not actively using the app.

### 12. Scheduled Contextual Notifications ⏰

* **Status**: Completed ✅
* **Folder**: `global_notifications/`
* **Plan**: `global_notifications/scheduled_notifications_plan.md`
* **Description**: Implemented a smart notification scheduling system that sends AI-generated,
  contextual messages from story characters 2 hours after the user
  leaves the app. This creates an immersive experience where characters reach out with personalized
  messages based on the current story context.

---
## Usage

To start working on a feature:

1. Open the corresponding `task.md` or `plan.md` in the feature's folder.
2. Follow the agent-specific instructions within.

---

## Detailed Feature Descriptions

### Live Notification Media Player Revamp (Feature #9)

**Status:** ✅ Completed

**What Was Implemented:**

The media notification was enhanced to provide rich saga progress information while maintaining the
persistent status bar chip behavior that users expect from media playback.

**Key Improvements:**

1. **Enhanced Title Formatting:**
    * Title now shows: `"[Saga Name] - Act [Roman] - Chapter [Roman]"`
    * Example: `"electrify - Act II - Chapter V"`
    * Uses localized string resource (`R.string.chat_view_subtitle`)
    * Reuses existing `toRoman()` extension from `DataExtensions.kt`

2. **Progress Tracking:**
    * Visual progress bar showing saga completion percentage
    * SubText displays: `"[Percentage]% • Act [Current] of [Total]"`
    * Example: `"100% • Act II of II"`

3. **Data Model Enhancement:**
    * Added `currentChapter` field to `PlaybackMetadata`
    * Added `totalActs` field for accurate progress calculation
    * `ChatViewModel` now passes chapter numbers using `chapterNumber()` extension

4. **Notification Features:**
    * Persistent status bar chip (non-dismissable when playing)
    * Saga color theming via `.setColorized()` and `.setColor()`
    * Timeline objective as notification content
    * Single Play/Pause action button
    * Opens app to current saga on tap

**Technical Decisions:**

* **MediaStyle over Live Updates:** After exploring Android 16's Live Updates API (
  `Notification.ProgressStyle`), we determined that MediaStyle is the appropriate choice for this
  use case:
    * Live Updates don't provide persistent status bar chips
    * Live Updates are designed for deliveries/rideshares where the system controls promotion
    * MediaStyle is specifically designed for ongoing media/audio experiences
    * Custom RemoteViews lose persistent chip behavior

**Files Modified:**

* `MediaNotificationManagerImpl.kt` - Enhanced notification building with progress and formatting
* `PlaybackMetadata.kt` - Added `currentChapter` and `totalActs` fields
* `ChatViewModel.kt` - Updated to pass chapter numbers and total acts
* `MediaNotificationManager.kt` - Interface remains unchanged

**Acceptance Criteria Met:**

✅ Notification displays saga title with Act/Chapter information  
✅ Shows current timeline objective  
✅ Visual progress bar indicates saga completion  
✅ Updates automatically as saga progresses  
✅ Persistent status bar chip when playing  
✅ Non-dismissable during playback  
✅ Uses localized string resources  
✅ Tapping opens app to current saga

**Learnings:**

* Android 16's Live Updates API is not suitable for persistent media notifications
* MediaStyle remains the best approach for ongoing playback experiences
* Custom notification layouts sacrifice critical UX features (persistent chip, non-dismissable)
* Working within MediaStyle constraints still allows meaningful customization
