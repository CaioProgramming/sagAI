package com.ilustris.sagai.ui.theme.components.mascot

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.core.services.model.MascotExpression
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.ui.theme.levitate
import kotlin.time.Duration.Companion.seconds

/**
 * Reads the blob eye spec for [tone] from Remote Config. Null when the tone is not configured,
 * which is the signal not to draw the mascot at all.
 */
@Composable
fun rememberMascotExpression(tone: EmotionalTone): MascotExpression? {
    val viewModel: MascotViewModel = hiltViewModel()
    val expressions by viewModel.expressions.collectAsStateWithLifecycle()
    return expressions[tone]
}

/**
 * The mascot as the app shows it: the blob wearing [emotionalTone], floating when [animate].
 *
 * The blob is the only way the app renders an emotional state — the per-genre emote images this
 * used to fall back to are gone, along with the Remote Config tables that fed them.
 */
@Composable
fun MascotEmotionFace(
    emotionalTone: EmotionalTone,
    modifier: Modifier,
    animate: Boolean = true,
    expression: MascotExpression? = rememberMascotExpression(emotionalTone),
    color: Color = emotionalTone.color,
    eyeColor: Color = MaterialTheme.colorScheme.background,
    look: () -> Offset? = { null },
) {
    BlobMascot(
        expression = expression,
        color = color,
        eyeColor = eyeColor,
        look = look,
        animate = animate,
        modifier =
            modifier.then(
                if (animate) Modifier.levitate(duration = 5.seconds, yOffset = 10f) else Modifier,
            ),
    )
}
