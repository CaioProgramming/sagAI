package com.ilustris.sagai.features.chapter.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.chapter.presentation.ChapterViewModel
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.newsaga.data.model.selectiveHighlight
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.components.AutoResizeText
import com.ilustris.sagai.ui.components.EmotionalCard
import com.ilustris.sagai.ui.theme.TypewriterText
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.fadeGradientTop
import com.ilustris.sagai.ui.theme.fadedGradientTopAndBottom
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.reactiveShimmer
import effectForGenre
import kotlin.time.Duration.Companion.seconds

@Composable
fun ChapterContentView(
    chapter: ChapterContent,
    content: SagaContent,
    modifier: Modifier,
    isLast: Boolean = false,
    imageSize: Dp = 250.dp,
    openCharacters: () -> Unit = {},
    viewModel: ChapterViewModel = hiltViewModel(),
) {
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val genre = remember { content.data.genre }
        val characters = remember { chapter.fetchCharacters(content) }

        if (chapter.data.coverImage.isEmpty()) {
            Image(
                painterResource(R.drawable.ic_spark),
                null,
                Modifier
                    .clickable {
                        viewModel.generateIcon(
                            content,
                            chapter,
                        )
                    }.size(50.dp)
                    .gradientFill(genre.gradient(true))
                    .padding(16.dp),
            )

            AutoResizeText(
                text = chapter.data.title,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .reactiveShimmer(isLast),
                style =
                    MaterialTheme.typography.displaySmall.copy(
                        fontFamily = genre.headerFont(),
                        brush = genre.gradient(true),
                        textAlign = TextAlign.Center,
                    ),
            )
        } else {
            var imageSize by remember {
                mutableStateOf(imageSize)
            }

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(imageSize)
                        .animateContentSize(),
            ) {
                AsyncImage(
                    model = chapter.data.coverImage,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    onError = {
                        imageSize = 0.dp
                    },
                    modifier =
                        Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .fillMaxSize()
                            .effectForGenre(genre)
                            .selectiveColorHighlight(genre.selectiveHighlight()),
                )

                Box(
                    Modifier
                        .fillMaxSize()
                        .background(fadedGradientTopAndBottom())
                        .align(Alignment.BottomCenter),
                )

                AutoResizeText(
                    text = chapter.data.title,
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(16.dp)
                            .reactiveShimmer(isLast),
                    style =
                        MaterialTheme.typography.displaySmall.copy(
                            fontFamily = genre.headerFont(),
                            brush = genre.gradient(true),
                            textAlign = TextAlign.Center,
                        ),
                )
            }
        }

        LazyRow(Modifier.align(Alignment.CenterHorizontally)) {
            items(characters) {
                CharacterAvatar(
                    it,
                    genre = genre,
                    softFocusRadius = 0f,
                    grainRadius = 0f,
                    modifier =
                        Modifier
                            .padding(8.dp)
                            .size(50.dp),
                )
            }
        }

        TypewriterText(
            text = chapter.data.overview,
            modifier =
                Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
            duration = 3.seconds,
            easing = LinearEasing,
            isAnimated = false,
            genre = genre,
            mainCharacter = content.mainCharacter?.data,
            characters = content.getCharacters(),
            wiki = content.wikis,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Normal,
                    fontFamily = genre.bodyFont(),
                    textAlign = TextAlign.Justify,
                    color = MaterialTheme.colorScheme.onBackground,
                ),
            onTextUpdate = {
            },
            onTextClick = openCharacters,
        )

        if (chapter.data.emotionalReview?.isNotEmpty() == true) {
            EmotionalCard(
                chapter.data.emotionalReview,
                genre,
                true,
                modifier = Modifier.padding(16.dp),
            )
        }
    }

    if (isGenerating) {
        Dialog(
            onDismissRequest = { },
            properties =
                DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                ),
        ) {
            StarryTextPlaceholder(
                modifier = Modifier.fillMaxSize().gradientFill(content.data.genre.gradient(true)),
            )
        }
    }
}
