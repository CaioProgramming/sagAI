package com.ilustris.sagai.features.live.ui

import android.Manifest
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.core.permissions.PermissionService
import com.ilustris.sagai.features.act.ui.toRoman
import com.ilustris.sagai.features.home.data.model.subtitleActAndChapterOrdinals
import com.ilustris.sagai.features.live.presentation.LiveCaption
import com.ilustris.sagai.features.live.presentation.LiveConversationViewModel
import com.ilustris.sagai.features.live.presentation.LiveHint
import com.ilustris.sagai.features.live.presentation.LivePhase
import com.ilustris.sagai.features.live.presentation.LiveUiState
import com.ilustris.sagai.features.saga.chat.data.voicing.BlockType
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.theme.hexToColor
import com.ilustris.sagai.ui.theme.themeBrushColors
import java.text.DateFormat
import java.util.Date

/** Shared-element key for the speaking character's avatar: chat input ⇄ hold-to-talk button. */
fun liveSpeakerKey(sagaId: Int) = "live_${sagaId}_speaker"

/**
 * Live conversation: a full screen over the saga's chat. Stars and the genre's wash behind a
 * cosmic blob holding whoever is in focus, captions under it, reactions orbiting it, and the
 * speaker carousel + hold-to-talk button at the bottom.
 */
@Composable
fun LiveConversationView(
    sagaId: Int,
    onBack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: LiveConversationViewModel = hiltViewModel(),
) {
    LaunchedEffect(sagaId) { viewModel.start(sagaId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val levelState = viewModel.level.collectAsStateWithLifecycle()
    val level = remember(levelState) { { levelState.value } }
    val reduceMotion = rememberReduceMotion()

    // Going to the background ends whatever was being recorded or played.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) viewModel.pause() }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val permissionLauncher = PermissionService.rememberPermissionLauncher { }
    LaunchedEffect(state.hint) {
        if (state.hint == LiveHint.PERMISSION_NEEDED) permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    val primary = MaterialTheme.colorScheme.primary
    val brush = themeBrushColors()
    val focusColor =
        state.focus
            ?.character
            ?.hexColor
            ?.hexToColor() ?: primary
    val speakerColor = state.selectedSpeaker?.hexColor?.hexToColor() ?: primary
    val nextColor = state.nextFocus?.let { it.character?.hexColor?.hexToColor() ?: primary }
    val phase = state.phase
    val mode =
        when (phase) {
            LivePhase.Listening -> BlobMode.LISTENING
            LivePhase.Thinking, LivePhase.Voicing -> BlobMode.THINKING
            is LivePhase.Speaking -> BlobMode.SPEAKING
            else -> BlobMode.IDLE
        }
    val palette =
        when (mode) {
            BlobMode.LISTENING -> listOf(speakerColor, primary, speakerColor, brush.lastOrNull() ?: primary)
            BlobMode.SPEAKING -> listOf(focusColor, primary, focusColor, brush.firstOrNull() ?: primary)
            // Voicing leans toward whoever is about to answer.
            else ->
                if (phase == LivePhase.Voicing && nextColor != null) {
                    listOf(focusColor, nextColor, primary, nextColor)
                } else {
                    brush.take(4).ifEmpty { listOf(primary) }
                }
        }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(SPACE),
    ) {
        val blobCenterY = maxHeight * BLOB_CENTER
        val blobSize = maxWidth * 0.85f

        with(animatedVisibilityScope) {
            // Stars first, then the genre wash.
            Box(
                Modifier
                    .fillMaxSize()
                    .animateEnterExit(enter = fadeIn(tween(600)), exit = fadeOut(tween(250))),
            ) {
                StarryTextPlaceholder(Modifier.fillMaxSize())
                Box(
                    Modifier
                        .fillMaxSize()
                        .drawBehind {
                            drawRect(
                                Brush.radialGradient(
                                    0f to primary.copy(alpha = 0.35f),
                                    1f to Color.Transparent,
                                    center = Offset(size.width / 2f, size.height * BLOB_CENTER),
                                    radius = size.width * 0.8f,
                                ),
                            )
                            drawRect(
                                Brush.radialGradient(
                                    0f to (brush.firstOrNull() ?: primary).copy(alpha = 0.3f),
                                    1f to Color.Transparent,
                                    center = Offset(size.width / 2f, size.height * 1.1f),
                                    radius = size.width,
                                ),
                            )
                        },
                )
            }

            // The blob is born from the dock and rises to the center.
            CosmicBlob(
                mode = mode,
                palette = palette,
                focus = state.focus,
                focusColor = focusColor,
                narratorImage = state.saga?.data?.icon,
                level = level,
                showRing = phase == LivePhase.Thinking || phase == LivePhase.Voicing,
                showWave = phase == LivePhase.Speaking(silent = false),
                reduceMotion = reduceMotion,
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = blobCenterY - blobSize / 2)
                        .size(blobSize)
                        .animateEnterExit(
                            enter =
                                fadeIn(tween(500, delayMillis = 150)) +
                                    scaleIn(
                                        spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                                        initialScale = 0.2f,
                                    ) +
                                    slideInVertically(spring(stiffness = Spring.StiffnessLow)) { it },
                            exit = fadeOut(tween(200)) + scaleOut(tween(250), targetScale = 0.4f),
                        ),
            )

            ReactionOrbit(
                reactions = viewModel.reactions,
                center = Offset01(0.5f, BLOB_CENTER),
                orbitRadius = 118.dp,
                reduceMotion = reduceMotion,
            )

            Column(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .animateEnterExit(enter = fadeIn(tween(400, delayMillis = 350)), exit = fadeOut(tween(150))),
            ) {
                TopBar(state, onBack)
                AnimatedVisibility(state.voicesRestingUntil != null) {
                    VoicesRestingBanner(state.voicesRestingUntil)
                }
            }

            Captions(
                state = state,
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = blobCenterY + 104.dp)
                        .padding(horizontal = 28.dp)
                        .animateEnterExit(enter = fadeIn(tween(400, delayMillis = 400)), exit = fadeOut(tween(150))),
                onRetry = viewModel::retry,
            )

            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, SPACE.copy(alpha = 0.95f))))
                    .navigationBarsPadding()
                    .padding(top = 40.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HintLine(state)
                LiveDock(
                    speakers = state.speakers,
                    selectedSpeakerId = state.selectedSpeakerId,
                    enabled = !state.inputBlocked && phase != LivePhase.Thinking && phase != LivePhase.Voicing,
                    canSwitchSpeaker = phase == LivePhase.Idle || phase is LivePhase.Speaking,
                    listening = phase == LivePhase.Listening,
                    level = level,
                    ringColors = brush.ifEmpty { listOf(primary) },
                    onSelectSpeaker = viewModel::selectSpeaker,
                    onPressStart = viewModel::onPressStart,
                    onCancelArmed = viewModel::onCancelArmed,
                    onPressEnd = viewModel::onPressEnd,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    sharedKey = liveSpeakerKey(sagaId),
                )
            }
        }
    }
}

