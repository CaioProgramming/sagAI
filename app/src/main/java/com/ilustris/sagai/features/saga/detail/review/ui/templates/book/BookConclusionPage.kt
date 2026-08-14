package com.ilustris.sagai.features.saga.detail.review.ui.templates.book

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.newsaga.data.model.compiledColorPalette
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.features.saga.detail.review.ui.SagaLegendLayout
import com.ilustris.sagai.features.share.domain.model.ShareType
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

/**
 * "The Legacy" — the same cast mosaic Default shows, sepia-framed instead of GTA black-bordered.
 * Stacked linearly (title, then mosaic, then caption, then share) rather than overlaid on top of
 * the mosaic — the continuous-scroll article format reads top-to-bottom, and text laid over a
 * busy 9-photo grid stopped being legible once the page was no longer a single full-screen frame.
 */
class BookConclusionPage(
    override val content: SagaContent,
) : ReviewPage {
    override val pageType: ReviewPageType = ReviewPageType.CONCLUSION

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val mainCharacter = content.mainCharacter ?: return
        val supportingCharacters = content.getCharacters(filterMainCharacter = true)
        val accent = content.data.genre.compiledColorPalette().firstOrNull() ?: MaterialTheme.colorScheme.primary
        val ink = LocalContentColor.current
        val surfaceContainer = MaterialTheme.colorScheme.surfaceContainer
        var showShareLink by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(2.seconds)
            showShareLink = true
        }

        val conclusion = content.data.review?.conclusion?.content

        Column(
            modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            conclusion?.title?.let {
                Text(
                    text = it.uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineSmall,
                )
            }

            SagaLegendLayout(
                mainCharacter = mainCharacter,
                supportingCharacters = supportingCharacters,
                sagaIcon = content.data.icon,
                cellBorderColor = surfaceContainer,
                cellShape = RoundedCornerShape(2.dp),
                modifier = Modifier.fillMaxWidth().aspectRatio(0.75f),
            )

            conclusion?.subtitle?.let {
                Text(
                    text = it,
                    fontStyle = FontStyle.Italic,
                    color = ink,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            AnimatedVisibility(showShareLink) {
                BookShareLink(ShareType.RELATIONS, accent, onAction)
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        BookBackground(modifier)
    }
}
