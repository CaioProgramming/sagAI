package com.ilustris.sagai.features.chapter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.SagaContent

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ChaptersGalleryContent(
    saga: SagaContent,
    chapters: List<com.ilustris.sagai.features.chapter.data.model.ChapterInfo>,
    focusChapterId: Int? = null,
    isGenerating: Boolean = false,
    loadingMessage: String? = null,
    onGenerateIcon: (com.ilustris.sagai.features.chapter.data.model.ChapterInfo) -> Unit = {},
    onReviewChapter: (com.ilustris.sagai.features.chapter.data.model.ChapterInfo) -> Unit = {},
) {
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    // Each chapter is a sticky header plus its content: two lazy items.
    androidx.compose.runtime.LaunchedEffect(focusChapterId, chapters.size) {
        val index = chapters.indexOfFirst { it.id == focusChapterId }
        if (focusChapterId != null && index >= 0) listState.animateScrollToItem(index * 2)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
    ) {
        chapters.forEachIndexed { index, chapter ->
            // a plain title: the animated genre title is for hero titles, not a list of headers
            stickyHeader {
                Text(
                    text = chapter.title,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            // solid like a top bar, so the chapter art scrolls cleanly underneath
                            .background(MaterialTheme.colorScheme.background)
                            .padding(16.dp),
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
