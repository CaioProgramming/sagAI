package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.shimmerColors
import com.ilustris.sagai.ui.theme.reactiveShimmer

@Composable
fun LoadingMilestoneOverlay(
    genre: Genre,
    sparkModifier: Modifier,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painterResource(R.drawable.ic_spark),
            null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.background),
            modifier =
                sparkModifier
                    .size(
                        50.dp,
                    ).reactiveShimmer(
                        true,
                        genre.shimmerColors(),
                        repeatMode = RepeatMode.Restart,
                        targetValue = 1000f,
                    ),
        )
    }
}
