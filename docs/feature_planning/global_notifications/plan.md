# AI Agent Task: Global Notification System Implementation 🔔

## Agent Instructions

**Objective**: Implement a comprehensive notification system for all saga events using the existing
infrastructure. Extend the current `ChatNotificationManager` to handle new chapters, acts,
characters, timeline events, and milestones - not just messages.

**Approach**: Use the **snackBarUpdate pattern** - one notification channel with dynamic content
based on event type. Reuse existing `CHAT_CHANNEL_ID` and notification infrastructure.

## Current State Analysis

**Existing System**:

- `ChatNotificationManager` handles only message notifications
- Uses `CHAT_CHANNEL_ID` and `CHAT_NOTIFICATION_ID`
- `ChatViewModel.notifyIfNeeded()` triggers message notifications
- Background detection via `AppLifecycleManager`

**Required Enhancement**: Extend to handle all saga events with dynamic content

## Solution

**Extend existing notification system using snackBarUpdate pattern** to handle:

- 📱 New messages (existing - character avatar icon)
- 📖 New chapters (chapter cover icon)
- 🎭 New acts (saga cover icon with act indicator)
- 👤 New characters (character avatar icon)
- ⚡ New timeline events (timeline icon)
- 🏆 Story milestones (achievement icon)

**Key Principles:**

- **Single Notification Channel**: Use existing `CHAT_CHANNEL_ID` for all events
- **SnackBar Pattern**: Dynamic content/icons like our snackBar system
- **Notification Clearing**: Clear notifications when opening ChatScreen
- **Character Creation**: Ensure `createCharacter()` triggers `notifyIfNeeded()`

## Technical Architecture

### Core Philosophy

- **Reuse Existing Infrastructure**: Extend `ChatNotificationManager` and `ChatViewModel`
- **SnackBarUpdate Pattern**: Same approach as `sagaContentManager.snackBarUpdate`
- **Single Notification ID**: Replace previous notifications (like snackBar behavior)
- **Event-Driven**: Use existing content observation patterns

### Key Components

1. **SagaEventType Enum** (New)
    - Defines event types with icons, titles, and priorities
    - Similar to snackBar message types but for notifications
    - Maps events to appropriate styling and content

2. **Enhanced ChatNotificationManager** (Extended)
    - Add `sendSagaEventNotification()` method
    - Add `clearNotifications()` method
    - Maintain existing `sendMessageNotification()` for backward compatibility

3. **Extended ChatViewModel** (Enhanced)
    - Enhance `notifyIfNeeded()` to detect all event types
    - Add change detection logic for chapters, acts, characters
    - Ensure `createCharacter()` triggers notifications
    - Add notification clearing when chat opens

4. **MainActivity Integration** (Enhanced)
    - Clear notifications when opening ChatScreen from deep link
    - Handle notification tap navigation

## Implementation Tasks

### Task 1: Create SagaEventType Enum

**File**: `app/src/main/java/com/ilustris/sagai/core/notifications/SagaEventType.kt`

Create enum with event types, icons, priorities, and helper methods. Include string resources for
titles and content. Support for dynamic content generation with context and parameters.

### Task 2: Enhance ChatNotificationManager Interface

**File**:
`app/src/main/java/com/ilustris/sagai/features/saga/chat/data/manager/ChatNotificationManager.kt`

Add new methods:

-
`sendSagaEventNotification(saga: SagaContent, eventType: SagaEventType, content: String, largeIcon: Bitmap?)`
- `clearNotifications()`

Maintain existing `sendMessageNotification()` for backward compatibility.

### Task 3: Implement Enhanced ChatNotificationManagerImpl

**File**:
`app/src/main/java/com/ilustris/sagai/features/saga/chat/data/manager/ChatNotificationManagerImpl.kt`

Implement new interface methods:

- Reuse existing `sendToNotificationChannel()` with dynamic event type support
- Add `clearNotifications()` using existing `CHAT_NOTIFICATION_ID`
- Maintain existing permission handling and styling

### Task 4: Enhance ChatViewModel Event Detection

**File**: `app/src/main/java/com/ilustris/sagai/features/saga/chat/presentation/ChatViewModel.kt`

Extend `notifyIfNeeded()` to detect:

- **New chapters**: Monitor `sagaContent.flatChapters()` size changes
- **New acts**: Monitor `sagaContent.acts` size changes
- **New characters**: Monitor `sagaContent.getCharacters()` size changes
- **Timeline events**: Monitor timeline objective changes

### Task 5: Character Creation Integration

Ensure `createCharacter()` method in `ChatViewModel` calls `notifyIfNeeded()` after successful
character creation. Use character avatar as notification large icon.

### Task 6: Notification Clearing System

Add `clearNotifications()` call when ChatScreen opens. Implement in appropriate lifecycle method -
either `ChatViewModel` initialization or `MainActivity` deep link handling.

### Task 7: Add String Resources

Add localized strings for all notification types in `res/values/strings.xml` and
`res/values-pt/strings.xml`.

## File Structure

