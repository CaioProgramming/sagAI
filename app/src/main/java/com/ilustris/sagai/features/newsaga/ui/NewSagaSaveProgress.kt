package com.ilustris.sagai.features.newsaga.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.data.model.Gender
import com.ilustris.sagai.features.newsaga.ui.presentation.NewSagaUiState
import com.ilustris.sagai.features.newsaga.ui.presentation.SaveStage
import com.ilustris.sagai.ui.theme.levitate
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.themePainter
import com.ilustris.sagai.ui.theme.themeShimmer
import kotlin.time.Duration.Companion.milliseconds

private val CardWidth = 250.dp
private val CardHeight = 296.dp
private const val FAN_OFFSET_DP = 66f
private const val PEEK_DP = 40f

/**
 * The save, shown as the two cards of the review levitating in simplified form: names only. The one
 * being worked on comes to the front; each lights up as its artwork arrives — the protagonist's
 * portrait, then the saga's cover. When both are done they fan out under a glow, and the screen
 * holds there until the chat opens.
 */
@Composable
internal fun NewSagaSaveProgress(
    uiState: NewSagaUiState,
    modifier: Modifier = Modifier,
) {
    val saga = uiState.lockedSaga ?: return
    val stage = uiState.saveStage
    val primary = MaterialTheme.colorScheme.primary
    val done = stage == SaveStage.Done
    val heroLit = stage == SaveStage.Saga || done
    val storyLit = done
    val heroFront = stage == SaveStage.Character

    // 0 = stacked behind, 1 = in front; the fan only exists once everything is done.
    val storyFront by animateFloatAsState(if (heroFront) 0f else 1f, tween(800), label = "save_story_front")
    val heroFrontProgress by animateFloatAsState(if (heroFront) 1f else 0f, tween(800), label = "save_hero_front")
    val fan by animateFloatAsState(if (done) 1f else 0f, tween(900), label = "save_fan")
    val glow by animateFloatAsState(if (done) 1f else 0f, tween(1200), label = "save_glow")

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.width(CardWidth + 40.dp).height(CardHeight + PEEK_DP.dp + 24.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                Modifier
                    .size(360.dp)
                    .align(Alignment.Center)
                    .alpha(glow * 0.55f)
                    .background(Brush.radialGradient(listOf(primary, Color.Transparent))),
            )
            StackedCard(front = heroFrontProgress, fan = fan, fanDirection = -1f, glowing = done, duration = 3200) {
                SaveHeroCard(
                    name = uiState.savedCharacter?.name?.takeIf { it.isNotBlank() } ?: uiState.lockedCharacter?.name.orEmpty(),
                    gender = uiState.lockedCharacter?.gender ?: Gender.Other,
                    portrait = uiState.savedCharacter?.image.orEmpty(),
                    lit = heroLit,
                )
            }
            StackedCard(front = storyFront, fan = fan, fanDirection = 1f, glowing = done, duration = 4000) {
                SaveStoryCard(
                    title = uiState.savedSaga?.title?.takeIf { it.isNotBlank() } ?: saga.title,
                    cover = uiState.savedSaga?.icon.orEmpty(),
                    lit = storyLit,
                )
            }
        }

        NewSagaReasoning(
            message =
                stringResource(
                    when (stage) {
                        SaveStage.Contract -> R.string.new_saga_saving_contract
                        SaveStage.Character -> R.string.new_saga_saving_character
                        SaveStage.Saga -> R.string.new_saga_saving_saga
                        SaveStage.Done -> R.string.new_saga_saving_done
                    },
                ),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .reactiveShimmer(!done, themeShimmer(), repeatMode = RepeatMode.Restart),
        )
    }
}

