package com.ilustris.sagai.features.share.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.ilustris.sagai.features.share.domain.model.ShareType
import com.ilustris.sagai.features.share.presentation.SharePlayViewModel
import com.ilustris.sagai.features.share.ui.card.CollectorShareFrame
import com.ilustris.sagai.features.share.ui.card.EmotionNote

/**
 * The player share: a portrait of the player as a storyteller, across every saga. It belongs to no
 * genre, so the page wears the app's own colours instead of a saga's.
 */
@Composable
fun PlayerShareView(viewModel: SharePlayViewModel = hiltViewModel()) {
    CollectorShareFrame(genre = null, exportName = ShareType.PLAYER.name, viewModel = viewModel) { tilt, captureLayer, shareText ->
        EmotionNote(
            genre = null,
            title = shareText?.title.orEmpty(),
            body = shareText?.text.orEmpty(),
            caption = shareText?.caption.orEmpty(),
            tilt = tilt,
            captureLayer = captureLayer,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
