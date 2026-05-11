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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.toRoman
import com.ilustris.sagai.features.chapter.ui.ChapterContentView
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.actNumber
import com.ilustris.sagai.ui.components.EmotionalCard
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.SagaTopBar
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
                SagaTopBar(
                    act.data.title.ifEmpty {
                        stringResource(
                            R.string.act_title_template,
                            saga.actNumber(act.data).toRoman(),
                        )
                    },
                    emptyString(),
                    genre,
                    onBackClick = onBack,
                    modifier =
                        Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .fillMaxWidth(),
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
                        EmotionalCard(
                            review = review,
                            genre = genre,
                            isExpanded = true,
                        )
                    }
                }
            }
        }
    }
}
