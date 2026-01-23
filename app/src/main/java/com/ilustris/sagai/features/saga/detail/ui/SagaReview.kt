package com.ilustris.sagai.features.saga.detail.ui

import android.view.MotionEvent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewExperienceFactory
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewLoadingPage
import com.ilustris.sagai.features.share.domain.model.ShareType
import com.ilustris.sagai.features.share.ui.ShareSheet
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFill
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalComposeUiApi::class,
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
)
@Composable
fun SagaReview(
    content: SagaContent,
    generatingReview: Boolean = false,
    requestDismiss: () -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    val genre = content.data.genre
    val animatedPages = remember { mutableStateOf(setOf<Int>()) }

    val experience =
        remember(content) {
            ReviewExperienceFactory.createExperience(content)
        }

    val pages = experience.pages
    val pagerState = rememberPagerState { pages.size }

    // Indicator logic
    var paused by remember { mutableStateOf(false) }
    var shareType by remember { mutableStateOf<ShareType?>(null) }

    suspend fun handleAction(action: ReviewAction) {
        when (action) {
            ReviewAction.Continue -> {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            }

            ReviewAction.Finish -> {
                requestDismiss()
            }

            ReviewAction.Restart -> {
                pagerState.animateScrollToPage(0)
            }

            is ReviewAction.Navigate -> {
                val pageIndex = pages.indexOfFirst { it.pageType == action.pageType }
                if (pageIndex != -1) {
                    pagerState.animateScrollToPage(pageIndex)
                }
            }

            is ReviewAction.Share -> {
                shareType = action.shareType
            }
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .pointerInteropFilter { event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> paused = true
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> paused = false
                    }
                    false
                },
    ) {
        pages.getOrNull(pagerState.currentPage)?.Background(modifier = Modifier.fillMaxSize())

        if (generatingReview) {
            ReviewLoadingPage(
                genre = genre,
                sagaTitle = content.data.title,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { pageIndex ->
                val canAnimate = pageIndex == 0 || !animatedPages.value.contains(pageIndex)

                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    if (pagerState.currentPage == pageIndex) {
                        pages.getOrNull(pageIndex)?.Show(
                            modifier = Modifier.fillMaxSize(),
                            canAnimate = canAnimate,
                        ) {
                            coroutineScope.launch {
                                handleAction(it)
                            }
                        }
                    } else {
                        Image(
                            painterResource(R.drawable.ic_spark),
                            null,
                            Modifier
                                .size(50.dp)
                                .gradientFill(content.data.genre.gradient()),
                        )
                    }
                }
            }
        }

        IconButton(
            onClick = {
                // navigate to next page or move back to first if is on last
                coroutineScope.launch {
                    val isLastPage = pagerState.currentPage == pages.size - 1
                    if (isLastPage) {
                        requestDismiss()
                    } else {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            },
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding(),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_spark),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
    }

    shareType?.let {
        ShareSheet(
            content,
            true,
            it,
            onDismiss = {
                shareType = null
            },
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Composable
fun SagaReviewPreview() {
    com.ilustris.sagai.ui.theme.SagAIScaffold {
        val content =
            SagaContent(
                data =
                    com.ilustris.sagai.features.home.data.model.Saga(
                        title = "Preview Saga",
                        isEnded = true,
                        genre = com.ilustris.sagai.features.newsaga.data.model.Genre.SHINOBI,
                        review =
                            com.ilustris.sagai.features.saga.detail.data.model.Review(
                                introduction =
                                    com.ilustris.sagai.features.saga.detail.data.model.ReviewStage(
                                        hook =
                                            com.ilustris.sagai.features.saga.detail.data.model.ReviewText(
                                                "The journey begins...",
                                                "A saga of shadows and light.",
                                            ),
                                        content =
                                            com.ilustris.sagai.features.saga.detail.data.model.ReviewText(
                                                "The Observer is watching.",
                                                "And he has a lot to say.",
                                            ),
                                    ),
                                playstyle =
                                    com.ilustris.sagai.features.saga.detail.data.model.ReviewStage(
                                        hook =
                                            com.ilustris.sagai.features.saga.detail.data.model.ReviewText(
                                                "Silent as the wind.",
                                                "Your blade moved with purpose.",
                                            ),
                                        content =
                                            com.ilustris.sagai.features.saga.detail.data.model.ReviewText(
                                                "Ninja Style.",
                                                "Maximum stealth, minimum mercy.",
                                            ),
                                    ),
                                conclusion =
                                    com.ilustris.sagai.features.saga.detail.data.model.ReviewStage(
                                        hook =
                                            com.ilustris.sagai.features.saga.detail.data.model.ReviewText(
                                                "The path ends here.",
                                                "But the legend lives on.",
                                            ),
                                        content =
                                            com.ilustris.sagai.features.saga.detail.data.model.ReviewText(
                                                "See you next time.",
                                                "The stars always remember.",
                                            ),
                                    ),
                            ),
                    ),
                acts = emptyList(),
            )
        SagaReview(content)
    }
}
