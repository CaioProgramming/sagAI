package com.ilustris.sagai.features.chapter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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

@Composable
fun ChaptersGalleryContent(
    section: DetailSectionView.ChapterSection,
    onBackClick: () -> Unit = {},
) {
    val genre = section.saga.data.genre
    val chapters = section.chapters ?: emptyList()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        stickyHeader {
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
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp)
                        .fillMaxWidth(),
            )
        }

        section.insight?.let {
            item {
                Text(
                    section.insight,
                    style =
                        MaterialTheme.typography.labelMedium.copy(
                            fontFamily = genre.bodyFont(),
                            textAlign = TextAlign.Center,
                            fontStyle = FontStyle.Italic,
                        ),
                    modifier =
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                            .alpha(.7f),
                )
            }
        }

        itemsIndexed(chapters) { index, chapter ->
            ChapterContentView(
                content = section.saga,
                chapter = chapter,
                modifier = Modifier.fillMaxWidth(),
                isLast = index == chapters.lastIndex,
            )
        }
    }
}
