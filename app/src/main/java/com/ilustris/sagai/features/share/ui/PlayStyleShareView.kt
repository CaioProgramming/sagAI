package com.ilustris.sagai.features.share.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterProfile
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.selectiveHighlight
import com.ilustris.sagai.features.share.domain.model.ShareType
import com.ilustris.sagai.features.share.presentation.SharePlayViewModel
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.SagaTitle
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.headerFont
import effectForGenre
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@Composable
fun PlayStyleShareView(
    content: SagaContent,
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

    DisposableEffect(lifecycleOwner, viewModel) {
        lifecycleOwner.lifecycle.addObserver(viewModel)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(viewModel)
        }
    }

    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(isLoading.not(), enter = fadeIn(), exit = fadeOut()) {
            Column(
                Modifier
                    .padding(16.dp)
                    .clickable {
                        coroutineScope.launch {
                            delay(2.seconds)
                            graphicsLayer.toImageBitmap().asAndroidBitmap().let { bitmap ->
                                viewModel.saveBitmap(bitmap, "play_style_share")
                            }
                        }
                    }.drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }
                        drawLayer(graphicsLayer)
                    }.fillMaxWidth()
                    .background(genre.color),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(.5f),
                ) {
                    AsyncImage(
                        saga.icon,
                        null,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .effectForGenre(genre)
                                .selectiveColorHighlight(genre.selectiveHighlight()),
                        contentScale = ContentScale.Crop,
                    )

                    Box(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .fillMaxHeight(.5f)
                            .background(fadeGradientBottom(genre.color)),
                    )

                    Text(
                        shareText?.text ?: emptyString(),
                        modifier =
                            Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                                .fillMaxWidth(),
                        style =
                            MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = genre.bodyFont(),
                                color = genre.iconColor,
                                textAlign = TextAlign.Center,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.W600,
                                letterSpacing = 5.sp,
                                shadow = Shadow(genre.color, blurRadius = 2f, offset = Offset(5f, 0f)),
                            ),
                    )

                    Column(
                        Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            content.mainCharacter?.data?.name ?: emptyString(),
                            modifier =
                                Modifier
                                    .fillMaxWidth(),
                            style =
                                MaterialTheme.typography.displayMedium.copy(
                                    brush = Brush.linearGradient(genre.color.darkerPalette()),
                                    fontFamily = genre.headerFont(),
                                    textAlign = TextAlign.Center,
                                ),
                        )

                        Text(
                            content.mainCharacter
                                ?.data
                                ?.profile
                                ?.occupation ?: emptyString(),
                            modifier =
                                Modifier
                                    .fillMaxWidth(),
                            style =
                                MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = genre.bodyFont(),
                                    color = genre.iconColor,
                                    textAlign = TextAlign.Center,
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
                    saga.title,
                    modifier =
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(8.dp),
                    style =
                        MaterialTheme.typography.titleSmall.copy(
                            color = genre.iconColor,
                            fontFamily = genre.headerFont(),
                            shadow = Shadow(genre.color, blurRadius = 10f),
                        ),
                )

                SagaTitle(
                    textStyle = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally),
                )
            }
        }

        AnimatedVisibility(isLoading, modifier = Modifier.fillMaxSize(), enter = fadeIn(), exit = fadeOut()) {
            StarryTextPlaceholder(
                modifier = Modifier.fillMaxSize(),
                starColor = genre.color,
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.generateShareText(sagaContent = content, ShareType.PLAYSTYLE)
    }

    LaunchedEffect(isLoading) {
        if (isLoading.not() && isSaving.not()) {
            coroutineScope.launch {
                delay(2.seconds)
                graphicsLayer.toImageBitmap().asAndroidBitmap().let { bitmap ->
                    viewModel.saveBitmap(bitmap, "play_style_share")
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

fun launchShareActivity(
    uri: Uri,
    context: Context,
) {
    val shareIntent =
        Intent().apply {
            action = Intent.ACTION_SEND
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(uri, context.contentResolver.getType(uri))
            putExtra(
                Intent.EXTRA_SUBJECT,
                context.resources.getString(R.string.app_name),
            )
            putExtra(Intent.EXTRA_STREAM, uri)
        }
    context.startActivity(
        Intent.createChooser(
            shareIntent,
            "Compartilhar post em...",
        ),
    )
}

@Preview(
    showBackground = true,
    device = "spec:width=1080px,height=2400px",
    showSystemUi = false,
)
@Composable
fun PlayStyleShareViewPreview() {
    SagAITheme {
        PlayStyleShareView(
            content =
                SagaContent(
                    Saga(
                        id = 1,
                        title = "BluePrinting",
                        description = "A saga of a legendary sword and the heroes who wield it.",
                        genre = Genre.SCI_FI,
                    ),
                ),
        )
    }
}
