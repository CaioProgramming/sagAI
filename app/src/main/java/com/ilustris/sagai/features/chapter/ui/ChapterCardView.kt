package com.ilustris.sagai.features.chapter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.core.utils.toRoman
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.newsaga.data.model.selectiveHighlight
import com.ilustris.sagai.features.saga.chat.ui.components.bubble
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.chat.BubbleTailAlignment
import com.ilustris.sagai.ui.theme.filters.effectForGenre
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.headerFont

@Composable
fun ChapterCardView(
    genre: Genre,
    chapter: Chapter,
    chapterIndex: Int,
    modifier: Modifier,
    showTitle: Boolean = true,
) {
    val shape = genre.bubble(BubbleTailAlignment.BottomRight, 0.dp, 0.dp, true)
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .clip(shape)
                .border(1.dp, genre.resolveColor().gradientFade(), shape)
                .background(MaterialTheme.colorScheme.surfaceContainer, shape)
                .fillMaxWidth()
                .weight(1f),
        ) {
            var textAlpha by remember {
                mutableFloatStateOf(1f)
            }
            AsyncImage(
                chapter.coverImage,
                contentDescription = chapter.title,
                contentScale = ContentScale.Crop,
                onError = {
                    textAlpha = 1f
                },
                onSuccess = {
                    textAlpha = 0f
                },
                modifier =
                    Modifier
                        .fillMaxSize()
                        .effectForGenre(genre)
                        .selectiveColorHighlight(genre.selectiveHighlight()),
            )

            Text(
                chapterIndex.toRoman(),
                style =
                    MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = genre.headerFont(),
                        textAlign = TextAlign.Center,
                        brush = genre.gradient(),
                    ),
                modifier =
                    Modifier
                        .alpha(textAlpha)
                        .padding(6.dp)
                        .align(Alignment.Center),
            )
        }

        if (showTitle) {
            Text(
                text = chapter.title,
                maxLines = 1,
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontFamily = genre.bodyFont(),
                        textAlign = TextAlign.Center,
                    ),
                modifier =
                    Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_9a")
@Composable
fun ChapterCardViewPreview() {
    val chapters =
        listOf(
            Chapter(
                title = "The Beginning",
                overview = "This is the first chapter of the saga.",
                coverImage = "https://i.pinimg.com/564x/0a/92/7d/0a927df0b8a6a12a5276e03882775739.jpg",
                createdAt = System.currentTimeMillis(),
                actId = 1,
            ),
            Chapter(
                actId = 1,
                title = "The Journey",
                overview = "The adventure continues.",
                coverImage = "https://i.pinimg.com/564x/0f/c0/2c/0fc02cf3c9f28d6c70607900b3e77f0c.jpg",
                createdAt = System.currentTimeMillis(),
            ),
            Chapter(
                actId = 1,
                title = "The End",
                overview = "The final chapter of the saga.",
                coverImage = "https://i.pinimg.com/564x/6c/9b/7f/6c9b7f5f4c02f10b78c93a9d941846c4.jpg",
                createdAt = System.currentTimeMillis(),
            ),
        )

    SagAIScaffold {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier =
                Modifier
                    .padding(8.dp)
                    .fillMaxSize(),
        ) {
            items(chapters.size) { index ->
                ChapterCardView(
                    chapter = chapters[index],
                    genre = Genre.FANTASY,
                    chapterIndex = index + 1,
                    modifier = Modifier.aspectRatio(1f),
                )
            }
        }
    }
}
