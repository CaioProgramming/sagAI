package com.ilustris.sagai.features.act.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.chapter.ui.ChapterContentView
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.wiki.ui.EmotionalReviewCard
import com.ilustris.sagai.ui.animations.genreVfx
import com.ilustris.sagai.ui.components.stylisedText
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.headerFont

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SagaStoryReaderContent(
    saga: SagaContent,
    onBack: () -> Unit,
) {
    val genre = saga.data.genre
    val acts = saga.acts

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        acts.forEachIndexed { actIndex, act ->
            stickyHeader {
                genre.stylisedText(
                    text = act.data.title,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors =
                                        listOf(
                                            MaterialTheme.colorScheme.background,
                                            Color.Transparent,
                                        ),
                                ),
                            ).padding(16.dp)
                            .genreVfx(genre),
                )
            }

            // Act Introduction
            if (act.data.introduction.isNotEmpty()) {
                item {
                    Text(
                        text = act.data.introduction,
                        style =
                            MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = genre.bodyFont(),
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2f,
                            ),
                        modifier =
                            Modifier
                                .padding(16.dp)
                                .alpha(0.9f),
                    )
                }
            }

            // Chapters in this Act
            itemsIndexed(act.chapters) { chapterIndex, chapter ->
                ChapterContentView(
                    chapter = chapter.toInfo(),
                    content = saga,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    isLast = chapterIndex == act.chapters.size - 1 && act.data.content.isEmpty() && act.data.emotionalReview == null,
                )
            }

            // Act Conclusion/Summary
            if (act.data.content.isNotEmpty()) {
                item {
                    Column(
                        modifier =
                            Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                    ) {
                        Text(
                            text = "Act Conclusion",
                            style =
                                MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = genre.headerFont(),
                                    fontWeight = FontWeight.Bold,
                                ),
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                        Text(
                            text = act.data.content,
                            style =
                                MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = genre.bodyFont(),
                                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2f,
                                ),
                            modifier = Modifier.alpha(0.8f),
                        )
                    }
                }
            }

            // Emotional Review for the Act
            act.data.emotionalReview?.let { review ->
                item {
                    Box(modifier = Modifier.padding(16.dp)) {
                        EmotionalReviewCard(
                            review = review,
                            genre = genre,
                        )
                    }
                }
            }
        }
    }
}
