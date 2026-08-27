package com.ilustris.sagai.ui.genre.comic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * A single comic frame: hard border and a flat ground, the gutter around it coming from whatever is
 * laying the frames out. Clips its content — pages still on the default (full-screen) layout are
 * taller than a panel's allotted height, and without clipping they bleed past the border into the
 * row below instead of just running out of room.
 */
@Composable
fun ComicPanel(
    modifier: Modifier = Modifier,
    borderColor: Color = Color.Black,
    background: Color = Color.White,
    framed: Boolean = true,
    shape: Shape = RectangleShape,
    content: @Composable () -> Unit,
) {
    // Unframed panels claim their space in the layout but draw nothing of their own — and notably
    // don't clip, so a page made only of balloons can let them spill wherever they land.
    if (!framed) {
        Box(modifier) { content() }
        return
    }

    Box(
        modifier
            .clip(shape)
            .background(background, shape)
            .border(3.dp, borderColor, shape),
    ) {
        content()
    }
}
