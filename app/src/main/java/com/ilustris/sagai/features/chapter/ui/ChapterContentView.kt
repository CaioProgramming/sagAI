package com.ilustris.sagai.features.chapter.ui

import com.ilustris.sagai.features.progression.data.model.HonorKind
import com.ilustris.sagai.features.progression.ui.ScopeHonors
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.chapter.data.model.ChapterInfo
import com.ilustris.sagai.features.chapter.presentation.ChapterViewModel
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.imagegeneration.model.ImageTarget
import com.ilustris.sagai.features.imagegeneration.ui.ImageGenerationSlot
import com.ilustris.sagai.features.imagegeneration.ui.imageReveal
import com.ilustris.sagai.features.imagegeneration.ui.rememberImageTargetState
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.ui.components.EmotionalCard
import com.ilustris.sagai.ui.components.StarryLoader
import com.ilustris.sagai.ui.theme.TypewriterText
import com.ilustris.sagai.ui.theme.components.SparkIcon
import com.ilustris.sagai.ui.theme.fadedGradientTopAndBottom
import com.ilustris.sagai.ui.theme.filters.effectForGenre
import com.ilustris.sagai.ui.theme.gradient
import kotlin.time.Duration.Companion.seconds

@Composable
fun ChapterContentView(
    chapter: ChapterInfo,
    modifier: Modifier,
    isGenerating: Boolean = false,
    loadingMessage: String? = null,
    isLast: Boolean = false,
    imageSize: Dp = 500.dp,
) {
    val viewModel = hiltViewModel<ChapterViewModel>()
    val sagaContent by viewModel.saga.collectAsStateWithLifecycle()
    val isGeneratingState by viewModel.isGenerating.collectAsStateWithLifecycle()

    val genre =
        remember(sagaContent) {
            sagaContent?.data?.genre ?: com.ilustris.sagai.features.newsaga.data.model.Genre.FANTASY
        }

    LaunchedEffect(chapter.id) {
        viewModel.loadSaga(chapter.sagaId.toString())
    }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val characters =
            remember(sagaContent, chapter.featuredCharacters) {
                chapter.featuredCharacters.map { sagaContent?.findCharacter(it) }.filterNotNull()
            }

        val target = remember(chapter.id) { ImageTarget.Chapter(chapter.id) }
        var coverHeight by remember { mutableStateOf(imageSize) }
        val coverMissing = chapter.coverImage.isEmpty()

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(coverHeight)
                    .animateContentSize(),
        ) {
            // Qualified: inside this Column the ColumnScope overload would otherwise win and refuse the call.
            androidx.compose.animation.AnimatedVisibility(
                visible = coverMissing,
                exit = fadeOut(tween(700)),
                modifier = Modifier.fillMaxSize(),
            ) {
                ImageGenerationSlot(
                    target = target,
                    genre = genre,
                    onGenerate = { viewModel.generateIcon(chapter.id) },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            if (!coverMissing) {
                AsyncImage(
                    model = chapter.coverImage,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    onError = {
                        coverHeight = 0.dp
                    },
                    modifier =
                        Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .fillMaxSize()
                            .imageReveal(target)
                            .effectForGenre(genre, enableSelectiveHighlight = true),
                )

                Box(
                    Modifier
                        .fillMaxSize()
                        .background(fadedGradientTopAndBottom())
                        .align(Alignment.BottomCenter),
                )

                RegenerateCoverButton(
                    target = target,
                    genre = genre,
                    onClick = { viewModel.generateIcon(chapter.id) },
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp),
                )
            }
        }

        LazyRow(Modifier.align(Alignment.CenterHorizontally)) {
            items(characters) {
                CharacterAvatar(
                    it.data,
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
            text = chapter.content,
            modifier =
                Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
            duration = 3.seconds,
            easing = LinearEasing,
            isAnimated = false,
            genre = genre,
            mainCharacter = sagaContent?.mainCharacter?.data,
            characters = sagaContent?.getCharacters() ?: emptyList(),
            wiki = sagaContent?.wikis ?: emptyList(),
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Normal,
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    lineHeight = 28.sp,
                    textAlign = TextAlign.Justify,
                    color = MaterialTheme.colorScheme.onBackground,
                ),
            onTextUpdate = {
            },
        )

        ScopeHonors(HonorKind.MEDAL, chapter.id, genre)

        if (chapter.emotionalReview?.isNotEmpty() == true) {
            EmotionalCard(
                chapter.emotionalReview,
                genre,
                true,
                modifier = Modifier.padding(16.dp),
            )
        }
    }

    StarryLoader(
        isGeneratingState || isGenerating,
        brushColors = genre.colorPalette(),
    )
}

@Composable
private fun RegenerateCoverButton(
    target: ImageTarget,
    genre: com.ilustris.sagai.features.newsaga.data.model.Genre,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by rememberImageTargetState(target)
    IconButton(
        onClick = onClick,
        enabled = !state.isActive,
        modifier = modifier,
    ) {
        SparkIcon(
            brush = genre.gradient(),
            modifier = Modifier.size(32.dp),
        )
    }
}
