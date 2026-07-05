package com.ilustris.sagai.features.home.ui.components

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.themeBrushColors
import com.ilustris.sagai.ui.theme.themeIcon

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CreateSagaCard(
    modifier: Modifier = Modifier,
    dynamicNewSagaTexts: DynamicSagaPrompt,
    onCreateNewChat: () -> Unit,
) {
    val genre = dynamicNewSagaTexts.genre

    SagAITheme(genre) {
        val genreBrush = Brush.verticalGradient(themeBrushColors())
        val shape = MaterialTheme.shapes.medium

        Row(
            modifier
                .dropShadow(shape) {
                    this.brush = genreBrush
                    radius = 10f
                    spread = 2f
                }.clip(shape)
                .background(MaterialTheme.colorScheme.background, shape)
                .background(fadeGradientBottom(MaterialTheme.colorScheme.primary))
                .clickable {
                    onCreateNewChat()
                }.padding(16.dp)
                .alpha(0.5f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                themeIcon(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier =
                    Modifier.size(12.dp),
            )

            Column(
                modifier =
                    Modifier
                        .weight(1f),
            ) {
                Text(
                    text = dynamicNewSagaTexts.title,
                    style = MaterialTheme.typography.labelLarge,
                    fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1,
                )

                Text(
                    text = dynamicNewSagaTexts.subtitle,
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Light,
                            textAlign = TextAlign.Start,
                            color = MaterialTheme.colorScheme.onPrimary,
                        ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier =
                        Modifier
                            .padding(vertical = 4.dp)
                            .fillMaxWidth()
                            .alpha(.7f),
                )
            }

            Icon(
                painterResource(R.drawable.round_arrow_forward_ios_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(12.dp),
            )
        }
    }
}
