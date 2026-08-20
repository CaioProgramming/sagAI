package com.ilustris.sagai.features.saga.detail.ui

import android.view.MotionEvent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import com.ilustris.sagai.features.saga.detail.review.ui.templates.book.CowboyBurnMarks
import com.ilustris.sagai.features.saga.detail.review.ui.templates.book.HorrorPoliceTapeOverlay
import com.ilustris.sagai.features.saga.detail.review.ui.templates.book.ShinobiInkBlooms
import com.ilustris.sagai.features.saga.detail.review.ui.templates.comic.ComicBoardReviewContainer
import com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal.TerminalBackground
import com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal.TerminalGlitchOverlay
import com.ilustris.sagai.features.share.domain.model.ShareType
import com.ilustris.sagai.features.share.ui.ShareSheet
import com.ilustris.sagai.ui.animations.AutoScrollLazyColumn
import com.ilustris.sagai.ui.animations.chromaticAberration
import com.ilustris.sagai.ui.animations.divineAura
import com.ilustris.sagai.ui.animations.glitch
import com.ilustris.sagai.ui.animations.grunge
import com.ilustris.sagai.ui.animations.inkBleed
import com.ilustris.sagai.ui.animations.ricePaper
import com.ilustris.sagai.ui.animations.vhs
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.levitate
import com.ilustris.sagai.ui.theme.morphingGradient
import com.ilustris.sagai.ui.theme.rememberVectorShape
import com.ilustris.sagai.ui.theme.reviewVfx
import com.ilustris.sagai.ui.theme.themeFilter
import com.ilustris.sagai.ui.theme.themeIconVector
import com.ilustris.sagai.ui.theme.themePainter
import com.ilustris.sagai.ui.theme.themeVfx
import kotlinx.coroutines.delay
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
                ReviewNavigationStyle.VerticalSwipe -> {
                    DefaultReviewContainer(
                        pages = pages,
                        hasLoadingSlot = hasLoadingSlot,
                        genre = genre,
                        onEnsureGeneration = onEnsureGeneration,
                        onDismiss = onDismiss,
                        onShare = onShare,
                        onRegenerate = onRegenerate,
                    )
                }

                ReviewNavigationStyle.TerminalSwipe -> {
                    TerminalReviewContainer(
                        pages = pages,
                        hasLoadingSlot = hasLoadingSlot,
                        genre = genre,
                        onEnsureGeneration = onEnsureGeneration,
                        onDismiss = onDismiss,
                        onShare = onShare,
                        onRegenerate = onRegenerate,
                    )
                }

                ReviewNavigationStyle.HorizontalPageFlip -> {
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

                ReviewNavigationStyle.ContinuousScroll -> {
                    ContinuousScrollReviewContainer(
                        pages = pages,
                        hasLoadingSlot = hasLoadingSlot,
                        genre = genre,
                        onEnsureGeneration = onEnsureGeneration,
                        onDismiss = onDismiss,
                        onShare = onShare,
                        onRegenerate = onRegenerate,
                    )
                }

                ReviewNavigationStyle.ChatScroll -> {
                    ChatScrollReviewContainer(
                        pages = pages,
                        hasLoadingSlot = hasLoadingSlot,
                        genre = genre,
                        onEnsureGeneration = onEnsureGeneration,
                        onDismiss = onDismiss,
                        onShare = onShare,
                        onRegenerate = onRegenerate,
                    )
                }

                ReviewNavigationStyle.ComicBoard -> {
                    ComicBoardReviewContainer(
                        pages = pages,
                        genre = genre,
                        onEnsureGeneration = onEnsureGeneration,
                        onDismiss = onDismiss,
                        onShare = onShare,
                        onRegenerate = onRegenerate,
                    )
                }
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

            ReviewAction.Finish -> {
                onDismiss()
            }

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

            is ReviewAction.Share -> {
                onShare(action.shareType)
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

            ReviewAction.Finish -> {
                onDismiss()
            }

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

            is ReviewAction.Share -> {
                onShare(action.shareType)
            }
        }
    }

    fun advanceOrFinish() {
        coroutineScope.launch {
            if (isLastPage) onDismiss() else handleAction(ReviewAction.Continue)
        }
    }

    Box(modifier = Modifier.fillMaxSize().themeFilter()) {
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
                contentAlignment = Alignment.Center,
            ) {
                val isLoadingPage = pageIndex >= pages.size
                if (isLoadingPage) {
                    ReviewLoadingIcon()
                } else if (pagerState.currentPage == pageIndex) {
                    pages.getOrNull(pageIndex)?.Show(
                        modifier = Modifier.fillMaxSize().reviewVfx(),
                        canAnimate = true,
                    ) {
                        coroutineScope.launch { handleAction(it) }
                    }
                }
            }
        }

        ReviewSkipButton(genre) { advanceOrFinish() }

        StoryProgressIndicator(
            progress = (pagerState.currentPage + 1).toFloat() / pageCount.toFloat(),
            color = MaterialTheme.colorScheme.primary,
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(32.dp)
                    .fillMaxWidth(),
        )

        Row(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
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
            ReviewAction.Continue -> {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            }

            ReviewAction.Finish -> {
                onDismiss()
            }

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

            is ReviewAction.Share -> {
                onShare(action.shareType)
            }
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

/**
 * Hands-free continuous scroll, like reading a newspaper article — no swipe/pager, the list
 * drifts on its own via [AutoScrollLazyColumn]. Sections are stacked at their own natural height
 * (not one-per-screen) so the "page" reads as one continuous flow, and the background is drawn
 * once for the whole scroll surface instead of per section — every [ReviewPage] here is expected
 * to size itself to content (`fillMaxWidth`, never `fillMaxSize`) since the list gives items an
 * unbounded height. Reuses the same [ReviewAction] contract as the other three containers, just
 * driven by [LazyListState] instead of `PagerState`.
 */
@Composable
private fun ContinuousScrollReviewContainer(
    pages: List<ReviewPage>,
    hasLoadingSlot: Boolean,
    genre: Genre,
    onEnsureGeneration: () -> Unit,
    onDismiss: () -> Unit,
    onShare: (ShareType) -> Unit,
    onRegenerate: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val itemCount = pages.size + if (hasLoadingSlot) 1 else 0

    LaunchedEffect(listState, pages.size, hasLoadingSlot) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo
                .lastOrNull()
                ?.index
        }.collect { lastVisibleIndex ->
            if (hasLoadingSlot && lastVisibleIndex != null && lastVisibleIndex >= (pages.size - 1).coerceAtLeast(0)) {
                onEnsureGeneration()
            }
        }
    }

    suspend fun handleAction(action: ReviewAction) {
        when (action) {
            ReviewAction.Continue -> {
                val current = listState.firstVisibleItemIndex
                listState.animateScrollToItem((current + 1).coerceAtMost(itemCount - 1))
            }

            ReviewAction.Finish -> {
                onDismiss()
            }

            ReviewAction.Restart -> {
                listState.animateScrollToItem(0)
            }

            ReviewAction.Regenerate -> {
                onRegenerate()
                listState.animateScrollToItem(0)
            }

            is ReviewAction.Navigate -> {
                val pageIndex = pages.indexOfFirst { it.pageType == action.pageType }
                if (pageIndex != -1) {
                    listState.animateScrollToItem(pageIndex)
                }
            }

            is ReviewAction.Share -> {
                onShare(action.shareType)
            }
        }
    }

    Box(Modifier.fillMaxSize().themeFilter()) {
        pages.firstOrNull()?.Background(modifier = Modifier.fillMaxSize())

        // Both are no-ops outside their own genre (checked internally). Same ambient-loop
        // pacing, independent of scroll — keeps breathing even while the reader holds still.
        ShinobiInkBlooms(modifier = Modifier.fillMaxSize())
        CowboyBurnMarks(modifier = Modifier.fillMaxSize())

        AutoScrollLazyColumn(
            state = listState,
            modifier = Modifier.background(MaterialTheme.colorScheme.background).fillMaxSize(),
        ) {
            items(pages.size) { pageIndex ->
                pages[pageIndex].Show(
                    modifier = Modifier.fillMaxWidth(),
                    canAnimate = true,
                ) {
                    coroutineScope.launch { handleAction(it) }
                }
            }

            if (hasLoadingSlot) {
                item {
                    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        ReviewLoadingIcon()
                    }
                }
            }
        }

        // Unlike the two background layers above, this one is drawn AFTER (on top of) the
        // scroll content on purpose — a real overlay, like the scene has already been taped off,
        // not a texture living behind the page.
        HorrorPoliceTapeOverlay(modifier = Modifier.fillMaxSize())

        ReviewSkipButton(genre) {
            coroutineScope.launch {
                val current = listState.firstVisibleItemIndex
                val isLastItem = current >= itemCount - 1
                if (isLastItem) {
                    onDismiss()
                } else {
                    listState.animateScrollToItem(current + 1)
                }
            }
        }
    }
}