@Composable
private fun TopBar(
    state: LiveUiState,
    onBack: () -> Unit,
) {
    val saga = state.saga
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                painterResource(R.drawable.round_close_24),
                contentDescription = stringResource(R.string.live_close),
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                saga?.data?.title.orEmpty(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
            )
            if (saga != null && !saga.data.isEnded) {
                val (act, chapter) = saga.subtitleActAndChapterOrdinals()
                Text(
                    stringResource(R.string.chat_view_subtitle, act.toRoman(), chapter.toRoman()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
            }
        }
        // Balances the close button so the title stays centered.
        Spacer(Modifier.size(48.dp))
    }
}

@Composable
private fun VoicesRestingBanner(until: Long?) {
    val time = remember(until) { until?.let { DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(it)) }.orEmpty() }
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            stringResource(R.string.live_voices_resting, time),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
            modifier =
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun Captions(
    state: LiveUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.widthIn(max = 420.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val focus = state.focus
        val speakerLabel =
            when {
                focus == null -> ""
                focus.isNarrator -> stringResource(R.string.live_narrator)
                state.caption?.isPlayerLine == true || state.phase == LivePhase.Listening || state.phase == LivePhase.Thinking ->
                    "${focus.character?.name.orEmpty()} · ${stringResource(R.string.live_you)}"
                else -> focus.character?.name.orEmpty()
            }
        AnimatedContent(speakerLabel, transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) }, label = "liveSpeaker") {
            Text(
                it.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.24.em),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            )
        }

        AnimatedVisibility(state.phase == LivePhase.Thinking && !state.reasoning.isNullOrBlank(), enter = fadeIn(), exit = fadeOut()) {
            Shimmering(state.reasoning.orEmpty())
        }

        AnimatedVisibility(state.playerLinePending, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            Text(
                stringResource(R.string.live_transcribing),
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            )
        }

        state.caption?.let { caption ->
            val window = captionWindow(caption)
            AnimatedContent(
                targetState = window,
                transitionSpec = {
                    (fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 3 }) togetherWith
                        (fadeOut(tween(250)) + androidx.compose.animation.slideOutVertically(tween(250)) { -it / 3 })
                },
                contentKey = { it.first },
                label = "liveCaption",
            ) { (_, text) ->
                Text(
                    text,
                    style = MaterialTheme.typography.titleLarge.copy(lineHeight = 30.sp),
                    textAlign = TextAlign.Center,
                )
            }
        }

        if (state.canRetry) {
            TextButton(onClick = onRetry) { Text(stringResource(R.string.live_retry)) }
        }
    }
}

