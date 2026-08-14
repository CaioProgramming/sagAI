package com.ilustris.sagai.features.saga.detail.ui

import android.view.MotionEvent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.detail.data.model.isComplete
import com.ilustris.sagai.features.saga.detail.review.presentation.SagaReviewViewModel
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewExperienceFactory
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewNavigationStyle
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.StoryProgressIndicator
import com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal.TerminalBackground
import com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal.TerminalGlitchOverlay
import com.ilustris.sagai.features.share.domain.model.ShareType
import com.ilustris.sagai.features.share.ui.ShareSheet
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.levitate
import com.ilustris.sagai.ui.theme.morphingGradient
import com.ilustris.sagai.ui.theme.rememberVectorShape
import com.ilustris.sagai.ui.theme.themeIconVector
import com.ilustris.sagai.ui.theme.themePainter
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.time.Duration.Companion.seconds

@OptIn(
    ExperimentalComposeUiApi::class,
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
)
@Composable
fun SagaReview(
    saga: Saga,
    onDismiss: () -> Unit = {},
    viewModel: SagaReviewViewModel = hiltViewModel(),
) {
    val sagaContent by viewModel.sagaContent.collectAsStateWithLifecycle()
    val genre = saga.genre

    LaunchedEffect(saga.id) {
        viewModel.loadSaga(saga.id)
    }

    sagaContent?.let { currentContent ->
        val review = currentContent.data.review
        val experience =
            remember(review) {
                ReviewExperienceFactory.createExperience(currentContent)
            }

        val pages = experience.pages
        val hasPendingSteps = !review.isComplete()
        val hasLoadingSlot = hasPendingSteps

        var shareType by remember { mutableStateOf<ShareType?>(null) }

        if (pages.isNotEmpty() || hasLoadingSlot) {
            val onEnsureGeneration = { viewModel.ensureGeneration(saga.id) }
            val onShare = { type: ShareType -> shareType = type }
            val onRegenerate = { viewModel.resetReview(currentContent) }

            when (experience.navigationStyle) {
                ReviewNavigationStyle.VerticalSwipe ->
                    DefaultReviewContainer(
                        pages = pages,
                        hasLoadingSlot = hasLoadingSlot,
                        genre = genre,
                        onEnsureGeneration = onEnsureGeneration,
                        onDismiss = onDismiss,
                        onShare = onShare,
                        onRegenerate = onRegenerate,
                    )

                ReviewNavigationStyle.TerminalSwipe ->
                    TerminalReviewContainer(
                        pages = pages,
                        hasLoadingSlot = hasLoadingSlot,
                        genre = genre,
                        onEnsureGeneration = onEnsureGeneration,
                        onDismiss = onDismiss,
                        onShare = onShare,
                        onRegenerate = onRegenerate,
                    )

                ReviewNavigationStyle.HorizontalPageFlip ->
                    BookReviewContainer(
                        pages = pages,
                        hasLoadingSlot = hasLoadingSlot,
                        genre = genre,
                        onEnsureGeneration = onEnsureGeneration,
                        onDismiss = onDismiss,
                        onShare = onShare,
                        onRegenerate = onRegenerate,
                    )
            }
        }

        shareType?.let {
            ShareSheet(
                currentContent.data,
                true,
                it,
                onDismiss = {
                    shareType = null
                },
            )
        }
    }
}

/** The story-morph icon shown while a pending review step is still generating. */
@Composable
private fun ReviewLoadingIcon(modifier: Modifier = Modifier) {
    val brush = Brush.verticalGradient(morphingGradient(duration = 5.seconds))
    Icon(
        themePainter(),
        null,
        modifier
            .size(100.dp)
            .levitate()
            .dropShadow(rememberVectorShape(themeIconVector())) {
                this.brush = brush
                radius = 20f
                spread = 1f
            }.gradientFill(brush),
        tint = MaterialTheme.colorScheme.primary,
    )
}

