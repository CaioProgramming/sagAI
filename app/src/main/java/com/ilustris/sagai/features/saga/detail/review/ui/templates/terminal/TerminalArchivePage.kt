package com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal

import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.data.model.ReviewText
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewImageSource
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType

/**
 * The journey stage: the run of chapter art gathered into one archive, with the stage's own words
 * printing underneath it.
 *
 * This used to be two screens — a text page for the recap and a separate plate for the art — which
 * split a single beat in half and left each side thin. The gathering *is* the recap; the prose is
 * what it means. Stacking them in one column lets the pile assemble while the terminal reports on
 * it, which is the relationship they always had.
 */
class TerminalArchivePage(
    override val content: SagaContent,
    private val images: List<ReviewImageSource>,
    private val journey: ReviewText?,
) : ReviewPage {
    override val pageType: ReviewPageType = ReviewPageType.JOURNEY

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val accent = MaterialTheme.colorScheme.primary
        val normal = MaterialTheme.colorScheme.onBackground

        val lines =
            buildList {
                add(
                    terminalPromptLine(
                        host = content.terminalHost(),
                        command = journey?.title ?: "archive --collect ${images.size}",
                        accent = accent,
                    ),
                )
                journey?.subtitle?.let {
                    add(
                        TerminalLine(
                            text = it,
                            style =
                                MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = normal,
                                ),
                            alpha = .75f,
                        ),
                    )
                }
            }

        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                GatheringPlates(
                    items = images,
                    canAnimate = canAnimate,
                    seed = images.size,
                ) { image, _ ->
                    Box(Modifier.fillMaxSize().plateFrame(accent)) {
                        AsyncImage(
                            model = image.url,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }

                TerminalTypewriter(
                    lines = lines,
                    canAnimate = canAnimate,
                    caretColor = accent,
                )
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            TerminalBackground(Modifier.fillMaxSize())
        }
    }
}
