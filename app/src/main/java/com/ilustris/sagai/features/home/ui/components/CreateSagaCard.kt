package com.ilustris.sagai.features.home.ui.components

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.ui.components.AutoResizeText
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.gradientFill
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

        Column(
            modifier
                .fillMaxWidth()
                .clickable {
                    onCreateNewChat()
                }.padding(8.dp)
                .gradientFill(genreBrush),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                themeIcon(),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )

            AutoResizeText(
                text = dynamicNewSagaTexts.title,
                style =
                    MaterialTheme.typography.labelLarge.copy(
                        fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
                        textAlign = TextAlign.Center,
                    ),
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
