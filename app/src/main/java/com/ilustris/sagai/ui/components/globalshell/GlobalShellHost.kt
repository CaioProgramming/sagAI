package com.ilustris.sagai.ui.components.globalshell

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.debug.DebugImageFallbackService
import com.ilustris.sagai.core.ai.model.ImageType
import com.ilustris.sagai.core.globalshell.BookGenerationWorkEffect
import com.ilustris.sagai.core.globalshell.BookReadyEffect
import com.ilustris.sagai.core.globalshell.ChatGenerationWorkEffect
import com.ilustris.sagai.core.globalshell.GlobalShellEffect
import com.ilustris.sagai.core.globalshell.GlobalShellExpansion
import com.ilustris.sagai.core.globalshell.GlobalShellUiState
import com.ilustris.sagai.core.globalshell.ImageGenerationWorkEffect
import com.ilustris.sagai.core.globalshell.NewChapterEffect
import com.ilustris.sagai.core.globalshell.NewCharacterEffect
import com.ilustris.sagai.core.globalshell.NewMessageEffect
import com.ilustris.sagai.core.globalshell.ReviewReadyEffect
import com.ilustris.sagai.core.navigation.SagaNavigationTracker
import com.ilustris.sagai.features.act.data.model.BookGenerationUiState
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterProfile
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.debug.ui.ManualImageFallbackContent
import com.ilustris.sagai.features.imagegeneration.model.ImageGenerationUiState
import com.ilustris.sagai.features.imagegeneration.model.ImageGenerationUiState.AwaitingManualFallback
import com.ilustris.sagai.features.imagegeneration.model.ImageGenerationUiState.Generating
import com.ilustris.sagai.features.imagegeneration.model.IslandExpansion
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.resolveBackground
import com.ilustris.sagai.features.saga.chat.data.model.ChatGenerationUiState
import com.ilustris.sagai.features.saga.chat.ui.components.ExpressiveText
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.fadeGradientTop
import com.ilustris.sagai.ui.theme.morphingGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer

/**
 * Global island overlay host for top island (notifications/generation work).
 * Bottom islands (chat objectives, advance trigger, home premium upsell) are rendered
 * by their respective screens via [DynamicBottomIsland] for now.
 */
@Composable
fun GlobalShellHost(
    globalState: GlobalShellUiState,
    imageGenState: ImageGenerationUiState,
    bookGenState: BookGenerationUiState,
    chatGenState: Map<Int, ChatGenerationUiState.Generating>,
    sagaNavigationTracker: SagaNavigationTracker,
    debugImageFallbackService: DebugImageFallbackService,
    onImageSetExpansion: (IslandExpansion) -> Unit,
    onImageCancel: () -> Unit,
    onImageDismissReveal: () -> Unit,
    onNavigate: (deepLink: String) -> Unit,
    onDismiss: () -> Unit,
    onSetGlobalExpansion: (GlobalShellExpansion) -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Read-only; establishes recomposition whenever the visible screen changes.
    @Suppress("UNUSED_VARIABLE")
    val currentKey by sagaNavigationTracker.currentKey.collectAsState()

    // Temporarily keep old behavior — just render content.
    // TODO: Implement top island once GlobalShellUiState is updated to support IslandContent.
    content()
}
