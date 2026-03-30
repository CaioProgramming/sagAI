# Feature Implementation Roadmap

This document outlines the planned order of execution for upcoming features. Each feature has its
own dedicated folder containing detailed tasks and implementation plans.

## Implementation Order

### 1. Cowboys Theme 🤠

* **Status**: Completed ✅
* **Documentation**: `../../features/cowboys_theme.md`
* **Archive**: `../../archive/feature_planning/cowboys/`
* **Description**: Implementation of the new "Cowboys" genre theme, including assets, colors, and
  styling.

### 2. Expressive Message Effects ✨

* **Status**: Completed ✅
* **Documentation**: `../../features/expressive_message_effects.md`
* **Archive**: `../../archive/feature_planning/expressive_messages/`
* **Description**: Adding iMessage-style animations and emotional tones to chat bubbles (Slam, Loud,
  Gentle, Invisible Ink).

### 3. Backup Extensions & Unified File 📦

* **Status**: Completed ✅
* **Documentation**: `../../features/backup_extensions.md`
* **Archive**: `../../archive/feature_planning/backup_extension/`
* **Description**: Refactoring backup system to use custom `.saga` (single export) and `.sagas` (
  full backup) file extensions with Intent filters for easy importing.

### 4. Depth Effect & Vertical UI 🎨

* **Status**: Completed ✅
* **Documentation**: `../../features/depth_effect_vertical_ui.md`
* **Archive**: `../../archive/feature_planning/depth_effect/`
* **Description**: Implement iOS-style depth effects using MLKit Subject Segmentation for Saga
  headers, icons, and character details. Includes vertical UI refactoring.

### 5. Character Reveal "New Challenger" 🥊
 
 * **Status**: Completed ✅
 * **Documentation**: `../../features/character_reveal.md`
 * **Archive**: `../../archive/feature_planning/character_reveal/`
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
* **Documentation**: `../../features/automated_string_resources.md`
* **Archive**: `../../archive/feature_planning/string_resources_agent/`
* **Description**: Automated agent that identifies hardcoded strings in Kotlin and XML files,
  extracts them to string resources (English and Portuguese), and refactors code to use resource
  references. Improves i18n efforts and ensures consistency in localization. Integrated into
  `/deliver_feature` workflow with `StringResourceHelper` singleton for ViewModels.

### 8. Conversation Snippet Sharing 💬

* **Status**: Completed ✅
* **Documentation**: `../../features/conversation_snippet_sharing.md`
* **Archive**: `../../archive/feature_planning/conversation_share/`
* **Description**: Enable users to select and share beautiful, branded screenshots of conversation
  snippets (6-10 messages). Features multi-select UI, genre-styled share cards using existing
  ChatBubble components, and integration with the existing ShareSheet system. Provides organic
  user-generated marketing content optimized for social media.

### 9. Live Notification Media Player Revamp 🎵

* **Status**: Completed ✅
* **Documentation**: `../../features/live_notification_media_player.md`
* **Archive**: `../../archive/feature_planning/live_notification/`
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

* **Status**: Completed ✅
* **Documentation**: `../../features/global_notification_system.md`
* **Archive**: `../../archive/feature_planning/global_notifications/`
* **Description**: Implemented a streamlined global notification system using `SnackBarState` to trigger
  notifications for major saga events (New Chapter, New Act, New Character) and messages.

### 12. Scheduled Contextual Notifications ⏰

* **Status**: Completed ✅
* **Documentation**: `../../features/scheduled_contextual_notifications.md`
* **Archive**: `../../archive/feature_planning/global_notifications/scheduled_notifications_plan.md`
* **Description**: Implemented a smart notification scheduling system that sends AI-generated,
  contextual messages from story characters 2 hours after the user
  leaves the app. This creates an immersive experience where characters reach out with personalized
  messages based on the current story context.

### 13. Audio Messages & Transcription 🎙️

* **Status**: Completed ✅
* **Documentation**: `../../features/audio_messages_transcription.md`
* **Archive**: `../../archive/feature_planning/audio_messages/`
* **Description**: Integrated audio recording and transcription, allowing users to send voice messages that are converted to text.

### 14. Audio Generation (TTS) 🔊

* **Status**: Completed ✅
* **Documentation**: `../../features/audio_generation.md`
* **Archive**: `../../archive/feature_planning/audio_generation/`
* **Description**: AI-driven audio generation for characters and narrators, with persistent voice assignments and premium integration.

### 15. Image Composition Reviewer Agent 🎨

* **Status**: Completed ✅
* **Documentation**: `../../features/image_composition_reviewer.md`
* **Archive**: `../../archive/feature_planning/image_composition/`
* **Description**: Automated reviewer agent that validates and corrects image prompts to ensure genre consistency and quality.

### 16. Saga Review "Wrapped" Revamp 🎁

* **Status**: Completed ✅
* **Documentation**: `../../features/saga_review_revamp.md`
* **Archive**: `../../archive/feature_planning/review_revamp_progress.md`
* **Description**: A "Spotify Wrapped" style interactive story recap with vertical navigation, dynamic summaries, and premium visuals.

### 17. Smart Zoom for Avatars 🔍

* **Status**: Completed ✅
* **Documentation**: `../../features/smart_zoom.md`
* **Archive**: `../../archive/feature_planning/smart_zoom_instructions.md`
* **Description**: Automatic subject-based zoom for character avatars to eliminate excessive negative space.

### 18. Punk Rock Theme 🎸

* **Status**: Completed ✅
* **Documentation**: `../../features/punk_rock_theme.md`
* **Archive**: `../../archive/feature_planning/punk_rock_theme_plan.md`
* **Description**: Implementation of the "Punk Rock" genre with rebellious aesthetics, graffiti visuals, and specific AI rules.

### 19. Onboarding System 🎓

* **Status**: Planning
* **Folder**: `onboarding/`
* **Plan**: `onboarding/plan.md`
* **Description**: AI-generated contextual onboarding using full-screen pager dialogs. RemoteConfig
  provides context per onboarding type, GemmaClient generates casual slides dynamically. Three
  contexts: App Intro (first launch), Creation Guide (first saga), Gameplay Guide (first chat).
  Phased rollout starting with App Intro. Component designed for future reuse in PremiumView.

---
## Usage

To start working on a feature:

1. Open the corresponding `task.md` or `plan.md` in the feature's folder.
2. Follow the agent-specific instructions within.
