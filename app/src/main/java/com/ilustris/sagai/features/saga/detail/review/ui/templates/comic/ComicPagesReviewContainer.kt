package com.ilustris.sagai.features.saga.detail.review.ui.templates.comic

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.ui.ReviewLoadingIcon
import com.ilustris.sagai.ui.genre.comic.COMIC_INK
import com.ilustris.sagai.ui.genre.comic.ComicTag
import com.ilustris.sagai.ui.genre.comic.LocalComicPlayback
import com.ilustris.sagai.ui.theme.rememberReduceMotion
import com.ilustris.sagai.ui.theme.themeFilter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/** How long a page is left on screen before the pager turns it over by itself. */
private const val AUTO_ADVANCE_MS = 10_000L

private const val TURN_MS = 650

/** The page that turns swings this far around its spine; the face is gone by the time it is edge-on. */
private const val TURN_DEGREES = 90f

private val BAR_HEIGHT = 44.dp

/**
 * Container for [com.ilustris.sagai.features.saga.detail.review.ui.ReviewNavigationStyle.ComicPages]:
 * the review as the pages of a printed comic, cut by [paginate], turned by hand or by an edge tap,
 * and left to turn itself after [AUTO_ADVANCE_MS] on each one.
 *
 * The pager is hand-rolled rather than a `HorizontalPager`: a page turn needs the leaving page to
 * stay drawn *over* the arriving one, and a pager translates its pages and draws them in index
 * order — the opposite of that, on every forward turn.
 */
@Composable
fun ComicPagesReviewContainer(
    pages: List<ReviewPage>,
    hasLoadingSlot: Boolean,
    genre: Genre,
    onEnsureGeneration: () -> Unit,
    onDismiss: () -> Unit,
    onRegenerate: () -> Unit,
) {
    val requests =
        pages.map { page ->
            val comicPage = page as? ComicPanelPage
            PanelRequest(comicPage?.panelSpan ?: PanelSpan.NORMAL, comicPage?.groupKey)
        }
    val sheets = remember(requests) { paginate(requests) }
    val sheetCount = sheets.size + if (hasLoadingSlot) 1 else 0
    val lastIndex = (sheetCount - 1).coerceAtLeast(0)

    LaunchedEffect(Unit) { onEnsureGeneration() }

    val scope = rememberCoroutineScope()
    var width by remember { mutableFloatStateOf(1f) }

    // Fractional page position: 2.3 is page 2 with its turn 30% done. Read in the draw phase only,
    // so a turn redraws the layers instead of recomposing the page.
    var position by remember { mutableFloatStateOf(0f) }

    // The page the reader is on once nothing is moving; content animates in only on this one.
    var settledPage by remember { mutableIntStateOf(0) }
    var dragging by remember { mutableStateOf(false) }
    var turn by remember { mutableStateOf<Job?>(null) }

    fun turnTo(page: Int) {
        val target = page.coerceIn(0, lastIndex)
        turn?.cancel()
        turn =
            scope.launch {
                animate(
                    initialValue = position,
                    targetValue = target.toFloat(),
                    animationSpec = tween(TURN_MS, easing = FastOutSlowInEasing),
                ) { value, _ -> position = value }
                settledPage = target
            }
    }

    // A review that shrinks (regenerated) must not leave the reader past its last page.
    LaunchedEffect(lastIndex) {
        if (settledPage > lastIndex) {
            position = lastIndex.toFloat()
            settledPage = lastIndex
        }
    }

    // Turns a page by itself after a pause; a drag in progress counts as the reader being busy.
    // Stops short of the loading slot — more pages are on the way, and the reader should meet
    // them rather than be carried onto a spinner.
    val advanceLimit = if (hasLoadingSlot) sheets.size - 1 else lastIndex
    LaunchedEffect(settledPage, advanceLimit) {
        if (settledPage >= advanceLimit) return@LaunchedEffect
        while (true) {
            delay(AUTO_ADVANCE_MS)
            if (!dragging && turn?.isActive != true) break
        }
        turnTo(settledPage + 1)
    }

    fun handle(action: ReviewAction) {
        when (action) {
            ReviewAction.Continue -> turnTo(settledPage + 1)
            ReviewAction.Restart -> turnTo(0)
            ReviewAction.Finish -> onDismiss()
            ReviewAction.Regenerate -> {
                onRegenerate()
                turnTo(0)
            }
            is ReviewAction.Navigate ->
                sheets
                    .indexOfFirst { sheet ->
                        (sheet.strips + sheet.panels).any { pages.getOrNull(it)?.pageType == action.pageType }
                    }.takeIf { it >= 0 }
                    ?.let(::turnTo)
            // The review no longer offers sharing from here.
            is ReviewAction.Share -> Unit
        }
    }

    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 36.dp
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + BAR_HEIGHT

    val pageLabel = stringResource(R.string.review_comic_page_indicator, settledPage + 1, sheetCount)
    val nextLabel = stringResource(R.string.review_comic_next_page)
    val previousLabel = stringResource(R.string.review_comic_previous_page)

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .themeFilter()
            .semantics {
                stateDescription = pageLabel
                customActions =
                    listOf(
                        CustomAccessibilityAction(nextLabel) {
                            turnTo(settledPage + 1)
                            true
                        },
                        CustomAccessibilityAction(previousLabel) {
                            turnTo(settledPage - 1)
                            true
                        },
                    )
            }.draggable(
                orientation = Orientation.Horizontal,
                state =
                    rememberDraggableState { delta ->
                        position = (position - delta / width).coerceIn(0f, lastIndex.toFloat())
                    },
                onDragStarted = {
                    turn?.cancel()
                    dragging = true
                },
                onDragStopped = { velocity ->
                    dragging = false
                    // Carries a fling a little further than the finger got, but never past the
                    // neighbouring page: a turn is one page.
                    val projected = position - velocity / width * 0.25f
                    turnTo(projected.roundToInt().coerceIn(settledPage - 1, settledPage + 1))
                },
            ).onSizeChanged { width = it.width.toFloat() },
    ) {
        // Armed-ness is a lambda read in effects, so a page turning does not recompose the
        // content of the pages being turned.
        val lower by remember { derivedStateOf { position.toInt().coerceIn(0, lastIndex) } }

        // Upper page first so the lower one — the one being turned — draws over it.
        listOf(lower + 1, lower).filter { it <= lastIndex }.forEach { index ->
            val sheet = sheets.getOrNull(index)
            key(sheet?.key ?: LOADING_KEY) {
                val armed = remember(index) { { settledPage == index } }
                CompositionLocalProvider(LocalComicPlayback provides armed) {
                    TurningPage(
                        index = index,
                        position = { position },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        if (sheet != null) {
                            ComicSheetView(
                                sheet = sheet,
                                pages = pages,
                                borderColor = MaterialTheme.colorScheme.onBackground,
                                panelBackground = MaterialTheme.colorScheme.surfaceContainer,
                                topInset = if (index == 0) 0.dp else topInset,
                                bottomInset = if (index == 0) 0.dp else bottomInset,
                                modifier = Modifier.fillMaxSize(),
                                onAction = ::handle,
                            )
                        } else {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                ReviewLoadingIcon()
                            }
                        }
                    }
                }
            }
        }

        // Edge taps: the outer third on each side turns the page. The middle is left alone so the
        // back cover's buttons and the panels keep their own taps.
        Row(Modifier.fillMaxSize()) {
            EdgeTap(previousLabel, Modifier.fillMaxHeight().weight(0.28f)) { turnTo(settledPage - 1) }
            Box(Modifier.fillMaxHeight().weight(0.44f))
            EdgeTap(nextLabel, Modifier.fillMaxHeight().weight(0.28f)) { turnTo(settledPage + 1) }
        }

        IconButton(
            onClick = onDismiss,
            modifier =
                Modifier
                    .size(24.dp)
                    .alpha(0.7f)
                    .align(Alignment.TopCenter)
                    .statusBarsPadding(),
        ) {
            Icon(
                painter = painterResource(genre.icon),
                contentDescription = stringResource(R.string.review_comic_close),
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }

        PageIndicator(
            current = settledPage.coerceAtMost(sheets.size - 1).coerceAtLeast(0),
            total = sheets.size,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .height(BAR_HEIGHT),
        )
    }
}

