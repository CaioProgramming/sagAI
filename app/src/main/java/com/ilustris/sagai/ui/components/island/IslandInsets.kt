package com.ilustris.sagai.ui.components.island

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Holds the current on-screen extent of the top and bottom islands so content can reserve space
 * for them — the island analogue of `WindowInsets.ime`. Updated by the island overlays as their
 * compact pills measure/appear/disappear.
 */
@Stable
class IslandInsets {
    var top: Dp by mutableStateOf(0.dp)
    var bottom: Dp by mutableStateOf(0.dp)
}

val LocalIslandInsets = staticCompositionLocalOf { IslandInsets() }

/**
 * Applies padding equal to the currently-visible island(s), and only while visible — exactly like
 * [androidx.compose.foundation.layout.imePadding] does for the keyboard. When no island is
 * showing, the padding animates back to zero.
 *
 * Use on scrollable/content surfaces that would otherwise sit behind the floating pill(s).
 */
fun Modifier.islandPadding(
    top: Boolean = true,
    bottom: Boolean = true,
): Modifier =
    composed {
        val insets = LocalIslandInsets.current
        val animatedTop by animateDpAsState(
            targetValue = if (top) insets.top else 0.dp,
            label = "islandPaddingTop",
        )
        val animatedBottom by animateDpAsState(
            targetValue = if (bottom) insets.bottom else 0.dp,
            label = "islandPaddingBottom",
        )
        padding(top = animatedTop, bottom = animatedBottom)
    }
