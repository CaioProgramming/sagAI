package com.ilustris.sagai.features.chapter.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.ilustris.sagai.features.chapter.presentation.ChapterViewModel
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.theme.components.SparkIcon
import com.ilustris.sagai.ui.theme.components.SparkLoader
import com.ilustris.sagai.ui.theme.genresGradient
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.holographicGradient
import kotlin.time.Duration.Companion.seconds

@Composable
fun ChapterView(
    navHostController: NavHostController,
    sagaId: String?,
    viewModel: ChapterViewModel = hiltViewModel(),
) {
    val saga by viewModel.saga.collectAsStateWithLifecycle()

    LaunchedEffect(saga) {
        if (saga == null) {
            viewModel.loadSaga(sagaId)
        }
    }

    AnimatedContent(saga) {
        when (it) {
            null ->
                Box(Modifier.fillMaxSize()) {
                    SparkIcon(
                        Modifier.size(75.dp),
                        brush = gradientAnimation(holographicGradient),
                    )
                }
            else -> ChapterContent(it)
        }
    }
}

@Composable
fun ChapterContent(
    saga: SagaContent,
    viewModel: ChapterViewModel = hiltViewModel(),
) {
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    LazyColumn {
        items(saga.flatChapters().filter { it.isComplete() }) {
            ChapterContentView(
                it.data,
                saga,
                imageSize = 500.dp,
                modifier = Modifier.fillMaxWidth(),
            ) { chapter ->
                viewModel.generateIcon(saga, it)
            }
        }

        item {
            Spacer(Modifier.height(50.dp))
        }
    }

    if (isGenerating) {
        Dialog(
            onDismissRequest = { },
            properties =
                DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                ),
        ) {
            StarryTextPlaceholder(
                modifier = Modifier.fillMaxSize().gradientFill(saga.data.genre.gradient(true)),
            )
        }
    }
}
