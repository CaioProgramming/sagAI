package com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.data.model.ReviewText
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType

/** `cat summary.log` — a command menu that can jump back to any stage. */
class TerminalSummaryPage(
    override val content: SagaContent,
    private val conclusion: ReviewText? = null,
    override val pageType: ReviewPageType = ReviewPageType.SUMMARY,
) : ReviewPage {
    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val accent = MaterialTheme.colorScheme.primary
        val review = content.data.review ?: return

        val entries =
            remember(review) {
                listOfNotNull(
                    ReviewPageType.EXPRESSIVENESS.takeIf { review.expressiveness != null },
                    ReviewPageType.PLAYSTYLE.takeIf { review.playstyle != null },
                    ReviewPageType.CHARACTERS.takeIf { review.topCharacters != null },
                    ReviewPageType.JOURNEY.takeIf { review.actsInsight != null },
                    ReviewPageType.CONCLUSION.takeIf { review.conclusion != null },
                )
            }

        // Terminal selection instead of a ripple — see TerminalSelectionIndication for why the
        // Material affordance is the one thing that breaks the fiction here.
        val selection = remember(accent) { terminalSelection(accent) }

        Box(modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
            Column(
                Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // The send-off's own words open the log rather than occupying a screen before it.
                conclusion?.let { text ->
                    TerminalTypewriter(
                        lines =
                            buildList {
                                text.title?.let {
                                    add(
                                        TerminalLine(
                                            text = "> $it",
                                            style =
                                                MaterialTheme.typography.titleMedium.copy(
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold,
                                                    color = accent,
                                                ).neonGlow(accent),
                                        ),
                                    )
                                }
                                text.subtitle?.let {
                                    add(
                                        TerminalLine(
                                            text = it,
                                            style =
                                                MaterialTheme.typography.bodyMedium.copy(
                                                    fontFamily = FontFamily.Monospace,
                                                ),
                                            alpha = .75f,
                                        ),
                                    )
                                }
                            },
                        canAnimate = canAnimate,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                }

                TerminalCommandLine(
                    host = content.terminalHost(),
                    command = "cat ${stringResource(R.string.review_summary_title).lowercase()}.log",
                    accent = accent,
                    style = MaterialTheme.typography.bodyLarge,
                )

                entries.forEach { stage ->
                    Text(
                        text = "> ${stage.name.lowercase()}",
                        fontFamily = FontFamily.Monospace,
                        color = accent.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier =
                            Modifier
                                .terminalClickable(selection) { onAction(ReviewAction.Navigate(stage)) }
                                .padding(vertical = 4.dp),
                    )
                }

                Text(
                    text = "\$ ${stringResource(R.string.review_restart_button).lowercase()}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier =
                        Modifier
                            .terminalClickable(selection) { onAction(ReviewAction.Restart) }
                            .padding(vertical = 4.dp),
                )

                if (BuildConfig.DEBUG) {
                    Text(
                        text = "\$ ${stringResource(R.string.review_regenerate_button).lowercase()}",
                        fontFamily = FontFamily.Monospace,
                        color = accent.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier =
                            Modifier
                                .terminalClickable(selection) { onAction(ReviewAction.Regenerate) }
                                .padding(vertical = 4.dp),
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