/**
 * A simulated live chat — Crime's iMessage-style template. Unlike [ContinuousScrollReviewContainer]
 * (everything laid out up front, the list drifts hands-free through it), messages here don't exist
 * in the list until revealed: [revealedCount] ticks up on a timer, and every tick forces a scroll
 * to the newest message, so it reads as watching a conversation arrive rather than reading an
 * already-finished page. The user can still scroll up freely to re-read; the next reveal just pulls
 * the view back down. Reuses the same [ReviewAction] contract as the other four containers.
 */
@Composable
private fun ChatScrollReviewContainer(
    pages: List<ReviewPage>,
    hasLoadingSlot: Boolean,
    genre: Genre,
    onEnsureGeneration: () -> Unit,
    onDismiss: () -> Unit,
    onShare: (ShareType) -> Unit,
    onRegenerate: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val itemCount = pages.size + if (hasLoadingSlot) 1 else 0

    var revealedCount by remember(pages.size) { mutableStateOf(0) }
    // Bumped by Restart/Regenerate so the reveal LaunchedEffect below — a coroutine that runs to
    // completion once all pages are revealed — restarts instead of staying dead after resetting
    // revealedCount back to 0.
    var restartTrigger by remember { mutableStateOf(0) }

    // Items scrolled far enough away get dropped from the LazyColumn's composition entirely; if
    // the reader scrolls back to one, it composes fresh and would otherwise play its pop-in/
    // typewriter again — several at once if several items re-enter view together. Indices in here
    // have already played their entrance once, so they render already-settled on any later
    // (re)composition instead of replaying.
    val animatedIndices = remember(restartTrigger) { mutableStateSetOf<Int>() }

    // Waits for whichever message is currently the last revealed one to actually finish its own
    // entrance animation (typing included, for a text bubble) before pausing another 2s and
    // moving on — a flat delay here regardless of message length was why everything felt rushed:
    // a long message's typewriter was still mid-sentence when the next bubble already popped in.
    suspend fun delayBeforeReveal(nextIndex: Int) {
        if (nextIndex == 0) {
            delay(400)
            return
        }
        val settleTime = pages[nextIndex - 1].estimatedRevealDurationMs.takeIf { it > 0 } ?: 800L
        delay(settleTime + 2000)
    }

    // Fire-and-forget: a user's manual drag preempts a running animateScrollToItem (LazyListState
    // only allows one scroll "owner" at a time), which throws a CancellationException at that
    // suspend call. Awaiting it directly inside the reveal loop below let that exception cancel
    // the *whole* coroutine — reveals just stopped forever the moment someone touched the list,
    // since nothing re-launches a LaunchedEffect whose keys never changed. Launching the scroll
    // in its own child coroutine means only that scroll gets cancelled; revealedCount keeps
    // progressing regardless of what the reader's finger is doing.
    fun scrollToLatest() {
        coroutineScope.launch { runCatching { listState.animateScrollToItem(0) } }
    }

    LaunchedEffect(pages.size, hasLoadingSlot, restartTrigger) {
        if (revealedCount >= pages.size) return@LaunchedEffect
        while (revealedCount < pages.size) {
            delayBeforeReveal(revealedCount)
            revealedCount++
            scrollToLatest()
        }
        if (hasLoadingSlot) {
            onEnsureGeneration()
        }
    }

    fun revealNext(): Boolean {
        if (revealedCount >= pages.size) return false
        revealedCount++
        scrollToLatest()
        return true
    }

    suspend fun handleAction(action: ReviewAction) {
        when (action) {
            ReviewAction.Continue -> {
                if (!revealNext()) {
                    scrollToLatest()
                }
            }

            ReviewAction.Finish -> {
                onDismiss()
            }

            ReviewAction.Restart -> {
                revealedCount = 0
                restartTrigger++
            }

            ReviewAction.Regenerate -> {
                onRegenerate()
                revealedCount = 0
                restartTrigger++
            }

            is ReviewAction.Navigate -> {
                val targetPageIndex = pages.indexOfFirst { it.pageType == action.pageType }
                if (targetPageIndex != -1) {
                    // Position 0 is always the newest revealed page (see the reversed `items`
                    // block below) — position = how many *newer* revealed pages sit after it.
                    val clampedPageIndex = targetPageIndex.coerceAtMost(revealedCount - 1).coerceAtLeast(0)
                    val position = (revealedCount - 1 - clampedPageIndex).coerceAtLeast(0)
                    coroutineScope.launch { runCatching { listState.animateScrollToItem(position) } }
                }
            }

            is ReviewAction.Share -> {
                onShare(action.shareType)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        pages.firstOrNull()?.Background(modifier = Modifier.fillMaxSize())

        // The skip icon lives at TopCenter + statusBarsPadding() below, floating over the list —
        // without matching top content padding here, the first bubble scrolls right up under it.
        val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            // reverseLayout so the newest bubble anchors at the *bottom* of the screen with
            // older context still visible above it — same convention the real in-game chat
            // (ChatView.kt) already uses. Before this, a freshly revealed bubble scrolled to the
            // *top* of the viewport (LazyColumn's default, non-reversed alignment), which dumped
            // everything above it out of view and left it typing alone against empty space below.
            reverseLayout = true,
            // The bottom edge is now where the actively-typing bubble lives — extra room here
            // (beyond just the nav-bar inset) is the breathing space a literal trailing Spacer
            // item would otherwise give it; a real item would also land at position 0 some of the
            // time under reverseLayout, breaking the "position 0 = newest" invariant below.
            contentPadding = PaddingValues(top = topInset + 56.dp, bottom = bottomInset + 96.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Declared first so it lands at index 0 — under reverseLayout that's the *bottom*
            // (newest) position, right after the last real message, not above everything like
            // declaring it after the items() block below would (declaration order still equals
            // index order; reverseLayout only mirrors how that index sequence is placed on
            // screen).
            if (hasLoadingSlot && revealedCount >= pages.size) {
                item(key = "loading") {
                    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        ReviewLoadingIcon()
                    }
                }
            }

            // Rendered newest-first (position 0 = pages[revealedCount - 1]) so reverseLayout
            // places the newest bubble at the bottom and "scroll to latest" is always just
            // `animateScrollToItem(0)` — no index arithmetic needed at the call site. Keyed by
            // the actual page index (not position `i`), since `i`'s mapping to a page shifts by
            // one every time a new page is revealed — without this key, Compose would treat that
            // shift as "a different item now occupies this slot" and could scramble each page's
            // remembered animation state across positions.
            items(
                count = revealedCount,
                key = { i -> revealedCount - 1 - i },
            ) { i ->
                val pageIndex = revealedCount - 1 - i
                // Captured once, NOT re-read reactively: `animatedIndices` is a
                // SnapshotStateSet, so a plain `pageIndex in animatedIndices` re-evaluates on
                // every recomposition — including the one the LaunchedEffect below itself
                // triggers a frame after first composing. That raced canAnimate from true to
                // false before any entrance animation got to run, so everything (typewriter,
                // pop-in, even the title card's handwriting) skipped straight to its settled
                // state. `remember` with no keys caches the decision from the *first*
                // composition only, immune to that self-triggered recomposition.
                val canAnimateThisItem = remember { pageIndex !in animatedIndices }
                LaunchedEffect(pageIndex) {
                    animatedIndices += pageIndex
                }
                pages[pageIndex].Show(
                    modifier = Modifier.fillMaxWidth(),
                    canAnimate = canAnimateThisItem,
                ) {
                    coroutineScope.launch { handleAction(it) }
                }
            }
        }

        ReviewSkipButton(genre) {
            coroutineScope.launch {
                if (!revealNext()) {
                    val isLastItem = revealedCount >= itemCount
                    if (isLastItem) {
                        onDismiss()
                    }
                }
            }
        }
    }
}
