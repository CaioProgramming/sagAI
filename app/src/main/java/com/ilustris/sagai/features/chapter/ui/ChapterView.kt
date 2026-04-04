@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.ilustris.sagai.features.chapter.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.ilustris.sagai.features.chapter.presentation.ChapterViewModel

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
}
