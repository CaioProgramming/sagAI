package com.ilustris.sagai.ui.genre.crime

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ilustris.sagai.ui.theme.filters.HeatHazeBackground

/**
 * The board's surface behind every pinned photo.
 *
 * A [HeatHazeBackground] rather than a morphing gradient: a gradient that only cross-fades its
 * colours has stops that never move, and behind a table that itself drifts sideways the stillness
 * is what gave it away. Warping the field instead means the light behind the photos bends and folds
 * on its own, at a pace slow enough that you notice it only if you stop and look.
 *
 * Kept deliberately slow and low-contrast — it is the room the photos are lit in, not something to
 * read.
 */
@Composable
fun CorkboardBackground(modifier: Modifier = Modifier) {
    HeatHazeBackground(
        modifier = modifier,
        speed = 0.04f,
        scale = 2.2f,
        warp = 1.4f,
        glow = 0.05f,
    )
}
