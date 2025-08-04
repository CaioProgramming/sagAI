package com.ilustris.sagai.features.newsaga.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.characters.ui.components.CharacterSection
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.ui.theme.components.BlurredGlowContainer
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.zoomAnimation
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Composable
fun SagaCard(
    saga: Saga,
    modifier: Modifier,
) {
    val cornerSize = saga.genre.cornerSize()
    var fraction by remember {
        mutableFloatStateOf(0.1f)
    }

    val imageSize by animateFloatAsState(
        fraction,
        tween(easing = EaseIn, durationMillis = 1.seconds.toInt(DurationUnit.MILLISECONDS)),
    )

    val backgroundColor by animateColorAsState(
        if (fraction == 1f) saga.genre.color else MaterialTheme.colorScheme.background,
    )
    BlurredGlowContainer(
        Modifier.padding(16.dp).wrapContentSize(),
        saga.genre.gradient(fraction == 1f),
        blurSigma = 100f,
        shape = RoundedCornerShape(saga.genre.cornerSize()),
    ) {
        Box(
            modifier
                .padding(4.dp)
                .clip(RoundedCornerShape(cornerSize))
                .background(backgroundColor)
                .clipToBounds(),
        ) {
            AsyncImage(
                saga.icon,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onSuccess = {
                    fraction = 1f
                },
                modifier =
                    Modifier
                        .background(backgroundColor)
                        .fillMaxWidth()
                        .fillMaxHeight(imageSize)
                        .zoomAnimation()
                        .clipToBounds(),
            )

            Column(
                modifier =
                    Modifier
                        .background(fadeGradientBottom())
                        .padding(16.dp)
                        .align(Alignment.BottomCenter)
                        .verticalScroll(
                            rememberScrollState(),
                        ),
            ) {
                Text(
                    text = saga.title,
                    style =
                        MaterialTheme.typography.displaySmall.copy(
                            fontFamily = saga.genre.headerFont(),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Normal,
                            brush = saga.genre.gradient(true),
                        ),
                    modifier =
                        Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                )

                CharacterSection(
                    title = "",
                    content = saga.description,
                    genre = saga.genre,
                )
            }
        }
    }
}
