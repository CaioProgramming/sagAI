package com.ilustris.sagai.features.share.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.components.stylisedText
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.shape
import effectForGenre
import kotlin.random.Random

private class RandomTrapezoidShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val path = Path()
        val factor = Random.nextFloat() * 0.4f
        val inset = size.width * factor

        when (Random.nextInt(4)) {
            0 -> { // Top inset
                path.moveTo(inset, 0f)
                path.lineTo(size.width - inset, 0f)
                path.lineTo(size.width, size.height)
                path.lineTo(0f, size.height)
            }
            1 -> { // Bottom inset
                path.moveTo(0f, 0f)
                path.lineTo(size.width, 0f)
                path.lineTo(size.width - inset, size.height)
                path.lineTo(inset, size.height)
            }
            2 -> { // Left inset (vertical trapezoid)
                path.moveTo(0f, size.height * factor)
                path.lineTo(size.width, 0f)
                path.lineTo(size.width, size.height)
                path.lineTo(0f, size.height * (1 - factor))
            }
            else -> { // Right inset (vertical trapezoid)
                path.moveTo(size.width * factor, 0f)
                path.lineTo(size.width, size.height * factor)
                path.lineTo(size.width, size.height * (1 - factor))
                path.lineTo(0f, size.height)
            }
        }
        path.close()
        return Outline.Generic(path)
    }
}

@Composable
private fun rememberRandomShape(): Shape = remember { RandomTrapezoidShape() }

@Composable
fun GTAStyleCover(
    sagaContent: SagaContent,
    subtitle: String,
) {
    val saga = sagaContent.data
    val chapters = sagaContent.flatChapters().map { it.data }
    val mainIcon = saga.icon
    val chapterIcons = chapters.map { it.coverImage }
    val allIcons = remember { (listOf(mainIcon) + chapterIcons) }
    val genre = saga.genre
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(genre.color.gradientFade()),
    ) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalItemSpacing = 4.dp,
            contentPadding = PaddingValues(2.dp),
        ) {
            items(
                items = allIcons,
                span = { icon ->
                    if (icon == mainIcon) {
                        StaggeredGridItemSpan.FullLine
                    } else {
                        StaggeredGridItemSpan.SingleLane
                    }
                },
            ) { icon ->
                val isMainIcon = icon == mainIcon
                val height = if (isMainIcon) 200.dp else (Random.nextInt(100, 200)).dp
                AsyncImage(
                    model = icon,
                    contentDescription = null,
                    modifier =
                        Modifier
                            .height(height)
                            .effectForGenre(genre, useFallBack = true),
                    contentScale = ContentScale.Crop,
                )
            }
        }

        Column(Modifier.align(Alignment.Center).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            genre.stylisedText(
                saga.title,
                fontSize = MaterialTheme.typography.headlineMedium.fontSize,
            )

            Text(
                subtitle,
                modifier = Modifier.fillMaxWidth(),
                style =
                    MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = saga.genre.bodyFont(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        fontStyle = FontStyle.Italic,
                        color = saga.genre.iconColor,
                        shadow =
                            Shadow(
                                saga.genre.color,
                                offset = Offset(5f, 0f),
                                blurRadius = 5f,
                            ),
                    ),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 600)
@Composable
fun GTAStyleCoverPreview() {
    val saga =
        Saga(
            id = 1,
            title = "Cosmic Odyssey",
            icon = "https://picsum.photos/seed/saga/800/600",
            genre = Genre.SPACE_OPERA,
        )
    val chapters =
        (1..9).map {
            Chapter(
                id = it,
                title = "Chapter $it",
                coverImage = "https://picsum.photos/seed/chapter$it/400/300",
                actId = 1,
            )
        }
    val sagaContent =
        SagaContent(
            data = saga,
            acts =
                listOf(
                    ActContent(
                        data = Act(id = 1, title = "Act 1", sagaId = 1),
                        chapters =
                            chapters.map {
                                ChapterContent(data = it, events = emptyList())
                            },
                    ),
                ),
        )

    SagAITheme {
        GTAStyleCover(sagaContent = sagaContent, "No one gets to see this yet")
    }
}
