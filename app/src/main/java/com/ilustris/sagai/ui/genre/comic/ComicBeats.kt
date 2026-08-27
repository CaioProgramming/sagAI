package com.ilustris.sagai.ui.genre.comic

import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

/** At most this many caption boxes for one body — past three the frame is flooded. */
private const val MAX_NARRATION_BEATS = 3

/**
 * Breaks prose on sentence boundaries and regroups it into at most [maxBeats] roughly even chunks,
 * so a long paragraph becomes a few caption boxes instead of one per sentence (which would flood
 * the frame) or one box holding everything (which is the slab we're avoiding).
 */
fun splitIntoBeats(
    text: String,
    maxBeats: Int,
): List<String> {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return emptyList()

    val sentences =
        Regex("(?<=[.!?…])\\s+")
            .split(trimmed)
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    if (sentences.size <= 1) return listOf(trimmed)

    val beats = minOf(maxBeats, sentences.size)
    val perBeat = (sentences.size + beats - 1) / beats
    return sentences.chunked(perBeat).map { it.joinToString(" ") }
}

/**
 * Where a text-only comic beat hangs its boxes: a tag at the top and the prose scattered down the
 * frame, alternating side to side and overhanging the edges.
 *
 * This is a list of [ComicBalloonSpec] rather than a composable because the comic language has two
 * very different renderers and only the *geometry* is shared between them. The story review's board
 * places these by absolute coordinates in its own measure policy, so that balloons break out of the
 * panel borders and the camera has something to fly between; a single full-screen beat renders the
 * same specs with a plain `Modifier.align` inside one `Box`. Sharing a composable instead would
 * have forced the board to give up the break-out, which is the whole reason it places balloons
 * itself.
 *
 * The boxes have no way to measure each other, so dividing the frame's height between them up front
 * is what keeps a long beat from landing on top of the next one.
 */
fun comicNarrationBalloons(
    title: String?,
    body: String?,
    maxBeats: Int = MAX_NARRATION_BEATS,
): List<ComicBalloonSpec> =
    buildList {
        title?.let {
            add(
                ComicBalloonSpec(
                    alignment = Alignment.TopStart,
                    widthFraction = 0.86f,
                    offset = DpOffset((-10).dp, 10.dp),
                ) { ComicFadeIn { ComicTag(text = it) } },
            )
        }

        val beats = splitIntoBeats(body.orEmpty(), maxBeats)
        beats.forEachIndexed { index, beat ->
            val verticalBias = -0.35f + 1.25f * ((index + 0.5f) / beats.size)
            val leaning = if (index % 2 == 0) 1 else -1
            add(
                ComicBalloonSpec(
                    alignment = BiasAlignment(0.28f * leaning, verticalBias),
                    widthFraction = 0.82f,
                    offset = DpOffset((14 * leaning).dp, 0.dp),
                ) {
                    ComicFadeIn(delayMillis = 250 + index * 400) {
                        ComicCaptionBox(text = beat, align = TextAlign.Start)
                    }
                },
            )
        }
    }
