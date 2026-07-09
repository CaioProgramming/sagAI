package com.ilustris.sagai.ui.components.globalshell

import android.graphics.Bitmap
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.debug.DebugImageFallbackService
import com.ilustris.sagai.core.globalshell.BookReadyEffect
import com.ilustris.sagai.core.globalshell.GlobalShellEffect
import com.ilustris.sagai.core.globalshell.GlobalShellExpansion
import com.ilustris.sagai.core.globalshell.GlobalShellPriority
import com.ilustris.sagai.core.globalshell.GlobalShellService
import com.ilustris.sagai.core.globalshell.GlobalShellUiState
import com.ilustris.sagai.core.globalshell.ImageGenerationWorkEffect
import com.ilustris.sagai.core.globalshell.NewChapterEffect
import com.ilustris.sagai.core.globalshell.NewCharacterEffect
import com.ilustris.sagai.core.globalshell.NewMessageEffect
import com.ilustris.sagai.core.globalshell.ReviewReadyEffect
import com.ilustris.sagai.features.debug.ui.ManualImageFallbackContent
import com.ilustris.sagai.features.imagegeneration.ImageGenerationService
import com.ilustris.sagai.features.imagegeneration.model.ImageGenerationUiState
import com.ilustris.sagai.features.imagegeneration.model.ImageGenerationUiState.AwaitingManualFallback
import com.ilustris.sagai.features.imagegeneration.model.ImageGenerationUiState.Generating
import com.ilustris.sagai.features.imagegeneration.model.IslandExpansion
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.resolveBackground
import com.ilustris.sagai.features.saga.chat.ui.components.ExpressiveText
import com.ilustris.sagai.ui.components.taskshell.TaskShellBar
import com.ilustris.sagai.ui.components.taskshell.TaskShellChevron
import com.ilustris.sagai.ui.components.taskshell.TaskShellCompactClick
import com.ilustris.sagai.ui.components.taskshell.TaskShellContent
import com.ilustris.sagai.ui.components.taskshell.TaskShellExpandedBody
import com.ilustris.sagai.ui.components.taskshell.TaskShellExpansion
import com.ilustris.sagai.ui.components.taskshell.TaskShellInnerShape
import com.ilustris.sagai.ui.components.taskshell.TaskShellLayout
import com.ilustris.sagai.ui.components.taskshell.TaskShellOuterShape
import com.ilustris.sagai.ui.components.taskshell.TaskShellScope
import com.ilustris.sagai.ui.components.taskshell.TaskShellSlotState
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.sagaShape
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect

/**
 * Global overlay host that replaces [com.ilustris.sagai.features.imagegeneration.ui.ImageGenerationContainer]
 * and the legacy notification banner/router.
 */
@Composable
fun GlobalShellHost(
    globalState: GlobalShellUiState,
    imageGenState: ImageGenerationUiState,
    debugImageFallbackService: DebugImageFallbackService,
    onImageSetExpansion: (IslandExpansion) -> Unit,
    onImageCancel: () -> Unit,
    onNavigate: (deepLink: String) -> Unit,
    onDismiss: () -> Unit,
    onSetGlobalExpansion: (GlobalShellExpansion) -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isImageGenShellActive =
        imageGenState is ImageGenerationUiState.Generating ||
            imageGenState is ImageGenerationUiState.AwaitingManualFallback

    // If image generation is active (shell), it takes the "sticky work" slot.
    if (isImageGenShellActive) {
        val imageSlotExpansion =
            if (imageGenState is Generating) {
                imageGenState.expansion
            } else if (imageGenState is AwaitingManualFallback) {
                imageGenState.expansion
            } else {
                IslandExpansion.Compact
            }
        val taskShellExpansion =
            if (imageSlotExpansion == IslandExpansion.Expanded) TaskShellExpansion.Expanded else TaskShellExpansion.Collapsed

        val topSlot =
            TaskShellSlotState(
                content =
                    ImageGenerationShellContent(
                        state = imageGenState,
                        debugImageFallbackService = debugImageFallbackService,
                        onImageCancel = onImageCancel,
                        onImageSetExpansion = onImageSetExpansion,
                    ),
                expansion = taskShellExpansion,
                onExpansionChange = { newExpansion ->
                    val islandExpansion =
                        when (newExpansion) {
                            TaskShellExpansion.Collapsed -> IslandExpansion.Compact

                            TaskShellExpansion.Expanded,
                            TaskShellExpansion.Full,
                            -> IslandExpansion.Expanded
                        }
                    onImageSetExpansion(islandExpansion)
                },
            )

        return TaskShellLayout(
            modifier = modifier,
            topSlot = topSlot,
        ) {
            content()
        }
    }

    val effect = globalState.effect
    if (effect == null) {
        Box(modifier = modifier.fillMaxSize()) { content() }
        return
    }

    val taskShellExpansion =
        when (globalState.expansion) {
            GlobalShellExpansion.Collapsed -> TaskShellExpansion.Collapsed

            GlobalShellExpansion.Expanded,
            GlobalShellExpansion.Full,
            -> TaskShellExpansion.Expanded
        }

    val topSlot =
        TaskShellSlotState(
            content = effect.toTaskShellContent(onNavigate, onDismiss),
            expansion = taskShellExpansion,
            onExpansionChange = { newExpansion ->
                val next =
                    when (newExpansion) {
                        TaskShellExpansion.Collapsed -> GlobalShellExpansion.Collapsed

                        TaskShellExpansion.Expanded,
                        TaskShellExpansion.Full,
                        -> GlobalShellExpansion.Expanded
                    }
                onSetGlobalExpansion(next)
            },
        )

    TaskShellLayout(
        modifier = modifier,
        topSlot = topSlot,
    ) {
        content()
    }
}

