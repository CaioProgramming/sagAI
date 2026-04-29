package com.ilustris.sagai.features.chapter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.DetailSectionView
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.LargeHorizontalHeader
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shape

@Composable
fun ChaptersGalleryContent(
    section: DetailSectionView.ChapterSection,
    onBackClick: () -> Unit = {},
) {
    val genre = section.saga.data.genre
    val chapters = section.chapters ?: emptyList()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                LargeHorizontalHeader(
                    section.title,
                    section.subtitle,
                    titleStyle =
                        MaterialTheme.typography.displaySmall.copy(
                            fontFamily = genre.headerFont(),
                        ),
                    subtitleStyle =
                        MaterialTheme.typography.labelMedium.copy(
                            fontFamily = genre.bodyFont(),
                        ),
                    modifier =
                        Modifier
                            .padding(bottom = 16.dp)
                            .fillMaxWidth(),
                )

                if (section.insight.isNullOrBlank()) {
                    Box(
                        Modifier
                            .padding(vertical = 24.dp)
                            .fillMaxWidth()
                            .height(60.dp)
                            .background(
                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                                genre.shape(),
                            ).reactiveShimmer(true),
                    )
                } else {
                    Text(
                        section.insight,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = genre.bodyFont(),
                                textAlign = TextAlign.Center,
                                fontStyle = FontStyle.Italic,
                            ),
                        modifier =
                            Modifier
                                .padding(vertical = 24.dp)
                                .fillMaxWidth()
                                .alpha(.7f),
                    )
                }
            }
        }

        itemsIndexed(chapters) { index, chapter ->
            ChapterCardView(
                genre = genre,
                chapter = chapter.data,
                chapterIndex = index + 1,
                modifier = Modifier.aspectRatio(1f),
            )
        }
    }
}
