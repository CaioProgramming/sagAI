package com.ilustris.sagai.features.audiobook.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.features.act.ui.BookReaderState
import com.ilustris.sagai.features.act.ui.BookReaderViewModel

/**
 * Its own navigation destination — deliberately not an overlay on top of [com.ilustris.sagai.features.act.ui.BookReaderView],
 * so it gets its own [BookReaderViewModel]/[AudiobookViewModel] scope and never leaves the reader
 * composed (and ghosting) underneath it.
 */
@Composable
fun AudiobookPlayerRoute(
    sagaId: Int,
    actId: Int,
    onBack: () -> Unit,
    readerViewModel: BookReaderViewModel = hiltViewModel(),
    audiobookViewModel: AudiobookViewModel = hiltViewModel(),
) {
    val state by readerViewModel.state.collectAsStateWithLifecycle()
    val audiobook by audiobookViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(sagaId, actId) { readerViewModel.load(sagaId, actId) }

    val readyState = state as? BookReaderState.Ready
    LaunchedEffect(readyState?.currentAct?.data?.id) {
        readyState?.let { audiobookViewModel.bind(it.saga, it.currentAct) }
    }

    if (readyState == null) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        return
    }

    AudiobookPlayerView(
        saga = readyState.saga,
        act = readyState.currentAct,
        pages = readyState.pages,
        audiobook = audiobook?.takeIf { it.bookId == readyState.currentAct.book?.id },
        onAction = audiobookViewModel::onAction,
        onSeekToFraction = { fraction ->
            val duration = audiobook?.durationMs ?: 0L
            audiobookViewModel.seekToSectionMs((fraction * duration).toLong())
        },
        onSeekBy = audiobookViewModel::seekBy,
        onBack = onBack,
    )
}
