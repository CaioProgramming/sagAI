package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.ui.components.stylisedText
import com.ilustris.sagai.ui.theme.reactiveShimmer
import kotlin.time.Duration.Companion.seconds

@Composable
fun TitleSplashOverlay(
    saga: Saga,
    modifier: Modifier = Modifier,
) {
    val genre = saga.genre

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        genre.stylisedText(
            text = saga.title,
            modifier =
                Modifier
                    .padding(horizontal = 32.dp)
                    .fillMaxWidth()
                    .reactiveShimmer(
                        isPlaying = true,
                        duration = 3.seconds,
                        targetValue = 400f,
                    ),
        )
    }
}
