package com.ilustris.sagai.features.saga.detail.review.ui.templates.book

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewImageSource
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.theme.components.HandwrittenText
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.fadedGradientTopAndBottom
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.morphingGradient
import com.ilustris.sagai.ui.theme.sagaBrush
import kotlin.time.Duration.Companion.seconds

/**
 * The book's front cover — the saga's own art, full screen height like a masthead, title
 * lettered over the same bottom-fade-to-background scrim used by the app's other hero images
 * (see [com.ilustris.sagai.features.saga.detail.ui.SagaDetailView],
 * [com.ilustris.sagai.features.characters.ui.CharacterDetailsView]) rather than a flat black
 * gradient.
 */
class BookCoverPage(
    override val content: SagaContent,
    private val cover: ReviewImageSource,
) : ReviewPage {
    override val pageType: ReviewPageType = ReviewPageType.INTRO

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        var showImage by remember { mutableStateOf(!canAnimate) }

        Box(modifier.fillMaxWidth().height(screenHeight)) {
            AnimatedVisibility(
                visible = showImage,
                enter = fadeIn(tween(1200)),
            ) {
                AsyncImage(
                    model = cover.url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .background(fadedGradientTopAndBottom(MaterialTheme.colorScheme.surfaceContainer)),
            )

            HandwrittenText(
                text = cover.caption.uppercase(),
                color = MaterialTheme.colorScheme.onBackground,
                shadow =
                    Shadow(
                        MaterialTheme.colorScheme.primary,
                        blurRadius = 10f,
                    ),
                duration = 2.seconds,
                fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                isItalic = false,
                isBold = false,
                centered = true,
                isAnimated = canAnimate,
                onAnimationFinished = { showImage = true },
                modifier =
                    Modifier
                        .gradientFill(Brush.horizontalGradient(morphingGradient()))
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(32.dp),
            )
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        BookBackground(modifier)
    }
}
