package com.ilustris.sagai.ui.genre.recap.plain

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.ui.components.bubble
import com.ilustris.sagai.ui.genre.DynamicCard
import com.ilustris.sagai.ui.genre.recap.RecapCard
import com.ilustris.sagai.ui.genre.recap.rememberRecapHeadline
import com.ilustris.sagai.ui.theme.components.chat.BubbleTailAlignment
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.reactiveShimmer

/**
 * The card as it has always looked: the genre's bubble shape filled with a vertical gradient of its
 * own palette, sweeping strokes behind a title and one rotating stat.
 *
 * Still the treatment for Book, Crime and Comic — none of those has earned a bespoke card yet, and
 * this one is genre-tinted rather than genre-blind, so falling through to it reads as "not styled
 * *yet*" rather than as a mistake.
 */
@Composable
fun PlainRecapCard(
    card: RecapCard,
    modifier: Modifier = Modifier,
    genre: Genre? = null,
) {
    val shape =
        genre.bubble(
            tailAlignment = BubbleTailAlignment.BottomRight,
            tailWidth = 0.dp,
            tailHeight = 0.dp,
        )

    DynamicCard(
        title = card.title,
        subtitle = rememberRecapHeadline(card),
        titleStyle = MaterialTheme.typography.headlineSmall.copy(color = MaterialTheme.colorScheme.onPrimary),
        subtitleStyle = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onPrimary),
        lineColor = MaterialTheme.colorScheme.onPrimary,
        modifier =
            modifier
                .background(
                    Brush.verticalGradient(MaterialTheme.colorScheme.primary.darkerPalette(factor = .3f)),
                    shape,
                ).clip(shape)
                .then(if (card.isReady) Modifier else Modifier.reactiveShimmer(true)),
    )
}
