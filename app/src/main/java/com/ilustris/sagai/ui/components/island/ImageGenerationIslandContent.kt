package com.ilustris.sagai.ui.components.island

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.debug.DebugImageFallbackService
import com.ilustris.sagai.core.ai.model.ImageType
import com.ilustris.sagai.features.debug.ui.ManualImageFallbackContent
import com.ilustris.sagai.features.imagegeneration.model.ImageGenerationUiState
import com.ilustris.sagai.features.imagegeneration.model.ImageGenerationUiState.AwaitingManualFallback
import com.ilustris.sagai.features.imagegeneration.model.ImageGenerationUiState.Generating
import com.ilustris.sagai.features.imagegeneration.model.ImageGenerationUiState.Reveal

/**
 * [IslandContent] for image generation — the richest of the top-island sources.
 *
 * - `Generating`: reasoning streams straight into the compact pill; expanded is just cancel.
 * - `AwaitingManualFallback`: (debug only) expanded shows the manual prompt input.
 * - `Reveal`: expanded shows the finished image with a "Continue" dismiss.
 *
 * Auto-expansion (Reveal / manual fallback) and reveal-dismiss-on-collapse are driven by the
 * host (MainActivity), which owns the island's expanded state.
 */
class ImageGenerationIslandContent(
    private val state: ImageGenerationUiState,
    private val debugImageFallbackService: DebugImageFallbackService,
    private val onCancel: () -> Unit,
    private val onDismissReveal: () -> Unit,
) : IslandContent {
    override val compact: CompactIslandData =
        when (state) {
            is Reveal -> {
                CompactIslandData(
                    label = state.label,
                    labelRes = revealTitleRes(state.imageType),
                    iconRes = R.drawable.ic_spark,
                )
            }

            is Generating -> {
                CompactIslandData(
                    label = state.reasoning,
                    labelRes = R.string.image_generation_default_label,
                    iconRes = R.drawable.ic_spark,
                    isLoading = true,
                )
            }

            is AwaitingManualFallback -> {
                CompactIslandData(
                    labelRes = R.string.image_generation_awaiting_manual,
                    iconRes = R.drawable.ic_spark,
                    isLoading = true,
                )
            }

            ImageGenerationUiState.Idle -> {
                CompactIslandData(iconRes = R.drawable.ic_spark)
            }
        }

    @Composable
    override fun Expanded(scope: IslandScope) {
        when (state) {
            is Reveal -> {
                RevealBody(state = state, onDismiss = onDismissReveal)
            }

            is Generating -> {
                GeneratingBody(onCancel = onCancel)
            }

            is AwaitingManualFallback -> {
                if (BuildConfig.DEBUG) {
                    ManualImageFallbackContent(
                        prompt = state.prompt,
                        debugImageFallbackService = debugImageFallbackService,
                        onSubmitted = { scope.onCollapse() },
                        onCancel = onCancel,
                        scrollEnabled = false,
                        autoCopyPrompt = true,
                        showHeader = false,
                        compact = true,
                    )
                }
            }

            ImageGenerationUiState.Idle -> {
                Unit
            }
        }
    }
}

private fun revealTitleRes(imageType: ImageType): Int =
    when (imageType) {
        ImageType.ICON -> R.string.image_generation_reveal_icon
        ImageType.COVER -> R.string.image_generation_reveal_cover
    }

@Composable
private fun RevealBody(
    state: Reveal,
    onDismiss: () -> Unit,
) {
    val title = state.label ?: stringResource(revealTitleRes(state.imageType))

    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(20.dp)),
        ) {
            Image(
                bitmap = state.bitmap.asImageBitmap(),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        TextButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(stringResource(R.string.image_generation_reveal_dismiss))
        }
    }
}

/** The reasoning is already streamed in the compact pill above (marquee-scrolling if long) —
 * this expanded body only needs to offer the one thing that isn't available compact: cancel. */
@Composable
private fun GeneratingBody(onCancel: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        TextButton(onClick = onCancel) {
            Text(
                text = stringResource(R.string.cancel),
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
