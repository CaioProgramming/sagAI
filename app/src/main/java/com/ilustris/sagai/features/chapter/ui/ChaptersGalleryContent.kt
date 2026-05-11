package com.ilustris.sagai.features.chapter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.ui.animations.genreVfx
import com.ilustris.sagai.ui.components.stylisedText

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ChaptersGalleryContent(
    saga: SagaContent,
    chapters: List<com.ilustris.sagai.features.chapter.data.model.ChapterInfo>,
    isGenerating: Boolean = false,
    loadingMessage: String? = null,
    onGenerateIcon: (com.ilustris.sagai.features.chapter.data.model.ChapterInfo) -> Unit = {},
    onReviewChapter: (com.ilustris.sagai.features.chapter.data.model.ChapterInfo) -> Unit = {},
) {
    val genre = saga.data.genre

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        chapters.forEachIndexed { index, chapter ->
            stickyHeader {
                genre.stylisedText(
                    text = chapter.title,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors =
                                        listOf(
                                            MaterialTheme.colorScheme.background,
                                            androidx.compose.ui.graphics.Color.Transparent,
                                        ),
                                ),
                            ).padding(16.dp)
                            .genreVfx(genre),
                )
            }

            item {
                ChapterContentView(
                    chapter = chapter,
                    isGenerating = isGenerating,
                    loadingMessage = loadingMessage,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    isLast = index == chapters.size - 1,
                )
            }
        }
    }
}
