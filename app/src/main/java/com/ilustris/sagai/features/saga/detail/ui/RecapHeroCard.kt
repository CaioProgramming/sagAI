package com.ilustris.sagai.features.saga.detail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.saga.chat.ui.components.bubble
import com.ilustris.sagai.features.saga.detail.review.ui.DynamicCard
import com.ilustris.sagai.ui.theme.components.chat.BubbleTailAlignment
import com.ilustris.sagai.ui.theme.darkerPalette
import kotlinx.coroutines.delay

@Composable
fun RecapHeroCard(
    saga: Saga,
    chaptersCount: Int,
    charactersCount: Int,
    messagesCount: Int,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val stats =
        listOf(
            stringResource(R.string.recap_messages_sent, messagesCount),
            stringResource(R.string.recap_characters_found, charactersCount),
            stringResource(R.string.recap_chapters_lived, chaptersCount),
            stringResource(R.string.recap_revisit_now),
        )
    var currentIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2500)
            currentIndex = (currentIndex + 1) % stats.size
        }
    }

    val shape =
        saga.genre.bubble(
            tailAlignment = BubbleTailAlignment.BottomRight,
            tailWidth = 0.dp,
            tailHeight = 0.dp,
        )

    saga.genre

    DynamicCard(
        stringResource(R.string.recap_your_journey),
        stats.last(),
        titleStyle =
            MaterialTheme.typography.headlineSmall.copy(
                color = MaterialTheme.colorScheme.onPrimary,
            ),
        subtitleStyle =
            MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.onPrimary,
            ),
        lineColor = MaterialTheme.colorScheme.onPrimary,
        modifier =
            modifier
                .background(
                    Brush.verticalGradient(MaterialTheme.colorScheme.primary.darkerPalette(factor = .3f)),
                    shape,
                ).clip(shape)
                .clickable {
                    onClick()
                },
    )
}
