# Live Notification Media Player Revamp

## Goal
Replace the current media player with a live notification system inspired by Samsung SmartThings TV controls. The notification will display the current saga progress with detailed information about the timeline objective, allowing users to track and control their saga playback directly from the notification shade.

## Motivation
- Provide a more immersive and contextual playback experience
- Allow users to track saga progress without opening the app
- Leverage Android's live notification capabilities for a modern, system-integrated experience
- Display rich context about the current narrative position (saga title, act, chapter)
- Improve user engagement by making saga progress visible at a glance

## Scope

### 1. Live Notification Implementation
- Create a persistent live notification that replaces the traditional media player interface
- Display the saga title as the main notification title with enhanced formatting
- Display the current timeline objective as the subtitle (similar to the in-app notification system)
- Include playback controls (play/pause, skip forward/backward) in the notification
- Support expanded and collapsed notification states

### 2. ChatViewModel Integration
- Detect saga flow updates in `ChatViewModel`
- Extract the current timeline objective from the saga state
- Update the live notification whenever the timeline progresses
- Track current act and chapter information from the saga structure

### 3. Enhanced Title Formatting
- Format the notification title to include detailed position information
- Example format: `"Cinnamon - Act I - Chapter II"` or `"[Saga Name] - Act [Roman Numeral] - Chapter [Roman Numeral]"`
- Ensure the title is concise enough to fit notification constraints while being informative
- Handle edge cases (no act/chapter info, very long saga names)

### 4. Notification Actions
- **Play/Pause**: Toggle saga playback
- **Next Objective**: Skip to the next timeline objective
- **Previous Objective**: Go back to the previous objective
- **Return to Saga**: Opens the app to the current saga's ChatView
- **Dismiss**: Stops playback and removes the notification

## Technical Approach

### 1. Foreground Service
- Create `SagaPlaybackService` extending `Service`
- Use `startForeground()` to maintain the notification during playback
- Handle service lifecycle (start, stop, pause, resume)
- Implement proper cleanup on service destruction

### 2. Notification Builder
- Use `NotificationCompat.Builder` with `MediaStyle`
- Configure notification channel for Android O+
- Set up notification actions with `PendingIntent`
- Handle notification tap to open the app to the current saga

### 3. ChatViewModel Updates
- Add `LiveData`/`StateFlow` for current timeline objective
- Expose current act and chapter information
- Notify the service when saga flow updates occur
- Provide methods to navigate between objectives

### 4. Notification Update Logic
```kotlin
// Pseudo-code structure
fun updateNotification(
    sagaTitle: String,
    act: Int?,
    chapter: Int?,
    objective: String,
    isPlaying: Boolean
) {
    val formattedTitle = formatTitle(sagaTitle, act, chapter)
    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setContentTitle(formattedTitle)
        .setContentText(objective)
        .setSmallIcon(R.drawable.ic_saga_notification)
        .setStyle(MediaStyle())
        .addAction(createAction(ACTION_PREVIOUS))
        .addAction(createAction(if (isPlaying) ACTION_PAUSE else ACTION_PLAY))
        .addAction(createAction(ACTION_NEXT))
        .build()
    
    notificationManager.notify(NOTIFICATION_ID, notification)
}
```

### 5. Permission Handling
- Request `POST_NOTIFICATIONS` permission for Android 13+ (Tiramisu)
- Handle permission denial gracefully
- Provide user feedback if notifications are disabled

## User Review Required
> [!IMPORTANT]
> **Media Player Replacement**: This feature completely replaces the current media player with a notification-based system.
> - **Pros**: System-integrated, accessible from anywhere, modern UX
> - **Cons**: Less visual real estate, relies on notification permissions
> 
> **Question**: Should we keep the in-app media player as a fallback or completely replace it?

> [!WARNING]
> **Battery Considerations**: A foreground service will consume battery during playback. We should:
> - Implement proper service cleanup when playback stops
> - Consider adding a timeout for idle playback
> - Monitor battery usage during testing

## Implementation Files

### New Files
- `app/src/main/java/com/ilustris/sagai/features/saga/playback/SagaPlaybackService.kt` - Foreground service for notification management
- `app/src/main/java/com/ilustris/sagai/features/saga/playback/NotificationHelper.kt` - Notification builder and update logic
- `app/src/main/java/com/ilustris/sagai/features/saga/playback/PlaybackActions.kt` - Action constants and handlers

### Modified Files
- `app/src/main/java/com/ilustris/sagai/features/saga/chat/viewmodel/ChatViewModel.kt` - Add timeline tracking and service integration
- `app/src/main/AndroidManifest.xml` - Register the foreground service and notification permissions

## Verification Plan

### Automated Tests
- Unit tests for title formatting logic
- Unit tests for notification action handling
- ViewModel tests for timeline tracking

### Manual Verification
- [ ] **Notification Display**: Verify notification appears with correct title and objective
- [ ] **Title Formatting**: Test with various saga names, acts, and chapters
- [ ] **Playback Controls**: Test all notification actions (play, pause, next, previous)
- [ ] **Timeline Updates**: Verify notification updates when saga progresses
- [ ] **App Navigation**: Tap notification to ensure it opens the correct saga
- [ ] **Permission Handling**: Test on Android 13+ with permissions granted/denied
- [ ] **Service Lifecycle**: Test service behavior on app kill, low memory, etc.
- [ ] **Battery Impact**: Monitor battery usage during extended playback sessions

## Tasks
- [ ] Create notification channel and configure for Android O+
- [ ] Implement `SagaPlaybackService` with foreground notification
- [ ] Create `NotificationHelper` for notification building and updates
- [ ] Implement title formatting logic with act/chapter support
- [ ] Add timeline tracking to `ChatViewModel`
- [ ] Integrate service with `ChatViewModel` for saga flow updates
- [ ] Implement notification action handlers (play, pause, next, previous)
- [ ] Add permission request flow for Android 13+
- [ ] Update `AndroidManifest.xml` with service and permissions
- [ ] Test notification behavior across different Android versions
- [ ] Optimize battery usage and service lifecycle
- [ ] Add analytics tracking for notification interactions
