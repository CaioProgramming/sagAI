package com.ilustris.sagai.features.share.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.sortCharactersContentByMessageCount
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.share.domain.model.ShareType
import com.ilustris.sagai.features.share.presentation.SharePlayViewModel
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.theme.SagaTitle
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.hexToColor
import com.ilustris.sagai.ui.theme.shape
import com.ilustris.sagai.ui.theme.solidGradient
import effectForGenre
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@Composable
fun RelationsShareView(
    sagaContent: SagaContent,
    viewModel: SharePlayViewModel = hiltViewModel(),
) {
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val shareText by viewModel.shareText.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val genre = remember { sagaContent.data.genre }
    val savedPath by viewModel.savedFilePath.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current
    val characters =
        remember {
            sortCharactersContentByMessageCount(
                sagaContent.characters,
                sagaContent.flatMessages(),
            )
        }

    DisposableEffect(lifecycleOwner, viewModel) {
        lifecycleOwner.lifecycle.addObserver(viewModel)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(viewModel)
        }
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AnimatedVisibility(isLoading.not() && shareText != null) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(genre.shape())
                    .background(MaterialTheme.colorScheme.background)
                    .drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }
                        drawLayer(graphicsLayer)
                    }.clickable {
                        coroutineScope.launch {
                            delay(2.seconds)
                            graphicsLayer.toImageBitmap().asAndroidBitmap().let { bitmap ->
                                viewModel.saveBitmap(bitmap, ShareType.RELATIONS.name)
                            }
                        }
                    },
            ) {
                LazyVerticalGrid(
                    GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.align(Alignment.Center).background(MaterialTheme.colorScheme.background),
                ) {
                    item(span = { GridItemSpan(3) }) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier =
                                Modifier
                                    .background(
                                        MaterialTheme.colorScheme.background,
                                    ).padding(16.dp)
                                    .fillMaxWidth(),
                        ) {
                            Image(
                                painterResource(R.drawable.ic_spark),
                                null,
                                colorFilter = ColorFilter.tint(genre.color),
                                modifier = Modifier.size(32.dp),
                            )
                            Text(
                                sagaContent.data.title,
                                style =
                                    MaterialTheme.typography.headlineMedium.copy(
                                        fontFamily = genre.headerFont(),
                                        brush = genre.gradient(),
                                        fontStyle = FontStyle.Italic,
                                        shadow = Shadow(genre.color, Offset(2f, 0f), 10f),
                                    ),
                                modifier =
                                    Modifier
                                        .weight(1f),
                            )
                        }
                    }
                    items(characters) {
                        val index = characters.indexOf(it)
                        val isTop3 = index <= 2
                        AsyncImage(
                            model = it.data.image,
                            contentDescription = it.data.name,
                            modifier =
                                Modifier
                                    .border(
                                        2.dp,
                                        if (isTop3) genre.gradient() else genre.color.solidGradient(),
                                        RectangleShape,
                                    ).effectForGenre(genre, useFallBack = true)
                                    .aspectRatio(.75f),
                            contentScale = ContentScale.Crop,
                            colorFilter =
                                ColorFilter.tint(
                                    (
                                        it.data.hexColor.hexToColor()
                                            ?: genre.color
                                    ).copy(alpha = .2f),
                                    blendMode = BlendMode.SrcOver,
                                ),
                        )
                    }

                    shareText?.let {
                        item(span = { GridItemSpan(3) }) {
                            Text(
                                it.text,
                                style =
                                    MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = genre.bodyFont(),
                                        shadow = Shadow(genre.color, Offset(5f, 3f), 5f),
                                        fontStyle = FontStyle.Italic,
                                        fontWeight = FontWeight.Light,
                                    ),
                                modifier =
                                    Modifier
                                        .background(MaterialTheme.colorScheme.background)
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                            )
                        }
                    }

                    item(span = { GridItemSpan(3) }) {
                        Column(
                            Modifier
                                .background(MaterialTheme.colorScheme.background)
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                "Descubra mais universos.",
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
            }
        }

        AnimatedVisibility(isLoading, enter = fadeIn(), exit = fadeOut()) {
            StarryTextPlaceholder(
                Modifier.fillMaxSize(),
                genre.color,
                sagaContent.messagesSize(),
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.generateShareText(sagaContent = sagaContent, ShareType.RELATIONS)
    }

    LaunchedEffect(isLoading) {
        if (isLoading.not() && isSaving.not()) {
            coroutineScope.launch {
                delay(2.seconds)
                graphicsLayer.toImageBitmap().asAndroidBitmap().let { bitmap ->
                    viewModel.saveBitmap(bitmap, ShareType.RELATIONS.name)
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
