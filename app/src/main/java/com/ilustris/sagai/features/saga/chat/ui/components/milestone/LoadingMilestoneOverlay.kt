package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.ui.animations.genreVfx
import com.ilustris.sagai.ui.components.stylisedText
import com.ilustris.sagai.ui.theme.lighter
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shimmerize
import kotlin.time.Duration.Companion.seconds

@Composable
fun LoadingMilestoneOverlay(
    saga: Saga,
    sparkModifier: Modifier,
    titleModifier: Modifier,
    modifier: Modifier = Modifier,
) {
    val genre = remember { saga.genre }
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
        ) {
            Image(
                painterResource(genre.icon),
                null,
                colorFilter = ColorFilter.tint(genre.resolveColor()),
                modifier =
                    sparkModifier
                        .genreVfx(genre)
                        .padding(8.dp)
                        .size(
                            32.dp,
                        ).reactiveShimmer(
                            true,
                            genre.resolveColor().lighter(.3f).shimmerize(),
                            duration = 4.seconds,
                            targetValue = 150f,
                            repeatMode = RepeatMode.Restart,
                        ),
            )

            genre.stylisedText(
                saga.title,
                modifier = titleModifier.fillMaxWidth(),
            )
        }
    }
}
