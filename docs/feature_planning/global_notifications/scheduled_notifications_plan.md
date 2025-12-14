# AI Agent Task: Scheduled Contextual Notifications Implementation

## Agent Instructions

**Objective**: Implement a smart notification scheduling system that sends AI-generated, contextual
messages from story characters 2 hours after the user leaves the app. Create an immersive experience
where characters reach out with personalized messages based on current story context, feeling like
friends waiting to continue the adventure.

**Critical Requirements**:

- ✅ **Complete Cleanup**: When user returns, cancel AlarmManager notifications AND clear all related
  DataStore preferences
- ✅ **No Ghost Notifications**: Prevent scenarios where user leaves at 12:00, returns at 13:45, but
  still gets notification at 14:00
- ✅ **Proper State Management**: Always sync AlarmManager state with DataStore preferences

## Core Concept

- **Trigger**: User leaves the app (detected via lifecycle events)
- **Delay**: 2 hours after app exit
- **Content**: AI-generated messages from present characters based on current story context
- **Feel**: Organic, story-driven messages that feel like characters are actively thinking about
  what comes next

## Implementation Architecture

### Service Interface

```kotlin
interface ScheduledNotificationService {
    suspend fun scheduleNotification(sagaContent: SagaContent)
    fun cancelScheduledNotifications()
}
```

### 1. Independent Notification Scheduling

- **Component**: `ScheduledNotificationService` (new)
- **Location**: `core/notifications/ScheduledNotificationService.kt`
- **Interface**: `fun scheduleNotification(sagaContent: SagaContent)`
- **Responsibility**:
    - **Single Entry Point**: Takes only `SagaContent` as parameter, handles everything else
      internally
    - **Message Generation**: Use `MessageUseCase.getSceneContext()` and
      `ChatPrompts.scheduledNotificationPrompt()`
    - **Character Selection**: Internal logic using existing helper functions
    - **JSON Storage**: Create and store `ScheduledNotification` object as JSON
    - **AlarmManager**: Schedule notification for 2 hours from exit time
    - **Complete Cleanup**: Provide `cancelScheduledNotifications()` method to clear everything
    - **No External Dependencies**: Independent from `SagaContentManager` and `ChatViewModel` data
      flows

### 2. Independent Notification Service

- **Component**: `ScheduledNotificationService` (new)
- **Location**: `core/notifications/ScheduledNotificationService.kt`
- **Interface**: Simple, takes only `SagaContent` as input
- **Responsibility**:
    - **Self-Contained**: Handles entire notification lifecycle without external dependencies
    - **Single Input**: Takes `SagaContent` and manages everything internally
    - **MessageUseCase Integration**: Use existing `MessageUseCase.getSceneContext()` and generation
    - **ChatPrompts Usage**: Use `ChatPrompts.scheduledNotificationPrompt()` following buildString
      pattern
    - **Character Selection**: Internal logic using `character.toAiNormalize(characterExclusions)`
    - **Complete Independence**: No data exchange with `SagaContentManager` or `ChatViewModel`

### 3. Scheduled Notification Delivery

- **Component**: `ScheduledNotificationReceiver` (new)
- **Location**: `core/notifications/ScheduledNotificationReceiver.kt`
- **Responsibility**:
    - Receive AlarmManager broadcast after 2-hour delay
    - **Parse JSON**: Deserialize `ScheduledNotification` from single DataStore preference
    - **Use Pre-generated Data**: Extract all notification info (message, character, saga) from
      parsed object
    - **Fallback System**: Use `fallbackMessage` field if main message is invalid
    - Send notification using existing `ChatNotificationManager` with CHAT style
    - Handle edge cases (invalid JSON, saga deleted, character missing)
    - **Lightweight Execution**: Minimal processing to meet Android BroadcastReceiver time limits
    - **Auto-cleanup**: Clear JSON preference after successful notification delivery

### 3. Lifecycle Integration

- **Component**: `ChatViewModel` (enhanced)
- **Modifications**:
    - `onPause()`: Call `scheduledNotificationService.scheduleNotification(content.value!!)`
    - `onResume()`: Call `scheduledNotificationService.cancelScheduledNotifications()` - **MUST
      clear all preferences**
    - **Simple Integration**: Only pass current `SagaContent`, service handles everything else
    - **Critical**: Ensure no scheduled notifications fire after user returns to app