```
app/src/main/java/com/ilustris/sagai/
├── core/
│   └── notifications/
│       └── SagaEventType.kt (new enum)
├── features/
│   └── saga/
│       └── chat/
│           └── domain/
│               └── manager/
│                   ├── ChatNotificationManager.kt (enhanced interface)
│                   └── ChatNotificationManagerImpl.kt (enhanced implementation)
├── features/
│   └── saga/
│       └── chat/
│           └── presentation/
│               └── ChatViewModel.kt (enhanced event detection + clearing)
└── MainActivity.kt (optional: notification clearing on deep link)
```

## Data Models

### SagaEventType

```kotlin
enum class SagaEventType(
    val titleRes: Int,
    val contentRes: Int,
    val smallIconRes: Int,
    val priority: Int = NotificationCompat.PRIORITY_DEFAULT
) {
    NEW_MESSAGE(R.string.notification_new_message, R.string.notification_new_message_content, R.drawable.ic_spark, NotificationCompat.PRIORITY_HIGH),
    NEW_CHAPTER(R.string.notification_new_chapter, R.string.notification_new_chapter_content, R.drawable.ic_chapter, NotificationCompat.PRIORITY_DEFAULT),
    NEW_ACT(R.string.notification_new_act, R.string.notification_new_act_content, R.drawable.ic_act, NotificationCompat.PRIORITY_DEFAULT),
    NEW_CHARACTER(R.string.notification_new_character, R.string.notification_new_character_content, R.drawable.ic_character, NotificationCompat.PRIORITY_DEFAULT),
    NEW_TIMELINE_EVENT(R.string.notification_timeline_event, R.string.notification_timeline_event_content, R.drawable.ic_timeline, NotificationCompat.PRIORITY_LOW),
    STORY_MILESTONE(R.string.notification_milestone, R.string.notification_milestone_content, R.drawable.ic_milestone, NotificationCompat.PRIORITY_HIGH);
    
    fun getTitle(context: Context, sagaTitle: String): String {
        return context.getString(titleRes, sagaTitle)
    }
    
    fun getContent(context: Context, vararg args: Any): String {
        return context.getString(contentRes, *args)
    }
}
```

### Enhanced ChatNotificationManager Interface

```kotlin
interface ChatNotificationManager {
    fun sendMessageNotification(saga: SagaContent, message: MessageContent)
    
    fun sendSagaEventNotification(
        saga: SagaContent,
        eventType: SagaEventType,
        content: String,
        largeIcon: Bitmap? = null
    )
    
    fun clearNotifications()
}
```

## Integration Points

### ChatViewModel Enhancement

- Extend existing `notifyIfNeeded()` to detect different event types
- Add change detection logic for chapters, acts, characters, timeline events
- Maintain existing message notification behavior
- Use same background/foreground detection from `AppLifecycleManager`
- Clear notifications when chat screen opens

### ChatNotificationManagerImpl Enhancement

- Extend existing `sendToNotificationChannel()` method to accept event types
- Add `sendSagaEventNotification()` method for new event types
- Add `clearNotifications()` method using existing `CHAT_NOTIFICATION_ID`
- Reuse existing notification channel (`CHAT_CHANNEL_ID`)
- Maintain existing permission handling and styling logic

### Character Creation Flow

- Ensure `createCharacter()` in ChatViewModel triggers `notifyIfNeeded()`
- Add character creation event to notification system
- Use character avatar as large icon for character notifications

### MainActivity Integration (Optional)

- Clear notifications when opening ChatScreen from deep link
- Handle notification tap navigation to correct saga context

## Expected Behavior

### Notification Flow

1. **Event Detection**: `ChatViewModel` detects saga content changes
2. **Background Check**: Only send notifications when app is in background
3. **Dynamic Notification**: Create notification with appropriate icon and content based on event
   type
4. **User Interaction**: Tapping notification opens app to correct saga context
5. **Notification Clearing**: Clear notifications when user opens ChatScreen

### Event Types and Icons

- 📱 **New Message**: Character avatar icon (existing behavior)
- 📖 **New Chapter**: Chapter cover or book icon
- 🎭 **New Act**: Saga cover with act indicator
- 👤 **New Character**: Character avatar icon
- ⚡ **Timeline Event**: Timeline/event icon
- 🏆 **Story Milestone**: Achievement/trophy icon

## Success Criteria

### Core Requirements

- [ ] All major saga events trigger appropriate notifications when app is in background
- [ ] Notifications use existing `CHAT_CHANNEL_ID` and `CHAT_NOTIFICATION_ID`
- [ ] Character creation calls `notifyIfNeeded()` after successful creation
- [ ] Notifications are cleared when ChatScreen opens
- [ ] Each event type has appropriate icon and content

### Technical Requirements

- [ ] Reuse existing notification infrastructure (`ChatNotificationManager`)
- [ ] Maintain backward compatibility with message notifications
- [ ] Use same background/foreground detection (`AppLifecycleManager`)
- [ ] Add proper string resources for English and Portuguese
- [ ] Handle notification permissions gracefully

## Testing Checklist

After implementation, verify:

- [ ] New chapter creation triggers chapter notification
- [ ] New act creation triggers act notification
- [ ] Character creation (`createCharacter()`) triggers character notification
- [ ] Timeline changes trigger timeline notifications
- [ ] Message notifications still work as before
- [ ] Notifications only appear when app is in background
- [ ] Tapping notification opens correct saga context
- [ ] Opening ChatScreen clears existing notifications
- [ ] All strings are properly localized
