package com.ilustris.sagai.features.share.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.share.domain.model.ShareType
import com.ilustris.sagai.features.share.presentation.SharePlayViewModel
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.theme.SagaTitle
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.shape
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@Composable
fun EmotionShareView(
    saga: SagaContent,
    viewModel: SharePlayViewModel = hiltViewModel(),
) {
    val shareText by viewModel.shareText.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val genre = remember { saga.data.genre }

    val lifecycleOwner = LocalLifecycleOwner.current
    val savedPath by viewModel.savedFilePath.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    val context = LocalContext.current

    DisposableEffect(lifecycleOwner, viewModel) {
        lifecycleOwner.lifecycle.addObserver(viewModel)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(viewModel)
        }
    }

    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(shareText != null && isLoading.not(), enter = scaleIn() + fadeIn(tween(500)), exit = fadeOut()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .clip(genre.shape())
                        .clickable {
                            coroutineScope.launch {
                                delay(2.seconds)
                                graphicsLayer.toImageBitmap().asAndroidBitmap().let { bitmap ->
                                    viewModel.saveBitmap(bitmap, ShareType.EMOTIONS.name)
                                }
                            }
                        }.drawWithContent {
                            graphicsLayer.record {
                                this@drawWithContent.drawContent()
                            }
                            drawLayer(graphicsLayer)
                        }.fillMaxWidth()
                        .padding(24.dp),
            ) {
                Text(
                    shareText?.title ?: emptyString(),
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            fontFamily = genre.headerFont(),
                            brush = genre.gradient(),
                        ),
                    modifier = Modifier.padding(vertical = 16.dp),
                )

                Text(
                    shareText?.text ?: emptyString(),
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = genre.bodyFont(),
                        ),
                )

                Column(Modifier.padding(vertical = 16.dp).alpha(.6f).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        shareText?.caption ?: emptyString(),
                        style =
                            MaterialTheme.typography.bodySmall.copy(
                                fontFamily = genre.bodyFont(),
                                textAlign = TextAlign.Center,
                            ),
                    )
                    SagaTitle(
                        textStyle = MaterialTheme.typography.titleSmall,
                    )
                }
            }
        }

        AnimatedVisibility(isLoading) {
            StarryTextPlaceholder(
                starColor = genre.color,
                starCount = saga.messagesSize(),
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.generateShareText(saga, ShareType.EMOTIONS)
    }

    LaunchedEffect(isLoading) {
        if (isLoading.not() && isSaving.not()) {
            coroutineScope.launch {
                delay(2.seconds)
                graphicsLayer.toImageBitmap().asAndroidBitmap().let { bitmap ->
                    viewModel.saveBitmap(bitmap, ShareType.EMOTIONS.name)
                }
            }
        }
    }

    LaunchedEffect(savedPath) {
        savedPath?.let {
            launchShareActivity(it, context = context)
        }
    }
}
