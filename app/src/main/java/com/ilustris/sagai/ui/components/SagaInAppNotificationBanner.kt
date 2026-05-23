package com.ilustris.sagai.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.core.notifications.SagaInAppNotification
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.resolveBackground
import com.ilustris.sagai.ui.theme.darker
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.sagaShape

@Composable
fun SagaInAppNotificationBanner(
    notification: SagaInAppNotification?,
    onOpen: (deepLink: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val genre = notification?.genre
    val mainColor = MaterialTheme.colorScheme.surface
    val contentColor = genre?.iconColor ?: MaterialTheme.colorScheme.onSurface
    val shape = sagaShape() ?: MaterialTheme.shapes.medium
    val borderBrush = genre?.gradient() ?: Brush.verticalGradient(holographicGradient)

    AnimatedVisibility(
        visible = notification != null,
        modifier = modifier,
        enter = slideInVertically { -it } + fadeIn(tween(400)),
        exit = slideOutVertically { -it },
    ) {
        notification?.let { item ->
            Row(
                Modifier
                    .dropShadow(
                        shape = shape,
                        shadow =
                            Shadow(
                                radius = 12.dp,
                                spread = 1.dp,
                                color = mainColor.darker(),
                                offset = DpOffset(0.dp, 4.dp),
                            ),
                    )
                    .clip(shape)
                    .border(1.dp, borderBrush, shape)
                    .background(mainColor, shape)
                    .fillMaxWidth()
                    .clickable { onOpen(item.deepLink) }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                NotificationAvatar(item.icon, genre)

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        item.sagaTitle,
                        style =
                            MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = contentColor,
                            ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        item.message,
                        style =
                            MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Text(
                    stringResource(R.string.notification_open_chat),
                    style =
                        MaterialTheme.typography.labelMedium.copy(
                            color = contentColor,
                            fontWeight = FontWeight.Bold,
                        ),
                    modifier =
                        Modifier
                            .clip(shape)
                            .clickable { onOpen(item.deepLink) }
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun NotificationAvatar(
    icon: Bitmap?,
    genre: Genre?,
) {
    val fallbackRes = genre?.resolveBackground(null) as? Int ?: R.drawable.ic_spark
    if (icon != null) {
        Image(
            bitmap = icon.asImageBitmap(),
            contentDescription = null,
            modifier =
                Modifier
                    .size(40.dp)
                    .clip(CircleShape),
        )
    } else {
        Image(
            painter = painterResource(fallbackRes),
            contentDescription = null,
            modifier =
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    .padding(8.dp),
        )
    }
}
