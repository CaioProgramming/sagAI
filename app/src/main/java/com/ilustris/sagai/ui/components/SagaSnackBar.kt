package com.ilustris.sagai.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.darker
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.sagaShape

data class SagaSnackBarMessage(
    val message: String,
    val action: Pair<String, () -> Unit>? = null,
)

/** Saga activity payload routed to in-app banner or system notification. */
data class SagaNotificationEvent(
    val sagaId: Int,
    val sagaTitle: String,
    val genre: Genre,
    val message: String,
    val icon: Bitmap? = null,
    val largeIcon: Bitmap? = null,
    val style: NotificationStyle = NotificationStyle.DEFAULT,
)

enum class NotificationStyle {
    DEFAULT,
    CHAT,
    MINIMAL,
}

@Composable
fun SagaSnackBar(
    snackBarMessage: SagaSnackBarMessage?,
    genre: Genre?,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
) {
    val mainColor = MaterialTheme.colorScheme.background
    val contentColor = genre?.iconColor ?: MaterialTheme.colorScheme.onBackground
    val shape = sagaShape() ?: RoundedCornerShape(10.dp)

    AnimatedVisibility(
        visible = snackBarMessage != null,
        modifier = modifier,
        enter = slideInVertically { -it } + fadeIn(tween(500)),
        exit = slideOutVertically { it },
    ) {
        snackBarMessage?.let { snackBar ->
            Row(
                Modifier
                    .dropShadow(
                        shape = sagaShape() ?: RoundedCornerShape(20.dp),
                        shadow =
                            Shadow(
                                radius = 10.dp,
                                spread = 2.dp,
                                color = mainColor.darker(),
                                offset = DpOffset.Zero,
                            ),
                    )
                    .clip(shape)
                    .border(
                        1.dp,
                        genre?.gradient() ?: Brush.verticalGradient(holographicGradient),
                        shape,
                    ).background(mainColor, shape)
                    .fillMaxWidth()
                    .animateContentSize(animationSpec = tween(200, easing = EaseIn))
                    .clickable(enabled = snackBar.action == null) { onDismiss() }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    snackBar.message,
                    style =
                        MaterialTheme.typography.bodySmall.copy(
                            color = contentColor,
                        ),
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.weight(1f),
                )

                snackBar.action?.let { (label, onClick) ->
                    Text(
                        label,
                        style =
                            MaterialTheme.typography.labelMedium.copy(
                                color = contentColor,
                                fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                            ),
                        modifier =
                            Modifier
                                .clip(shape)
                                .clickable {
                                    onClick()
                                    onDismiss()
                                }.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}
