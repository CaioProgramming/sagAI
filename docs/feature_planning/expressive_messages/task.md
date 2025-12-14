# Expressive Message Effects Feature Plan

**Status**: 🚧 In Progress (Phase 4 Complete - Testing Pending)  
**Date Created**: 2025-11-24  
**Date Started**: 2025-11-28  
**Estimated Effort**: 8-12 hours  
**Priority**: Medium (Enhancement)

---

## Overview

Add iMessage-style expressive animations to chat bubbles based on `EmotionalTone`. Each emotional
tone will have a unique entrance animation that subtly communicates emotional context through
motion, scale, and visual effects—without explicitly labeling the emotion.

### Design Philosophy

- **Subtle, Not Explicit**: Animations enhance reading experience without being distracting
- **Genre-Aware**: Effects complement existing genre-specific bubble shapes
- **Performance-Conscious**: Smooth animations that don't impact scrolling
- **User-Controlled**: Toggle in Settings to enable/disable effects

---

## Animation Mapping

| Emotional Tone              | Effect Name       | Duration | Characteristics                         |
|-----------------------------|-------------------|----------|-----------------------------------------|
| JOYFUL, HOPEFUL, EMPATHETIC | **Gentle Bounce** | 600ms    | Soft bounce with slight rotation wobble |
| ANGRY, FRUSTRATED           | **Impact**        | 400ms    | Sharp scale-in with screen shake        |
| SAD, MELANCHOLIC            | **Drift Down**    | 800ms    | Slow descent with fade-in               |
| ANXIOUS, CONCERNED          | **Tremor**        | 500ms    | Micro-vibrations with flickering alpha  |
| CURIOUS, DETERMINED         | **Pop In**        | 500ms    | Elastic overshoot with rotation         |
| CYNICAL                     | **Slide Fade**    | 700ms    | Horizontal slide with sarcastic tilt    |
| CALM, NEUTRAL               | **Smooth Fade**   | 400ms    | Simple fade with minimal scale          |

---

## Implementation Checklist

### Phase 1: Core Animation System ✅ (3-4 hours) - COMPLETED

- [x] Create `EmotionalToneAnimations.kt` in
  `app/src/main/java/com/ilustris/sagai/features/saga/chat/ui/animations/`
- [x] Implement `Modifier.emotionalEntrance()` extension function (accepts nullable
  `EmotionalTone?`)
- [x] Implement private animation functions:
    - [x] `gentleBounce()` - JOYFUL, HOPEFUL, EMPATHETIC
    - [x] `impactEffect()` - ANGRY, FRUSTRATED
    - [x] `driftDown()` - SAD, MELANCHOLIC
    - [x] `tremorEffect()` - ANXIOUS, CONCERNED
    - [x] `popIn()` - CURIOUS, DETERMINED
    - [x] `slideFade()` - CYNICAL
    - [x] `smoothFade()` - CALM, NEUTRAL
- [x] Test each animation in isolation with preview composables (
  `EmotionalToneAnimationsPreview.kt`)

### Phase 2: Data Layer ✅ (Already Complete!)

- [x] ~~Update `Message.kt` data class~~ - **Already has `emotionalTone: EmotionalTone?` field**
- [x] ~~Emotional tone extraction~~ - **Already implemented in `MessageUseCaseImpl.saveMessage()`**
- [ ] Verify emotional tone is being extracted correctly for user messages
- [ ] Test that AI-generated messages preserve their emotional tone

### Phase 3: Settings Integration ✅ (1-2 hours) - COMPLETED

- [x] Add `MESSAGE_EFFECTS_ENABLED_KEY` constant to `SettingsUseCase.kt`
- [x] Add `getMessageEffectsEnabled(): Flow<Boolean>` to `SettingsUseCase` interface
- [x] Add `setMessageEffectsEnabled(enabled: Boolean)` to `SettingsUseCase` interface
- [x] Implement methods in `SettingsUseCaseImpl`
- [x] Add `messageEffectsEnabled` StateFlow to `SettingsViewModel`
- [x] Add `setMessageEffectsEnabled()` method to `SettingsViewModel`
- [x] Add state collection in `SettingsView`
- [x] Add `PreferencesContainer` toggle in `SettingsView` (after Smart Fix preference)
- [x] Add string resources:
    - [x] `message_effects` - "Message Effects"
    - [x] `message_effects_description` - "Expressive animations based on emotional tone"

### Phase 4: ChatBubble Integration ✅ (2-3 hours) - COMPLETED

