package com.ilustris.sagai.ui.theme

import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInBack
import androidx.compose.animation.core.EaseInBounce
import androidx.compose.animation.core.EaseInElastic
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseInOutBack
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.EaseOutBounce
import androidx.compose.animation.core.EaseOutElastic
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone

/**
 * Maps an [EmotionalTone] to a Compose [Easing] curve.
 *
 * Heuristic:
 * - Brutal/intense emotions use elastic/back/bounce-in easings (harsher feel).
 * - Neutral/focused use linear or accelerate curves.
 * - Soft/positive emotions use out/inOut and bounce-out/elastic-out (playful/pleasant).
 */
fun EmotionalTone.toEasing(): Easing =
    when (this) {
        // Brutal / not-happy emotions
        EmotionalTone.ANGRY -> EaseInElastic

        EmotionalTone.FRUSTRATED -> EaseInBack

        EmotionalTone.SAD -> EaseInOutBack

        EmotionalTone.CYNICAL -> EaseIn

        // Negative but softer emotions
        EmotionalTone.ANXIOUS -> EaseInBounce

        EmotionalTone.CONCERNED -> EaseOutBounce

        EmotionalTone.MELANCHOLIC -> EaseInOut

        // Neutral / focused
        EmotionalTone.NEUTRAL -> LinearEasing

        EmotionalTone.DETERMINED -> FastOutLinearInEasing

        // Positive / exploratory and warm
        EmotionalTone.CURIOUS -> EaseOutElastic

        EmotionalTone.HOPEFUL -> EaseOut

        EmotionalTone.CALM -> EaseInOut

        EmotionalTone.EMPATHETIC -> LinearOutSlowInEasing

        // Most joyful gets playful bounce-out
        EmotionalTone.JOYFUL -> EaseOutBounce
    }

/** Convenience function if you prefer a top-level call rather than the extension. */
fun easingForTone(tone: EmotionalTone): Easing = tone.toEasing()
