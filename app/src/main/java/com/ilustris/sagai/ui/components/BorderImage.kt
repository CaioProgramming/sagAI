package com.ilustris.sagai.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.ui.theme.rememberVectorShape

/**
 * Draws [painter] with a flat, hard-edged outline behind it — a handful of tinted duplicates
 * offset a few px in each direction, then the real tinted image on top. Works with any image
 * (vector or raster, any SVG-derived `VectorDrawable`, PNG, ...) since it's just compositing
 * plain `Image` calls — no shape/outline extraction from the source asset needed, so it works
 * even on multi-path or oddly-shaped icons where tracing a real outline would be impractical.
 *
 * Deliberately NOT built on `dropShadow`/any other `RenderEffect`-based blur modifier — those
 * recompute their blur on every recomposition, which crashed the app when used inside a
 * frequently-recomposed chat bubble (typing retriggers recomposition on every keystroke; see the
 * chat-bubble-decorations project notes, 2026-07-29). This trades a soft blur for a crisp flat
 * edge, but the tradeoff is it's unconditionally safe to use anywhere, including composables that
 * recompose often — that's the whole reason this exists as a shared component instead of each
 * call site reinventing its own (possibly unsafe) version.
 *
 * [borderWidth] is the offset distance for each duplicate, not a stroke-path width — small values
 * (1–2dp) read as a clean outline; larger values start looking like a drop shadow without blur.
 */
@Composable
fun BorderImage(
    painter: Painter,
    tint: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
    borderWidth: Dp = 1.4.dp,
    contentDescription: String? = null,
) {
    Box(modifier) {
        listOf(
            -borderWidth to 0.dp,
            borderWidth to 0.dp,
            0.dp to -borderWidth,
            0.dp to borderWidth,
        ).forEach { (dx, dy) ->
            Image(
                painter,
                contentDescription,
                colorFilter = ColorFilter.tint(borderColor),
                modifier =
                    Modifier
                        .matchParentSize()
                        .offset(dx, dy),
            )
        }
        Image(
            painter,
            contentDescription,
            colorFilter = ColorFilter.tint(tint),
            modifier = Modifier.matchParentSize(),
        )
    }
}

/** Convenience overload taking a drawable resource id directly instead of a resolved [Painter]. */
@Composable
fun BorderImage(
    @DrawableRes id: Int,
    tint: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
    borderWidth: Dp = 1.4.dp,
    contentDescription: String? = null,
) {
    val painterShape = rememberVectorShape(id)
    Image(
        painter = painterResource(id),
        contentDescription = contentDescription,
        colorFilter = ColorFilter.tint(tint),
        modifier = modifier.border(borderWidth, borderColor, painterShape),
    )
}
