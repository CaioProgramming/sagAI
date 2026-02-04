package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.fadeGradientTop

@Composable
fun ObjectiveOverlay(
    title: String,
    subtitle: String,
    genre: Genre,
    sparkModifier: Modifier,
    onDismiss: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier =
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .background(fadeGradientTop(genre.color))
                .clickable {
                    onDismiss()
                }.statusBarsPadding()
                .fillMaxWidth()
                .padding(16.dp),
    ) {
        Image(
            painterResource(R.drawable.ic_spark),
            null,
            sparkModifier.size(32.dp),
            colorFilter = ColorFilter.tint(genre.iconColor),
        )

        Text(
            title,
            style =
                MaterialTheme.typography.labelLarge.copy(
                    fontFamily = genre.bodyFont(),
                    color = genre.iconColor.copy(alpha = .7f),
                ),
        )

        Text(
            subtitle,
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = genre.bodyFont(),
                    color = genre.iconColor,
                    textAlign = TextAlign.Center,
                ),
        )
    }
}
