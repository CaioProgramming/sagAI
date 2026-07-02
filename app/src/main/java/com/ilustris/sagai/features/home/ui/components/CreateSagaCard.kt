package com.ilustris.sagai.features.home.ui.components

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.ui.theme.GradientType
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.iconShape
import com.ilustris.sagai.ui.theme.sagaBrush
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
        val genreBrush = sagaBrush(gradientType = GradientType.VERTICAL)
        Column(
            Modifier
                .clickable {
                    onCreateNewChat()
                },
        ) {
            Row(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(15.dp))
                        .gradientFill(genreBrush)
                        .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val themeShape = genre.iconShape()
                Icon(
                    themeIcon(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier =
                        Modifier.size(50.dp),
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier =
                        Modifier
                            .weight(1f),
                ) {
                    Text(
                        text = dynamicNewSagaTexts.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                    )

                    Text(
                        text = dynamicNewSagaTexts.subtitle,
                        style =
                            MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Start,
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
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = .1f)),
            )
        }
    }
}
