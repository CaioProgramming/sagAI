package com.ilustris.sagai.features.newsaga.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.features.newsaga.data.model.UniverseEcho
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.themePainter
import kotlinx.coroutines.delay

private const val CHIP_STAGGER_MS = 60L
private const val CHIP_ROWS = 2

/**
 * The idea suggestions as two rows of chips that scroll sideways. Each chip is its own genre's
 * theme, but only as a light: dark glass with the genre's color caught by its edges.
 * Tapping one sends its pitch, like typing it. Chips arrive one after the other.
 */
@Composable
internal fun IdeaChipsSection(
    echoes: List<UniverseEcho>,
    onEchoSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // The grid's item scope has no weight (that lives in RowScope), so the fraction of the screen
    // comes from the measured width instead. Long titles wrap inside the chip and the next chip
    // peeks in from the edge, which tells the player the row scrolls.
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val chipWidth = maxWidth * IDEA_CHIP_WIDTH_FRACTION
        LazyHorizontalGrid(
            rows = GridCells.Fixed(CHIP_ROWS),
            modifier = Modifier.fillMaxWidth().height(IDEA_CHIPS_HEIGHT),
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(echoes, key = { index, echo ->
                echo.pitch
            }) { index, echo ->
                IdeaChip(
                    echo = echo,
                    index = index,
                    onClick = { onEchoSelected(echo.pitch) },
                    modifier = Modifier.width(chipWidth).fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
private fun IdeaChip(
    echo: UniverseEcho,
    index: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * CHIP_STAGGER_MS)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter =
            fadeIn(tween(450)) +
                scaleIn(tween(450), initialScale = 0.9f) +
                slideInVertically(tween(450)) { it / 4 },
        modifier = modifier,
    ) {
        SagAITheme(genre = echo.genre) {
            // The shared dark glass, a notch softer than the action buttons.
            Row(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .glassSurface(RoundedCornerShape(14.dp), MaterialTheme.colorScheme.primary) { 0.75f }
                        .clickable(onClick = onClick)
                        .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    painter = themePainter(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = echo.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private val IDEA_CHIPS_HEIGHT = 120.dp
private const val IDEA_CHIP_WIDTH_FRACTION = 0.3f
