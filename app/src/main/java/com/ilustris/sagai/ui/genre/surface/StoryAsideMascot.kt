package com.ilustris.sagai.ui.genre.surface

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ilustris.sagai.ui.theme.components.mascot.BlobMascot
import com.ilustris.sagai.ui.theme.components.mascot.rememberMascotExpression
import com.ilustris.sagai.ui.theme.components.mascot.rememberTiltLook

/**
 * The mascot wearing the tone an aside is commenting on — the aside's speaker, in effect.
 *
 * Draws nothing when the beat carries no tone or Remote Config has no expression for it, the same
 * contract everywhere the blob appears. That also means it takes no room: callers can put it in a
 * row without a size-shaped hole when it is absent.
 *
 * Every style decides where it goes, how big it is and, if it wants one, what colour; this
 * only decides what it is — the tone's own colour unless told otherwise. It follows
 * the tilt of the phone and squashes when tapped, because a milestone is a screen of its own and a
 * tap has nothing else to do there.
 */
@Composable
internal fun AsideMascot(
    aside: StoryAside,
    modifier: Modifier = Modifier,
    color: Color? = null,
    canAnimate: Boolean = true,
    entranceDelayMillis: Long = 0L,
) {
    val tone = aside.tone ?: return
    val tilt = rememberTiltLook(enabled = canAnimate)
    BlobMascot(
        expression = rememberMascotExpression(tone),
        color = color ?: tone.color,
        look = { tilt.value },
        animate = canAnimate,
        pokeable = true,
        entranceDelayMillis = entranceDelayMillis,
        modifier = modifier,
    )
}
