package com.ilustris.sagai.features.live.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.data.model.Character
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

/**
 * The bottom of the live screen: a carousel of the characters the player can speak as, exactly
 * like Instagram's story filters — the one in the center slot *is* the hold-to-talk button.
 * Swiping the carousel changes the speaker; holding the center records; dragging up while
 * holding arms cancel.
 */
@Composable
fun LiveDock(
    speakers: List<Character>,
    selectedSpeakerId: Int?,
    enabled: Boolean,
    canSwitchSpeaker: Boolean,
    listening: Boolean,
    level: () -> Float,
    ringColors: List<Color>,
    onSelectSpeaker: (Int) -> Unit,
    onPressStart: () -> Unit,
    onCancelArmed: (Boolean) -> Unit,
    onPressEnd: (cancel: Boolean) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope,
    sharedKey: String,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val initialPage = speakers.indexOfFirst { it.id == selectedSpeakerId }.coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialPage) { speakers.size }

    LaunchedEffect(pagerState, speakers) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .drop(1)
            .collect { page ->
                speakers.getOrNull(page)?.let {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSelectSpeaker(it.id)
                }
            }
    }
    LaunchedEffect(selectedSpeakerId, speakers) {
        val index = speakers.indexOfFirst { it.id == selectedSpeakerId }
        if (index >= 0 && index != pagerState.currentPage) pagerState.animateScrollToPage(index)
    }

    val selected = speakers.getOrNull(pagerState.currentPage)
    var cancelArmed by remember { mutableStateOf(false) }

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        BoxWithConstraints(Modifier.fillMaxWidth().height(RECORD_SIZE + 12.dp), contentAlignment = Alignment.Center) {
            val side = (maxWidth - AVATAR_SLOT) / 2
            HorizontalPager(
                state = pagerState,
                pageSize = PageSize.Fixed(AVATAR_SLOT),
                contentPadding = PaddingValues(horizontal = side),
                userScrollEnabled = canSwitchSpeaker && !listening,
                modifier = Modifier.fillMaxWidth(),
            ) { page ->
                val character = speakers[page]
                val isCenter = page == pagerState.currentPage
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = character.image,
                        contentDescription = character.name,
                        contentScale = ContentScale.Crop,
                        modifier =
                            Modifier
                                .size(AVATAR_SIZE)
                                .graphicsLayer {
                                    // The center slot is covered by the record button.
                                    alpha = if (isCenter) 0f else 0.55f
                                    scaleX = 0.86f
                                    scaleY = 0.86f
                                }.clip(CircleShape)
                                .pointerInput(page, canSwitchSpeaker) {
                                    if (!canSwitchSpeaker) return@pointerInput
                                    awaitEachGesture {
                                        awaitFirstDown()
                                        scope.launch { pagerState.animateScrollToPage(page) }
                                    }
                                },
                    )
                }
            }

            HoldToTalkButton(
                character = selected,
                enabled = enabled,
                listening = listening,
                cancelArmed = cancelArmed,
                level = level,
                ringColors = ringColors,
                onPressStart = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onPressStart()
                },
                onCancelArmed = { armed ->
                    if (armed != cancelArmed) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    cancelArmed = armed
                    onCancelArmed(armed)
                },
                onPressEnd = { cancel ->
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    cancelArmed = false
                    onPressEnd(cancel)
                },
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                sharedKey = sharedKey,
            )
        }

        Text(
            text = selected?.name.orEmpty(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun HoldToTalkButton(
    character: Character?,
    enabled: Boolean,
    listening: Boolean,
    cancelArmed: Boolean,
    level: () -> Float,
    ringColors: List<Color>,
    onPressStart: () -> Unit,
    onCancelArmed: (Boolean) -> Unit,
    onPressEnd: (cancel: Boolean) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope,
    sharedKey: String,
) {
    val faceScale by animateFloatAsState(
        targetValue = if (listening) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "recFace",
    )
    val ringAlpha by animateFloatAsState(if (enabled) 1f else 0.35f, label = "recRingAlpha")
    val cancelThreshold = 70.dp

    Box(
        Modifier
            .size(RECORD_SIZE)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    onPressStart()
                    var armed = false
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        change.consume()
                        val nowArmed = down.position.y - change.position.y > cancelThreshold.toPx()
                        if (nowArmed != armed) {
                            armed = nowArmed
                            onCancelArmed(armed)
                        }
                        if (!change.pressed) break
                    }
                    onPressEnd(armed)
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        // Thin gradient ring that swells with the mic level while holding.
        Canvas(Modifier.fillMaxSize()) {
            val swell = if (listening) level() * 0.18f else 0f
            val rotation = if (listening) level() * 90f else 0f
            rotate(rotation) {
                drawCircle(
                    brush = Brush.sweepGradient(ringColors + ringColors.take(1)),
                    radius = size.minDimension / 2f * (1f + swell) - 1.5.dp.toPx(),
                    style = Stroke(width = 1.5.dp.toPx()),
                    alpha = if (cancelArmed) 0.3f else ringAlpha,
                )
            }
        }
        with(sharedTransitionScope) {
            AsyncImage(
                model = character?.image,
                contentDescription = character?.name,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .sharedElement(rememberSharedContentState(sharedKey), animatedVisibilityScope)
                        .padding(4.dp)
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = faceScale
                            scaleY = faceScale
                            alpha = if (enabled) 1f else 0.45f
                        }.clip(CircleShape),
            )
        }

        AnimatedVisibility(listening, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.offset(y = -(RECORD_SIZE / 2 + 26.dp))) {
            Text(
                stringResource(if (cancelArmed) R.string.live_release_to_cancel else R.string.live_drag_to_cancel),
                style = MaterialTheme.typography.labelSmall,
                color = if (cancelArmed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            )
        }
    }
}

private val RECORD_SIZE = 86.dp
private val AVATAR_SIZE = 50.dp
private val AVATAR_SLOT = 68.dp
