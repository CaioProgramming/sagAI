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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.saga.chat.ui.components.bubble
import com.ilustris.sagai.features.saga.detail.review.ui.DynamicCard
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.chat.BubbleTailAlignment
import com.ilustris.sagai.ui.theme.headerFont
import kotlinx.coroutines.delay

@Composable
fun RecapHeroCard(
    saga: SagaContent,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val stats =
        listOf(
            stringResource(R.string.recap_messages_sent, saga.flatMessages().size),
            stringResource(R.string.recap_characters_found, saga.characters.size),
            stringResource(R.string.recap_chapters_lived, saga.flatChapters().size),
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
        saga.data.genre.bubble(
            tailAlignment = BubbleTailAlignment.BottomRight,
            tailWidth = 0.dp,
            tailHeight = 0.dp,
        )

    val genre = saga.data.genre

    DynamicCard(
        stringResource(R.string.recap_your_journey),
        stats.last(),
        titleStyle =
            MaterialTheme.typography.headlineSmall.copy(
                fontFamily = saga.data.genre.headerFont(),
                color = genre.iconColor,
            ),
        subtitleStyle =
            MaterialTheme.typography.labelMedium.copy(
                fontFamily = saga.data.genre.bodyFont(),
                color = genre.iconColor,
            ),
        lineColor = genre.iconColor,
        modifier =
            modifier
                .background(
                    genre.colorPalette().random(),
                    shape,
                ).clip(shape)
                .clickable {
                    onClick()
                },
    )
}
