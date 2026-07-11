package com.ilustris.sagai.ui.theme.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.components.AutoResizeText
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shimmerize
import com.ilustris.sagai.ui.theme.themeBrushColors

@Composable
fun SagaTopBar(
    title: String,
    subtitle: String = emptyString(),
    genre: Genre?,
    actionContent: (@Composable () -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier,
    isLoading: Boolean = false,
    titleModifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        onBackClick?.let {
            Icon(
                painterResource(R.drawable.ic_back_left),
                tint = MaterialTheme.colorScheme.onBackground,
                contentDescription = stringResource(R.string.back_button_description),
                modifier =
                    Modifier
                        .clip(CircleShape)
                        .clickable {
                            onBackClick()
                        }.size(24.dp)
                        .padding(4.dp),
            )
        }

        Column(
            modifier =
                Modifier
                    .reactiveShimmer(
                        isLoading,
                        repeatMode = RepeatMode.Restart,
                        shimmerColors = Color.White.shimmerize(),
                    ).padding(horizontal = 8.dp)
                    .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AutoResizeText(
                title,
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                        brush = Brush.verticalGradient(themeBrushColors()),
                        textAlign = TextAlign.Center,
                    ),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .then(titleModifier),
            )

            if (subtitle.isNotEmpty()) {
                Text(
                    subtitle,
                    style =
                        MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Light,
                            textAlign = TextAlign.Center,
                        ),
                    modifier = Modifier.alpha(.4f),
                )
            }
        }

        actionContent?.invoke()
    }
}
