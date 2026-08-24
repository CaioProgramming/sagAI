package com.ilustris.sagai.ui.genre.recap.book

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.features.newsaga.data.model.compiledColorPalette
import com.ilustris.sagai.ui.genre.book.BookBackground
import com.ilustris.sagai.ui.genre.book.CowboyBurnMarks
import com.ilustris.sagai.ui.genre.book.ShinobiInkBlooms
import com.ilustris.sagai.ui.genre.recap.RecapCard
import com.ilustris.sagai.ui.theme.LocalSagaGenre
import com.ilustris.sagai.ui.theme.components.HandwrittenText

/**
 * The recap as a book's colophon — the closing page that states what the volume turned out to be.
 *
 * The counts are set as index entries, label on the left and figure on the right with a dotted
 * leader running between them. That leader is the whole idea: it is a typesetting convention no
 * other medium here would reach for, and it turns three numbers into something that reads as
 * printed rather than as a dashboard.
 *
 * Serves all four Book genres. What keeps them apart is the paper itself — [BookBackground] already
 * gives Shinobi rice-paper fibres where the others get parchment grain — plus the two ambient
 * layers below and the accent drawn from each genre's own palette.
 *
 * Deliberately no `themeVfx`. Cowboy's is a full-surface fireplace that repaints everything it
 * covers through `SrcAtop` at near-full alpha; over a card whose entire job is three legible
 * numbers it turns the text orange and unreadable. Horror's `psychosis` has the same problem more
 * mildly. That effect was written for the chat, and [CowboyBurnMarks]' own doc already recorded the
 * decision to keep it out of these surfaces in favour of the restrained version — this card
 * follows that rather than contradicting it.
 *
 * [ShinobiInkBlooms] and [CowboyBurnMarks] *are* used: unlike the police tape or the punk
 * scribbles, both are localized marks rather than screen-scale furniture, so they scale down to a
 * card without one strip crossing the whole thing corner to corner. Each is a no-op outside its own
 * genre, so both can be called unconditionally.
 */
@Composable
fun BookRecapCard(
    card: RecapCard,
    modifier: Modifier = Modifier,
) {
    val genre = LocalSagaGenre.current
    val accent = genre?.compiledColorPalette()?.firstOrNull() ?: MaterialTheme.colorScheme.primary
    val ink = LocalContentColor.current

    Box(modifier) {
        BookBackground(Modifier.matchParentSize())

        // No-ops outside their own genre, checked internally.
        ShinobiInkBlooms(Modifier.matchParentSize())
        CowboyBurnMarks(Modifier.matchParentSize())

        Column(
            Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            HandwrittenText(
                text = card.title.uppercase(),
                color = accent,
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                isBold = true,
                isItalic = false,
                isAnimated = false,
            )

            HorizontalDivider(color = accent.copy(alpha = .4f))

            if (card.isReady) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    card.stats.forEach { stat ->
                        IndexEntry(label = stat.label, figure = stat.value, ink = ink)
                    }
                }

                Text(
                    text = "${card.callToAction} →",
                    style =
                        MaterialTheme.typography.labelMedium.copy(
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.SemiBold,
                            color = accent,
                        ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                card.progress?.let {
                    Text(
                        text = it.message,
                        style =
                            MaterialTheme.typography.labelMedium.copy(
                                fontStyle = FontStyle.Italic,
                                color = ink.copy(alpha = .7f),
                            ),
                    )
                }
            }
        }
    }
}

/** `Personagens . . . . . . 11` — the line an index prints, leader and all. */
@Composable
private fun IndexEntry(
    label: String,
    figure: String,
    ink: Color,
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelMedium.copy(color = ink.copy(alpha = .8f)),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        DottedLeader(
            color = ink.copy(alpha = .35f),
            modifier = Modifier.weight(1f).padding(horizontal = 6.dp),
        )
        Text(
            text = figure,
            style =
                MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = ink,
                ),
        )
    }
}

@Composable
private fun DottedLeader(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier.height(6.dp)) {
        val radius = 0.9.dp.toPx()
        val step = 5.dp.toPx()
        var x = 0f
        while (x <= size.width) {
            drawCircle(color = color, radius = radius, center = Offset(x, size.height / 2f))
            x += step
        }
    }
}
