package com.ilustris.sagai.ui.genre

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.ui.genre.collage.readableTextColor
import com.ilustris.sagai.ui.theme.darker
import com.ilustris.sagai.ui.theme.lighter

private val CAP_HEIGHT = 52.dp
private val DEPTH = 6.dp
private val BORDER_WIDTH = 2.dp
private val BORDER_COLOR = Color.Black.copy(alpha = .85f)

/**
 * A chunky, physical-feeling button: a coloured cap sitting on a darker base, the base's own lip
 * showing beneath it as the cap's shadow — and pressing it slides the cap down onto that base until
 * the lip disappears, the way an arcade or board-game button sinks into its housing.
 *
 * Built for [com.ilustris.sagai.ui.genre.GenreSurfaceStyle.COMIC] and [com.ilustris.sagai.features.newsaga.data.model.Genre.HORROR]
 * — two genres whose identity is already physical/tactile (ink and panel borders; a scene taped
 * off) rather than printed or typed, where a flat Material button reads as the wrong material
 * entirely. [accent] is the only genre knowledge this component needs, so any caller can reach for
 * it without this file knowing which genre asked.
 */
@Composable
fun PhysicalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
    shape: Shape = RoundedCornerShape(14.dp),
    enabled: Boolean = true,
    busy: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val capOffset by animateDpAsState(
        targetValue = if (pressed) DEPTH else 0.dp,
        // Snappy down, springy back up — the button falls faster than it rises, same asymmetry a
        // real spring-loaded button has.
        animationSpec = if (pressed) tween(60) else spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "physicalButtonPress",
    )

    val baseColor = accent.darker(.4f)
    val capColor = accent
    val highlightColor = accent.lighter(.35f)
    val contentColor = accent.readableTextColor()

    Box(
        modifier
            .height(CAP_HEIGHT + DEPTH)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !busy,
                onClick = onClick,
            ),
    ) {
        // The base never moves. Its own top edge is the "lip" the cap's shadow shows as, and the
        // surface the cap comes to rest on once pressed.
        Box(
            Modifier
                .fillMaxWidth()
                .height(CAP_HEIGHT)
                .align(Alignment.BottomCenter)
                .clip(shape)
                .background(baseColor)
                .border(BORDER_WIDTH, BORDER_COLOR, shape),
        )

        Box(
            Modifier
                .fillMaxWidth()
                .height(CAP_HEIGHT)
                .offset(y = capOffset)
                .clip(shape)
                .background(capColor)
                .border(BORDER_WIDTH, BORDER_COLOR, shape),
            contentAlignment = Alignment.Center,
        ) {
            // A thin lighter band under the top edge — the single highlight a beveled cap needs to
            // read as lit from above, without building a full gradient.
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(CAP_HEIGHT * .3f)
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(highlightColor.copy(alpha = .35f)),
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (busy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = contentColor,
                    )
                } else {
                    Text(
                        text = text.uppercase(),
                        color = contentColor,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
