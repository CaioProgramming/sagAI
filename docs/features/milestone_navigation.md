# Current Objective Milestone - Simplified Architecture

## Overview

Implemented a non-intrusive milestone system for displaying current objectives. The key insight: *
*ChatView doesn't need to know about milestone types** - it just renders chat, and MilestoneOverlay
handles its own presentation.

## Simplified Architecture

### The Key Principle

> The Box layering naturally handles overlaps. MilestoneOverlay controls its own background opacity.
> ChatView just renders chat.

### ChatView Structure

```kotlin
Box {  // Parent container
    AnimatedContent(chatState) {  // Chat layer - ALWAYS renders
        // Chat content here
        
        // Milestone renders INSIDE AnimatedContent to access rootScope
        milestone?.let {
            MilestoneOverlay(
                milestone = it,
                animatedVisibilityScope = rootScope  // From AnimatedContent
            )
        }
    }
    
    SagaSnackBar(...)  // Also overlays on top
}
```

### Why This Is Better

**Before (overcomplicated):**

- ChatView conditionally rendered chat OR milestone based on `isIntrusive`
- Extra AnimatedContent switching logic
- ChatView needed to understand milestone types

**After (simplified):**

- ChatView always renders chat
- Milestone overlays on top when present
- MilestoneOverlay decides its own background:
    - Intrusive: Solid background → completely covers chat
    - Non-intrusive: Faded background → chat shows through
- Natural Box layering does the work

## Implementation Details

### 1. MilestoneOverlay Controls Presentation

```kotlin
// Background logic in MilestoneOverlay
Box(
    modifier = Modifier
        .fillMaxSize()
        .background(
            if (milestone.isIntrusive) {
                MaterialTheme.colorScheme.background  // Solid - covers everything
            } else {
                Color.Black.copy(alpha = 0.4f)  // Faded - chat visible
            }
        )
)
```

### 2. CurrentObjective Milestone

**Properties:**

- `isIntrusive = false` → Uses faded background
- No AI message generation (saves API calls)
- Immediate dismissal (no 5-second delay)
- Positioned at top instead of center

**Data Structure:**

```kotlin
class CurrentObjective(
    val timeline: Timeline,  // The Timeline entity (not TimelineContent)
) : SagaMilestone(
    R.string.current_objective,
    timeline.currentObjective ?: "",
) {
    override val isIntrusive = false
}
```

### 3. When Objective Milestone Triggers

1. **New timeline created** → `getTimelineObjective()` generates objective → Milestone emitted
2. **Missing objective detected** in `checkObjective()` → Generated → Milestone emitted

```kotlin
// In SagaContentManagerImpl
val updatedTimeline = timelineUseCase
    .getTimelineObjective(content.value!!, it)
    .getSuccess()

updatedTimeline?.let { timeline ->
    if (timeline.currentObjective?.isNotEmpty() == true) {
        emitMilestone(SagaMilestone.CurrentObjective(timeline))
    }
}
```

## Benefits of Simplified Approach

1. **Separation of Concerns**: ChatView doesn't care about milestone types
2. **Natural Layering**: Box stacking does the overlap work automatically
3. **Easier to Maintain**: Less conditional logic
4. **More Flexible**: Easy to add new milestone types without touching ChatView
5. **Better Performance**: Chat stays rendered (no re-composition on milestone show/hide)

## User Experience

### Intrusive Milestones (Events, Chapters, Acts, Characters)

- Full screen with solid background
- Chat completely hidden
- User must acknowledge before continuing
- 5-second delay to view achievements (0 for characters)

### Non-Intrusive Milestone (Current Objective)

- Faded overlay (40% opacity)
- Chat visible and slightly darkened behind
- Quick notification style
- Immediate dismissal

## Files Modified

1. **SagaMilestone.kt** - Added CurrentObjective with isIntrusive property
2. **MilestoneOverlay.kt** - Self-manages background based on milestone type
3. **ChatView.kt** - Simplified to always render chat, milestone overlays on top
4. **SagaContentManagerImpl.kt** - Triggers CurrentObjective milestone with Timeline entity

## Testing Notes

- ✅ Chat renders continuously (check with performance monitor)
- ✅ Intrusive milestones fully cover chat (solid background)
- ✅ CurrentObjective shows faded background with chat behind
- ✅ Spark transition works in both directions
- ✅ No API calls for CurrentObjective congrats message
- ✅ Timeline entity passed correctly to milestone

## Date

January 12, 2026

