package com.ilustris.sagai.features.home.ui.components

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.iridescentGradient
import com.ilustris.sagai.ui.theme.solidGradient

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CreateSagaCard(
    modifier: Modifier = Modifier,
    dynamicNewSagaTexts: DynamicSagaPrompt?,
    isLoadingDynamicPrompts: Boolean,
    onCreateNewChat: () -> Unit,
) {
    val shape = MaterialTheme.shapes.large
    Column(
        modifier =
            modifier
                .dropShadow(shape) {
                    brush = Brush.verticalGradient(iridescentGradient)
                    radius = 10f
                    spread = 5f
                }.border(1.dp, MaterialTheme.colorScheme.onBackground.gradientFade(), shape)
                .background(Brush.verticalGradient(iridescentGradient), shape)
                .clip(shape)
                .fillMaxWidth()
                .clickable { onCreateNewChat() }
                .padding(8.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .gradientFill(Color.Black.solidGradient()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // The Forge Portal Icon
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier.size(50.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_spark),
                    contentDescription = null,
                    modifier =
                        Modifier
                            .size(24.dp),
                    tint = Color.Unspecified,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text =
                        (
                            dynamicNewSagaTexts?.title
                                ?: stringResource(R.string.home_create_new_saga_title)
                        ).uppercase(),
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                        ),
                )

                Text(
                    text =
                        dynamicNewSagaTexts?.subtitle
                            ?: stringResource(R.string.home_create_new_saga_subtitle),
                    style =
                        MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                        ),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private fun Float.toSp() =
    androidx.compose.ui.unit
        .TextUnit(this, androidx.compose.ui.unit.TextUnitType.Sp)
