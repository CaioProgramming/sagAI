package com.ilustris.sagai.features.share.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.share.domain.model.ShareType
import com.ilustris.sagai.features.share.presentation.SharePlayViewModel
import com.ilustris.sagai.features.share.ui.card.CollectorShareFrame
import com.ilustris.sagai.features.share.ui.card.EmotionNote

/** The emotional share: a page from the saga's book, with the generated reflection on it. */
@Composable
fun EmotionShareView(
    saga: Saga,
    viewModel: SharePlayViewModel = hiltViewModel(),
) {
    val genre = remember(saga) { saga.genre }

    CollectorShareFrame(genre, ShareType.EMOTIONS.name, viewModel) { tilt, captureLayer, shareText ->
        EmotionNote(
            genre = genre,
            title = shareText?.title.orEmpty(),
            body = shareText?.text.orEmpty(),
            caption = shareText?.caption.orEmpty(),
            tilt = tilt,
            captureLayer = captureLayer,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
