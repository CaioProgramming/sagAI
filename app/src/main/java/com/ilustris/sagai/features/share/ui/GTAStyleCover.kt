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
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.newsaga.data.model.resolveIconColor
import com.ilustris.sagai.ui.components.stylisedText
import com.ilustris.sagai.ui.theme.SagaTitle
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.filters.effectForGenre
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
    saga: SagaContent,
    title: String,
    subtitle: String,
    caption: String,
) {
    val chapters = saga.flatChapters().map { it.data }
    val mainIcon = saga.data.icon
    val chapterIcons = chapters.map { it.coverImage }
    val allIcons = remember(saga) { (listOf(mainIcon) + chapterIcons) }
    val genre = saga.data.genre
    val resolvedColor = genre.resolveColor()
    val resolvedIconColor = genre.resolveIconColor()
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalItemSpacing = 2.dp,
            contentPadding = PaddingValues(2.dp),
        ) {
            items(
                items = allIcons,
            ) { icon ->
                val isMainIcon = icon == mainIcon
                val height = if (isMainIcon) 200.dp else (Random.nextInt(80, 150)).dp
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

        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = .5f)),
        )

        Column(
            Modifier
                .align(Alignment.Center)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                title,
                modifier = Modifier.fillMaxWidth(),
                style =
                    MaterialTheme.typography.titleSmall.copy(
                        fontFamily = saga.data.genre.bodyFont(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        color = resolvedIconColor,
                        shadow =
                            Shadow(
                                resolvedColor,
                                offset = Offset(5f, 0f),
                                blurRadius = 10f,
                            ),
                    ),
            )

            genre.stylisedText(
                saga.data.title,
                fontSize = MaterialTheme.typography.headlineMedium.fontSize,
            )

            Text(
                subtitle,
                modifier = Modifier.fillMaxWidth(),
                style =
                    MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = saga.data.genre.bodyFont(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        fontStyle = FontStyle.Italic,
                        color = resolvedIconColor,
                        shadow =
                            Shadow(
                                resolvedColor,
                                offset = Offset(5f, 0f),
                                blurRadius = 5f,
                            ),
                    ),
            )

            Text(
                caption,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = genre.bodyFont(),
                    ),
            )
            SagaTitle(textStyle = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(4.dp))
        }
    }
}