### 5. Notification Delivery

- **Component**: Existing `ChatNotificationManager` (reused)
- **Styling**: Use `NotificationStyle.CHAT` for rich character avatar presentation
- **Deep Links**: Direct to specific saga chat when tapped

## Data Storage Strategy

### ScheduledNotification Data Class

```kotlin
@Serializable
data class ScheduledNotification(
    val sagaId: String,
    val sagaTitle: String,
    val characterId: String,
    val characterName: String,
    val characterAvatarPath: String?,
    val generatedMessage: String,
    val fallbackMessage: String,
    val exitTimestamp: Long,
    val scheduledTimestamp: Long,
    val generationTimestamp: Long,
    val sceneSummary: SceneSummary? = null
)
```

### DataStore Preferences Key

```kotlin
companion object {
    private const val SCHEDULED_NOTIFICATION_JSON_KEY = "scheduled_notification_json"
}
```

### JSON Storage Benefits

- **Single Source of Truth**: All notification data in one JSON object
- **Atomic Operations**: Store/retrieve/clear all data together
- **Type Safety**: Strongly typed data class instead of multiple string keys
- **Easy Serialization**: Use kotlinx.serialization for JSON conversion
- **Complete Cleanup**: Single preference key to clear everything
- **Extensibility**: Easy to add new fields without managing multiple keys

## AI Message Generation Strategy

### Context Analysis

1. **Scene Summary**: Use existing `MessageUseCase.getSceneContext()` to generate `SceneSummary`
2. **Character Selection**: Choose from `SceneSummary.charactersPresent` based on:
    - Last speaking character (most contextual)
    - Most active character in recent messages
    - Rotation to avoid repetitive senders
3. **Story State**: Analyze `immediateObjective`, `currentConflict`, and `mood`

### Message Generation via ChatPrompts

```kotlin
// Add to ChatPrompts.kt following existing buildString pattern
object ChatPrompts {
    fun scheduledNotificationPrompt(
        saga: SagaContent,
        selectedCharacter: Character,
        sceneSummary: SceneSummary,
        characterExclusions: List<Character> = emptyList()
    ) = buildString {
        // Reuse existing functions for consistency
        append(SagaPrompts.mainContext(saga))
        appendLine()
        append(GenrePrompts.conversationDirective(saga.data.genre))
        appendLine()

        appendLine("Character Context:")
        append(selectedCharacter.toAiNormalize(characterExclusions))
        appendLine()

        // Leverage existing relationship mapping system
        if (!saga.mainCharacter?.relationships.isNullOrEmpty()) {
            appendLine("### Character Relationships with Player:")
            // Find relationships involving the selected character
            val characterRelationships = saga.mainCharacter.relationships.filter {
                it.characterOne.id == selectedCharacter.id || it.characterTwo.id == selectedCharacter.id
            }
            if (characterRelationships.isNotEmpty()) {
                appendLine(characterRelationships.joinToString(";\n") { it.summarizeRelation() })
            } else {
                appendLine("${selectedCharacter.name} has no established relationship history with the player yet.")
            }
            appendLine()
        }

        appendLine("Current Story Moment:")
        appendLine("Location: ${sceneSummary.currentLocation}")
        appendLine("Characters Present: ${sceneSummary.charactersPresent.joinToString(", ")}")
        sceneSummary.immediateObjective?.let { appendLine("Objective: $it") }
        sceneSummary.currentConflict?.let { appendLine("Conflict: $it") }
        sceneSummary.mood?.let { appendLine("Mood: $it") }

        appendLine()
        appendLine("Task: Generate a brief, authentic message (1-2 sentences) as ${selectedCharacter.name} reaching out to the player who just left.")
        appendLine("- Follow your established personality and voice")
        appendLine("- Consider your relationship history and emotional connection with the player")
        appendLine("- Reference current story elements and shared experiences naturally")
        appendLine("- Make it feel like genuine concern or curiosity from someone who knows you")
        appendLine("- Maintain the ${saga.data.genre.name} theme and emotional tone")

        appendLine()
        appendLine("Your message as ${selectedCharacter.name}:")
    }
}
```

### Character Voice Consistency