/** Places a card in the stack: in front or peeking from behind, then spread to its side in the fan. */
@Composable
private fun StackedCard(
    front: Float,
    fan: Float,
    fanDirection: Float,
    glowing: Boolean,
    duration: Int,
    content: @Composable () -> Unit,
) {
    val primary = MaterialTheme.colorScheme.primary
    val glowAlpha by animateFloatAsState(if (glowing) 0.9f else 0f, tween(1200), label = "save_card_glow")
    Box(
        modifier =
            Modifier
                .zIndex(front)
                .padding(top = PEEK_DP.dp)
                .width(CardWidth)
                .height(CardHeight)
                .graphicsLayer {
                    val behind = 1f - front
                    val scale = 1f - 0.08f * behind * (1f - fan) - 0.12f * fan
                    scaleX = scale
                    scaleY = scale
                    translationY = (-PEEK_DP * behind * (1f - fan) + 10f * fan) * density
                    translationX = FAN_OFFSET_DP * fan * fanDirection * density
                    rotationZ = 6f * fan * fanDirection
                    alpha = 1f - 0.3f * behind * (1f - fan)
                    shadowElevation = 0f
                }.drawBehind {
                    if (glowAlpha > 0f) {
                        drawRoundRect(
                            color = primary.copy(alpha = glowAlpha * 0.35f),
                            cornerRadius = CornerRadius(28.dp.toPx()),
                            size = size,
                        )
                    }
                }.levitate(duration = duration.milliseconds, yOffset = 12f),
    ) {
        content()
    }
}

/** The finished artwork as the card's whole background, fading out toward the bottom under the name. */
@Composable
private fun FadedArtwork(
    model: String,
    alpha: Float,
) {
    AsyncImage(
        model = model,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        alignment = Alignment.TopCenter,
        modifier =
            Modifier
                .fillMaxSize()
                .alpha(alpha)
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(0.55f to Color.Black, 1f to Color.Transparent),
                        blendMode = BlendMode.DstIn,
                    )
                },
    )
}

@Composable
private fun SaveStoryCard(
    title: String,
    cover: String,
    lit: Boolean,
) {
    val primary = MaterialTheme.colorScheme.primary
    val litProgress by animateFloatAsState(if (lit) 1f else 0f, tween(900), label = "save_story_lit")
    val hasCover = lit && cover.isNotBlank()
    Box(modifier = Modifier.fillMaxSize().cardSurface(sheenOffset = 0f)) {
        if (hasCover) {
            FadedArtwork(model = cover, alpha = litProgress)
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 20.dp).padding(bottom = 30.dp),
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    painter = themePainter(),
                    contentDescription = null,
                    tint = primary.copy(alpha = 0.4f + 0.6f * litProgress),
                    modifier = Modifier.padding(top = 64.dp).size(72.dp),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth().padding(top = 28.dp),
                )
            }
        }
    }
}

@Composable
private fun SaveHeroCard(
    name: String,
    gender: Gender,
    portrait: String,
    lit: Boolean,
) {
    val primary = MaterialTheme.colorScheme.primary
    val litProgress by animateFloatAsState(if (lit) 1f else 0f, tween(900), label = "save_hero_lit")
    Box(modifier = Modifier.fillMaxSize().cardSurface(sheenOffset = 0f, glow = 0.45f)) {
        if (lit && portrait.isNotBlank()) {
            FadedArtwork(model = portrait, alpha = litProgress)
        } else {
            Image(
                painter =
                    painterResource(
                        when (gender) {
                            Gender.MALE -> R.drawable.ic_silhouette_male
                            Gender.FEMALE -> R.drawable.ic_silhouette_female
                            Gender.Other -> R.drawable.ic_silhouette_neutral
                        },
                    ),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                alignment = Alignment.BottomCenter,
                colorFilter =
                    ColorFilter.tint(
                        androidx.compose.ui.graphics.lerp(Color.Black.copy(alpha = 0.82f), primary, litProgress),
                    ),
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(top = 16.dp)
                        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                        .drawWithContent {
                            drawContent()
                            drawRect(
                                brush = Brush.verticalGradient(0.35f to Color.Black, 0.98f to Color.Transparent),
                                blendMode = BlendMode.DstIn,
                            )
                        },
            )
        }
        Text(
            text = name,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 20.dp).padding(bottom = 30.dp),
        )
    }
}
