# Instagram Rotation Effect for Message Reactions

## Overview
This feature implements an Instagram Stories-style avatar rotation effect for the **last message** in the ChatList. When a message has reactions from multiple characters, their avatars will animate in a shifting/rotating pattern, with small speech bubbles displaying the reaction emoji and optional thought text.

This feature is **Pirates theme specific** and will be placed in the ideas folder for future implementation.

---

## Visual Reference

![Instagram Rotation Effect Reference](/Users/caioferreira/.gemini/antigravity/brain/9b85ab09-5568-419b-a1d9-4e3778632175/uploaded_image_1764873021704.png)

The reference image shows:
- Multiple circular avatars arranged horizontally
- Avatars shift/rotate position with smooth animation
- A dark speech bubble appears showing reaction emojis (😂😂😂😂)
- The bubble is positioned near the avatars with a tail/pointer

---

## Feature Specification

### Core Behavior

1. **Trigger Condition**: 
   - Only applies to the **last message** in the ChatList
   - Message must have **2 or more reactions** from different characters
   - Animation starts automatically when the message becomes visible

2. **Avatar Rotation Animation**:
   - Character avatars are displayed in a horizontal row
   - Avatars continuously shift positions in a circular rotation pattern
   - Each avatar cycles through the visible positions (e.g., 3 visible slots)
   - Smooth transition between positions using `animateFloatAsState` or `Animatable`
   - Rotation speed: ~2-3 seconds per full cycle
   - Infinite repeat while message is visible

3. **Reaction Bubble Display**:
   - Small speech bubble appears above/beside the rotating avatars
   - Bubble shows the **currently featured reaction's `thought` text** (from `Reaction.thought`)
   - Text limited to **1 line maximum** with ellipsis overflow
   - Bubble rotates/changes content in sync with avatar rotation
   - Uses the **same ChatBubble component** as messages for visual consistency

### Visual Design (Pirates Theme)

#### Avatar Container
- Circular avatars with pirate-themed borders
- Size: **24-32dp per avatar** (smaller to avoid calling too much attention)
- Spacing: -8dp overlap for stacked effect
- Border: 1dp themed stroke (subtle)
- Shadow: Minimal elevation for depth

#### Reaction Bubble
- **Component**: Reuse existing `ChatBubble` component for consistency
- **Content**: Display `Reaction.thought` text only (no emoji)
- **Text Style**: 
  - Font size: 12sp
  - Max lines: 1
  - Overflow: `TextOverflow.Ellipsis`
  - Color: Based on genre theme (same as ChatBubble)
- **Styling**: Inherits from ChatBubble (genre-specific shape, colors, borders)

#### Animation Specifications
- **Avatar Shift**: 
  - Duration: 800ms
  - Easing: `FastOutSlowInEasing`
  - Translation: Horizontal slide with fade in/out
- **Bubble Transition**:
  - Duration: 400ms (faster than avatar)
  - Easing: `EaseInOutCubic`
  - Cross-fade between reaction contents

---

## Technical Implementation Plan

### Data Structure

```kotlin
// Extension function to determine if message should show rotation effect
fun MessageContent.shouldShowRotationEffect(isLastMessage: Boolean): Boolean {
    return isLastMessage && reactions.size >= 2
}

// State holder for rotation animation
data class RotationState(
    val visibleAvatarCount: Int = 3,
    val currentIndex: Int = 0,
    val reactions: List<ReactionContent>
)
```

### Component Architecture

#### 1. `RotatingReactionAvatars.kt` (New Component)
Main composable that orchestrates the rotation effect.

**Responsibilities**:
- Manage rotation state and animation timing
- Render avatar row with rotation animation
- Display reaction bubble synced with current featured reaction
- Handle infinite animation loop

**Key Parameters**:
```kotlin
@Composable
fun RotatingReactionAvatars(
    reactions: List<ReactionContent>,
    genre: Genre, // For pirate-themed styling
    modifier: Modifier = Modifier,
    visibleAvatarCount: Int = 3,
    rotationDurationMs: Int = 2500,
    bubblePosition: BubblePosition = BubblePosition.Top
)
```

#### 2. Reaction Bubble Integration
Reuse existing `ChatBubble` component for displaying reaction thought text.

**Approach**:
- Wrap `ChatBubble` with minimal configuration
- Pass `reaction.data.thought` as message text
- Apply compact styling (small padding, single line)
- Position above/beside rotating avatars

**Implementation**:
```kotlin
// Use existing ChatBubble component
ChatBubble(
    messageContent = MessageContent(
        message = Message(
            text = reaction.data.thought ?: "",
            // ... minimal required fields
        ),
        character = reaction.character,
        reactions = emptyList()
    ),
    genre = genre,
    modifier = modifier,
    compact = true // If such parameter exists, or apply compact styling
)
```

