package com.ilustris.sagai.ui.genre.crime

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.random.Random

/**
 * Chrome for one item pinned to
 * [com.ilustris.sagai.features.saga.detail.review.ui.templates.crime.CorkboardBoard]: a slightly
 * rotated paper card with a pushpin dot in the genre's accent color, like a photo or index card
 * tacked to a detective's board. [seed] gives each pin a small, stable rotation — derived once
 * rather than re-randomized on every recomposition — so the board doesn't visibly jitter as the
 * camera flies past.
 */
@Composable
fun CorkPin(
    modifier: Modifier = Modifier,
    seed: Int = 0,
    pinColor: Color = MaterialTheme.colorScheme.primary,
    paperColor: Color = Color(0xFFFFFDF6),
    content: @Composable () -> Unit,
) {
    val rotation = remember(seed) { (Random(seed).nextFloat() - 0.5f) * 9f }

    Box(
        modifier
            .rotate(rotation)
            .shadow(6.dp, RoundedCornerShape(3.dp), clip = false)
            .background(paperColor, RoundedCornerShape(3.dp))
            .border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(3.dp))
            .padding(10.dp),
    ) {
        content()

        Box(
            Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-14).dp)
                .size(12.dp)
                .shadow(2.dp, CircleShape)
                .background(pinColor, CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
        )
    }
}
