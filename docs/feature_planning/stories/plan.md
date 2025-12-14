# Stories Feature Implementation Plan

## Overview

Add an engaging "Stories" row to `HomeView` displaying active sagas (not ended, > 1 chapter).
Tapping a story opens a full-screen bottom sheet with two pages showing AI-generated content to
re-engage users with their ongoing sagas.

## User Experience Flow

### 1. Home Screen

- **Stories Row**: Horizontal scrollable row of saga icons (similar to Instagram/WhatsApp stories)
- **Filtering**: Only show sagas that are:
    - Not ended (`isEnded = false`)
    - Have more than 1 chapter
- **Visual**: Saga icons with story indicator ring around them

### 2. Story Sheet (Bottom Sheet)

When user taps a story icon:

- **Full-screen `ModalBottomSheet`** opens
- **Two pages** using `HorizontalPager`:
    1. **"Previously on [Saga Title]"** - AI-generated summary
    2. **"The history continues"** - AI-generated hook/teaser
- **Story indicators** at the top (like Instagram stories)
- **Swipe up gesture** to continue the saga (opens Chat)
- **Continue button** at the bottom as fallback

### 3. Visual Design

- **Background**: Saga icon with black tint overlay (0.4-0.5f alpha)
- **Text**: Briefing text overlaid on the image
- **Loading**: `StarryLoader` while generating the briefing
- **Story Indicators**: Reusable component based on `SagaReview.kt` pattern (lines 310-347)

## Technical Implementation

### Architecture

#### Data Layer

**New Model:**

```kotlin
// StoryDailyBriefing.kt
data class StoryDailyBriefing(
    val summary: String,      // "Previously on..." text
    val hook: String          // "The history continues..." text
)
```

**UseCase Extension:**

- Add to `SagaDetailUseCase.kt`:
    - `generateStoryBriefing(saga: SagaContent): RequestResult<StoryDailyBriefing>`
- Implement in `SagaDetailUseCaseImpl.kt`:
    - Use `GemmaClient.generate<StoryDailyBriefing>()` for cost-effective generation
    - Single-shot generation (both summary + hook in one call)
    - Create prompt in `SagaPrompts` that generates both fields

#### ViewModel Layer

**Caching Strategy:**

```kotlin
// In HomeViewModel or new StoryViewModel
private val _briefingCache = mutableMapOf<Int, StoryDailyBriefing>()

fun getBriefing(sagaId: Int) {
    // 1. Check cache first
    // 2. If not cached, generate via UseCase
    // 3. Cache the result
    // 4. Emit to UI
}
```

**Benefits:**

- First open: Generates fresh content
- Subsequent opens (same session): Instant from cache
- Cache clears on ViewModel destruction (app close)

#### UI Layer

**New Components:**

1. **`StoriesRow.kt`** (in `HomeView.kt` or separate file)
    - Horizontal `LazyRow` of saga icons
    - Story indicator rings around icons
    - Filter logic for eligible sagas

2. **`StorySheet.kt`**
    - `ModalBottomSheet` with `HorizontalPager`
    - Two pages: Summary + Hook
    - Story indicators (reusable component)
    - Continue button + swipe up gesture

3. **`StoryIndicator.kt`** (Reusable Component)
    - Extract pattern from `SagaReview.kt` (lines 310-347)
    - Horizontal row of progress bars
    - Animated progress based on current page
    - Clickable to jump between pages

**Integration in `HomeView.kt`:**

- Add `StoriesRow` after the "Create New Saga" card
- Add state for `selectedStory: Saga?`
- Show `StorySheet` when `selectedStory != null`

### AI Generation

**Prompt Strategy:**

- **Single call** to `GemmaClient` for efficiency
- **Context**: Provide saga title, acts, chapters, and recent events
- **Output**: JSON with `summary` and `hook` fields
- **Tone**: Engaging, mysterious, hook-driven (like TV show recaps)

**Example Prompt Structure:**

```
You are creating a story briefing for a saga titled "[Title]".

Context:
- Acts: [Act summaries]
- Chapters: [Chapter summaries]
- Recent events: [Last few key moments]

Generate a JSON with:
1. "summary": A compelling 2-3 sentence recap of what happened so far (like "Previously on...")
2. "hook": An intriguing 1-2 sentence teaser about what comes next (build anticipation)

Make it dramatic and engaging, like a TV show recap.
```

### Generation Timing

- **On-demand**: Generate when user taps a story (not preemptively)
- **With caching**: Cache in ViewModel for session duration
- **Loading UX**: Show `StarryLoader` during generation (~1-3s)

## Implementation Checklist

### Phase 1: Core Logic

- [ ] Create `StoryDailyBriefing.kt` data model
- [ ] Add `hasMoreThanOneChapter` helper to `SagaContent.kt`
- [ ] Add `generateStoryBriefing()` to `SagaDetailUseCase.kt`
- [ ] Implement in `SagaDetailUseCaseImpl.kt` with `GemmaClient`
- [ ] Create prompt in `SagaPrompts.kt`

### Phase 2: UI Components

- [ ] Extract `StoryIndicator.kt` reusable component from `SagaReview.kt`
- [ ] Create `StoriesRow` composable
- [ ] Create `StorySheet.kt` with `HorizontalPager`
- [ ] Implement two pages: Summary + Hook
- [ ] Add swipe up gesture to continue saga

### Phase 3: Integration

- [ ] Add caching logic to ViewModel
- [ ] Integrate `StoriesRow` into `HomeView.kt`
- [ ] Wire up navigation to Chat on continue
- [ ] Add loading states with `StarryLoader`

### Phase 4: Polish

- [ ] Refine visual design (tint, spacing, typography)
- [ ] Test with multiple sagas
- [ ] Test cache behavior
- [ ] Ensure smooth animations

## Files to Create

- `app/src/main/java/com/ilustris/sagai/features/stories/data/model/StoryDailyBriefing.kt`
- `app/src/main/java/com/ilustris/sagai/features/stories/ui/StorySheet.kt`
- `app/src/main/java/com/ilustris/sagai/features/stories/ui/StoryIndicator.kt`
- `app/src/main/java/com/ilustris/sagai/features/stories/ui/StoriesRow.kt`

## Files to Modify

- `app/src/main/java/com/ilustris/sagai/features/home/data/model/SagaContent.kt`
- `app/src/main/java/com/ilustris/sagai/features/saga/detail/data/usecase/SagaDetailUseCase.kt`
- `app/src/main/java/com/ilustris/sagai/features/saga/detail/data/usecase/SagaDetailUseCaseImpl.kt`
- `app/src/main/java/com/ilustris/sagai/core/ai/prompts/SagaPrompts.kt`
- `app/src/main/java/com/ilustris/sagai/features/home/ui/HomeView.kt`
- `app/src/main/java/com/ilustris/sagai/features/home/ui/HomeViewModel.kt`

## Success Criteria

- [ ] Stories row appears in HomeView for eligible sagas
- [ ] Tapping a story opens the bottom sheet
- [ ] Briefing generates in ~1-3 seconds
- [ ] Both summary and hook display correctly
- [ ] Story indicators work (tap to switch pages)
- [ ] Swipe up or button navigates to Chat
- [ ] Cache prevents redundant generation in same session
- [ ] UI matches design spec (tint, layout, typography)

## Future Enhancements

- Daily refresh of briefings (cache invalidation)
- Animated transitions between pages
- Custom story ring colors based on genre
- "Mark as read" functionality
- Analytics tracking for engagement
