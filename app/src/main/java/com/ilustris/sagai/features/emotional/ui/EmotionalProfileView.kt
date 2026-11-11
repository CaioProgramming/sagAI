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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.features.share.domain.model.ShareType
import com.ilustris.sagai.features.share.ui.ShareSheet
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.features.wiki.ui.EmotionalSheet

@Composable
fun EmotionalProfileView(
    sagaId: String,
    onBack: () -> Unit = {},
    viewModel: EmotionalProfileViewModel = hiltViewModel(),
) {
    val saga by viewModel.saga.collectAsStateWithLifecycle()
    var showShare by remember { mutableStateOf(false) }

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
                    painterResource(R.drawable.ic_chevron_left),
                    contentDescription = stringResource(R.string.back_button_description),
                )
            }

            saga?.let {
                IconButton(
                    onClick = { showShare = true },
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .statusBarsPadding()
                            .padding(8.dp)
                            .size(40.dp),
                ) {
                    Icon(
                        painterResource(R.drawable.ic_share),
                        contentDescription = stringResource(R.string.share),
                    )
                }
            }
        }
    }

    ShareSheet(
        saga = saga,
        isVisible = showShare,
        shareType = ShareType.EMOTIONS,
        onDismiss = { showShare = false },
    )
}
