package com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType

/**
 * `scan --emotion` — the same dominant-emotion reading [com.ilustris.sagai.features.saga.detail.review.ui.ReviewExpressivenessPage]
 * shows via a drawn vibe shape, rendered here as a terminal stat line instead.
 */
class TerminalEmotionScanPage(
    override val content: SagaContent,
) : ReviewPage {
    override val pageType: ReviewPageType = ReviewPageType.EXPRESSIVENESS

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val accent = MaterialTheme.colorScheme.primary
        val normal = MaterialTheme.colorScheme.onBackground
        val dominantTone =
            remember {
                content
                    .flatEvents()
                    .map { it.emotionalRanking() }
                    .firstOrNull()
                    ?.firstOrNull()
                    ?.first
            }

        Box(modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
            Column(
                Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "${content.terminalHost()}:~$ scan --emotion",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    style = MaterialTheme.typography.bodyLarge.neonGlow(accent),
                )

                Text(
                    text = "> scan complete",
                    fontFamily = FontFamily.Monospace,
                    color = normal.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium,
                )

                dominantTone?.let {
                    Text(
                        text = "dominant_signal: ${it.getTitle().uppercase()}",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                        style = MaterialTheme.typography.headlineSmall.neonGlow(accent, blurRadius = 18f),
                    )
                }
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        TerminalBackground(modifier)
    }
}
