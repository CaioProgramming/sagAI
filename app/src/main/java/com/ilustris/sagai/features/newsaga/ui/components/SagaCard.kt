package com.ilustris.sagai.features.newsaga.ui.components

import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.ui.theme.GradientType
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.zoomAnimation
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Composable
fun SagaCard(
    sagaData: SagaData,
    modifier: Modifier,
) {
    val cornerSize = sagaData.genre.cornerSize()
    var fraction by remember {
        mutableFloatStateOf(01f)
    }

    val imageSize by animateFloatAsState(
        fraction,
        tween(easing = EaseIn, durationMillis = 1.seconds.toInt(DurationUnit.MILLISECONDS)),
    )
    Box(
        modifier
            .padding(4.dp)
            .border(
                2.dp,
                gradientAnimation(sagaData.genre.color.darkerPalette()),
                RoundedCornerShape(cornerSize),
            ).clip(RoundedCornerShape(cornerSize))
            .background(sagaData.genre.color)
            .clipToBounds(),
    ) {
        AsyncImage(
            sagaData.icon,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            onSuccess = {
                fraction = .5f
            },
            modifier =
                Modifier
                    .background(sagaData.genre.color)
                    .fillMaxWidth()
                    .fillMaxHeight(imageSize)
                    .zoomAnimation()
                    .clipToBounds(),
        )

        Box(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(imageSize)
                .background(
                    fadeGradientBottom(),
                ),
        )

        Column(
            modifier =
                Modifier.padding(16.dp).align(Alignment.Center).verticalScroll(
                    rememberScrollState(),
                ),
        ) {
            Text(
                text = sagaData.title,
                style =
                    MaterialTheme.typography.displaySmall.copy(
                        fontFamily = sagaData.genre.headerFont(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Normal,
                        brush =
                            gradientAnimation(
                                sagaData.genre.color.darkerPalette(),
                                gradientType = GradientType.VERTICAL,
                            ),
                    ),
                modifier =
                    Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
            )

            CharacterSection(
                title = "",
                content = sagaData.description,
                genre = sagaData.genre,
            )
        }
    }
}