- [x] Import `emotionalEntrance` extension in `ChatBubble.kt`
- [x] Collect `messageEffectsEnabled` state from ViewModel (via `ChatViewModel`)
- [x] Pass `messageEffectsEnabled` through `ChatView` → `ChatContent` → `ChatList` → `ChatBubble`
- [x] Apply `.emotionalEntrance()` modifier to bubble for `SenderType.USER`
- [x] Apply `.emotionalEntrance()` modifier to bubble for `SenderType.CHARACTER`
- [x] Apply `.emotionalEntrance()` modifier to bubble for `SenderType.THOUGHT`
- [x] Apply `.emotionalEntrance()` modifier to bubble for `SenderType.ACTION`
- [x] Apply `.emotionalEntrance()` modifier to bubble for `SenderType.NARRATOR`
- [x] Apply `.emotionalEntrance()` modifier to bubble for `MessageStatus.LOADING` state
- [x] Animations respect both `isAnimated` and `messageEffectsEnabled` flags
- [x] Removed `alreadyAnimatedMessages` tracking (only last message animates)
- [ ] Test animations only trigger for latest messages in list
- [ ] Verify animations don't interfere with typewriter animation

### Phase 5: Testing & Polish (2-3 hours)

- [ ] **Manual Testing**:
    - [ ] Test each emotional tone animation
    - [ ] Test across all genres (CYBERPUNK, HEROES, SHINOBI, HORROR, FANTASY, SPACE_OPERA)
    - [ ] Test Settings toggle (enable/disable)
    - [ ] Test messages without emotional tone (should default to NEUTRAL)
    - [ ] Test rapid message succession
    - [ ] Test screen rotation during animation
- [ ] **Performance Testing**:
    - [ ] Send 50+ messages rapidly
    - [ ] Verify smooth scrolling
    - [ ] Check memory usage
    - [ ] Test on low-end device (if available)
- [ ] **Edge Cases**:
    - [ ] Messages with null/invalid emotional tone
    - [ ] Animation interruption (user scrolls away)
    - [ ] Multiple messages animating simultaneously
- [ ] Fine-tune animation parameters based on feel
- [ ] Optimize performance if needed

---

## Code Structure

### New Files

####
`app/src/main/java/com/ilustris/sagai/features/saga/chat/ui/animations/EmotionalToneAnimations.kt`

```kotlin
package com.ilustris.sagai.features.saga.chat.ui.animations

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone

/**
 * Main extension function that applies entrance animation based on EmotionalTone
 */
@Composable
fun Modifier.emotionalEntrance(
    emotionalTone: EmotionalTone,
    isAnimated: Boolean
): Modifier {
    if (!isAnimated) return this
    
    return when (emotionalTone) {
        EmotionalTone.JOYFUL, EmotionalTone.HOPEFUL, EmotionalTone.EMPATHETIC -> 
            gentleBounce(onAnimationComplete)
        EmotionalTone.ANGRY, EmotionalTone.FRUSTRATED -> 
            impactEffect(onAnimationComplete)
        EmotionalTone.SAD, EmotionalTone.MELANCHOLIC -> 
            driftDown(onAnimationComplete)
        EmotionalTone.ANXIOUS, EmotionalTone.CONCERNED -> 
            tremorEffect(onAnimationComplete)
        EmotionalTone.CURIOUS, EmotionalTone.DETERMINED -> 
            popIn(onAnimationComplete)
        EmotionalTone.CYNICAL -> 
            slideFade(onAnimationComplete)
        EmotionalTone.CALM, EmotionalTone.NEUTRAL -> 
            smoothFade(onAnimationComplete)
    }
}

// Private animation implementations...
// (See full implementation in artifact: implementation_plan.md)
```

### Modified Files

#### `Message.kt`

```kotlin
@Entity(tableName = "messages")
data class Message(
    // ... existing fields
    val emotionalTone: EmotionalTone? = null, // ✅ Already exists!
)
```

#### `ChatBubble.kt`

```kotlin
// Import
import com.ilustris.sagai.features.saga.chat.ui.animations.emotionalEntrance

// In ChatBubble composable
val messageEffectsEnabled by viewModel.messageEffectsEnabled.collectAsStateWithLifecycle(true)
val emotionalTone = EmotionalTone.getTone(message.emotionalTone)

// Apply to bubble modifier
val bubbleModifier = Modifier
    .wrapContentSize()
    .emotionalEntrance(
        emotionalTone = emotionalTone,
        isAnimated = messageEffectsEnabled && canAnimate
    )
    .background(bubbleStyle.backgroundColor, bubbleShape)
```

