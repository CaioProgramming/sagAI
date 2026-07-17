package com.ilustris.sagai.ui.components.island

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.act.data.model.BookGenerationUiState
import com.ilustris.sagai.features.saga.chat.data.model.ChatGenerationUiState
import com.ilustris.sagai.ui.theme.SagAITheme

/**
 * [IslandContent] mappings for the "persistent work" generation sources that currently render
 * through the top slot of `GlobalShellHost`. Their compact form is a simple label + spinner;
 * the streaming `reasoning` lives in [IslandContent.Expanded].
 */

/** Book (Act prose) generation — compact shows the act title, expanded streams the reasoning. */
class BookGenerationIslandContent(
    private val state: BookGenerationUiState.Generating,
) : IslandContent {
    override val compact: CompactIslandData =
        CompactIslandData(
            label = state.actTitle,
            iconRes = state.genre.icon,
            isLoading = true,
            genre = state.genre,
        )

    @Composable
    override fun Expanded(scope: IslandScope) {
        SagAITheme(state.genre) {
            ReasoningExpandedBody(
                text = state.reasoning
                    ?: stringResource(R.string.book_generation_reasoning_placeholder),
            )
        }
    }
}

/** Chat reply generation — compact shows the speaker/saga, expanded streams the reasoning. */
class ChatGenerationIslandContent(
    private val state: ChatGenerationUiState.Generating,
) : IslandContent {
    override val compact: CompactIslandData =
        CompactIslandData(
            label = state.speakerName ?: state.sagaTitle,
            iconRes = state.genre.icon,
            isLoading = true,
            genre = state.genre,
        )

    @Composable
    override fun Expanded(scope: IslandScope) {
        SagAITheme(state.genre) {
            ReasoningExpandedBody(
                text = state.reasoning
                    ?: stringResource(R.string.chat_generation_reasoning_placeholder),
                textAlign = TextAlign.Start,
            )
        }
    }
}

@Composable
private fun ReasoningExpandedBody(
    text: String,
    textAlign: TextAlign = TextAlign.Center,
) {
    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = textAlign,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
