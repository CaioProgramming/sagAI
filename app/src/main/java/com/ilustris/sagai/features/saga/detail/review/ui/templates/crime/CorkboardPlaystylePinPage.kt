package com.ilustris.sagai.features.saga.detail.review.ui.templates.crime

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.compiledColorPalette
import com.ilustris.sagai.features.playthrough.AnimatedPlaytimeCounter
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.genre.crime.CorkPin
import com.ilustris.sagai.ui.genre.crime.CorkboardBackground
import com.ilustris.sagai.ui.theme.components.HandwrittenText
import kotlin.time.Duration.Companion.seconds

/**
 * The board's playtime pin, sized down from a full stat page to a card — reuses
 * [AnimatedPlaytimeCounter], the same stat
 * [com.ilustris.sagai.features.saga.detail.review.ui.templates.book.BookPlaystylePage] shows.
 */
class CorkboardPlaystylePinPage(
    override val content: SagaContent,
    private val caption: String?,
) : ReviewPage, CorkboardPinPage {
    override val pageType: ReviewPageType = ReviewPageType.PLAYSTYLE

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val genre = content.data.genre
        val accent = genre.compiledColorPalette().firstOrNull() ?: MaterialTheme.colorScheme.primary

        CorkPin(
            modifier = modifier.padding(16.dp),
            seed = content.data.id + PLAYSTYLE_SEED_OFFSET,
            pinColor = accent,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(170.dp),
            ) {
                AnimatedPlaytimeCounter(
                    playtimeMs = content.data.playTimeMs,
                    label = stringResource(R.string.playtime_title),
                    animationDuration = 1.2.seconds,
                    isAnimated = canAnimate,
                    textStyle = MaterialTheme.typography.titleLarge,
                    labelStyle =
                        MaterialTheme.typography.labelSmall.copy(
                            fontStyle = FontStyle.Italic,
                            color = accent,
                        ),
                )
                caption?.let {
                    HandwrittenText(
                        text = it,
                        fontSize = 12.sp,
                        centered = true,
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
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