### Integration Points

#### ChatBubble.kt Modification
Add rotation effect to the message component when conditions are met:

```kotlin
@Composable
fun ChatBubble(
    messageContent: MessageContent,
    // ... existing parameters
    isLastMessage: Boolean = false, // NEW PARAMETER
) {
    // ... existing code
    
    // Add rotation effect after message content
    if (messageContent.shouldShowRotationEffect(isLastMessage)) {
        RotatingReactionAvatars(
            reactions = messageContent.reactions,
            genre = genre,
            modifier = Modifier
                .padding(top = 8.dp)
                .align(Alignment.Start)
        )
    }
}
```

#### ChatList.kt Modification
Track the last message and pass the flag to ChatBubble:

```kotlin
// In ChatList composable, when rendering messages
val isLastMessage = messageContent.message.id == lastMessageId

ChatBubble(
    messageContent = messageContent,
    // ... existing parameters
    isLastMessage = isLastMessage
)
```

---

## Animation Details

### Avatar Rotation Cycle

**Pattern** (for 5 reactions, 3 visible slots):
```
Cycle 0: [Avatar1] [Avatar2] [Avatar3]  (Avatar4, Avatar5 hidden)
Cycle 1: [Avatar2] [Avatar3] [Avatar4]  (Avatar5, Avatar1 hidden)
Cycle 2: [Avatar3] [Avatar4] [Avatar5]  (Avatar1, Avatar2 hidden)
Cycle 3: [Avatar4] [Avatar5] [Avatar1]  (Avatar2, Avatar3 hidden)
Cycle 4: [Avatar5] [Avatar1] [Avatar2]  (Avatar3, Avatar4 hidden)
```

**Implementation Approach**:
```kotlin
val infiniteTransition = rememberInfiniteTransition()
val currentIndex by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = reactions.size.toFloat(),
    animationSpec = infiniteRepeatable(
        animation = tween(
            durationMillis = rotationDurationMs * reactions.size,
            easing = LinearEasing
        ),
        repeatMode = RepeatMode.Restart
    )
)

// Calculate visible reactions based on currentIndex
val visibleReactions = reactions.getVisibleReactions(
    currentIndex = currentIndex.toInt() % reactions.size,
    visibleCount = visibleAvatarCount
)
```

### Bubble Content Sync

The reaction bubble should display the **middle avatar's** reaction thought text (featured position):

```kotlin
val featuredReaction = visibleReactions[visibleAvatarCount / 2]

// Only show bubble if thought text exists
if (featuredReaction.data.thought?.isNotBlank() == true) {
    ChatBubble(
        messageContent = MessageContent(
            message = Message(
                text = featuredReaction.data.thought!!,
                senderType = SenderType.CHARACTER,
                // ... other required fields
            ),
            character = featuredReaction.character,
            reactions = emptyList()
        ),
        genre = genre,
        modifier = Modifier
            .offset(y = (-36).dp) // Position above avatars
            .widthIn(max = 200.dp) // Limit width
            .animateContentSize(),
        compact = true
    )
}
```

---

## Pirates Theme Styling

### Color Palette
- **Avatar Border**: Weathered Gold (`Color(0xFFD4AF37)`)
- **Bubble Background**: Deep Ocean Blue (`Color(0xFF006B7D)`) at 90% opacity
- **Bubble Border**: Weathered Gold (`Color(0xFFD4AF37)`)
- **Text Color**: White (`Color.White`)
- **Shadow**: Black at 20% opacity

### Shape Customization
- **Avatar**: `CircleShape` with weathered border effect
- **Bubble**: `RoundedCornerShape(12.dp)` with custom tail path
- **Tail**: Triangle path drawn using `Canvas` or custom `Shape`

### Texture Effects (Optional Enhancement)
- Apply subtle grain texture to bubble background
- Weathered/scratched border effect
- Slight rotation/tilt to avatars for hand-drawn feel

---

## Performance Considerations

### Optimization Strategies

1. **Lazy Composition**:
   - Only compose rotation effect for the last message
   - Dispose animation when message scrolls out of view

2. **Animation Efficiency**:
   - Use `remember` to cache avatar bitmaps
   - Limit visible avatar count to 3-4 maximum
   - Use `graphicsLayer` for transformations (hardware accelerated)

3. **Reaction Limit**:
   - Cap maximum reactions displayed to 10
   - If more than 10 reactions, show "+N more" indicator

4. **Conditional Rendering**:
   - Only render when message is in viewport (use `LazyColumn` item visibility)
   - Pause animation when app is in background

---

## User Experience Flow

### Scenario: New Reaction Added to Last Message

