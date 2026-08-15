package com.ilustris.sagai.features.saga.detail.review.ui.templates.crime

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.compiledColorPalette
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewImageSource
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType

private val ALBUM_STACK_SIZE = 150.dp

/**
 * The chapter stills, sent as a photo-stack attachment — collapsed to a few overlapping, slightly
 * rotated frames like a stack of prints, the way a messaging app previews a multi-photo share.
 * Tapping expands it inline into the full grid, same images
 * [com.ilustris.sagai.features.saga.detail.review.ui.templates.book.BookJourneyPlatePage] lays out
 * on parchment.
 */
class CrimeAlbumMessagePage(
    override val content: SagaContent,
    private val images: List<ReviewImageSource>,
    private val isMe: Boolean,
) : ReviewPage {
    override val pageType: ReviewPageType = ReviewPageType.JOURNEY

    /** No typing to wait for, just its own pop-in. */
    override val estimatedRevealDurationMs: Long = 500L

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val genre = content.data.genre
        val accent = genre.compiledColorPalette().firstOrNull() ?: MaterialTheme.colorScheme.primary
        var expanded by remember { mutableStateOf(false) }
        val rotation by animateFloatAsState(targetValue = if (expanded) 90f else 0f)

        CrimeBubbleFrame(
            isMe = isMe,
            genre = genre,
            useSpeechShape = false,
            canAnimate = canAnimate,
            modifier = modifier,
        ) { ink ->
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    Modifier.clickable { expanded = !expanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(Modifier.size(ALBUM_STACK_SIZE), contentAlignment = Alignment.Center) {
                        images.take(3).reversed().forEachIndexed { index, image ->
                            val stackRotation = (images.take(3).size - 1 - index) * 8f - 8f
                            AsyncImage(
                                model = image.url,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier =
                                    Modifier
                                        .size(ALBUM_STACK_SIZE * 0.8f)
                                        .rotate(stackRotation)
                                        .clip(RoundedCornerShape(12.dp)),
                            )
                        }
                    }

                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.review_stage_journey_title),
                            color = ink,
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Text(
                            text = "${images.size}",
                            fontStyle = FontStyle.Italic,
                            color = accent,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }

                    Icon(
                        painter = painterResource(R.drawable.round_arrow_forward_ios_24),
                        contentDescription = null,
                        tint = ink.copy(alpha = 0.6f),
                        modifier = Modifier.size(10.dp).rotate(rotation),
                    )
                }

                AnimatedVisibility(visible = expanded) {
                    Column(
                        Modifier.width(240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        images.chunked(2).forEach { rowImages ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                rowImages.forEach { image ->
                                    Column(Modifier.weight(1f)) {
                                        AsyncImage(
                                            model = image.url,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .aspectRatio(0.85f)
                                                    .clip(RoundedCornerShape(10.dp)),
                                        )
                                        Text(
                                            text = image.caption,
                                            fontStyle = FontStyle.Italic,
                                            color = ink.copy(alpha = 0.7f),
                                            textAlign = TextAlign.Start,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(top = 2.dp),
                                        )
                                    }
                                }
                                if (rowImages.size == 1) {
                                    Column(Modifier.weight(1f)) {}
                                }
                            }
                        }
                    }
                }

                Text(
                    text = stringResource(if (expanded) R.string.review_tap_to_collapse else R.string.review_tap_to_expand),
                    color = ink.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        CrimeBackground(modifier)
    }
}
