package com.ilustris.sagai.features.share.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.ilustris.sagai.features.newsaga.data.model.defaultHeaderImage
import com.ilustris.sagai.features.share.presentation.SharePlayViewModel
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.shape
import effectForGenre
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@Composable
fun PlayStyleShareView(
    content: SagaContent,
    character: Character,
    viewModel: SharePlayViewModel = hiltViewModel(),
) {
    val saga = remember { content.data }
    val genre = remember { saga.genre }
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val shareText by viewModel.shareText.collectAsStateWithLifecycle()
    val savedPath by viewModel.savedFilePath.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    val context = LocalContext.current
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(isLoading.not(), enter = fadeIn(), exit = fadeOut()) {
            Column(
                Modifier
                    .padding(16.dp)
                    .drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }
                        drawLayer(graphicsLayer)
                    }.fillMaxWidth()
                    .border(1.dp, genre.color.gradientFade(), genre.shape())
                    .background(genre.color, genre.shape()),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(.5f),
                ) {
                    AsyncImage(
                        character.image,
                        null,
                        modifier = Modifier.fillMaxSize().effectForGenre(genre),
                        contentScale = ContentScale.Crop,
                    )

                    Box(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .fillMaxHeight(.4f)
                            .background(fadeGradientBottom(genre.color)),
                    )

                    Text(
                        shareText ?: emptyString(),
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
                                shadow = Shadow(genre.color, blurRadius = 5f),
                            ),
                    )

                    Column(
                        Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            character.name,
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
                            character.profile.occupation,
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
                        MaterialTheme.typography.titleMedium.copy(
                            color = genre.iconColor,
                            fontFamily = genre.headerFont(),
                            shadow = Shadow(genre.color, blurRadius = 10f),
                        ),
                )

                Text(
                    "Created with Sagas",
                    modifier =
                        Modifier
                            .padding(8.dp)
                            .align(Alignment.CenterHorizontally),
                    style =
                        MaterialTheme.typography.bodySmall.copy(
                            fontFamily = genre.bodyFont(),
                            color = genre.iconColor,
                        ),
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
        viewModel.generatePlayStyleText(sagaContent = content, character = character)
    }

    LaunchedEffect(isLoading) {
        if (isLoading.not()) {
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
            character =
                Character(
                    id = 1,
                    name = "Lyz",
                    backstory = "A young warrior destined to find the Azure Blade.",
                    sagaId = 1,
                    details = Details(),
                    profile =
                        CharacterProfile(
                            occupation = "CyberMerc",
                        ),
                ),
        )
    }
}