/**
 * At most three blocks around the one being spoken, so the caption stays a few lines tall; the
 * window slides as the voice moves on. Returns (window start, styled text).
 */
@Composable
private fun captionWindow(caption: LiveCaption): Pair<Int, AnnotatedString> {
    val on = MaterialTheme.colorScheme.onBackground
    val blocks = caption.blocks
    val anchor = caption.current.coerceAtLeast(0)
    val start = if (caption.isPlayerLine || caption.current < 0) 0 else (anchor - 1).coerceAtLeast(0)
    val end = (start + 3).coerceAtMost(blocks.size)
    val text =
        buildAnnotatedString {
            blocks.subList(start, end).forEach { block ->
                val color =
                    when {
                        caption.isPlayerLine -> on
                        caption.allDone -> on.copy(alpha = 0.8f)
                        block.index == caption.current -> on
                        caption.current >= 0 && block.index < caption.current -> on.copy(alpha = 0.7f)
                        else -> on.copy(alpha = 0.3f)
                    }
                val quiet = block.type == BlockType.ACTION || block.type == BlockType.THINK
                withStyle(
                    SpanStyle(
                        color = if (quiet) color.copy(alpha = color.alpha * 0.8f) else color,
                        fontStyle = if (quiet) FontStyle.Italic else FontStyle.Normal,
                        fontSize = if (quiet) 16.sp else 21.sp,
                    ),
                ) { append(block.text) }
                append(" ")
            }
        }
    return start to text
}

@Composable
private fun Shimmering(text: String) {
    val transition = rememberInfiniteTransition(label = "reasoningShimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(1200), repeatMode = androidx.compose.animation.core.RepeatMode.Reverse),
        label = "reasoningAlpha",
    )
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
        maxLines = 2,
        modifier = Modifier.graphicsLayer { this.alpha = alpha },
    )
}

@Composable
private fun HintLine(state: LiveUiState) {
    val speaker = state.selectedSpeaker?.name.orEmpty()
    val answering = state.nextFocus?.character?.name ?: stringResource(R.string.live_narrator)
    val text =
        when (state.hint) {
            LiveHint.HOLD_TO_TALK -> stringResource(R.string.live_hold_to_talk, speaker)
            LiveHint.RELEASE_TO_SEND -> stringResource(R.string.live_release_to_send)
            LiveHint.RELEASE_TO_CANCEL -> stringResource(R.string.live_release_to_cancel)
            LiveHint.CANCELLED -> stringResource(R.string.live_cancelled)
            LiveHint.TOO_SHORT -> stringResource(R.string.live_too_short)
            LiveHint.WAITING_REPLY -> stringResource(R.string.live_waiting_reply)
            LiveHint.PREPARING_VOICE -> stringResource(R.string.live_preparing_voice, answering)
            LiveHint.INTERRUPT -> stringResource(R.string.live_interrupt)
            LiveHint.SPEAKING_SILENTLY -> stringResource(R.string.live_speaking_silently)
            LiveHint.REPLY_FAILED -> stringResource(R.string.live_reply_failed)
            LiveHint.GUARDRAIL -> stringResource(R.string.live_guardrail)
            LiveHint.MIC_UNAVAILABLE -> stringResource(R.string.live_mic_unavailable)
            LiveHint.PERMISSION_NEEDED -> stringResource(R.string.live_permission_needed)
            LiveHint.MILESTONE -> stringResource(R.string.live_milestone)
        }
    val warn =
        state.hint in
            setOf(LiveHint.RELEASE_TO_CANCEL, LiveHint.CANCELLED, LiveHint.TOO_SHORT, LiveHint.REPLY_FAILED, LiveHint.MIC_UNAVAILABLE)
    AnimatedContent(text, transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) }, label = "liveHint") {
        Text(
            it,
            style = MaterialTheme.typography.labelMedium,
            color = if (warn) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
    }
}

/** The system's "remove animations" setting: motion slows to a crawl instead of stopping. */
@Composable
private fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}

private val SPACE = Color(0xFF07060D)
private const val BLOB_CENTER = 0.36f
