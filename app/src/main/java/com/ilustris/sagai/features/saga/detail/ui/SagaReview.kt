package com.ilustris.sagai.features.saga.detail.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.selectiveHighlight
import com.ilustris.sagai.features.saga.detail.data.model.Review
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.SparkIcon
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.shaderBackground
import com.mikepenz.hypnoticcanvas.shaderBackground
import effectForGenre
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class ReviewPages {
    INTRO,
    PLAYSTYLE,
    CHARACTERS,
    CHAPTERS,
    CONCLUSION,
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SagaReview(content: SagaContent) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState { ReviewPages.entries.size }
    val color = content.data.genre.color
    var currentProgress by remember { mutableFloatStateOf(0f) }
    var isPlaying by remember { mutableStateOf(true) }
    val animatedProgress by animateFloatAsState(
        targetValue = currentProgress,
        label = "progressAnimation",
        animationSpec = tween(durationMillis = if (isPlaying) 10000 else 0),
    )

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == ReviewPages.entries.size - 1) {
            isPlaying = false
        } else {
            isPlaying = true
            if (isPlaying) {
                currentProgress = 0f
                currentProgress = 1f
            }
        }
    }
    LaunchedEffect(animatedProgress) {
        if (isPlaying && animatedProgress == 1f && pagerState.currentPage < ReviewPages.entries.size - 1) {
            delay(200)
            pagerState.animateScrollToPage(pagerState.currentPage + 1, animationSpec = tween(500))
        }
    }
    val genre = content.data.genre
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .effectForGenre(genre)
                .shaderBackground(
                    genre.shaderBackground(),
                ),
        )
        if (content.data.isEnded.not()) {
            Column(modifier = Modifier.align(Alignment.Center).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                SparkIcon(
                    brush = genre.gradient(true),
                    tint = genre.color.copy(alpha = .4f),
                    modifier =
                        Modifier
                            .size(64.dp)
                            .align(Alignment.CenterHorizontally),
                )

                Text(
                    "Você ainda não está pronto... Volte em breve",
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            fontFamily = genre.bodyFont(),
                            color = genre.iconColor,
                            textAlign = TextAlign.Center,
                        ),
                )
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(vertical = 50.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(onLongPress = {
                            isPlaying = false
                        }, onPress = {
                            awaitRelease()
                            isPlaying = true
                        })
                    },
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp),
                ) {
                    ReviewPages.entries.forEachIndexed { index, _ ->
                        val isSelected = index <= pagerState.currentPage
                        val progress by animateFloatAsState(
                            targetValue = if (isSelected) 1f else 0.5f,
                            animationSpec = tween(500),
                            label = "progressAnimation",
                        )
                        Box(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .padding(horizontal = 2.dp)
                                    .height(5.dp)
                                    .background(
                                        color = color.copy(alpha = progress),
                                        shape = MaterialTheme.shapes.small,
                                    ).clickable {
                                        if (pagerState.currentPage != index) {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(index, animationSpec = tween(500))
                                            }
                                        }
                                    },
                        )
                    }
                }
                HorizontalPager(pagerState, modifier = Modifier.weight(1f)) {
                    val page = ReviewPages.entries[it]
                    when (page) {
                        ReviewPages.INTRO -> ReviewIntroduction(content, content.data.review?.introduction)
                        ReviewPages.PLAYSTYLE -> ReviewIntroduction(content, content.data.review?.playstyle)
                        ReviewPages.CHARACTERS -> ReviewIntroduction(content, content.data.review?.topCharacters)
                        ReviewPages.CHAPTERS -> ReviewIntroduction(content, content.data.review?.chaptersInsight)
                        ReviewPages.CONCLUSION -> ReviewIntroduction(content, content.data.review?.conclusion)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Composable
fun SagaReviewPreview() {
    SagAIScaffold {
        val content =
            SagaContent(
                data =
                    Saga(
                        title = "Preview Saga",
                        review =
                            Review(
                                introduction = "Embark on an epic journey through the captivating world of this saga. Prepare to be immersed in a tale of adventure, mystery, and unforgettable characters.",
                                playstyle = "Experience a dynamic blend of strategic combat and intricate puzzle-solving. Master unique abilities and adapt your tactics to overcome formidable foes and unravel ancient secrets.",
                                topCharacters = "Meet a diverse cast of heroes and villains, each with their own compelling backstories and motivations. From valiant warriors to enigmatic sorcerers, these characters will leave a lasting impression.",
                                chaptersInsight = "Explore breathtaking landscapes and uncover hidden truths as you progress through a series of gripping chapters. Each stage presents new challenges and revelations, keeping you on the edge of your seat.",
                                conclusion = "As the saga reaches its climactic conclusion, prepare for an unforgettable finale that will tie together all the threads of this epic adventure. The fate of the world hangs in the balance.",
                            ),
                    ),
            )
        SagaReview(content)
    }
}

@Composable
fun ReviewIntroduction(
    sagaData: SagaContent,
    text: String?,
) {
    Column(verticalArrangement = Arrangement.Center, modifier = Modifier.padding(16.dp)) {
        val genre = sagaData.data.genre

        Text(
            sagaData.data.title,
            style =
                MaterialTheme.typography.displaySmall.copy(
                    fontFamily = genre.headerFont(),
                    color = genre.iconColor,
                ),
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 12.dp),
        )

        text?.let {
            Text(
                it,
                style =
                    MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = genre.bodyFont(),
                        color = genre.iconColor,
                    ),
            )
        }
    }
}