1. User sends a message (becomes last message)
2. AI characters react to the message over time
3. **First reaction**: Standard reaction display (no rotation)
4. **Second reaction**: Rotation effect triggers
   - Avatars appear with fade-in animation
   - Rotation cycle begins
   - Bubble shows first reaction
5. **Additional reactions**: Seamlessly added to rotation cycle
6. User scrolls or new message arrives: Effect stops (no longer last message)

### Edge Cases

- **Single Reaction**: No rotation effect, standard display
- **Message Deleted**: Effect stops immediately
- **Rapid Reactions**: Queue reactions and add to cycle smoothly
- **Screen Rotation**: Preserve animation state
- **Low-End Devices**: Reduce animation complexity or disable

---

## Future Enhancements (Post-MVP)

### Multi-Theme Support
- Extend beyond Pirates theme to all genres
- Theme-specific bubble shapes and colors
- Genre-appropriate animation styles

### Interactive Features
- Tap avatar to view full reaction details
- Long-press to see all reactions in a sheet
- Swipe to manually control rotation

### Advanced Animations
- 3D rotation effect using `graphicsLayer` rotation
- Parallax effect between avatars and bubble
- Particle effects (sparkles, water splashes for Pirates)

### Accessibility
- VoiceOver/TalkBack support for reaction content
- Reduce motion option to disable rotation
- High contrast mode for bubble visibility

---

## Implementation Checklist

### Phase 1: Core Components
- [ ] Create `RotatingReactionAvatars.kt` composable
- [ ] Integrate existing `ChatBubble` component for reaction display
- [ ] Implement avatar rotation animation logic
- [ ] Implement bubble content sync with featured avatar's thought text
- [ ] Apply compact styling to reaction bubble (24-32dp avatars, 1-line text)

### Phase 2: Integration
- [ ] Add `isLastMessage` parameter to `ChatBubble`
- [ ] Modify `ChatList` to track last message ID
- [ ] Add `shouldShowRotationEffect()` extension function
- [ ] Integrate rotation effect into `ChatBubble` conditionally

### Phase 3: Polish & Optimization
- [ ] Add fade-in/fade-out transitions for avatars
- [ ] Ensure bubble only shows when `thought` text exists
- [ ] Optimize animation performance (graphicsLayer)
- [ ] Add viewport visibility detection
- [ ] Handle edge cases (single reaction, empty thought, deleted message)

### Phase 4: Testing
- [ ] Test with 2, 3, 5, 10+ reactions
- [ ] Test scrolling behavior (effect stops when not last)
- [ ] Test new message arrival (effect transfers)
- [ ] Test screen rotation and configuration changes
- [ ] Performance testing on low-end devices

### Phase 5: Documentation
- [ ] Add KDoc comments to new components
- [ ] Create preview composables for design review
- [ ] Document animation parameters and customization
- [ ] Add usage examples in component documentation

---

## Technical Dependencies

### Existing Code
- `MessageContent.kt` - Data model for messages with reactions
- `ReactionContent.kt` - Data model for individual reactions
- `ChatBubble.kt` - Main message bubble component
- `ChatList.kt` - Message list container
- `Genre.kt` - Theme configuration (Pirates)

### Compose Libraries
- `androidx.compose.animation:animation` - For `animateFloatAsState`, `InfiniteTransition`
- `androidx.compose.ui:ui` - For `graphicsLayer`, `Canvas`
- `androidx.compose.foundation:foundation` - For `Image`, `Box`, `Row`

### No New External Dependencies Required

---

## Acceptance Criteria

### Functional Requirements
- ✅ Rotation effect only appears on the last message
- ✅ Requires minimum 2 reactions to trigger
- ✅ Avatars rotate smoothly in a continuous cycle
- ✅ Reaction bubble displays current featured reaction
- ✅ Effect stops when message is no longer last
- ✅ Animation is smooth and performant (60fps)

### Visual Requirements
- ✅ Pirates theme styling applied (ocean blue, weathered gold)
- ✅ Avatars have circular shape with themed borders
- ✅ Bubble has speech bubble shape with tail
- ✅ Animations use appropriate easing curves
- ✅ Proper spacing and alignment with message bubble

### Performance Requirements
- ✅ No frame drops during animation
- ✅ Minimal memory overhead
- ✅ Animation pauses when off-screen
- ✅ Graceful degradation on low-end devices

---

## Notes

- **Scope**: This is a Pirates theme-specific feature for now
- **Location**: Placed in `docs/ideas/` as it's not immediately scheduled for implementation
- **Inspiration**: Instagram Stories reaction animations
- **Priority**: Low/Medium - Polish feature for enhanced UX
- **Estimated Effort**: 2-3 days for full implementation and testing

---

**Status**: Concept/Planning - Ready for review and future implementation
**Next Steps**: Review with team, gather feedback, prioritize in roadmap
