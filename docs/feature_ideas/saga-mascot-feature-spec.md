# sagAI Mascot Feature Specification

## Overview

This document outlines the design, implementation strategy, and technical considerations for
introducing "Saga" - a mascot character for the sagAI storytelling platform. Similar to Duolingo's
Duo, Saga serves as an emotional anchor and brand identity that enhances user engagement through
carefully timed appearances and adaptive theming.

---

## Rationale

### Why a Mascot?

**Emotional Connection**

- Creates a memorable brand identity
- Humanizes the AI-powered storytelling experience
- Provides a consistent "companion" throughout the user journey

**Engagement Enhancement**

- Celebrates user milestones and achievements
- Softens system notifications and prompts
- Adds personality to an already highly personalized app

**Strategic Fit**

- sagAI is already 95% personalized (text, colors, animations)
- The mascot becomes the physical embodiment of this personality
- Acts as the "cherry on top" rather than a core dependency

### Reference Inspirations

- **Duolingo's Duo**: Memorable, emotionally engaging, strategically timed
- **Crash Bandicoot / Spyro**: Iconic, expressive, personality-driven characters
- **Clippy (lessons learned)**: Avoid being intrusive or overused

---

## Design Philosophy

### Core Principles

1. **Rare and Meaningful**: Appears only at significant moments, never becomes annoying
2. **Adaptive**: Changes appearance based on story genre/theme
3. **Personality-Driven**: Speaks with tone matching current genre context
4. **Non-Intrusive**: Enhances experience without blocking core functionality
5. **Performance-Conscious**: Lightweight assets, optimized animations

### Character Concept: "Saga" the Dragon

**Base Design**

- Chinese dragon (serpentine, wise, guardian archetype)
- Flat 2D cartoon style (Duolingo aesthetic)
- Simple geometric shapes for easy adaptation
- Expressive eyes and minimal details for clarity

**Why a Dragon?**

- Mythologically associated with wisdom and storytelling across cultures
- Serpentine form is easy to animate (fluid, organic movement)
- Works across all art styles (oil painting, anime, pixel art, comic book)
- Can be noble/epic AND playful/friendly (versatile personality)

---

## Adaptive Theming System

Saga adapts its visual appearance based on the current story genre while maintaining recognizable
core identity.

### Genre Adaptations

#### FANTASY