- Use character's established personality and speaking style
- Reference recent story events and relationships
- Maintain appropriate emotional tone for current story tension

## Android System Behavior & Message Generation Timing

### Generation Strategy Decision

**Question**: Generate message on app exit vs. when notification fires?

**Recommended Approach**: **Generate on App Exit** ✅

- **Why**: Android system has aggressive background task limitations
- **Risk**: BroadcastReceiver has ~10 seconds execution time limit for AI generation
- **Benefit**: Pre-generated messages ensure reliable notification delivery
- **Fallback**: Store backup generic messages per character personality type

### Android Limitations

- **Background AI Generation**: Limited execution time in BroadcastReceiver
- **Network Requests**: May be restricted or delayed in background
- **Battery Optimization**: Users can whitelist app, but can't rely on it
- **Doze Mode**: Android may delay notifications, but will eventually deliver

### Hybrid Approach Implementation

1. **On App Exit**: Attempt to generate contextual message immediately
2. **Store Generated Message**: Save in DataStore preferences with expiration
3. **Fallback System**: Pre-defined character personality templates if generation fails
4. **Notification Time**: Use stored message or fallback when AlarmManager fires

## Technical Implementation Details

### Configurable Timing Constants

```kotlin
// Add to ScheduledNotificationService companion object
companion object {
    // Configurable notification timing for easy testing and development
    private const val NOTIFICATION_DELAY_PRODUCTION_HOURS = 2
    private const val NOTIFICATION_DELAY_DEBUG_MINUTES = 5
    private const val NOTIFICATION_DELAY_TESTING_MINUTES = 3 // For quick testing

    private fun getNotificationDelay(context: Context): Long {
        return when {
            BuildConfig.DEBUG -> NOTIFICATION_DELAY_DEBUG_MINUTES * 60 * 1000L
            // For quick testing, you can temporarily use:
            // NOTIFICATION_DELAY_TESTING_MINUTES * 60 * 1000L
            else -> NOTIFICATION_DELAY_PRODUCTION_HOURS * 60 * 60 * 1000L
        }
    }
}
```

### AlarmManager Integration

```kotlin
// Schedule notification with configurable timing
val notificationDelay = getNotificationDelay(context)
alarmManager.setExactAndAllowWhileIdle(
    AlarmManager.RTC_WAKEUP,
    exitTime + notificationDelay,
    notificationPendingIntent
)
```

### BroadcastReceiver Setup

```xml
<!-- In AndroidManifest.xml -->
<receiver 
    android:name=".core.notifications.ScheduledNotificationReceiver"
    android:exported="false">
    <intent-filter>
        <action android:name="com.ilustris.sagai.SCHEDULED_NOTIFICATION" />
    </intent-filter>
</receiver>
```

### Permissions Required

- `SCHEDULE_EXACT_ALARM` (Android 12+)
- `POST_NOTIFICATIONS` (already implemented)
- `WAKE_LOCK` (for reliable delivery)

## Integration with Existing Systems

### Independent Architecture

- **No SnackBar Dependencies**: Service operates independently without requiring `SnackBarState`
  integration
- **Direct Notification**: Uses `ChatNotificationManager` directly with pre-generated content
- **Clean Separation**: No data flow dependencies with `SagaContentManager` or reactive systems

### Character Data Access

- **Source**: Use `SagaContent.getCharacters()` and recent message history
- **Avatar Icons**: Leverage existing character avatar system for notification large icons
- **Voice Consistency**: Access character backstory and personality for authentic messaging

### Saga State Management

- **Active Saga Detection**: Use last viewed saga from `ChatViewModel.content.value`
- **State Validation**: Ensure saga still exists and has available characters
- **Fallback Handling**: Graceful degradation if saga is deleted or corrupted

## Edge Cases & Error Handling

### Saga State Issues

- **Saga Deleted**: Cancel scheduled notification, log event
- **No Characters**: Send generic saga-themed message instead
- **Empty Scene**: Use basic saga information for context

### System Limitations

- **Permission Denied**: Log warning, skip scheduling
- **AlarmManager Failure**: Graceful fallback, retry mechanism
- **AI Generation Failure**: Use predefined character-specific templates

### User Behavior

