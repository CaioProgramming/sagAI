package com.ilustris.sagai.ui.components.views

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage

@Composable
fun DepthLayout(
    imagePath: String,
    modifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier,
    backgroundImageModifier: Modifier = imageModifier,
    foregroundImageModifier: Modifier = imageModifier,
    viewModel: DepthLayoutViewModel = hiltViewModel(),
    onLoadError: () -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    val originalBitmap by viewModel.originalBitmap.collectAsStateWithLifecycle()
    val segmentedBitmap by viewModel.segmentedBitmap.collectAsStateWithLifecycle()

    LaunchedEffect(imagePath) {
        viewModel.processImage(imagePath)
    }

    AnimatedContent(
        targetState = segmentedBitmap != null && originalBitmap != null,
        transitionSpec = {
            fadeIn(tween(1000, easing = EaseIn)) togetherWith
                fadeOut(tween(500, easing = FastOutSlowInEasing))
        },
        label = "DepthLayoutTransition",
        modifier = modifier,
    ) { isSegmented ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (isSegmented && originalBitmap != null && segmentedBitmap != null) {
                Image(
                    bitmap = originalBitmap!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = backgroundImageModifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )

                content()

                Image(
                    bitmap = segmentedBitmap!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = foregroundImageModifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                AsyncImage(
                    model = imagePath,
                    contentDescription = null,
                    onState = { state ->
                        if (state is coil3.compose.AsyncImagePainter.State.Error) {
                            onLoadError()
                        }
                    },
                    modifier = backgroundImageModifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )

                content()
            }
        }
    }
}

@Composable
fun DepthLayout(
    originalImage: Bitmap,
    segmentedImage: Bitmap,
    modifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier,
    backgroundImageModifier: Modifier = imageModifier,
    foregroundImageModifier: Modifier = imageModifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier) {
        Image(
            bitmap = originalImage.asImageBitmap(),
            contentDescription = null,
            modifier = backgroundImageModifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        content()

        Image(
            bitmap = segmentedImage.asImageBitmap(),
            contentDescription = null,
            modifier = foregroundImageModifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    }
}