- **Color Palette**: Warm reds (#C62828), gold accents (#FFD700)
- **Visual Style**: Oil painting aesthetic with visible brushstrokes
- **Effects**: Golden sparkle particles, ethereal glow
- **Personality**: Noble, wise, uses archaic/formal language
- **Example Dialogue**: "Ah, traveler... Thy epic journey hath reached a wondrous milestone! ✨"

#### CYBERPUNK (SCI_FI)

- **Color Palette**: Deep purple (#7C4DFF), neon cyan (#00E5FF)
- **Visual Style**: Retro anime cel-shading with clean outlines
- **Effects**: Holographic glitches, scan lines, digital pulse
- **Personality**: Cynical, street-smart, uses tech jargon
- **Example Dialogue**: "Yo netrunner, that infiltration was clean. Corps never saw it coming. ⚡"

#### HORROR

- **Color Palette**: Desaturated blue-gray (#455A64), pale accents
- **Visual Style**: Pixel art 32-bit with blocky shading
- **Effects**: Shadow flicker, subtle trembling, fade in/out
- **Personality**: Whispered, unsettling, builds dread
- **Example Dialogue**: "you... felt that too... didn't you? the shadows... they changed... 💀"

#### HEROES (Urban Hero)

- **Color Palette**: Electric blue (#0D47A1), vibrant cyan (#00BCD4)
- **Visual Style**: Comic book line art with cross-hatching
- **Effects**: Speed blur, electric sparks, motion lines
- **Personality**: Direct, energetic, street slang
- **Example Dialogue**: "YO! Those moves were FIRE! The streets won't forget this run! ⚡"

---

## Appearance Triggers

Saga appears at carefully selected moments to maximize impact and minimize intrusion.

### Primary Trigger Points

#### 1. Onboarding

- **When**: First app launch after installation
- **Purpose**: Introduce Saga as the user's "guide through infinite stories"
- **Interaction**: Brief welcome, sets tone, disappears gracefully
- **Duration**: 3-5 seconds

#### 2. Story Completion Milestones

- **When**:
    - First story completed
    - 5th, 10th, 25th, 50th, 100th story milestones
    - Completing particularly long multi-chapter sagas
- **Purpose**: Celebrate achievement, reinforce accomplishment
- **Interaction**: Enthusiastic reaction matching genre tone
- **Duration**: 4-6 seconds

#### 3. Achievement Unlocks

- **When**: User unlocks significant achievements
- **Purpose**: Highlight progression, gamification reinforcement
- **Interaction**: Proud/excited reaction, brief commentary
- **Duration**: 3-4 seconds

#### 4. Re-engagement (Subtle)

- **When**: User hasn't opened app in 7+ days
- **Purpose**: Gentle reminder via push notification
- **Interaction**: Shows in notification icon/preview
- **Duration**: Static image only (no animation in notification)

#### 5. Genre Theme Switch

- **When**: User starts story in different genre from previous session
- **Purpose**: Showcase Saga's transformation, delight user
- **Interaction**: Quick morph animation to new theme
- **Duration**: 2-3 seconds

#### 6. Error Recovery (Optional)

- **When**: Network error or generation failure occurs
- **Purpose**: Soften frustration with personality
- **Interaction**: Apologetic but hopeful message
- **Duration**: 2-3 seconds
- **Note**: Only if error is recoverable; avoid on critical failures

### Anti-Patterns (What to Avoid)

- ❌ Appearing during active reading (breaks immersion)
- ❌ Blocking UI or requiring dismissal
- ❌ Appearing more than once per session without significant trigger
- ❌ Animating during performance-critical moments
- ❌ Using as tutorial/help system (separate feature)

---

## Technical Implementation

### Phase 1: Static MVP (Weeks 1-2)

**Goal**: Validate emotional impact with minimal investment

**Assets Required**

- 4 static PNG images (one per genre theme)
- 3 expressions per theme: neutral, happy, surprised
- Total: 12 images @ ~50KB each = ~600KB

**Implementation**

```kotlin
object MascotManager {
    data class MascotConfig(
        val genre: Genre,
        val expression: Expression,
        val message: String
    )
    
    enum class Expression {
        NEUTRAL, HAPPY, SURPRISED, CELEBRATING
    }
    
    fun show(
        trigger: TriggerPoint,
        genre: Genre,
        onDismiss: () -> Unit
    ) {
        val config = generateConfig(trigger, genre)
        // Simple fade in, display 3-5s, fade out
        displayMascot(config, onDismiss)
    }
}
```

**Generation Strategy**

- Use Imagen 3 (Google's image generation)
- Generate sprite sheets with consistent character design
- Manual selection of best poses
- Export as optimized PNGs

**Validation Metrics**

- User engagement increase after mascot appears
- Feedback mentions in reviews/support
- Retention impact of users who see mascot vs control group

---

### Phase 2: Lottie Animations (Weeks 3-4, if Phase 1 succeeds)

**Goal**: Add fluid motion for enhanced personality

**Assets Required**

- 1 base Lottie animation per theme
- Animations: idle (breathing), appear (fade+scale), celebrate (bounce), disappear
- Total: 4 themes × 4 animations = 16 Lottie files @ ~20-30KB each = ~400-500KB

**Tool**: Rive (recommended over After Effects)

- Purpose-built for interactive character animation
- Lighter learning curve than After Effects
- Native Android/iOS export
- Built-in rigging/skeletal animation

**Implementation**

```kotlin
class MascotAnimationView(context: Context) : FrameLayout(context) {
    private val lottieView = LottieAnimationView(context)
    
    fun playForGenre(genre: Genre, animation: AnimationType) {
        val composition = loadAnimation(genre, animation)
        lottieView.setComposition(composition)
        
        // Apply dynamic color mapping
        applyGenreColors(genre)
        
        lottieView.playAnimation()
    }
    
    private fun applyGenreColors(genre: Genre) {
        // Use existing Genre enum color palette
        lottieView.addValueCallback(
            KeyPath("**", "Fill 1"),
            LottieProperty.COLOR
        ) { genre.primaryColor.toArgb() }
    }
}
```

**Workflow**

1. Generate base poses in Imagen 3
2. Vectorize in Figma/Illustrator
3. Import to Rive
4. Create bone rigging
5. Animate (idle, appear, celebrate, disappear)
6. Export as Lottie JSON
7. Integrate with existing `Genre` enum

---

### Phase 3: Advanced Features (Future, if product scales)

**Dynamic Dialogue Generation**

- Use existing Claude API integration to generate contextual mascot dialogue
- Respects genre conversation directives from `GenrePrompts`
- Personalizes based on user progress and story choices

```kotlin
suspend fun generateMascotDialogue(
    genre: Genre,
    trigger: TriggerPoint,
    userStats: UserStats
): String {
    val prompt = """
    You are Saga, the mascot of a storytelling app.
    Context: ${genre.conversationDirective}
    User just: ${trigger.description}
    User stats: ${userStats.storiesCompleted} stories, ${userStats.xp} XP
    
    Respond in 1-2 sentences matching the ${genre.displayTitle} tone.
    Be encouraging but not repetitive.
    """
    
    return claudeApi.generate(prompt)
}
```

**User Customization**

- Optional: Let users toggle mascot appearances in settings
- Frequency settings (minimal, normal, frequent)
- Opt-out option (always respect user preference)

**3D Rendering** (only if commercial scale)

- Model Saga in Blender for marketing materials
- Reuse 3D model for higher fidelity animations
- Investment only justified if mascot proves high ROI

---

## Technical Considerations

### Performance

**Asset Loading**

- Lazy load mascot assets only when needed
- Cache loaded animations per session
- Preload during onboarding when network available

```kotlin
object MascotAssetManager {
    private val loadedAnimations = mutableMapOf<Genre, LottieComposition>()
    
    suspend fun preload(genre: Genre) {
        if (!loadedAnimations.containsKey(genre)) {
            val composition = loadAnimationAsync(genre)
            loadedAnimations[genre] = composition
        }
    }
    
    fun getAnimationSync(genre: Genre): LottieComposition? {
        return loadedAnimations[genre]
    }
}
```

**Memory Management**

- Release unused genre animations after 5 minutes of inactivity
- Maximum 2 genre animations in memory simultaneously
- Use WeakReferences for cached bitmaps

**Animation Performance**

- Target 60 FPS on mid-range devices (2019+)
- Hardware acceleration enabled for Lottie
- Disable animations on devices with <2GB RAM (fallback to static)

### Accessibility

**Screen Reader Support**

```kotlin
mascotView.contentDescription = "Saga the dragon mascot appears, celebrating your achievement"
mascotView.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
```

**Reduced Motion**

- Detect system "Reduce motion" preference
- Show static image with fade transition only
- Never animate if user has motion sensitivity enabled

**Color Contrast**

- Ensure mascot has sufficient contrast against backgrounds
- Test readability of dialogue text overlays
- Support high contrast modes

### Localization

**Text Considerations**

- All mascot dialogue goes through existing localization system
- Respect cultural differences in tone/humor
- Test genre-specific language adaptations (archaic terms, slang, etc.)

**Visual Consistency**

- Mascot design itself remains universal (dragon is cross-cultural)
- Only text/dialogue changes per locale

---

## Integration Points

### Existing Systems

**Genre Enum**

```kotlin
// Extend existing Genre enum
enum class Genre(
    // ... existing properties
    val mascotConfig: MascotThemeConfig
) {
    FANTASY(
        // ... existing
        mascotConfig = MascotThemeConfig(
            primaryColor = MaterialColor.Red800,
            accentColor = Color(0xFFFFD700),
            particleType = ParticleType.SPARKLES,
            conversationTone = ConversationTone.FORMAL_EPIC
        )
    ),
    // ... other genres
}
```

**GenrePrompts Integration**

- Mascot dialogue respects existing `conversationDirective`
- Leverages `nameDirectives` for character consistency
- Uses `moodDescription` for visual effect guidance

**Achievement System**

- Hook into existing XP/achievement triggers
- Read from `UserStats` for milestone detection
- Coordinate with existing notification system

### New Components

**MascotRepository**

```kotlin
interface MascotRepository {
    suspend fun shouldShowMascot(trigger: TriggerPoint): Boolean
    suspend fun recordMascotAppearance(trigger: TriggerPoint, genre: Genre)
    fun getMascotConfig(trigger: TriggerPoint, genre: Genre): MascotConfig
}
```

**MascotViewModel**

```kotlin
class MascotViewModel(
    private val repository: MascotRepository,
    private val userStatsRepository: UserStatsRepository
) : ViewModel() {
    
    val mascotState: StateFlow<MascotState>
    
    fun onTriggerPoint(trigger: TriggerPoint, genre: Genre) {
        viewModelScope.launch {
            if (repository.shouldShowMascot(trigger)) {
                val config = repository.getMascotConfig(trigger, genre)
                _mascotState.value = MascotState.Showing(config)
            }
        }
    }
}
```

---

## Success Metrics

### Key Performance Indicators

**Engagement**

- Increase in session length after mascot appears
- Return rate of users who've seen mascot vs control
- Stories completed per user after mascot introduction

**Sentiment**

- App store review mentions of mascot (positive/negative ratio)
- In-app feedback specifically about mascot
- User-generated content featuring mascot

**Technical**

- Asset load time (<200ms)
- Animation frame rate (target 60 FPS)
- Memory footprint (<10MB additional)
- Crash rate delta (should be 0%)

### A/B Testing Plan

**Phase 1 MVP**

- 50% of new users see mascot
- 50% control group (no mascot)
- Duration: 2 weeks
- Decision point: If engagement lift >5%, proceed to Phase 2

**Phase 2 Animated**

- Roll out to 100% of Phase 1 mascot group
- Measure animation vs static preference
- Collect qualitative feedback

---

## Risk Assessment

### Potential Issues

**1. Annoying/Intrusive**

- **Mitigation**: Strict trigger limits, never block UI, easily dismissible
- **Fallback**: Add settings toggle for users to disable

**2. Performance Impact**

- **Mitigation**: Lazy loading, memory management, hardware acceleration
- **Fallback**: Static images on low-end devices, disable if FPS drops

**3. Inconsistent AI Generation**

- **Mitigation**: Manual curation of initial assets, style reference images
- **Fallback**: Commission professional illustrator if AI fails

**4. Cultural Misalignment**

- **Mitigation**: Test mascot concept with diverse user group
- **Fallback**: Regional variants if needed (unlikely for dragon)

**5. Scope Creep**

- **Mitigation**: Strict phase gates, no feature addition without validation
- **Fallback**: Kill feature if Phase 1 MVP doesn't show engagement lift

---

## Timeline Summary

| Phase                      | Duration | Deliverable                         | Go/No-Go Decision           |
|----------------------------|----------|-------------------------------------|-----------------------------|
| Phase 1: Static MVP        | 2 weeks  | 12 static images, basic triggers    | Engagement lift >5%         |
| Phase 2: Lottie Animations | 2 weeks  | 16 animated files, Rive integration | User feedback positive      |
| Phase 3: Advanced (Future) | TBD      | Dynamic dialogue, 3D assets         | Product at commercial scale |

**Total Initial Investment**: 4 weeks development time, ~$0 tooling cost

---

## Conclusion

The Saga mascot represents a high-impact, low-risk enhancement to sagAI's already personalized
experience. By serving as the physical embodiment of the app's adaptive personality system and
appearing only at meaningful moments, Saga can significantly boost emotional engagement and brand
memorability without compromising performance or user experience.

The phased approach ensures validation at each step, minimizing wasted effort if the concept doesn't
resonate with users. The technical implementation leverages existing systems (Genre enum,
GenrePrompts) and proven animation technologies (Lottie/Rive), making execution straightforward for
the development team.

**Recommendation**: Proceed with Phase 1 Static MVP to validate core hypothesis with minimal
investment.

---

## Appendix A: Prompt Templates for Asset Generation

### Imagen 3 Prompt - Base Character

```
A cute Chinese dragon mascot character in flat 2D style, Duolingo aesthetic, simple geometric shapes, friendly round face, big expressive eyes, small horns, whiskers, serpentine body coiled playfully, minimal details, soft gradients, white background, front view, character design, vector art style, kawaii, approachable
```

### Imagen 3 Prompt - Sprite Sheet

```
Character sprite sheet of cute Chinese dragon mascot, flat 2D cartoon style like Duolingo, 6 different expressions and poses: 1-neutral idle, 2-happy excited jump, 3-surprised wide eyes, 4-waving friendly, 5-thinking pondering, 6-celebrating arms up, same character in all poses, consistent design, simple shapes, white background, evenly spaced, animation reference sheet
```

### Genre-Specific Adaptations

```
[Base dragon description], [GENRE] theme adaptation, [COLOR_PALETTE], [VISUAL_EFFECTS], [ART_STYLE_MODIFIER], white background

FANTASY: warm red and gold palette, ethereal glow, golden accents, sparkles
CYBERPUNK: deep purple and neon cyan, holographic effect, glowing circuits, scan lines
HORROR: desaturated blue-gray, shadowy translucent, pale ghostly, pixel texture
HEROES: electric blue and cyan, comic book lines, speed blur, dynamic energy
```

---

## Appendix B: Reference Material

**Character Inspiration**

- Duolingo's Duo (emotional engagement, timing)
- Clippy (what NOT to do - avoid intrusion)
- Crash Bandicoot (expressive personality)
- Spyro (iconic dragon character design)

**Technical References**

- Lottie: https://airbnb.io/lottie/
- Rive: https://rive.app/
- Imagen 3: https://deepmind.google/technologies/imagen-3/

**Design Principles**

- "Calm Technology" by Amber Case (non-intrusive design)
- Material Design Motion Guidelines
- iOS Human Interface Guidelines - Animation

---

*Document Version: 1.0*  
*Last Updated: January 2026*  
*Author: Product Specification for AI Agent*