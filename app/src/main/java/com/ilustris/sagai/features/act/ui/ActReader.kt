package com.ilustris.sagai.features.act.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.formatDate
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.chapterNumber
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.selectiveHighlight
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.headerFont
import effectForGenre
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun ActReader(saga: SagaContent) {
    val pagerState = rememberPagerState { saga.acts.size + 1 }
    val genre = saga.data.genre
    HorizontalPager(pagerState) {
        if (it == 0) {
            IntroductionPage(saga)
        } else {
            var isVisible by remember {
                mutableStateOf(false)
            }
            val actPosition = it - 1
            val act = saga.acts[actPosition]
            AnimatedVisibility(
                isVisible,
                modifier = Modifier.fillMaxSize(),
                enter = fadeIn(tween(1500)) + slideInVertically { -it },
                exit = fadeOut(),
            ) {
                ActReadingContent(act, saga)
            }
            LaunchedEffect(Unit) {
                isVisible = true
            }
        }
    }
}

@Composable
fun ActReadingContent(
    act: ActContent,
    sagaContent: SagaContent,
) {
    val genre = remember { sagaContent.data.genre }
    LazyColumn(modifier = Modifier.padding(vertical = 16.dp)) {
        items(act.chapters) {
            val chapterPosition = act.chapters.indexOf(it)
            val shape = RoundedCornerShape(genre.cornerSize())
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                Text(
                    "${sagaContent.chapterNumber(it.data).toRoman()} - ${it.data.title}",
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            fontFamily = genre.headerFont(),
                            textAlign = TextAlign.Start,
                        ),
                    modifier =
                        Modifier
                            .padding(vertical = 12.dp)
                            .fillMaxWidth(),
                )

                AsyncImage(
                    model = it.data.coverImage,
                    contentDescription = it.data.title,
                    placeholder = painterResource(R.drawable.ic_spark),
                    error = painterResource(R.drawable.ic_spark),
                    fallback = painterResource(R.drawable.ic_spark),
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .selectiveColorHighlight(genre.selectiveHighlight())
                            .effectForGenre(genre)
                            .fillMaxWidth()
                            .fillParentMaxHeight(.4f)
                            .clip(shape),
                )

                Text(
                    it.data.overview,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = genre.bodyFont(),
                        ),
                )
            }
        }
        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = .1f),
                    modifier = Modifier.padding(vertical = 12.dp).height(1.dp),
                )

                Text(
                    act.data.content,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = genre.bodyFont(),
                            textAlign = TextAlign.Justify,
                        ),
                )
            }
        }
        item { Spacer(Modifier.height(50.dp)) }
    }
}

@Composable
fun IntroductionPage(saga: SagaContent) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp),
    ) {
        val genre = saga.data.genre

        Text(
            saga.data.createdAt.formatDate(),
            style =
                MaterialTheme.typography.labelMedium.copy(
                    fontFamily = genre.bodyFont(),
                ),
            textAlign = TextAlign.Center,
        )

        Text(
            saga.data.title,
            style =
                MaterialTheme.typography.displaySmall.copy(
                    textAlign = TextAlign.Center,
                    fontFamily = genre.headerFont(),
                    brush = genre.gradient(true),
                ),
        )

        Text(
            saga.data.description,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    textAlign = TextAlign.Justify,
                    fontFamily = genre.bodyFont(),
                ),
        )
    }
}
