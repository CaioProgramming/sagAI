# Conversation Snippet Sharing

## Goal
Enable users to select and share beautiful, branded screenshots of conversation snippets (similar to Spotify's lyrics sharing feature). Users can select 6-10 messages from their saga conversations and generate a shareable card optimized for social media (Instagram stories, Twitter, etc.).

## Motivation
- **Social Sharing**: Allow users to share memorable moments from their sagas with friends
- **Marketing**: User-generated content showcasing the app's conversations
- **Engagement**: Encourage users to revisit and highlight favorite dialogue
- **Simplicity**: Direct, visual sharing without complex AI processing
- **Branding**: Subtle app promotion through watermarked share cards

## Scope

### 1. Multi-Selection UI
- Long press on a message to enter selection mode
- Tap messages to select/deselect (visual feedback with checkmarks or highlights)
- Display selection counter (e.g., "3/10 messages selected")
- Limit selection to 6-10 messages (configurable based on card layout)
- Show "Share" button when at least 1 message is selected
- Exit selection mode with back button or cancel action

### 2. Share Card Generation
- Render selected messages as a beautiful, branded card
- **Reuse existing `ChatBubble` components** to ensure authentic conversation appearance:
  - Same bubble shapes (genre-specific)
  - Same colors and gradients
  - Same fonts and text styling
  - Character avatars and names
  - All visual elements identical to ChatView
- Include saga title at the top of the card
- Add `SagaTitle` watermark at the bottom (same as other share cards)
- Use the same aspect ratio as existing share cards (works well for all social media)

### 3. ShareSheet Integration
- Extend existing `ShareSheet.kt` with a new "Share Conversation" mode
- Add tab/option to switch between existing share types and conversation sharing
- Reuse existing share infrastructure (Android share intent)

### 4. Rendering & Export
- Capture the composed card as a bitmap
- Save to cache directory
- Trigger Android share intent with the generated image
- Clean up cached images after sharing

## Technical Approach

### 1. Selection State Management
Add selection state to `ChatViewModel`:
```kotlin
// In ChatViewModel
data class MessageSelectionState(
    val isSelectionMode: Boolean = false,
    val selectedMessageIds: Set<String> = emptySet(),
    val maxSelection: Int = 10
)

private val _selectionState = MutableStateFlow(MessageSelectionState())
val selectionState: StateFlow<MessageSelectionState> = _selectionState.asStateFlow()

fun toggleSelectionMode() { ... }
fun toggleMessageSelection(messageId: String) { ... }
fun clearSelection() { ... }
```

### 2. UI Updates in ChatView
- Add long press gesture detector to `ChatBubble`
- Show selection overlay (checkmark icon) when in selection mode
- Display floating action button or top bar with "Share" action
- Highlight selected messages with border or background tint

### 3. Share Card Composable
Create a new composable for rendering the share card:
```kotlin
@Composable
fun ConversationShareCard(
    sagaTitle: String,
    genre: Genre,
    messages: List<Message>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .background(genre.color)
            .padding(24.dp)
    ) {
        // Saga title header
        Text(sagaTitle, style = genre.headerFont())
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Render each message using the EXACT same ChatBubble component
        messages.forEach { message ->
            ChatBubble(
                message = message,
                genre = genre,
                // ... same params as ChatView
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Watermark using SagaTitle (same as other share cards)
        SagaTitle(
            textStyle = MaterialTheme.typography.labelMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
```

### 4. Bitmap Capture
Use Compose's rendering capabilities:
```kotlin
suspend fun captureShareCard(
    context: Context,
    composable: @Composable () -> Unit
): Bitmap {
    // Use ComposeView or similar to render composable to bitmap
    // Return bitmap for sharing
}
```

### 5. ShareSheet Extension
Update `ShareSheet.kt` to include conversation sharing:
- Add new share type enum/sealed class
- Add UI to switch between share modes
- Handle conversation share flow

> [!NOTE]
> **No AI Required**: This feature focuses on visual presentation of user-selected content, not AI analysis. This keeps it simple, fast, and reliable.

> [!NOTE]
> **Reusing ChatBubble Components**: The share card will use the exact same `ChatBubble` composable from `ChatView`, ensuring the conversation looks identical to what users see in the app. This maintains visual consistency and authenticity.

> [!NOTE]
> **Single Aspect Ratio**: Following the existing share card pattern (same dimensions as `PlayStyleShareView`, `CharacterShareView`, etc.), which works well across all social media platforms. No need for multiple sizes.

## Implementation Files

### New Files
- `app/src/main/java/com/ilustris/sagai/features/share/ui/ConversationShareView.kt` - Share view for conversation snippets (follows pattern of `PlayStyleShareView`, `CharacterShareView`, etc.)

### Modified Files
- `app/src/main/java/com/ilustris/sagai/features/saga/chat/viewmodel/ChatViewModel.kt` - Add message selection state and logic
- `app/src/main/java/com/ilustris/sagai/features/saga/chat/ui/ChatView.kt` - Add selection UI and gestures
- `app/src/main/java/com/ilustris/sagai/ui/theme/components/chat/ChatBubble.kt` - Add selection mode visual feedback (checkmark overlay)
- `app/src/main/java/com/ilustris/sagai/features/share/ui/ShareSheet.kt` - Add `ShareType.CONVERSATION` case
- `app/src/main/java/com/ilustris/sagai/features/share/domain/model/ShareType.kt` - Add `CONVERSATION` enum value

## Design Considerations

### Card Layout
```
┌─────────────────────────┐
│   Saga Title            │ ← Header (genre headerFont)
├─────────────────────────┤
│                         │
│  [ChatBubble]           │ ← Message 1 (actual ChatBubble component)
│  with avatar, shape,    │   Same as ChatView!
│  colors, etc.           │
│                         │
│  [ChatBubble]           │ ← Message 2
│  User message           │   Same bubble shapes!
│                         │
│  [ChatBubble]           │ ← Message 3
│  Character reply        │   Same colors!
│                         │
│         ...             │ ← More messages (up to 10)
│                         │
├─────────────────────────┤
│    [Spark Icon]         │ ← Same as other share cards
│   SagaTitle component   │ ← Watermark (reused component)
└─────────────────────────┘
```

**Key Point**: The messages section uses the **exact same `ChatBubble` component** from `ChatView`, ensuring perfect visual consistency.

### Selection UI States
1. **Normal Mode**: Standard chat view
2. **Selection Mode Active**: 
   - Checkmark overlays on messages
   - Selected messages highlighted
   - Counter badge showing "X/10"
   - Share FAB visible
3. **Share Preview**: Show card preview before sharing

## Verification Plan

### Manual Verification
- [ ] **Selection Mode**: Long press enters selection mode correctly
- [ ] **Multi-Select**: Can select/deselect multiple messages
- [ ] **Selection Limit**: Cannot select more than max (10) messages
- [ ] **Visual Feedback**: Selected messages clearly highlighted
- [ ] **Card Generation**: Share card renders with correct styling
- [ ] **Genre Styling**: Card applies correct genre theme (colors, fonts, shapes)
- [ ] **Avatars**: Character avatars display correctly
- [ ] **Watermark**: App branding visible but subtle
- [ ] **Share Intent**: Android share dialog opens with generated image
- [ ] **Image Quality**: Shared image is high quality and readable
- [ ] **Different Message Counts**: Test with 1, 5, and 10 messages
- [ ] **Long Messages**: Test with very long message text (truncation?)
- [ ] **RTL Support**: Test with RTL languages if applicable

### Edge Cases
- [ ] Messages with images/media (include or skip?)
- [ ] System messages (actions, thoughts) - should they be shareable?
- [ ] Very long character names or saga titles
- [ ] Empty message content

## Tasks
- [ ] Add message selection state to `ChatViewModel`
- [ ] Implement selection mode toggle and message selection logic
- [ ] Add long press gesture to `ChatBubble` to enter selection mode
- [ ] Create selection UI overlay (checkmarks, highlights)
- [ ] Add selection counter and share FAB to `ChatView`
- [ ] Design and implement `ConversationShareCard` composable
- [ ] Apply genre-specific styling to share card
- [ ] Add saga title header and app watermark footer
- [ ] Implement bitmap capture helper
- [ ] Integrate conversation sharing into `ShareSheet`
- [ ] Test share card rendering with different message counts
- [ ] Test with different genres and themes
- [ ] Optimize card dimensions for Instagram stories
- [ ] Add analytics tracking for share feature usage
- [ ] Handle edge cases (long text, special message types)

## Future Enhancements
- [ ] Multiple aspect ratio support (square, horizontal)
- [ ] Custom background options (solid colors, gradients, images)
- [ ] Text size adjustment for readability
- [ ] Option to hide/show character avatars
- [ ] Direct sharing to specific platforms (Instagram, Twitter APIs)
