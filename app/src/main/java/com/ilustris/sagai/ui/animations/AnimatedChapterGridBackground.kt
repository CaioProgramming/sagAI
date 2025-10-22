package com.ilustris.sagai.ui.animations

import android.util.Log
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.newsaga.data.model.Genre
import effectForGenre
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Composable
fun AnimatedChapterGridBackground(
    sagaIcon: String?,
    chapters: List<Chapter>,
    genre: Genre,
) {
    if (chapters.isEmpty()) {
        return
    }

    val lazyGridState = rememberLazyGridState()
    val itemHeight = LocalConfiguration.current.screenHeightDp.dp / 2

    var scrollTarget by remember { mutableFloatStateOf(0f) }
    var maxOffset by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        scrollTarget =
            lazyGridState.layoutInfo.visibleItemsInfo
                .last()
                .offset.y
                .toFloat()
    }

    LaunchedEffect(scrollTarget) {
        Log.d("AnimatedGrid", "Scrolling to -> $scrollTarget ")
        Log.d(
            "AnimatedGrid",
            "current offset end -> ${lazyGridState.layoutInfo.visibleItemsInfo.lastOrNull()?.offset?.y?.toFloat()}",
        )
        lazyGridState.animateScrollBy(
            scrollTarget,
            animationSpec =
                tween(
                    10.seconds.toInt(DurationUnit.MILLISECONDS),
                    easing = EaseIn,
                ),
        )

        if (lazyGridState.canScrollForward) {
            if (maxOffset != 0f) {
                scrollTarget = maxOffset
            } else {
                scrollTarget +=
                    lazyGridState.layoutInfo.visibleItemsInfo
                        .last()
                        .offset.y
                        .toFloat()
            }
        } else {
            maxOffset = scrollTarget
            scrollTarget = scrollTarget.unaryMinus()
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = lazyGridState,
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = false,
    ) {
        item {
            AsyncImage(
                model = sagaIcon,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .effectForGenre(genre)
                        .fillMaxWidth()
                        .height(itemHeight),
            )
        }

        items(chapters, key = { it.id }) { chapter ->
            AsyncImage(
                model = chapter.coverImage,
                contentDescription = chapter.title,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_spark),
                error = painterResource(id = R.drawable.ic_spark),
                modifier =
                    Modifier
                        .effectForGenre(genre, useFallBack = true)
                        .fillMaxWidth()
                        .height(itemHeight),
            )
        }
    }
}
