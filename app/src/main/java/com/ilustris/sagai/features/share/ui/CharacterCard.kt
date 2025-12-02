package com.ilustris.sagai.features.share.ui

import android.graphics.Bitmap
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.share.domain.model.ShareText
import com.ilustris.sagai.ui.components.views.DepthLayout
import com.ilustris.sagai.ui.theme.SagaTitle
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.hexToColor
import effectForGenre

@Composable
fun CharacterCard(
    character: CharacterContent,
    sagaContent: SagaContent,
    modifier: Modifier = Modifier,
    segmentedImage: Bitmap? = null,
    originalImage: Bitmap? = null,
    shareText: ShareText? = null,
    showWatermark: Boolean = false
) {
    val genre = remember { sagaContent.data.genre }
    val characterColor = remember { character.data.hexColor.hexToColor() ?: genre.color }

    Box(
        modifier = modifier
    ) {
        if (originalImage != null && segmentedImage != null) {
            DepthLayout(
                originalImage = originalImage,
                segmentedImage = segmentedImage,
                modifier =
                Modifier
                    .fillMaxSize()
                    .clipToBounds(),
                backgroundImageModifier =
                Modifier
                    .blur(1.dp)
                    .effectForGenre(genre),
                foregroundImageModifier = Modifier.effectForGenre(genre),
            ) {
                Text(
                    text = "${character.data.name} ${(character.data.lastName ?: emptyString())}".trim(),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.TopCenter),
                    style =
                    MaterialTheme.typography.displaySmall.copy(
                        fontFamily = genre.headerFont(),
                        textAlign = TextAlign.Center,
                        brush =
                        Brush.verticalGradient(
                            listOf(
                                genre.color,
                                characterColor,
                                genre.iconColor,
                            ),
                        ),
                        shadow = Shadow(genre.color, blurRadius = 15f),
                    ),
                )

            }
            Text(
                shareText?.title ?: emptyString(),
                modifier = Modifier.align(Alignment.Center),
                style =
                MaterialTheme.typography.labelMedium.copy(
                    fontFamily = genre.bodyFont(),
                    color = genre.iconColor,
                    textAlign = TextAlign.Center,
                    shadow =
                    Shadow(
                        genre.color,
                        blurRadius = 10f,
                        offset = Offset(2f, 0f),
                    ),
                ),
            )

        } else {
            AsyncImage(
                character.data.image,
                null,
                modifier =
                Modifier
                    .fillMaxSize()
                    .clipToBounds()
                    .effectForGenre(genre),
                contentScale = ContentScale.Crop,
            )

            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxSize()
                    .background(fadeGradientBottom(genre.color)),
            )

            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    shareText?.title ?: emptyString(),
                    style =
                    MaterialTheme.typography.labelLarge.copy(
                        fontFamily = genre.bodyFont(),
                        color = genre.iconColor,
                        textAlign = TextAlign.Center,
                        shadow =
                        Shadow(
                            genre.color,
                            blurRadius = 10f,
                            offset = Offset(2f, 0f),
                        ),
                    ),
                )

                Text(
                    character.data.name,
                    modifier =
                    Modifier
                        .fillMaxWidth(),
                    style =
                    MaterialTheme.typography.displayMedium.copy(
                        fontFamily = genre.headerFont(),
                        textAlign = TextAlign.Center,
                        brush =
                        Brush.verticalGradient(
                            listOf(
                                genre.color,
                                characterColor,
                                genre.iconColor,
                            ),
                        ),
                        shadow = Shadow(genre.color, blurRadius = 15f),
                    ),
                )

                Text(
                    shareText?.text ?: emptyString(),
                    style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontFamily = genre.bodyFont(),
                        color = genre.iconColor,
                        textAlign = TextAlign.Center,
                        fontStyle = FontStyle.Italic,
                        letterSpacing = 3.sp,
                        shadow =
                        Shadow(
                            genre.color,
                            blurRadius = 5f,
                            offset = Offset(5f, 0f),
                        ),
                    ),
                )
            }
        }
        if (showWatermark) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxSize()
                    .background(fadeGradientBottom(genre.color)),
            )
            Column(Modifier.align(Alignment.BottomCenter)) {
                Image(
                    painter = painterResource(R.drawable.ic_spark),
                    null,
                    modifier =
                    Modifier
                        .size(24.dp)
                        .align(Alignment.CenterHorizontally),
                    colorFilter = ColorFilter.tint(genre.iconColor),
                )

                Text(
                    shareText?.caption ?: emptyString(),
                    style =
                    MaterialTheme.typography.labelMedium.copy(
                        fontFamily = genre.bodyFont(),
                        color = genre.iconColor,
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )

                SagaTitle(
                    textStyle = MaterialTheme.typography.labelMedium,
                    modifier =
                    Modifier
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally),
                )
            }
        }
    }
}
