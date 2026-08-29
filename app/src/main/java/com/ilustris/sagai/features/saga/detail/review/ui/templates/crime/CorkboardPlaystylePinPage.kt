package com.ilustris.sagai.features.saga.detail.review.ui.templates.crime

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.compiledColorPalette
import com.ilustris.sagai.features.playthrough.AnimatedPlaytimeCounter
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.genre.crime.CorkPin
import com.ilustris.sagai.ui.genre.crime.PinBackNote
import com.ilustris.sagai.ui.genre.crime.PinCaption
import com.ilustris.sagai.ui.genre.crime.PinProse
import com.ilustris.sagai.ui.genre.crime.PinSignature
import com.ilustris.sagai.ui.genre.crime.PinTitle
import com.ilustris.sagai.ui.genre.crime.CorkboardBackground
import kotlin.time.Duration.Companion.seconds

/** The playstyle caption sits under a counter, so it gets a few lines rather than a page. */
private const val CAPTION_MAX_LINES = 5

/**
 * The table's playtime card — reuses [AnimatedPlaytimeCounter], the same stat
 * [com.ilustris.sagai.features.saga.detail.review.ui.templates.book.BookPlaystylePage] shows.
 *
 * The counter's own styles are given explicit colors rather than inherited ones: it is being
 * printed on the pin's paper, not on the app's surface, and its defaults resolve against the
 * theme's background instead. See [CorkPin].
 */
class CorkboardPlaystylePinPage(
    override val content: SagaContent,
    private val caption: String?,
) : ReviewPage, CorkboardPinPage {
    override val pageType: ReviewPageType = ReviewPageType.PLAYSTYLE
    override val pinSize: CorkPinSize = CorkPinSize.NOTE

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val genre = content.data.genre
        val accent = genre.compiledColorPalette().firstOrNull() ?: MaterialTheme.colorScheme.primary

        CorkPin(
            modifier = modifier,
            seed = content.data.id + PLAYSTYLE_SEED_OFFSET,
            pinColor = accent,
        ) { ink ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                AnimatedPlaytimeCounter(
                    playtimeMs = content.data.playTimeMs,
                    label = stringResource(R.string.playtime_title),
                    animationDuration = 1.2.seconds,
                    isAnimated = canAnimate,
                    textStyle = MaterialTheme.typography.titleLarge.copy(color = ink),
                    labelStyle =
                        MaterialTheme.typography.labelSmall.copy(
                            fontStyle = FontStyle.Italic,
                            color = accent,
                        ),
                )
                caption?.let {
                    PinProse(
                        text = it,
                        ink = ink,
                        centered = true,
                        maxLines = CAPTION_MAX_LINES,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        CorkboardBackground(modifier)
    }

    private companion object {
        const val PLAYSTYLE_SEED_OFFSET = 17
    }
}
