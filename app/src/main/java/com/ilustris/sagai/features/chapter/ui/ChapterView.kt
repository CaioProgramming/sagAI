package com.ilustris.sagai.features.chapter.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.ilustris.sagai.features.chapter.presentation.ChapterViewModel
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.ui.theme.components.SagaTopBar
import com.ilustris.sagai.ui.theme.components.SparkIcon
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.holographicGradient

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
fun ChapterContent(saga: SagaContent) {
    val isVisible =
        remember {
            mutableStateOf(true)
        }
    LazyColumn {
        stickyHeader {
            SagaTopBar(
                saga.data.title,
                "${saga.chapters.size} capítulos",
                saga.data.genre,
                modifier =
                    Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .fillMaxWidth()
                        .padding(top = 50.dp, start = 16.dp),
            )
        }

        items(saga.chapters) {
            ChapterContentView(
                saga.data.genre,
                it,
                saga.mainCharacter,
                saga.characters,
                saga.wikis,
                MaterialTheme.colorScheme.onBackground,
                FontStyle.Normal,
                isAnimated = false,
            )
        }
    }
}
