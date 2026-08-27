package com.ilustris.sagai.ui.genre.recap.collage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.compiledColorPalette
import com.ilustris.sagai.ui.genre.collage.AssemblingPiece
import com.ilustris.sagai.ui.genre.collage.PAPER_INK
import com.ilustris.sagai.ui.genre.collage.TornPaperScrap
import com.ilustris.sagai.ui.genre.collage.readableTextColor
import com.ilustris.sagai.ui.genre.recap.RecapCard
import com.ilustris.sagai.ui.theme.LocalSagaGenre

/**
 * The recap as a page torn out and glued down: the whole card is one ragged scrap, with each stat
 * pasted on as its own smaller scrap in the genre's accent — number loud, label small under it.
 *
 * Shows every stat at once rather than rotating. A collage is an assembled artefact; the pieces are
 * all stuck down at the same time, and a number that swapped itself every two seconds would undo
 * the one thing the medium is saying.
 *
 * The [AssemblingPiece] wrapper is what keeps the scrap alive — it slaps down on first composition
 * and then tremors very slightly, the same stop-motion idle every other Collage surface wears.
 */
@Composable
fun CollageRecapCard(
    card: RecapCard,
    modifier: Modifier = Modifier,
) {
    val genre = LocalSagaGenre.current ?: Genre.PUNK_ROCK
    val accent = genre.compiledColorPalette().firstOrNull() ?: MaterialTheme.colorScheme.primary

    AssemblingPiece(
        modifier = modifier,
        rotation = -1.4f,
        delayMs = 120L,
        canAnimate = true,
        seed = card.title.hashCode(),
        scaleFrom = 1f,
    ) {
        TornPaperScrap(
            seed = card.title.hashCode(),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 18.dp),
        ) {
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                Text(
                    text = card.title.uppercase(),
                    color = PAPER_INK,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (card.isReady) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        card.stats.forEachIndexed { index, stat ->
                            StatScrap(
                                value = stat.value,
                                label = stat.label,
                                accent = accent,
                                // Alternating lean, so a row of three reads as three separate
                                // things stuck down rather than one strip cut into cells.
                                rotation = if (index % 2 == 0) -2.5f else 2f,
                                seed = 300 + index,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    Text(
                        text = "${card.callToAction.uppercase()} →",
                        color = PAPER_INK,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                } else {
                    card.progress?.let {
                        Text(
                            text = it.message,
                            color = PAPER_INK.copy(alpha = .75f),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }
    }
}

/** One stat pasted on as its own scrap — the number is the point, the label is the caption. */
@Composable
private fun StatScrap(
    value: String,
    label: String,
    accent: androidx.compose.ui.graphics.Color,
    rotation: Float,
    seed: Int,
    modifier: Modifier = Modifier,
) {
    val ink = accent.readableTextColor()

    AssemblingPiece(
        modifier = modifier,
        rotation = rotation,
        delayMs = 320L + seed,
        canAnimate = true,
        seed = seed,
        scaleFrom = 1f,
    ) {
        TornPaperScrap(
            seed = seed,
            paperColor = accent,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = value,
                    color = ink,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                )
                Text(
                    text = label,
                    color = ink.copy(alpha = .85f),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
