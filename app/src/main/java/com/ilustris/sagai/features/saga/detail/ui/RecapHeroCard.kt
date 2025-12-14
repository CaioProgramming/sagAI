package com.ilustris.sagai.features.saga.detail.ui

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.saga.chat.ui.components.bubble
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.chat.BubbleTailAlignment
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.reactiveShimmer
import effectForGenre
import kotlinx.coroutines.delay

@Composable
fun RecapHeroCard(
    saga: SagaContent,
    modifier: Modifier,
    onClick: () -> Unit,
    originalBitmap: Bitmap? = null,
    segmentedBitmap: Bitmap? = null,
) {
    val stats =
        listOf(
            stringResource(R.string.recap_messages_sent, saga.flatMessages().size),
            stringResource(R.string.recap_characters_found, saga.characters.size),
            stringResource(R.string.recap_chapters_lived, saga.flatChapters().size),
            stringResource(R.string.recap_revisit_now),
        )
    var currentIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2500)
            currentIndex = (currentIndex + 1) % stats.size
        }
    }

    val shape = saga.data.genre.bubble(
        tailAlignment = BubbleTailAlignment.BottomRight,
        tailWidth = 0.dp,
        tailHeight = 0.dp
    )
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(170.dp)
                .dropShadow(
                    shape,
                    Shadow(
                        5.dp,
                        saga.data.genre.gradient(true),
                    ),
                )
                .clip(shape)
                .border(
                    1.dp,
                    saga.data.genre.gradient(),
                    shape,
                )
                .background(
                    Brush.verticalGradient(
                        saga.data.genre.color.darkerPalette(factor = .5f)
                    ), shape
                )


                .clickable {
                    onClick()
                },
    ) {
        if (originalBitmap != null && segmentedBitmap != null) {


            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = stringResource(R.string.recap_your_journey),
                    style =
                        MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = saga.data.genre.headerFont(),
                            textAlign = TextAlign.Start,
                            shadow =
                                androidx.compose.ui.graphics.Shadow(
                                    saga.data.genre.color,
                                    blurRadius = 15f,
                                ),
                        ),
                )

                AnimatedContent(
                    targetState = currentIndex,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(500)) +
                                slideInVertically { it } togetherWith
                                fadeOut(animationSpec = tween(500)) +
                                slideOutVertically { -it }
                    },
                ) { index ->
                    Text(
                        text = stats[index],
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                color = saga.data.genre.iconColor,
                                fontFamily = saga.data.genre.bodyFont(),
                                textAlign = TextAlign.Start,
                            ),
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .alpha(.6f),
                    )
                }
            }

            Image(
                bitmap = segmentedBitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .offset(x = 100.dp)
                        .effectForGenre(saga.data.genre, useFallBack = true)
            )


        } else {
            saga.data.icon.let {
                AsyncImage(
                    it,
                    contentDescription = null,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .effectForGenre(saga.data.genre),
                    contentScale = ContentScale.Crop,
                )
            }

            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            fadeGradientBottom(),
                        ),
            )

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .reactiveShimmer(true)
                        .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = stringResource(R.string.recap_your_journey),
                    style =
                        MaterialTheme.typography.displaySmall.copy(
                            fontFamily = saga.data.genre.headerFont(),
                            brush =
                                saga.data.genre.gradient(),
                            shadow =
                                androidx.compose.ui.graphics.Shadow(
                                    saga.data.genre.color,
                                    blurRadius = 15f,
                                ),
                        ),
                )

                AnimatedContent(
                    targetState = currentIndex,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(500)) + slideInVertically { it } togetherWith
                                fadeOut(animationSpec = tween(500)) + slideOutVertically { -it }
                    },
                ) { index ->
                    Text(
                        text = stats[index],
                        style =
                            MaterialTheme.typography.headlineSmall.copy(
                                color = saga.data.genre.iconColor,
                                fontFamily = saga.data.genre.bodyFont(),
                                textAlign = TextAlign.Center,
                            ),
                        modifier = Modifier.padding(8.dp),
                    )
                }
            }
        }
    }
}