/** The small top-center icon that advances a page, or dismisses the review on the last one. */
@Composable
private fun BoxScope.ReviewSkipButton(
    genre: Genre,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier =
            Modifier
                .size(24.dp)
                .alpha(.6f)
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

/** Today's Instagram/Spotify-Wrapped style vertical swipe — used by every genre without a template. */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
private fun DefaultReviewContainer(
    pages: List<ReviewPage>,
    hasLoadingSlot: Boolean,
    genre: Genre,
    onEnsureGeneration: () -> Unit,
    onDismiss: () -> Unit,
    onShare: (ShareType) -> Unit,
    onRegenerate: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val animatedPages = remember { mutableStateOf(setOf<Int>()) }
    val pageCount = pages.size + if (hasLoadingSlot) 1 else 0
    val pagerState = rememberPagerState { pageCount.coerceAtLeast(1) }

    LaunchedEffect(pagerState.currentPage, pages.size, hasLoadingSlot) {
        if (hasLoadingSlot && pagerState.currentPage >= (pages.size - 1).coerceAtLeast(0)) {
            onEnsureGeneration()
        }
    }

    var paused by remember { mutableStateOf(false) }

    suspend fun handleAction(action: ReviewAction) {
        when (action) {
            ReviewAction.Continue -> {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            }

            ReviewAction.Finish -> onDismiss()

            ReviewAction.Restart -> {
                pagerState.animateScrollToPage(0)
            }

            ReviewAction.Regenerate -> {
                onRegenerate()
                pagerState.animateScrollToPage(0)
            }

            is ReviewAction.Navigate -> {
                val pageIndex = pages.indexOfFirst { it.pageType == action.pageType }
                if (pageIndex != -1) {
                    pagerState.animateScrollToPage(pageIndex)
                }
            }

            is ReviewAction.Share -> onShare(action.shareType)
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
        if (pagerState.currentPage < pages.size) {
            pages
                .getOrNull(pagerState.currentPage)
                ?.Background(modifier = Modifier.fillMaxSize())
        }

        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { pageIndex ->
            val isLoadingPage = pageIndex >= pages.size

            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                if (isLoadingPage) {
                    ReviewLoadingIcon()
                } else if (pagerState.currentPage == pageIndex) {
                    val canAnimate =
                        pageIndex == 0 || !animatedPages.value.contains(pageIndex)
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
                            .gradientFill(genre.gradient()),
                    )
                }
            }
        }

        ReviewSkipButton(genre) {
            coroutineScope.launch {
                val isLastPage = pagerState.currentPage == pageCount - 1
                if (isLastPage) {
                    onDismiss()
                } else {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            }
        }
    }
}

/**
 * Cyberpunk's terminal template: a real [VerticalPager] (same mechanics as
 * [DefaultReviewContainer]) dressed as a command line — each swipe reads as
 * running the next "command". Reuses the same [ReviewAction] contract.
 */
@Composable
private fun TerminalReviewContainer(
    pages: List<ReviewPage>,
    hasLoadingSlot: Boolean,
    genre: Genre,
    onEnsureGeneration: () -> Unit,
    onDismiss: () -> Unit,
    onShare: (ShareType) -> Unit,
    onRegenerate: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val pageCount = pages.size + if (hasLoadingSlot) 1 else 0
    val pagerState = rememberPagerState { pageCount.coerceAtLeast(1) }
    val isLastPage = pagerState.currentPage == pageCount - 1

    LaunchedEffect(pagerState.currentPage, pages.size, hasLoadingSlot) {
        if (hasLoadingSlot && pagerState.currentPage >= (pages.size - 1).coerceAtLeast(0)) {
            onEnsureGeneration()
        }
    }

    suspend fun handleAction(action: ReviewAction) {
        when (action) {
            ReviewAction.Continue -> {
                pagerState.animateScrollToPage((pagerState.currentPage + 1).coerceAtMost(pageCount - 1))
            }

            ReviewAction.Finish -> onDismiss()

            ReviewAction.Restart -> {
                pagerState.animateScrollToPage(0)
            }

            ReviewAction.Regenerate -> {
                onRegenerate()
                pagerState.animateScrollToPage(0)
            }

            is ReviewAction.Navigate -> {
                val pageIndex = pages.indexOfFirst { it.pageType == action.pageType }
                if (pageIndex != -1) {
                    pagerState.animateScrollToPage(pageIndex)
                }
            }

            is ReviewAction.Share -> onShare(action.shareType)
        }
    }

    fun advanceOrFinish() {
        coroutineScope.launch {
            if (isLastPage) onDismiss() else handleAction(ReviewAction.Continue)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (pagerState.currentPage < pages.size) {
            pages
                .getOrNull(pagerState.currentPage)
                ?.Background(modifier = Modifier.fillMaxSize())
        } else {
            TerminalBackground(Modifier.fillMaxSize())
        }

        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { pageIndex ->
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                val isLoadingPage = pageIndex >= pages.size
                if (isLoadingPage) {
                    ReviewLoadingIcon()
                } else if (pagerState.currentPage == pageIndex) {
                    pages.getOrNull(pageIndex)?.Show(
                        modifier = Modifier.fillMaxSize(),
                        canAnimate = true,
                    ) {
                        coroutineScope.launch { handleAction(it) }
                    }
                }
            }
        }

        TerminalGlitchOverlay(modifier = Modifier.fillMaxSize())

        ReviewSkipButton(genre) { advanceOrFinish() }

        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StoryProgressIndicator(
                progress = (pagerState.currentPage + 1).toFloat() / pageCount.toFloat(),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = { onShare(ShareType.REVIEW_ACTIVITY) }) {
                    Text(
                        "[ ${stringResource(R.string.share)} ]",
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    )
                }

                TextButton(onClick = { advanceOrFinish() }) {
                    Text(
                        "[ ${stringResource(R.string.next)}_ ]",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

/**
 * Fantasy's storybook template: a [HorizontalPager] with a 3D page-turn transform
 * instead of a flat swipe. Reuses the same page-flip recipe used by the Act reader
 * (see BookReader.kt's cameraDistance/rotationY graphicsLayer block).
 */
@Composable
private fun BookReviewContainer(
    pages: List<ReviewPage>,
    hasLoadingSlot: Boolean,
    genre: Genre,
    onEnsureGeneration: () -> Unit,
    onDismiss: () -> Unit,
    onShare: (ShareType) -> Unit,
    onRegenerate: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val pageCount = pages.size + if (hasLoadingSlot) 1 else 0
    val pagerState = rememberPagerState { pageCount.coerceAtLeast(1) }

    LaunchedEffect(pagerState.currentPage, pages.size, hasLoadingSlot) {
        if (hasLoadingSlot && pagerState.currentPage >= (pages.size - 1).coerceAtLeast(0)) {
            onEnsureGeneration()
        }
    }

    suspend fun handleAction(action: ReviewAction) {
        when (action) {
            ReviewAction.Continue -> pagerState.animateScrollToPage(pagerState.currentPage + 1)
            ReviewAction.Finish -> onDismiss()
            ReviewAction.Restart -> pagerState.animateScrollToPage(0)
            ReviewAction.Regenerate -> {
                onRegenerate()
                pagerState.animateScrollToPage(0)
            }

            is ReviewAction.Navigate -> {
                val pageIndex = pages.indexOfFirst { it.pageType == action.pageType }
                if (pageIndex != -1) {
                    pagerState.animateScrollToPage(pageIndex)
                }
            }

            is ReviewAction.Share -> onShare(action.shareType)
        }
    }

    Box(Modifier.fillMaxSize()) {
        if (pagerState.currentPage < pages.size) {
            pages.getOrNull(pagerState.currentPage)?.Background(modifier = Modifier.fillMaxSize())
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { pageIndex ->
            val pageOffset =
                ((pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction)
                    .coerceIn(-1f, 1f)

            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        cameraDistance = 15 * density
                        rotationY = pageOffset * -35f
                        val scale = 1f - (abs(pageOffset) * 0.08f)
                        scaleX = scale
                        scaleY = scale
                        alpha = (1f - abs(pageOffset)).coerceAtLeast(0.5f)
                        transformOrigin =
                            if (pageOffset > 0) {
                                TransformOrigin(0f, 0.5f)
                            } else {
                                TransformOrigin(1f, 0.5f)
                            }
                    },
                contentAlignment = Alignment.Center,
            ) {
                val isLoadingPage = pageIndex >= pages.size
                if (isLoadingPage) {
                    ReviewLoadingIcon()
                } else if (pagerState.currentPage == pageIndex) {
                    pages.getOrNull(pageIndex)?.Show(
                        modifier = Modifier.fillMaxSize(),
                        canAnimate = true,
                    ) {
                        coroutineScope.launch { handleAction(it) }
                    }
                }
            }
        }

        ReviewSkipButton(genre) {
            coroutineScope.launch {
                val isLastPage = pagerState.currentPage == pageCount - 1
                if (isLastPage) {
                    onDismiss()
                } else {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            }
        }
    }
}
