package com.ilustris.sagai.features.chapter.ui

import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.newsaga.data.model.selectiveHighlight
import com.ilustris.sagai.ui.components.EmotionalCard
import com.ilustris.sagai.ui.theme.TypewriterText
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.fadeGradientTop
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import effectForGenre
import kotlin.time.Duration.Companion.seconds

@Composable
fun ChapterContentView(
    chapter: Chapter,
    content: SagaContent,
    modifier: Modifier,
    isLast: Boolean = false,
    openCharacters: () -> Unit = {},
    regenerateCover: (Chapter) -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val genre = content.data.genre

        if (chapter.coverImage.isEmpty()) {
            Image(
                painterResource(R.drawable.ic_spark),
                null,
                Modifier
                    .clickable {
                        regenerateCover(chapter)
                    }.size(50.dp)
                    .gradientFill(genre.gradient(true))
                    .padding(16.dp),
            )
        }

        Box(
            Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(fadeGradientBottom()),
        )

        var imageSize by remember {
            mutableFloatStateOf(
                .35f,
            )
        }

        val sizeAnimation by animateFloatAsState(
            targetValue = imageSize,
            label = "Image Size Animation",
        )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(sizeAnimation),
        ) {
            AsyncImage(
                model = chapter.coverImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onError = {
                    imageSize = 0f
                },
                modifier =
                    Modifier
                        .fillMaxSize()
                        .effectForGenre(genre)
                        .selectiveColorHighlight(genre.selectiveHighlight()),
            )

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(fadeGradientTop())
                    .align(Alignment.TopCenter),
            )

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(fadeGradientBottom())
                    .align(Alignment.BottomCenter),
            )
        }

        Text(
            text = chapter.title,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
            style =
                MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Light,
                    letterSpacing = 5.sp,
                    fontFamily = genre.headerFont(),
                    brush = genre.gradient(isLast),
                    textAlign = TextAlign.Center,
                ),
        )

        TypewriterText(
            text = chapter.overview,
            modifier =
                Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
            duration = 3.seconds,
            easing = LinearEasing,
            isAnimated = isLast,
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

        if (chapter.emotionalReview?.isNotEmpty() == true) {
            EmotionalCard(chapter.emotionalReview, genre, true)
        }

        if (isLast) {
            Box(
                Modifier.background(fadeGradientTop()).fillMaxWidth().height(50.dp),
            )
        }
    }
}
