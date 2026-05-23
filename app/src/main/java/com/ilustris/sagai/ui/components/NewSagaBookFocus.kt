@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.ilustris.sagai.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.features.newsaga.data.usecase.SagaBook
import com.ilustris.sagai.features.newsaga.ui.presentation.AgenticAction
import com.ilustris.sagai.ui.animations.chromaticAberration
import com.ilustris.sagai.ui.animations.divineAura
import com.ilustris.sagai.ui.theme.levitate

@Composable
fun SharedTransitionScope.NewSagaBookFocus(
    book: SagaBook,
    visualConfig: GenreVisualConfig,
    reasoning: String?,
    isOpened: Boolean,
    isLoading: Boolean,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedContentKey: String,
    lockedCharacter: com.ilustris.sagai.features.characters.data.model.CharacterInfo? = null,
    onToggle: () -> Unit = {},
    onAction: (AgenticAction) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val genre = book.draft.genre

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CosmicBook(
            book = book,
            visualConfig = visualConfig,
            isOpened = isOpened,
            lockedCharacter = lockedCharacter,
            isLoading = isLoading,
            reasoning = null,
            onToggle = onToggle,
            onAction = onAction,
            modifier =
                Modifier
                    .sharedBounds(
                        rememberSharedContentState(key = sharedContentKey),
                        animatedVisibilityScope,
                    )
                    .width(280.dp)
                    .fillMaxHeight(0.5f)
                    .levitate(true)
                    .divineAura()
                    .chromaticAberration(),
        )

        BookGenerationReasoning(
            reasoning = reasoning,
            genre = genre,
            visualConfig = visualConfig,
            modifier = Modifier.padding(top = 20.dp),
        )
    }
}
