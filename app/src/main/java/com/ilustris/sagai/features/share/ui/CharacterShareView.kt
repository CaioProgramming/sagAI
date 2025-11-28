package com.ilustris.sagai.features.share.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.share.domain.model.ShareType
import com.ilustris.sagai.features.share.presentation.SharePlayViewModel
import com.ilustris.sagai.ui.components.StarryLoader
import com.ilustris.sagai.ui.theme.SagaTitle
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.hexToColor
import effectForGenre
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@Composable
fun CharacterShareView(
    content: SagaContent,
    character: CharacterContent,
    viewModel: SharePlayViewModel = hiltViewModel(),
) {
    val saga = remember { content.data }
    val genre = remember { saga.genre }
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val shareText by viewModel.shareText.collectAsStateWithLifecycle()
    val savedPath by viewModel.savedFilePath.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val characterColor = remember { character.data.hexColor.hexToColor() ?: genre.color }

    DisposableEffect(lifecycleOwner, viewModel) {
        lifecycleOwner.lifecycle.addObserver(viewModel)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(viewModel)
        }
    }

    Box(
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(isLoading.not(), enter = fadeIn() + scaleIn(), exit = fadeOut()) {
            Column(
                Modifier
                    .padding(20.dp)
                    .drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }
                        drawLayer(graphicsLayer)
                    }.shadow(10.dp, RectangleShape, spotColor = genre.color)
                    .clip(RectangleShape)
                    .background(genre.color, RectangleShape)
                    .clickable {
                        coroutineScope.launch {
                            delay(1.seconds)
                            viewModel.startSaving()
                            graphicsLayer.toImageBitmap().asAndroidBitmap().let { bitmap ->
                                viewModel.saveBitmap(bitmap, ShareType.CHARACTER.name)
                            }
                        }
                    },
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(.5f),
                ) {
                    AsyncImage(
                        character.data.image,
                        null,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .clipToBounds()
                                .effectForGenre(genre),
                        contentScale = ContentScale.Crop,
                    )

                    Box(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxSize()
                            .background(fadeGradientBottom(genre.color)),
                    )

                    Column(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            shareText?.title ?: emptyString(),
                            style =
                                MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = genre.bodyFont(),
                                    color = genre.iconColor,
                                    textAlign = TextAlign.Center,
                                    shadow =
                                        Shadow(
                                            genre.color,
                                            blurRadius = 10f,
                                            offset = Offset(2f, 0f),
                                        ),
                                ),
                        )

                        Text(
                            character.data.name,
                            modifier =
                                Modifier
                                    .fillMaxWidth(),
                            style =
                                MaterialTheme.typography.displayMedium.copy(
                                    fontFamily = genre.headerFont(),
                                    textAlign = TextAlign.Center,
                                    brush =
                                        Brush.verticalGradient(
                                            listOf(
                                                genre.color,
                                                characterColor,
                                                genre.iconColor,
                                            ),
                                        ),
                                    shadow = Shadow(genre.color, blurRadius = 15f),
                                ),
                        )

                        Text(
                            shareText?.text ?: emptyString(),
                            style =
                                MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = genre.bodyFont(),
                                    color = genre.iconColor,
                                    textAlign = TextAlign.Center,
                                    fontStyle = FontStyle.Italic,
                                    letterSpacing = 3.sp,
                                    shadow =
                                        Shadow(
                                            genre.color,
                                            blurRadius = 5f,
                                            offset = Offset(5f, 0f),
                                        ),
                                ),
                        )
                    }
                }

                Image(
                    painter = painterResource(R.drawable.ic_spark),
                    null,
                    modifier =
                        Modifier
                            .size(24.dp)
                            .align(Alignment.CenterHorizontally),
                    colorFilter = ColorFilter.tint(genre.iconColor),
                )

                Text(
                    shareText?.caption ?: emptyString(),
                    style =
                        MaterialTheme.typography.labelMedium.copy(
                            fontFamily = genre.bodyFont(),
                            color = genre.iconColor,
                        ),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )

                SagaTitle(
                    textStyle = MaterialTheme.typography.labelMedium,
                    modifier =
                        Modifier
                            .padding(8.dp)
                            .align(Alignment.CenterHorizontally),
                )
            }
        }

        StarryLoader(isLoading, brushColors = content.data.genre.colorPalette())
    }

    LaunchedEffect(Unit) {
        viewModel.generateShareText(sagaContent = content, ShareType.CHARACTER, character)
    }

    LaunchedEffect(isLoading) {
        if (isLoading.not() && isSaving.not()) {
            coroutineScope.launch {
                delay(2.seconds)
                graphicsLayer.toImageBitmap().asAndroidBitmap().let { bitmap ->
                    viewModel.saveBitmap(bitmap, "character_share")
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
