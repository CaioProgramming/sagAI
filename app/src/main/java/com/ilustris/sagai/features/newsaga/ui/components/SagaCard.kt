package com.ilustris.sagai.features.newsaga.ui.components

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

@Composable
fun SagaCard(
    sagaData: SagaData,
    modifier: Modifier,
) {
    val cornerSize = sagaData.genre.cornerSize()
    Box(
        modifier
            .padding(16.dp)
            .border(
                2.dp,
                gradientAnimation(sagaData.genre.color.darkerPalette()),
                RoundedCornerShape(cornerSize),
            ).clip(RoundedCornerShape(cornerSize))
            .clipToBounds(),
    ) {
        AsyncImage(
            sagaData.icon,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .background(sagaData.genre.color)
                    .fillMaxWidth()
                    .fillMaxHeight(.5f)
                    .zoomAnimation()
                    .clipToBounds(),
        )

        Box(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(.5f)
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
                title = "História",
                content = sagaData.description,
                genre = sagaData.genre,
            )
        }
    }
}
