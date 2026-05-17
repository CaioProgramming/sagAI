package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
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
                .background(fadeGradientTop(MaterialTheme.colorScheme.primary))
                .clickable {
                    onDismiss()
                }.statusBarsPadding()
                .fillMaxWidth()
                .padding(16.dp),
    ) {
        Icon(
            painterResource(genre.icon),
            null,
            sparkModifier.size(32.dp),
            tint = MaterialTheme.colorScheme.onPrimary,
        )

        Text(
            title,
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                ),
        )

        Text(
            subtitle,
            style =
                MaterialTheme.typography.labelMedium.copy(
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                ),
        )
    }
}
