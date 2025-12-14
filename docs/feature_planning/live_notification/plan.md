# Live Notification Media Player Revamp Plan

## Objective

Replace the current media player with a **Live Updates notification** using Android 16's new Live
Updates SDK. The notification will display the current saga progress as a dynamic, progress-centric
notification similar to rideshare or delivery tracking apps.

## User Constraints

- **Playback Controls**: Only Play and Pause.
- **Notification Style**: Should NOT look like a traditional media player.
- **Compatibility**: Use Android 16 Live Updates SDK with fallback for older devices.

## Scope

### 1. Live Updates Notification Implementation (Android 16+)

- Use `Notification.ProgressStyle` for progress-centric notifications
- **Title**: Saga Title
- **Content**: Current timeline objective (e.g., "Find the hidden treasure")
- **Progress**: Visual progress through acts/chapters
- **Controls**: Play and Pause actions (custom, not media controls)
- **Visibility**: Appears on always-on display, status bar chip, lock screen

### 2. Fallback for Older Devices (API < 36)

- Use standard `BigTextStyle` notification
- Maintain same content and controls
- Less prominent display but functional

### 3. ChatViewModel Integration

- Detect saga flow updates
- Extract current timeline objective
- Calculate progress (current chapter / total chapters)
- Update notification dynamically

### 4. Compatibility & Requirements

- **Target SDK**: 36 (Android 16) for Live Updates
- **Min SDK**: 27 (maintain current)
- **Permissions**: POST_NOTIFICATIONS (Android 13+)
- **Service Type**: Foreground service (not media playback type)

## Implementation Steps

1. **Update Target SDK**: Set targetSdk to 36 in build.gradle
2. **Service Refactoring**: Convert `SagaMediaService` to use Live Updates
3. **Notification Builder**: Implement `Notification.ProgressStyle` with version checks
4. **Progress Calculation**: Add logic to calculate saga progress
5. **ViewModel Integration**: Update `ChatViewModel` to send progress data
6. **Testing**: Verify on Android 16 and fallback on older versions
