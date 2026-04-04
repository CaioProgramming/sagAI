package com.ilustris.sagai.features.act.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.chapter.ui.ChapterCardView
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.shape
import kotlin.time.Duration.Companion.seconds

private val DEFAULT_DELAY = 1.seconds

@Composable
fun ActComponent(
    act: ActContent,
    actCount: Int,
    content: SagaContent,
    modifier: Modifier = Modifier,
) {
    // Animation flags

    val chapters = remember { act.chapters }
    val genre = remember { content.data.genre }

    Column(modifier) {
        LazyRow(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items(chapters) {
                ChapterCardView(
                    content.data.genre,
                    it.data,
                    chapters.indexOf(it),
                    Modifier
                        .padding(12.dp)
                        .clip(genre.shape())
                        .size(100.dp),
                    false,
                )
            }
        }

        Text(
            actCount.toRoman(),
            style =
                MaterialTheme.typography.labelMedium.copy(
                    fontFamily = content.data.genre.headerFont(),
                ),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            act.data.title,
            style =
                MaterialTheme.typography.displaySmall.copy(
                    fontFamily = content.data.genre.headerFont(),
                    brush = content.data.genre.gradient(true),
                ),
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
        )

        Text(
            act.data.content,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = content.data.genre.bodyFont(),
                    textAlign = TextAlign.Justify,
                    color = MaterialTheme.colorScheme.onBackground,
                ),
            modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
        )

        if (act.data.emotionalReview?.isNotEmpty() == true) {
            Text(
                act.data.emotionalReview,
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        fontFamily = content.data.genre.bodyFont(),
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                modifier =
                    Modifier
                        .padding(16.dp)
                        .alpha(.5f),
            )
        }
    }
}
