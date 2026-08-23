package com.ilustris.sagai.features.saga.detail.review.ui.templates.book

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.compiledColorPalette
import com.ilustris.sagai.features.saga.detail.data.model.ReviewText
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.genre.book.BookBackground
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import kotlin.time.Duration.Companion.milliseconds

/**
 * A single storybook page: [isEpigraph] renders it as a centered italic quote
 * (used for a stage's "hook" text), otherwise as a titled body paragraph
 * (used for the stage's main content).
 */
class BookTextPage(
    override val content: SagaContent,
    private val text: ReviewText,
    override val pageType: ReviewPageType,
    private val isEpigraph: Boolean = false,
) : ReviewPage {
    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val accent =
            content.data.genre
                .compiledColorPalette()
                .firstOrNull() ?: MaterialTheme.colorScheme.primary
        val ink = LocalContentColor.current

        if (isEpigraph) {
            Box(modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    text.title?.let {
                        SimpleTypewriterText(
                            text = "“$it”",
                            style =
                                MaterialTheme.typography
                                    .titleLarge
                                    .copy(color = MaterialTheme.colorScheme.primary),
                            textAlign = TextAlign.Center,
                        )
                    }
                    text.subtitle?.let {
                        SimpleTypewriterText(
                            text = it,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    }
                }
            }
        } else {
            Column(
                modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                text.title?.let {
                    SimpleTypewriterText(
                        text = it.uppercase(),
                        style =
                            MaterialTheme.typography.titleLarge.copy(
                                color = MaterialTheme.colorScheme.primary,
                            ),
                    )
                    HorizontalDivider(color = accent.copy(alpha = 0.4f))
                }
                text.subtitle?.let {
                    SimpleTypewriterText(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge.copy(color = ink),
                        duration = (it.length * 16).coerceIn(800, 4000).milliseconds,
                        isAnimated = canAnimate,
                    )
                }
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        BookBackground(modifier)
    }
}
