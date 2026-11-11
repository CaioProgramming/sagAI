package com.ilustris.sagai.features.imagegeneration.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.features.imagegeneration.model.ImageTarget
import com.ilustris.sagai.features.imagegeneration.model.ImageTargetState
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.components.SparkIcon
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shimmerize
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

private const val PhraseIntervalMs = 2_800L
private const val RevealMs = 1_000
private const val RevealBlurDp = 28f

/** Collects [target]'s state where it is read, so only the leaf that renders it recomposes. */
@Composable
fun rememberImageTargetState(target: ImageTarget): State<ImageTargetState> {
    val viewModel = hiltViewModel<ImageTargetStateViewModel>()
    val flow = remember(viewModel, target) { viewModel.stateOf(target) }
    return flow.collectAsStateWithLifecycle(initialValue = ImageTargetState.Idle)
}

/**
 * Fills the frame an image will occupy while that image does not exist yet: a prominent call to
 * action when idle or failed, and an in-place animated placeholder while it is on its way. Hosts
 * show it only when the image is missing, and fade it out as the image arrives.
 */
@Composable
fun ImageGenerationSlot(
    target: ImageTarget,
    genre: Genre,
    onGenerate: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    contentAlignment: Alignment = Alignment.Center,
) {
    val state by rememberImageTargetState(target)

    Box(
        modifier = modifier.clip(shape),
        contentAlignment = contentAlignment,
    ) {
        Crossfade(targetState = state.isWorking(), animationSpec = tween(400), label = "imageSlot") { working ->
            if (working) {
                GenerationPlaceholder(genre = genre, state = state, contentAlignment = contentAlignment)
            } else {
                GenerateImageCta(
                    genre = genre,
                    failed = state is ImageTargetState.Failed,
                    onClick = onGenerate,
                )
            }
        }
    }
}

private fun ImageTargetState.isWorking() = isActive || this is ImageTargetState.Revealing

@Composable
private fun GenerateImageCta(
    genre: Genre,
    failed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val brush = genre.gradient()
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (failed) {
            Text(
                text = stringResource(R.string.image_generation_failed),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        TextButton(onClick = onClick) {
            SparkIcon(
                brush = brush,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text =
                    stringResource(
                        if (failed) R.string.try_again else R.string.image_generation_cta,
                    ),
                style = MaterialTheme.typography.labelLarge.copy(brush = brush, fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun GenerationPlaceholder(
    genre: Genre,
    state: ImageTargetState,
    contentAlignment: Alignment,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(R.string.image_generation_default_label)
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = .3f))
                // Over the whole frame, icon and phrase included, so the sweep reads as one loading surface.
                .reactiveShimmer(
                    isPlaying = true,
                    shimmerColors = genre.color.shimmerize(),
                    duration = 2.seconds,
                    targetValue = 1200f,
                    repeatMode = RepeatMode.Restart,
                ).semantics { contentDescription = label },
        contentAlignment = contentAlignment,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp),
        ) {
            Icon(
                painter = painterResource(genre.icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(56.dp),
            )
            if (state is ImageTargetState.Queued) {
                Text(
                    text = stringResource(R.string.image_generation_reasoning_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            } else {
                RotatingPhrase(color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun RotatingPhrase(color: Color) {
    val phrases = stringArrayResource(R.array.image_generation_phrases)
    var index by remember { mutableIntStateOf(0) }
    LaunchedEffect(phrases) {
        while (true) {
            delay(PhraseIntervalMs)
            index = (index + 1) % phrases.size
        }
    }
    AnimatedContent(
        targetState = phrases[index],
        transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(500)) },
        label = "generationPhrase",
    ) { phrase ->
        Text(
            text = phrase,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Lets an image that was just generated arrive instead of popping in: it starts blurred and
 * translucent and settles sharp. Only plays while [target] is Revealing, so an image that was
 * already there when the screen opened is untouched.
 */
@Composable
fun Modifier.imageReveal(target: ImageTarget): Modifier {
    val state by rememberImageTargetState(target)
    val revealing = state is ImageTargetState.Revealing
    val progress = remember { Animatable(if (revealing) 0f else 1f) }
    LaunchedEffect(revealing) {
        if (revealing) {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(RevealMs))
        }
    }
    // Read inside the layer block: the animation runs in the draw phase and never recomposes the host.
    return this.graphicsLayer {
        val settled = progress.value
        alpha = .25f + .75f * settled
        val radius = (1f - settled) * RevealBlurDp.dp.toPx()
        renderEffect = if (radius > .5f) BlurEffect(radius, radius) else null
    }
}