#### `SettingsUseCase.kt`

```kotlin
interface SettingsUseCase {
    // ... existing methods
    fun getMessageEffectsEnabled(): Flow<Boolean>
    suspend fun setMessageEffectsEnabled(enabled: Boolean)
}

class SettingsUseCaseImpl {
    companion object {
        const val MESSAGE_EFFECTS_ENABLED_KEY = "message_effects_enabled"
    }
    
    override fun getMessageEffectsEnabled() = 
        dataStorePreferences.getBoolean(MESSAGE_EFFECTS_ENABLED_KEY, true)
    
    override suspend fun setMessageEffectsEnabled(enabled: Boolean) =
        dataStorePreferences.setBoolean(MESSAGE_EFFECTS_ENABLED_KEY, enabled)
}
```

#### `SettingsViewModel.kt`

```kotlin
val messageEffectsEnabled = settingsUseCase.getMessageEffectsEnabled()

fun setMessageEffectsEnabled(enabled: Boolean) {
    viewModelScope.launch {
        settingsUseCase.setMessageEffectsEnabled(enabled)
    }
}
```

#### `SettingsView.kt`

```kotlin
val messageEffectsEnabled by viewModel.messageEffectsEnabled.collectAsStateWithLifecycle(true)

// In preferences Column
PreferencesContainer(
    stringResource(R.string.message_effects),
    stringResource(R.string.message_effects_description),
    isActivated = messageEffectsEnabled,
    onClickSwitch = {
        viewModel.setMessageEffectsEnabled(!it)
    },
)
```

---

## Technical Considerations

### Performance

- ✅ Animations only trigger for latest messages (controlled by `canAnimate`)
- ✅ No animations during scroll
- ✅ User can disable globally via Settings
- ✅ Lightweight `graphicsLayer` transformations
- ✅ No state tracking needed - animations are stateless

### Accessibility

- ✅ User preference toggle in Settings
- ⚠️ Consider adding system reduced motion detection (future enhancement)
- ✅ Animations are subtle and don't interfere with readability

### Genre Integration

- ✅ Effects work with all genre-specific bubble shapes
- ✅ Animations complement existing visual styles
- ✅ No conflicts with current typewriter text animation

---

## Future Enhancements

1. ~~**AI Integration**~~ - ✅ **Already implemented!** Emotional tone extraction happens in
   `MessageUseCaseImpl.saveMessage()` using `EmotionalPrompt.emotionalToneExtraction()`
2. **Haptic Feedback**: Add subtle vibration patterns that match each animation effect
    - Light tap for GENTLE_BOUNCE
    - Strong impact for IMPACT
    - Gentle pulse for DRIFT_DOWN
    - Rapid taps for TREMOR
    - Quick burst for POP_IN
    - Slide pattern for SLIDE_FADE
    - Minimal for SMOOTH_FADE
3. **Sound Effects**: Optional subtle audio feedback paired with animations
    - Must be very subtle and not annoying
    - Consider genre-specific sound palettes
    - Toggle in Settings (separate from visual effects)
    - Challenge: Finding/creating appropriate sounds for all tones
4. **Genre-Specific Variations**: Unique visual effects per genre (e.g., glitch effect for
   CYBERPUNK, magic sparkles for FANTASY)
5. **Particle Systems**: Genre-themed particle effects (stars for SPACE_OPERA, sparks for CYBERPUNK,
   petals for FANTASY)
6. **Reduced Motion Support**: Detect system accessibility settings (
   `Settings.Global.TRANSITION_ANIMATION_SCALE`) and provide minimal animations
7. **Preview Screen**: Showcase all animations in a demo/testing screen within Settings

---

## References

- **Inspiration**: iMessage message effects (Echo, Impact, Gentle, etc.)
- **Related Files**:
    - `EmotionalTone.kt` - Enum defining all emotional tones
    - `ChatBubble.kt` - Main chat bubble composable
    - `SettingsView.kt` - App settings UI
- **Full Implementation Details**: See artifact `implementation_plan.md` for complete code samples

---

## Notes

- This feature is **non-blocking** and should be developed in a separate feature branch
- Animations are **opt-in** via the existing `canAnimate` parameter and Settings toggle
- The system is **extensible** - new emotional tones or effects can be added easily
- **Backward compatible** - messages without emotional tone data will use NEUTRAL effect
- Consider creating a **demo mode** in Settings to preview all animations

---

**When ready to implement**: Create a new feature branch `feature/expressive-message-effects` and
follow the checklist above phase by phase.