@Composable
private fun GlobalShellEffect.toTaskShellContent(
    onNavigate: (String) -> Unit,
    onDismiss: () -> Unit,
): TaskShellContent =
    when (this) {
        is NewMessageEffect -> {
            NewMessageShellContent(this, onNavigate, onDismiss)
        }

        is NewChapterEffect,
        is NewCharacterEffect,
        is ReviewReadyEffect,
        is BookReadyEffect,
        -> {
            GenericNotificationShellContent(this, onNavigate, onDismiss)
        }

        is ImageGenerationWorkEffect -> {
            GenericNotificationShellContent(
                this,
                onNavigate,
                onDismiss,
            )
        }
    }

private class ImageGenerationShellContent(
    val state: ImageGenerationUiState,
    val debugImageFallbackService: DebugImageFallbackService,
    val onImageCancel: () -> Unit,
    val onImageSetExpansion: (IslandExpansion) -> Unit,
) : TaskShellContent {
    override val isDraggable: Boolean get() = false
    override val isExpandable: Boolean get() = true

    @Composable
    override fun Compact(scope: TaskShellScope) {
        val isExpanded = scope.expansion != TaskShellExpansion.Collapsed

        TaskShellBar(
            title = imageGenerationTaskTitle(state),
            isExpanded = isExpanded,
            onToggleExpand = {
                if (scope.expansion == TaskShellExpansion.Collapsed) {
                    onImageSetExpansion(IslandExpansion.Expanded)
                } else {
                    onImageSetExpansion(IslandExpansion.Compact)
                }
            },
            onLongClick = onImageCancel,
            trailingContent = {
                val queueBadge =
                    when (state) {
                        is Generating -> state.queuePosition.takeIf { it > 0 }
                        else -> null
                    }
                queueBadge?.let { count ->
                    Text(
                        text = stringResource(R.string.image_generation_queue_badge, count),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
        )
    }

    @Composable
    override fun Expanded(scope: TaskShellScope) {
        TaskShellExpandedBody {
            ImageGenerationPanelBody(
                state = state,
                debugImageFallbackService = debugImageFallbackService,
                onCollapse = { onImageSetExpansion(IslandExpansion.Compact) },
                onCancel = onImageCancel,
            )
        }
    }
}

@Composable
private fun ImageGenerationPanelBody(
    state: ImageGenerationUiState,
    debugImageFallbackService: DebugImageFallbackService,
    onCollapse: () -> Unit,
    onCancel: () -> Unit,
) {
    when (state) {
        is Generating -> {
            val reasoning = state.reasoning
            Text(
                text = reasoning ?: stringResource(R.string.image_generation_reasoning_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Start,
            )
        }

        is AwaitingManualFallback -> {
            if (BuildConfig.DEBUG) {
                ManualImageFallbackContent(
                    prompt = state.prompt,
                    debugImageFallbackService = debugImageFallbackService,
                    onSubmitted = onCollapse,
                    onCancel = onCancel,
                    scrollEnabled = false,
                    autoCopyPrompt = true,
                    showHeader = false,
                )
            }
        }

        else -> {
            Unit
        }
    }
}

@Composable
private fun imageGenerationTaskTitle(state: ImageGenerationUiState): String =
    when (state) {
        is Generating -> stringResource(R.string.image_generation_default_label)
        is AwaitingManualFallback -> stringResource(R.string.image_generation_awaiting_manual)
        else -> ""
    }

private class NewMessageShellContent(
    val effect: NewMessageEffect,
    val onNavigate: (String) -> Unit,
    val onDismiss: () -> Unit,
) : TaskShellContent {
    override val isDraggable: Boolean get() = false
    override val compactClick: TaskShellCompactClick
        get() = TaskShellCompactClick.Toggle

    @Composable
    override fun Compact(scope: TaskShellScope) {
        val isExpanded = scope.expansion != TaskShellExpansion.Collapsed

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable {
                        scope.onToggle()
                    },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ShellAvatar(icon = effect.icon, genre = effect.genre)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.notification_new_message),
                    style =
                        MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                )
                Text(
                    text = effect.speakerName,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
            }

            TaskShellChevron(
                isExpanded = isExpanded,
                onClick = {
                    if (scope.expansion == TaskShellExpansion.Collapsed) scope.onToggle() else scope.onMinimize()
                },
            )
        }
    }

    @Composable
    override fun Expanded(scope: TaskShellScope) {
        TaskShellExpandedBody {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Render rich tags without showing the tag syntax.
                ExpressiveText(
                    text = effect.rawText,
                    genre = effect.genre,
                    style =
                        MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Start,
                        ),
                    modifier = Modifier.fillMaxWidth(),
                    shouldAnimate = false,
                    characters = emptyList(),
                    wiki = emptyList(),
                    mainCharacter = null,
                    onAnnotationClick = { _ -> },
                )

                Button(
                    onClick = {
                        onNavigate(effect.deepLink)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(text = stringResource(R.string.notification_open_chat))
                }
            }
        }
    }
}

