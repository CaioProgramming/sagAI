package com.ilustris.sagai.features.chapter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.LargeHorizontalHeader
import com.ilustris.sagai.ui.theme.headerFont

@Composable
fun ChaptersGalleryContent(
    title: String,
    subtitle: String,
    saga: SagaContent,
    chapters: List<ChapterContent>,
    onBackClick: () -> Unit = {},
) {
    val genre = saga.data.genre

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        stickyHeader {
            LargeHorizontalHeader(
                title,
                subtitle,
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

        itemsIndexed(chapters) { index, chapter ->
            ChapterContentView(
                content = saga,
                chapter = chapter,
                modifier = Modifier.fillMaxWidth(),
                isLast = index == chapters.lastIndex,
            )
        }
    }
}
