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
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.sagaBrush
import com.ilustris.sagai.ui.theme.sagaShape

@Composable
fun ObjectiveOverlay(
    title: String,
    objective: String,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
) {

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
                MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            text = objective,
            style =
                MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
