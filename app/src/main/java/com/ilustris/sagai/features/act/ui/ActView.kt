package com.ilustris.sagai.features.act.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.chapter.ui.ChapterCardView
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.selectiveHighlight
import com.ilustris.sagai.ui.components.EmotionalCard
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.fadeGradientTop
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.shape
import effectForGenre
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

private val DEFAULT_DELAY = 1.seconds

@Composable
fun ActComponent(
    act: ActContent,
    actCount: Int,
    content: SagaContent,
    modifier: Modifier = Modifier,
) {
    // Animation flags
    var visibleCoversCount by remember {
        mutableIntStateOf(0)
    }

    val chapters = remember { act.chapters }
    val genre = remember { content.data.genre }

    Column(modifier) {
        LazyRow(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items(chapters) {
                ChapterCardView(
                    content,
                    it.data,
                    Modifier.padding(12.dp).clip(genre.shape()).size(100.dp),
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
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
        )

        Text(
            act.data.content,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = content.data.genre.bodyFont(),
                    textAlign = TextAlign.Justify,
                    color = MaterialTheme.colorScheme.onBackground,
                ),
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
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
                modifier = Modifier.padding(16.dp).alpha(.5f),
            )
        }
    }
}
