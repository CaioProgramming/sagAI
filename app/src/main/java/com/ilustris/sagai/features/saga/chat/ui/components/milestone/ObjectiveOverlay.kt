package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.ui.theme.sagaShape
import com.ilustris.sagai.ui.theme.themeIcon

@Composable
fun ObjectiveOverlay(
    title: String,
    objective: String,
    progress: Float,
    sparkModifier: Modifier = Modifier,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
) {
    val shape = sagaShape() ?: RoundedCornerShape(12.dp)
    val cardColor = MaterialTheme.colorScheme.surfaceContainer
    val resolvedColor = MaterialTheme.colorScheme.primary

    Column(
        modifier =
            modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
                .dropShadow(
                    shape = shape,
                    shadow =
                        Shadow(
                            radius = 4.dp,
                            spread = 1.dp,
                            color = resolvedColor,
                            offset = DpOffset.Zero,
                        ),
                ).clip(shape)
                .background(cardColor, shape)
                .clickable(onClick = onDismiss),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    themeIcon(),
                    contentDescription = null,
                    modifier = sparkModifier.size(22.dp),
                    tint = resolvedColor,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
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
                        MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        LinearProgressIndicator(
            modifier =
                Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .height(3.dp),
            progress = { progress },
            drawStopIndicator = {},
            gapSize = 0.dp,
            color = resolvedColor,
            trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = .1f),
        )
    }
}
