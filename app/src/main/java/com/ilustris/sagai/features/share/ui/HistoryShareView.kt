package com.ilustris.sagai.features.share.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.share.domain.model.ShareType
import com.ilustris.sagai.features.share.presentation.SharePlayViewModel
import com.ilustris.sagai.features.share.ui.card.CollectorCard
import com.ilustris.sagai.features.share.ui.card.CollectorShareFrame
import com.ilustris.sagai.features.share.ui.card.drift

/**
 * The saga share: the same collector card as the character's, with the saga cover as the art, the
 * saga title on top and the generated teaser under it.
 */
@Composable
fun HistoryShareView(
    saga: SagaContent,
    viewModel: SharePlayViewModel = hiltViewModel(),
) {
    val genre = remember(saga) { saga.data.genre }

    CollectorShareFrame(genre, ShareType.HISTORY.name, viewModel) { tilt, captureLayer, shareText ->
        CollectorCard(
            genre = genre,
            title = saga.data.title,
            subtitle = shareText?.title,
            number = "#${saga.data.id}",
            body = shareText?.text,
            tilt = tilt,
            captureLayer = captureLayer,
            modifier = Modifier.fillMaxWidth(),
        ) { cardTilt, scale ->
            SagaCoverArt(saga.data.icon, cardTilt, scale)
        }
    }
}

/** The cover is already art-directed, so it is shown as it is, drifting a little with the tilt. */
@Composable
private fun BoxScope.SagaCoverArt(
    imageUrl: String,
    tilt: () -> Offset,
    scale: Float,
) {
    AsyncImage(
        model = imageUrl,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize().drift(tilt, -6f, 1.1f, scale),
    )
}
