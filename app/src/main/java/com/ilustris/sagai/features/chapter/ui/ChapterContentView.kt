package com.ilustris.sagai.features.chapter.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.chapter.data.model.ChapterInfo
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.newsaga.data.model.selectiveHighlight
import com.ilustris.sagai.ui.components.EmotionalCard
import com.ilustris.sagai.ui.components.StarryLoader
import com.ilustris.sagai.ui.theme.TypewriterText
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.fadedGradientTopAndBottom
import com.ilustris.sagai.ui.theme.filters.effectForGenre
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFill
import kotlin.time.Duration.Companion.seconds

@Composable
fun ChapterContentView(
    chapter: ChapterInfo,
    content: SagaContent,
    modifier: Modifier,
    isGenerating: Boolean = false,
    loadingMessage: String? = null,
    isLast: Boolean = false,
    imageSize: Dp = 500.dp,
    onGenerateIcon: (ChapterInfo) -> Unit = {},
    onReviewChapter: (ChapterInfo) -> Unit = {},
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val genre = remember { content.data.genre }
        val characters =
            remember {
                chapter.featuredCharacters.map { content.findCharacter(it) }.filterNotNull()
            }

        if (chapter.coverImage.isEmpty()) {
            Image(
                painterResource(genre.icon),
                null,
                Modifier
                    .clickable {
                        onGenerateIcon(chapter)
                    }.size(100.dp)
                    .gradientFill(genre.gradient())
                    .padding(16.dp),
                colorFilter = ColorFilter.tint(genre.resolveColor()),
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
                    model = chapter.coverImage,
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
            text = chapter.overview,
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
        )

        if (chapter.emotionalReview?.isNotEmpty() == true) {
            EmotionalCard(
                chapter.emotionalReview,
                genre,
                true,
                modifier = Modifier.padding(16.dp),
            )
        }
    }

    StarryLoader(isGenerating, loadingMessage, brushColors = content.data.genre.colorPalette())
}