private class GenericNotificationShellContent(
    val effect: GlobalShellEffect,
    val onNavigate: (String) -> Unit,
    val onDismiss: () -> Unit,
) : TaskShellContent {
    override val isDraggable: Boolean get() = false

    @Composable
    override fun Compact(scope: TaskShellScope) {
        val isExpanded = scope.expansion != TaskShellExpansion.Collapsed

        SagAITheme(effect.genre) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .combinedClickable(
                            onClick = {
                                if (scope.expansion == TaskShellExpansion.Collapsed) scope.onToggle() else scope.onMinimize()
                            },
                        ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ShellAvatar(icon = effect.icon, genre = effect.genre)

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = genericLabelFor(effect),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    )
                    Text(
                        text = effect.message,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                }

                TaskShellChevron(
                    isExpanded = isExpanded,
                    onClick = {
                        if (scope.expansion == TaskShellExpansion.Collapsed) scope.onToggle() else scope.onMinimize()
                    },
                )
            }
        }
    }

    @Composable
    override fun Expanded(scope: TaskShellScope) {
        TaskShellExpandedBody {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = effect.message,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium,
                        ),
                    modifier = Modifier.fillMaxWidth(),
                )

                if (effect !is ImageGenerationWorkEffect) {
                    Button(
                        onClick = {
                            onNavigate(effect.deepLink)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text(text = stringResource(R.string.notification_open_chat))
                    }
                }
            }
        }
    }

    private fun genericLabelFor(effect: GlobalShellEffect): String =
        when (effect) {
            is NewChapterEffect -> "Novo capítulo"
            is NewCharacterEffect -> "Novo personagem"
            is ImageGenerationWorkEffect -> "Gerando imagem"
            is ReviewReadyEffect -> "Review pronto"
            is BookReadyEffect -> "Livro gerado"
            else -> "Atualização"
        }
}

@Composable
private fun ShellAvatar(
    icon: Bitmap?,
    genre: Genre,
) {
    val fallbackRes =
        genre?.resolveBackground(null) as? Int ?: R.drawable.ic_spark

    if (icon != null) {
        Image(
            bitmap = icon.asImageBitmap(),
            contentDescription = null,
            modifier =
                Modifier
                    .size(40.dp)
                    .clip(CircleShape),
        )
    } else {
        Image(
            painter = painterResource(fallbackRes),
            contentDescription = null,
            modifier =
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp),
        )
    }
}
