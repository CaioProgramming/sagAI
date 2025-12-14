# WorkManager Migration - Scheduled Notifications

## Overview

Migrated scheduled notification system from `AlarmManager` + coroutines to **WorkManager** for
reliable background execution when the app is closed or in background.

## Implementation Date

December 13, 2025

## Problem Solved

- Network requests were failing when users left the app due to process termination
- Coroutines were being cancelled when app process was killed
- Notifications weren't being generated reliably in Doze Mode

## Solution Architecture

### 1. **NotificationGenerationWorker** (`core/notifications/NotificationGenerationWorker.kt`)

- `@HiltWorker` for dependency injection
- Receives `sagaId` (Int) via `WorkerParameters.inputData`
- Injections: `GemmaClient`, `SagaRepository`, `DataStorePreferences`
- **Flow:**
    1. Fetches complete `SagaContent` from `SagaRepository.getSagaById()`
    2. Generates `SceneSummary` using Gemini AI
    3. Generates smart message from random character
    4. Saves `ScheduledNotification` to DataStore
    5. Schedules final notification via AlarmManager
- **Timeout**: 5 minutes to prevent exceeding WorkManager limits
- **Retry Policy**: `BackoffPolicy.LINEAR` with 15-second delay

### 2. **WorkManagerScheduler** (`core/notifications/WorkManagerScheduler.kt`)

- Interface: `scheduleNotificationWork(sagaId)`, `cancelNotificationWork(sagaId)`,
  `cancelAllNotificationWork()`
- **Constraints:**
    - `NetworkType.CONNECTED` - requires internet connection
    - `RequiresBatteryNotLow(true)` - only runs when battery is not low
- **Delay:** 2 hours (production) / 1 minute (debug)
- **Deduplication:** Uses `ExistingWorkPolicy.REPLACE` with unique tag `"notification_$sagaId"`

### 3. **ScheduledNotificationService** (Refactored)

- **Simplified interface:** `scheduleNotification(sagaId: Int)` instead of
  `scheduleNotification(sagaContent: SagaContent)`
- **Removed dependencies:** No longer needs `GemmaClient` or AI generation logic
- Delegates all work to `WorkManagerScheduler`
- Maintains `cancelScheduledNotifications()` for cleanup

### 4. **HiltWorkerFactory Configuration**

- `SagaApp` implements `Configuration.Provider`
- Injects `HiltWorkerFactory` for Worker dependency injection
- **AndroidManifest.xml:** Disabled default WorkManager initialization with `tools:node="remove"`

## Key Benefits

✅ **Reliable Execution**: Works survive app closure, process death, and device reboots  
✅ **Battery Friendly**: Respects Doze Mode and battery restrictions automatically  
✅ **Network Aware**: Only runs when internet is available  
✅ **Low Coupling**: Worker only depends on `GemmaClient`, `SagaRepository`, and
`DataStorePreferences`  
✅ **Retry Logic**: Automatic retry on failure with exponential backoff  
✅ **Deduplication**: Prevents multiple notifications per saga

## Files Modified

### Created

- `app/src/main/java/com/ilustris/sagai/core/notifications/NotificationGenerationWorker.kt`
- `app/src/main/java/com/ilustris/sagai/core/notifications/WorkManagerScheduler.kt`

### Modified

- `app/src/main/java/com/ilustris/sagai/core/notifications/ScheduledNotificationService.kt`
- `app/src/main/java/com/ilustris/sagai/features/saga/chat/presentation/ChatViewModel.kt`
- `app/src/main/java/com/ilustris/sagai/di/AppModule.kt`
- `app/src/main/java/com/ilustris/sagai/SagaApp.kt`
- `app/src/main/AndroidManifest.xml`

### Unchanged

- `ScheduledNotificationReceiver` - Still displays notifications from DataStore (no changes needed)

## Usage Example

```kotlin
// Old way (before migration)
scheduledNotificationService.scheduleNotification(sagaContent)

// New way (after migration)
scheduledNotificationService.scheduleNotification(sagaContent.data.id)
```

## Testing Checklist

- [ ] Run app in debug mode (1-minute delay)
- [ ] Exit app to background
- [ ] Verify Worker is enqueued in WorkManager
- [ ] Wait for notification generation
- [ ] Verify notification is displayed
- [ ] Test with low battery (should NOT execute)
- [ ] Test without network (should wait for connection)
- [ ] Test with multiple rapid app exits (should deduplicate)

## Monitoring

Check Worker status via:

```kotlin
WorkManager.getInstance(context)
    .getWorkInfosByTag("notification_$sagaId")
    .get()
```

Logs to look for:

- `NotificationWorker: Starting notification generation for saga: X`
- `NotificationWorker: Notification scheduled at: ...`
- `WorkManagerScheduler: Scheduled notification work for saga: X`

## Future Improvements

1. Add WorkManager observability in UI (show pending notifications)
2. Implement PeriodicWorkRequest for recurring reminders
3. Add user preferences for notification timing
4. Track success/failure metrics in Analytics

## Notes

- Worker delay matches old service (2h prod / 1min debug)
- AlarmManager still used for final notification trigger (WorkManager → AlarmManager → Receiver)
- DataStore key remains unchanged: `"scheduled_notification_json"`
- No breaking changes to existing notification flow

