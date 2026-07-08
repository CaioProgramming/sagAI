package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.ui.theme.sagaShape

@Composable
fun ObjectiveOverlay(
    title: String,
    objective: String,
    progress: Float,
    sparkModifier: Modifier = Modifier,
    modifier: Modifier = Modifier,
    applyStatusBarsPadding: Boolean = true,
    onDismiss: () -> Unit,
) {
    val shape = sagaShape() ?: RoundedCornerShape(12.dp)
    val cardColor = MaterialTheme.colorScheme.surfaceContainer
    val resolvedColor = MaterialTheme.colorScheme.primary

    Column(
        modifier =
            modifier
                .padding(16.dp)
                .fillMaxWidth()
                .clickable(onClick = onDismiss),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style =
                MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f),
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            text = objective,
            style =
                MaterialTheme.typography.labelLarge.copy(
                    textAlign = TextAlign.Center,
                ),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
