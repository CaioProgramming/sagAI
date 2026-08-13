package com.ilustris.sagai.features.home.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shimmerize
import kotlin.time.Duration.Companion.seconds

@Composable
fun PersonalizedHomeHeader(
    dynamicContent: DynamicSagaPrompt?,
    onCreateNewSaga: () -> Unit,
) {
    SagAITheme(dynamicContent?.genre) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
        ) {
            Text(
                text = dynamicContent?.title ?: stringResource(R.string.app_name),
                style =
                    MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = null,
                    ),
                fontWeight = FontWeight.Black,
            )

            Text(
                text = dynamicContent?.subtitle ?: stringResource(R.string.home_prompt_subtitle),
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        shadow = Shadow(MaterialTheme.colorScheme.primary, blurRadius = 10f),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    ),
                modifier =
                    Modifier
                        .clickable {
                            onCreateNewSaga()
                        }.reactiveShimmer(
                            true,
                            duration = 10.seconds,
                            repeatMode = RepeatMode.Restart,
                        ),
            )
        }
    }
}
