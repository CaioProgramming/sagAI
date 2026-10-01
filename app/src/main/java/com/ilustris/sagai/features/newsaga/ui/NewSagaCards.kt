package com.ilustris.sagai.features.newsaga.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.characters.data.model.Gender
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.themePainter
import com.ilustris.sagai.ui.theme.themeShimmer
import androidx.compose.animation.core.RepeatMode
import kotlin.math.abs

private val CardShape = RoundedCornerShape(28.dp)
private const val SHEEN_ALPHA = 0.26f

/**
 * A soft band of light that crosses the card as it moves through the pager, so the card reads as
 * glossy under the genre's own color. [offset] is the page's distance from the center (-1..1).
 */
private fun Modifier.sheen(
    offset: Float,
    tint: Color,
): Modifier =
    drawWithContent {
        drawContent()
        val shift = (-offset * 0.9f - 0.1f) * size.width
        drawRect(
            brush =
                Brush.linearGradient(
                    colorStops =
                        arrayOf(
                            0.30f to Color.Transparent,
                            0.40f to Color.White.copy(alpha = 0.06f),
                            0.47f to Color.White.copy(alpha = 0.30f),
                            0.51f to tint,
                            0.57f to Color.White.copy(alpha = 0.10f),
                            0.66f to Color.Transparent,
                        ),
                    start = Offset(shift, 0f),
                    end = Offset(shift + size.width, size.height * 0.6f),
                ),
            alpha = SHEEN_ALPHA,
            blendMode = BlendMode.Screen,
        )
    }

/** The genre's primary color at the top, fading into the background: the card's surface. */
@Composable
internal fun Modifier.cardSurface(
    sheenOffset: Float,
    glow: Float = 0.55f,
): Modifier {
    val primary = MaterialTheme.colorScheme.primary
    return this
        .clip(CardShape)
        .border(1.dp, primary.gradientFade(), CardShape)
        .background(MaterialTheme.colorScheme.background)
        .background(Brush.verticalGradient(listOf(primary.copy(alpha = glow), Color.Transparent)))
        .sheen(sheenOffset, primary)
}

/**
 * A story proposal: the genre's icon over its own glow, then title and pitch. Must be composed in
 * the draft's own [SagAITheme] so colors and fonts follow its genre, whatever screen it sits on.
 */
@Composable
internal fun StoryCard(
    draft: SagaDraft,
    modifier: Modifier = Modifier,
    sheenOffset: Float = 0f,
    compact: Boolean = false,
) {
    val primary = MaterialTheme.colorScheme.primary
    Column(
        modifier = modifier.cardSurface(sheenOffset).padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(if (compact) 120.dp else 190.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(if (compact) 110.dp else 170.dp)
                    .background(
                        Brush.radialGradient(listOf(primary.copy(alpha = 0.45f), Color.Transparent)),
                    ),
            )
            Icon(
                painter = themePainter(),
                contentDescription = null,
                tint = primary,
                modifier = Modifier.size(if (compact) 52.dp else 76.dp),
            )
        }
        Text(
            text = draft.title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = draft.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
            maxLines = if (compact) 4 else 8,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 24.dp),
        )
    }
}

/**
 * A protagonist candidate as a locked silhouette that fades out under the name, the way a character
 * select screen teases someone you haven't unlocked. The fade is an alpha mask on the silhouette
 * itself, never a colored overlay, so it never bands against whatever the card sits on.
 */
@Composable
internal fun HeroCard(
    character: CharacterInfo,
    modifier: Modifier = Modifier,
    sheenOffset: Float = 0f,
) {
    Box(modifier = modifier.cardSurface(sheenOffset, glow = 0.45f)) {
        Image(
            painter = painterResource(character.gender.silhouette()),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            alignment = Alignment.BottomCenter,
            colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.82f)),
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(top = 16.dp)
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            brush =
                                Brush.verticalGradient(
                                    0.30f to Color.Black,
                                    0.62f to Color.Black.copy(alpha = 0.55f),
                                    0.98f to Color.Transparent,
                                ),
                            blendMode = BlendMode.DstIn,
                        )
                    },
        )
        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
        ) {
            Text(
                text = character.name,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = character.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                maxLines = 6,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

private fun Gender.silhouette(): Int =
    when (this) {
        Gender.MALE -> R.drawable.ic_silhouette_male
        Gender.FEMALE -> R.drawable.ic_silhouette_female
        Gender.Other -> R.drawable.ic_silhouette_neutral
    }

/** The last page of the stories pager: reaching it asks for stories of genres not offered yet. */
@Composable
internal fun MoreStoriesCard(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = CardShape
    Column(
        modifier =
            modifier
                .clip(shape)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Icon(
            painter = themePainter(),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier =
                Modifier
                    .size(32.dp)
                    .reactiveShimmer(
                        repeatMode = RepeatMode.Restart,
                        shimmerColors = themeShimmer(),
                        isPlaying = isLoading,
                    ),
        )
        Text(
            text =
                stringResource(
                    if (isLoading) R.string.new_saga_more_genres else R.string.new_saga_more_genres_hint,
                ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        )
    }
}

/**
 * Cards you swipe through, neighbours peeking from both sides. Each page is composed in its own
 * genre's [SagAITheme], so what you see keeps its colors and fonts even while another genre is in
 * focus; the cards tilt, shrink and dim as they leave the center, and the whole pager rises in
 * when it first appears.
 */
@Composable
internal fun <T> GenreCardPager(
    items: List<T>,
    genreOf: (T) -> Genre,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    trailingPage: (@Composable () -> Unit)? = null,
    card: @Composable (item: T, sheenOffset: Float) -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 8 },
        modifier = modifier,
    ) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 45.dp, vertical = 8.dp),
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            val offset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val distance = abs(offset).coerceIn(0f, 1f)
                        val scale = 1f - 0.08f * distance
                        scaleX = scale
                        scaleY = scale
                        alpha = 1f - 0.5f * distance
                        rotationY = offset * 16f
                        cameraDistance = 12f * density
                    },
            ) {
                val item = items.getOrNull(page)
                if (item != null) {
                    SagAITheme(genre = genreOf(item)) {
                        card(item, offset.coerceIn(-1f, 1f))
                    }
                } else {
                    trailingPage?.invoke()
                }
            }
        }
    }
}
