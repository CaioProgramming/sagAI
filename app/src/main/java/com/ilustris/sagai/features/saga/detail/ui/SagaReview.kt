package com.ilustris.sagai.features.saga.detail.ui

import android.view.MotionEvent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.saga.detail.review.presentation.SagaReviewViewModel
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewExperienceFactory
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewLoadingPage
import com.ilustris.sagai.features.share.domain.model.ShareType
import com.ilustris.sagai.features.share.ui.ShareSheet
import com.ilustris.sagai.ui.components.StarryLoader
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
    saga: SagaContent,
    onDismiss: () -> Unit = {},
    viewModel: SagaReviewViewModel = hiltViewModel(),
) {
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val loadingMessage by viewModel.loadingMessage.collectAsStateWithLifecycle()
    val genre = saga.data.genre
    LaunchedEffect(Unit) {
        viewModel.createReview(saga)
    }

    saga.let { sagaContent ->
        val coroutineScope = rememberCoroutineScope()
        val genre = sagaContent.data.genre
        val animatedPages = remember { mutableStateOf(setOf<Int>()) }

        val experience =
            remember(sagaContent) {
                ReviewExperienceFactory.createExperience(sagaContent)
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
                    onDismiss()
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
                                .gradientFill(sagaContent.data.genre.gradient()),
                        )
                    }
                }
            }

            IconButton(
                onClick = {
                    // navigate to next page or move back to first if is on last
                    coroutineScope.launch {
                        val isLastPage = pagerState.currentPage == pages.size - 1
                        if (isLastPage) {
                            onDismiss()
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
                    painter = painterResource(genre.icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }

        shareType?.let {
            ShareSheet(
                sagaContent,
                true,
                it,
                onDismiss = {
                    shareType = null
                },
            )
        }
    }

    StarryLoader(
        isGenerating,
        loadingMessage,
        brushColors = genre.colorPalette(),
    )
}
