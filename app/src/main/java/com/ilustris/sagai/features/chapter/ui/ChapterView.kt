@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.ilustris.sagai.features.chapter.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.features.chapter.presentation.ChapterViewModel
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.ui.components.SectionLoading

@Composable
fun ChapterView(
    sagaId: String,
    onBack: () -> Unit = {},
    viewModel: ChapterViewModel = hiltViewModel(),
) {
    val saga by viewModel.saga.collectAsStateWithLifecycle()

    BackHandler {
        onBack()
    }

    LaunchedEffect(Unit) {
        viewModel.loadSaga(sagaId)
    }

    Box(
        Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        AnimatedContent(
            saga,
            transitionSpec = {
                fadeIn(tween(500)) togetherWith fadeOut(tween(400))
            },
            label = "SagaChaptersContent",
        ) { sagaContent ->
            if (sagaContent != null) {
                val chapters = sagaContent.flatChapters()
                ChaptersGalleryContent(
                    title = stringResource(R.string.saga_detail_section_title_chapters),
                    subtitle =
                        stringResource(
                            R.string.saga_detail_section_subtitle_chapters,
                            chapters.size,
                        ),
                    saga = sagaContent,
                    chapters = chapters,
                    onBackClick = onBack,
                )
            } else {
                SectionLoading()
            }
        }
    }
}