private const val LOADING_KEY = Int.MIN_VALUE

@Composable
private fun TurningPage(
    index: Int,
    position: () -> Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val reduceMotion = rememberReduceMotion()
    val pageColor = MaterialTheme.colorScheme.background
    Box(
        modifier
            .graphicsLayer {
                // How far this page is from being the one on screen: positive once the reader is
                // past it (it is turning away), negative while it waits underneath.
                val away = position() - index
                if (away >= 0.995f) {
                    alpha = 0f
                    return@graphicsLayer
                }
                if (away > 0f) {
                    if (reduceMotion) {
                        alpha = 1f - away
                    } else {
                        // Swings around the spine on its left edge; perspective keeps the far edge
                        // from slicing through the near plane.
                        transformOrigin = TransformOrigin(0f, 0.5f)
                        cameraDistance = 16f * density
                        rotationY = -away * TURN_DEGREES
                    }
                } else {
                    val depth = (1f + away).coerceIn(0f, 1f)
                    val scale = 0.94f + 0.06f * depth
                    scaleX = scale
                    scaleY = scale
                }
            }.drawWithContent {
                drawContent()
                val away = position() - index
                if (away > 0f && away < 1f && !reduceMotion) {
                    // The fold: darkest at the spine, easing out across the turning face.
                    drawRect(
                        Brush.horizontalGradient(
                            listOf(Color.Black.copy(alpha = 0.45f * away), Color.Transparent),
                            startX = 0f,
                            endX = size.width * 0.6f,
                        ),
                    )
                } else if (away < 0f && away > -1f) {
                    // The page underneath, dimmed until the one above is out of the way.
                    drawRect(Color.Black.copy(alpha = 0.4f * -away))
                }
            }.background(pageColor),
    ) {
        content()
    }
}

@Composable
private fun EdgeTap(
    label: String,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ).semantics { contentDescription = label },
    )
}

@Composable
private fun PageIndicator(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    if (total <= 0) return
    Row(
        modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            repeat(total) { index ->
                Box(
                    Modifier
                        .size(width = 14.dp, height = 4.dp)
                        .background(
                            if (index <= current) COMIC_INK else COMIC_INK.copy(alpha = 0.25f),
                        ),
                )
            }
        }
        ComicTag(text = "${current + 1}/$total")
    }
}
