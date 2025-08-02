package com.ilustris.sagai.features.chapter.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.ui.components.buildCharactersAnnotatedString
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.GradientType
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.headerFont
import effectForGenre

@Composable
fun ChapterCardView(
    chapter: Chapter,
    genre: Genre,
    characters: List<Character> = emptyList(),
    modifier: Modifier,
) {
    val shape = RoundedCornerShape(genre.cornerSize())
    val showCover = remember { mutableStateOf(false) }
    Box(
        modifier =
            modifier
                .border(
                    1.dp,
                    gradientAnimation(
                        genre.color.darkerPalette(),
                        gradientType = GradientType.VERTICAL,
                    ),
                    shape,
                ).background(MaterialTheme.colorScheme.surface, shape)
                .clip(shape)
                .clickable {
                    showCover.value = showCover.value.not()
                },
    ) {
        AnimatedVisibility(
            showCover.value.not(),
            modifier = Modifier.fillMaxSize(),
            enter =
                fadeIn(
                    tween(600, easing = EaseIn),
                ),
            exit = fadeOut(),
        ) {
            AsyncImage(
                chapter.coverImage,
                contentDescription = chapter.title,
                modifier = Modifier.fillMaxSize().effectForGenre(
                    genre,
                ),
                contentScale = ContentScale.Crop,
            )
        }

        Box(Modifier.fillMaxSize().background(fadeGradientBottom()))

        AnimatedVisibility(
            showCover.value.not(),
            enter = fadeIn(),
            exit = fadeOut(),
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp),
        ) {
            Text(
                text = chapter.title,
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontFamily = genre.headerFont(),
                        brush = Brush.verticalGradient(genre.color.darkerPalette()),
                        textAlign = TextAlign.Center,
                    ),
            )
        }

        AnimatedVisibility(
            showCover.value,
            modifier = Modifier.fillMaxSize(),
            enter = slideInVertically { -it } + fadeIn(),
            exit = fadeOut(),
        ) {
            Column(
                Modifier.padding(8.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = chapter.title,
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontFamily = genre.headerFont(),
                            brush = gradientAnimation(genre.color.darkerPalette()),
                            textAlign = TextAlign.Center,
                        ),
                    modifier =
                        Modifier
                            .padding(8.dp),
                )
                Text(
                    text = buildCharactersAnnotatedString(chapter.overview, characters),
                    style =
                        MaterialTheme.typography.bodySmall.copy(
                            fontFamily = genre.bodyFont(),
                            textAlign = TextAlign.Start,
                        ),
                )
            }
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_9a")
@Composable
fun ChapterCardViewPreview() {
    val genre = Genre.FANTASY
    val chapters =
        listOf(
            Chapter(
                sagaId = 1,
                title = "The Beginning",
                overview = "This is the first chapter of the saga.",
                messageReference = 1,
                coverImage = "https://i.pinimg.com/564x/0a/92/7d/0a927df0b8a6a12a5276e03882775739.jpg",
                visualDescription = "A dark forest with a mysterious path.",
                createdAt = System.currentTimeMillis(),
            ),
            Chapter(
                sagaId = 1,
                title = "The Journey",
                overview = "The adventure continues.",
                messageReference = 2,
                coverImage = "https://i.pinimg.com/564x/0f/c0/2c/0fc02cf3c9f28d6c70607900b3e77f0c.jpg",
                visualDescription = "A long road through the mountains.",
                createdAt = System.currentTimeMillis(),
            ),
            Chapter(
                sagaId = 1,
                title = "The End",
                overview = "The final chapter of the saga.",
                messageReference = 3,
                coverImage = "https://i.pinimg.com/564x/6c/9b/7f/6c9b7f5f4c02f10b78c93a9d941846c4.jpg",
                visualDescription = "A castle in the sunset.",
                createdAt = System.currentTimeMillis(),
            ),
        )

    SagAIScaffold {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(8.dp),
        ) {
            items(chapters.size) { index ->
                ChapterCardView(
                    chapter = chapters[index],
                    genre = genre,
                    characters = emptyList(),
                    modifier = Modifier.fillMaxWidth(.4f).fillMaxHeight(.2f),
                )
            }
        }
    }
}
