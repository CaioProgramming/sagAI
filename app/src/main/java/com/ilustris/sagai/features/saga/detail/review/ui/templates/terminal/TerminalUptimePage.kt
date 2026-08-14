package com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.playthrough.AnimatedPlaytimeCounter
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType

/**
 * `uptime` — the same playtime counter [com.ilustris.sagai.features.saga.detail.review.ui.ReviewPlaystylePage]
 * shows via [AnimatedPlaytimeCounter], restyled in monospace for the terminal template.
 */
class TerminalUptimePage(
    override val content: SagaContent,
) : ReviewPage {
    override val pageType: ReviewPageType = ReviewPageType.PLAYSTYLE

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val accent = MaterialTheme.colorScheme.primary

        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "${content.terminalHost()}:~$ uptime",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    style = MaterialTheme.typography.bodyLarge,
                )

                AnimatedPlaytimeCounter(
                    playtimeMs = content.data.playTimeMs,
                    label = stringResource(R.string.playtime_title),
                    textStyle =
                        MaterialTheme.typography.displayMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = accent,
                        ),
                    labelStyle =
                        MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            color = accent.copy(alpha = 0.7f),
                        ),
                )
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        TerminalBackground(modifier)
    }
}
