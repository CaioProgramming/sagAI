# Global Notifications Implementation Summary

## Overview

Implemented a streamlined global notification system to keep users engaged with their saga progress.
The system leverages the existing `SnackBar` architecture to trigger system notifications for major
events (New Chapter, New Act, New Character) and messages.

## Implementation Details

### 1. Unified Event Flow via SnackBarState

- **Mechanism**: The notification logic is built into `SagaContentManager` and transmitted via
  `SnackBarState`.
- **Flags**: `SnackBarState` now simplifed to:
    - `showInUi: Boolean`: Controls whether the SnackBar appears in the App UI.
    - **Implicit Notification**: Any SnackBar emission is considered a candidate for a system
      notification.
- **Event Handling**:
    - **Narrative Updates**: Emitted as standard SnackBars (`showInUi = true`).
    - **New Messages (Background)**: Emitted as "silent" SnackBars (`showInUi = false`), preventing
      UI toast but triggering notification logic.

### 2. SagaContentManager Logic

- **Responsibility**: Detects events during saga processing (`updateChapter`, `updateAct`,
  `generateCharacter`, `loadSaga`).
- **Action**: Emits a `SnackBarState`.
- **Simplicity**: No complex state tracking or special notification flags; just standard content
  updates.

### 3. ChatViewModel Integration

- **Observer**: `observeSnackBarUpdates` listens for states from `SagaContentManager`.
- **Logic**:
    - If `showInUi` is true, displays the in-app SnackBar.
    - **Always** calls `ChatNotificationManager.sendNotification` with the SnackBar message.
    - Relies on `ChatNotificationManager` to filter out notifications when the app is in the
      foreground.

### 4. ChatNotificationManager

- **Interface**: Simplified to `sendNotification(saga, title, body, largeIcon)`.
- **Behavior**:
    - **Foreground Check**: Checks `appLifecycleManager.isAppInForeground`.
    - **Customization**:
        - **Small Icon**: Uses `saga.data.genre.background` (e.g., horse for Cowboy, dragon for
          Fantasy).
        - **Large Icon**: Defaults to `saga.data.genre.background` if specific icon is missing.
        - **Color**: Uses `saga.data.genre.color` for unified branding.
    - **Routing**: Constructs deep link to the specific chat.

## Verification Checklist

Before releasing, please verify the following:

### 1. Permissions

- [ ] Ensure `POST_NOTIFICATIONS` permission is requested and granted.
- [ ] Verify behavior when permission is denied (should log warning but not crash).

### 2. Notification Channel

- [ ] Confirm `CHAT_CHANNEL_ID` is properly created in `MainActivity` or `App`.
- [ ] Verify channel settings (sound, vibration) are appropriate.

### 3. Deep Links

- [ ] Test tapping a notification. It should open the app and navigate to the correct Saga/Chat.
- [ ] Verify `MainActivity` handles the deep link intent correctly.

### 4. Event Triggers

- [ ] **Foreground Test**: Trigger an event (e.g. new chapter) while viewing the app. Confirm
  SnackBar appears but **NO** system notification is sent.
- [ ] **Background Test**: Trigger an event while app is backgrounded. Confirm **system notification
  ** is sent.

### 5. Resources

- [ ] **Icon Visibility**: Verify that Genre icons (`R.drawable.fantasy`, etc.) render correctly as
  Small Icons in the status bar (check for white blob issues).
- [ ] **Branding**: Confirm notification color matches the Genre theme.
- [ ] Review localized strings in Portuguese and English.

## Future Improvements

- **Rich Media**: Fully implement fetching and displaying Cover Images for Chapters.
- **Expanded Timeline**: Add granular notifications for specific timeline updates.
