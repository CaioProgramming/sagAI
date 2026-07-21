package com.ilustris.sagai.features.emotional.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.features.wiki.ui.EmotionalSheet

@Composable
fun EmotionalProfileView(
    sagaId: String,
    onBack: () -> Unit = {},
    viewModel: EmotionalProfileViewModel = hiltViewModel(),
) {
    val saga by viewModel.saga.collectAsStateWithLifecycle()

    LaunchedEffect(sagaId) {
        sagaId.toIntOrNull()?.let { viewModel.loadEmotionalIcon(it) }
    }

    SagAITheme(genre = saga?.genre) {
        Box(Modifier.fillMaxSize()) {
            saga?.let {
                EmotionalSheet(saga = it, profileViewModel = viewModel)
            }

            IconButton(
                onClick = onBack,
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(8.dp)
                        .size(40.dp),
            ) {
                Icon(
                    painterResource(R.drawable.ic_back_left),
                    contentDescription = stringResource(R.string.back_button_description),
                )
            }
        }
    }
}