- **Quick Return**: Cancel notification if user returns within 10 minutes
- **Ghost Notification Prevention**: Example scenario - User exits at 12:00 PM, returns at 1:45 PM →
  notification scheduled for 2:00 PM MUST be cancelled and preferences cleared
- **Multiple Sagas**: Track and schedule for most recently active saga only
- **Rapid Switching**: Debounce exit tracking to avoid excessive scheduling

## Testing Strategy

### Unit Tests

- [ ] `NotificationScheduler.recordAppExit()` correctly stores preferences
- [ ] `ContextualMessageGenerator` produces valid messages for different scenarios
- [ ] `ScheduledNotificationReceiver` handles missing saga gracefully
- [ ] Character selection logic rotates appropriately

### Integration Tests

- [ ] End-to-end flow: exit app → wait 2 hours → receive contextual notification
- [ ] Notification cancellation when returning to app
- [ ] Deep link navigation from scheduled notification
- [ ] Multiple saga switching scenarios

### Manual Testing Scenarios

- [ ] **Happy Path**: Exit during active conversation → receive character message
- [ ] **No Characters**: Exit during system message → receive saga-themed notification
- [ ] **Saga Deleted**: Exit, delete saga, wait → no notification or graceful fallback
- [ ] **Permission Changes**: Revoke notification permission → log warning
- [ ] **Quick Return**: Exit and return within 10 minutes → notification cancelled

## Performance Considerations

### Battery Optimization

- **Minimal Processing**: Keep `ScheduledNotificationReceiver` lightweight
- **Efficient Queries**: Optimize saga and character data loading
- **Smart Scheduling**: Only schedule when user has active engagement patterns

### Memory Management

- **Lazy Loading**: Load saga context only when notification fires
- **Resource Cleanup**: Proper disposal of AI generation resources
- **Cache Strategy**: Consider caching recent character messages for faster generation

## Privacy & User Experience

### User Control

- **Settings Toggle**: Option to disable scheduled notifications
- **Frequency Control**: Allow users to adjust timing (1-4 hours)
- **Character Preferences**: Option to prefer certain characters for notifications

### Privacy Considerations

- **Local Processing**: All character selection and message generation happens locally
- **Data Minimization**: Store only necessary saga state for generation
- **Clear Consent**: Inform users about notification scheduling behavior

## Future Enhancements

### Smart Timing

- **Usage Patterns**: Learn user's active hours and schedule accordingly
- **Dynamic Delays**: Adjust timing based on story tension and user engagement
- **Multiple Notifications**: Send follow-up messages for long absences

### Rich Content

- **Character Avatars**: Enhanced avatar display in notifications
- **Story Previews**: Include scene artwork or chapter covers
- **Interactive Elements**: Quick reply options for simple responses

### Advanced AI Features

- **Emotional Intelligence**: Detect story emotional beats for appropriate messaging
- **Relationship Awareness**: ✅ **Integrated** - Use existing
  `RelationshipContent.summarizeRelation()` to factor in character relationships and dynamics with
  the main character
- **Shared History**: Reference established relationship events and emotional connections
- **Narrative Hooks**: Generate messages that tease upcoming story developments
- **Contextual Intimacy**: Messages feel more personal based on relationship depth and history

## Success Metrics

### Engagement Metrics

- **Return Rate**: Percentage of users who return within 24 hours of scheduled notification
- **Notification CTR**: Click-through rate on scheduled notifications vs. immediate ones
- **Session Duration**: Average session length after returning from scheduled notification
- **Story Progression**: Continued narrative advancement after notification engagement

### Technical Metrics

- **Delivery Success**: Percentage of scheduled notifications successfully delivered
- **Generation Speed**: Time to generate contextual message (target: <2 seconds)
- **Battery Impact**: Minimal impact on device battery life
- **Error Rate**: Frequency of fallback scenarios and graceful degradations

## Implementation Requirements

### Core Tasks

- [ ] Create `ScheduledNotificationService` as independent service with simple interface
- [ ] **Single Entry Point**: `scheduleNotification(sagaContent: SagaContent)` method handles
  everything internally
- [ ] **ChatPrompts Integration**: Add `scheduledNotificationPrompt()` to `ChatPrompts.kt` following
  buildString pattern
- [ ] **Reuse Existing Functions**: Use `SagaPrompts.mainContext()`, `character.toAiNormalize()`,
  `GenrePrompts.conversationDirective()`
- [ ] **Relationship Mapping**: Leverage existing `RelationshipContent.summarizeRelation()` for
  contextual character connections
- [ ] **MessageUseCase Integration**: Use existing `MessageUseCase.getSceneContext()` and
  GemmaClient
- [ ] Create `ScheduledNotificationReceiver` for lightweight notification delivery using
  pre-generated messages
- [ ] **JSON Storage**: Implement `ScheduledNotification` data class with kotlinx.serialization
- [ ] **Complete Independence**: No data exchange with `SagaContentManager` or reactive systems
- [ ] Enhance `ChatViewModel` lifecycle with simple service calls
- [ ] Add AndroidManifest.xml receiver registration

### Critical Implementation Details

- [ ] **Complete State Cleanup**: `cancelScheduledNotifications()` must clear both AlarmManager AND
  the single JSON DataStore preference
- [ ] **JSON Serialization**: Use kotlinx.serialization to convert `ScheduledNotification` to/from
  JSON
- [ ] **Atomic Operations**: Store all notification data as one JSON object for consistency
- [ ] **MessageUseCase Integration**: Reuse existing `MessageUseCase.getSceneContext()` and
  GemmaClient for authentic character voice
- [ ] **Character Personality**: Follow established character backstory and personality traits in
  generated messages
- [ ] **Relationship Context**: Use existing `saga.mainCharacter.relationships` and
  `summarizeRelation()` to reference shared history
- [ ] **Genre Consistency**: Use `GenrePrompts.conversationDirective()` to maintain
  theme-appropriate language
- [ ] **Ghost Prevention**: Verify no notifications fire after user returns to app
- [ ] **Android Limitations**: Generate messages on exit due to BroadcastReceiver time constraints
- [ ] **Fallback System**: Include `fallbackMessage` field in JSON for when AI generation fails
- [ ] **Battery Optimization**: Lightweight `ScheduledNotificationReceiver` with simple JSON parsing
- [ ] **Configurable Timing**: Create constants for easy testing and debug builds
- [ ] **Debug Build Support**: Use shorter notification delays for development testing

## Rollback Strategy

### Quick Disable

- **Feature Flag**: Add boolean preference to completely disable scheduled notifications
- **Graceful Degradation**: System continues working without scheduled component
- **Clean Removal**: Cancel all pending notifications and clear preferences

### Component Isolation

- **Independent Module**: Scheduled notifications don't affect existing immediate notifications
- **Separate Preferences**: Isolated DataStore keys for easy cleanup
- **Optional Dependencies**: Core app functionality unaffected by scheduled notification failures

## Final Implementation Instructions

### Pre-Implementation Setup

- [ ] **Create Feature Branch**: Create new branch `feature/scheduled-contextual-notifications`
  before starting implementation to avoid code conflicts
- [ ] **Verify Base State**: Ensure main branch is clean and all existing features are working
  correctly

### Post-Implementation Tasks

- [ ] **Update Roadmap**: After successful implementation, update
  `/docs/feature_planning/roadmap.md`:
    - Change status from "Planning" to "Completed ✅" for "Scheduled Contextual Notifications"
    - Add implementation details and key features to the roadmap description
    - Follow the same format as other completed features in the roadmap

### Testing Configuration

- [ ] **Debug Timing**: Use `NOTIFICATION_DELAY_DEBUG_MINUTES = 5` for debug builds (automatic)
- [ ] **Quick Testing**: For immediate testing, temporarily change to
  `NOTIFICATION_DELAY_TESTING_MINUTES = 3`
- [ ] **Production Ready**: Ensure `NOTIFICATION_DELAY_PRODUCTION_HOURS = 2` for release builds

### Verification Checklist

- [ ] All notification timing constants are properly configured
- [ ] Debug builds automatically use shorter delays
- [ ] Complete state cleanup prevents ghost notifications
- [ ] Relationship context enhances message authenticity
- [ ] JSON storage handles all edge cases gracefully
- [ ] Roadmap is updated to reflect completed implementation

This comprehensive plan ensures we build a robust, engaging notification system that enhances user
experience while maintaining system reliability and performance.
