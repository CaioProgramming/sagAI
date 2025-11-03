package com.ilustris.sagai.ui.components

import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.shape

@Composable
fun SagaSnackBar(
    genre: Genre,
    snackBar: SnackBarState,
    onAction: (SnackAction) -> Unit,
) {
    val shape = remember { genre.shape() }
    Row(
        Modifier
            .dropShadow(
                shape = RoundedCornerShape(20.dp),
                shadow =
                    Shadow(
                        radius = 10.dp,
                        spread = 6.dp,
                        color = genre.color,
                        offset = DpOffset.Zero,
                    ),
            ).clip(shape)
            .border(1.dp, genre.color.gradientFade(), shape)
            .background(
                genre.color,
                shape,
            ).fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(200, easing = EaseIn),
            ).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val resource = snackBar.icon
        val colorFilter = if (resource == null) ColorFilter.tint(genre.iconColor) else null
        val shape = if (resource !is Painter) CircleShape else shape

        AsyncImage(
            resource,
            null,
            colorFilter = colorFilter,
            placeholder = painterResource(R.drawable.ic_spark),
            error = painterResource(R.drawable.ic_spark),
            modifier =
                Modifier
                    .size(24.dp)
                    .padding(4.dp)
                    .clip(shape),
        )

        Text(
            snackBar.message,
            style =

                MaterialTheme.typography.bodySmall.copy(
                    color = genre.iconColor,
                ),
            fontFamily = genre.bodyFont(),
            textAlign = TextAlign.Start,
            modifier =
                Modifier
                    .padding(8.dp)
                    .weight(1f),
        )

        snackBar.action?.let { snackAction ->
            Text(
                stringResource(snackAction.actionRes ?: R.string.empty),
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        color = genre.iconColor,
                        fontFamily = genre.bodyFont(),
                    ),
                modifier =
                    Modifier
                        .padding(8.dp)
                        .clip(shape)
                        .clickable {
                            onAction(snackAction)
                        },
            )
        }
    }
}

fun snackBar(
    message: String,
    builder: SnackBarBuilder.() -> Unit = {},
): SnackBarState = SnackBarBuilder(message).apply(builder).build()

class SnackBarBuilder(
    private val message: String,
) {
    var icon: Any? = null
    private var snackAction: SnackAction? = null

    fun build(): SnackBarState = SnackBarState(icon = icon, message = message, action = snackAction)

    fun action(builder: SnackActionBuilder.() -> Unit) {
        snackAction = SnackActionBuilder().apply(builder).action
    }
}

class SnackActionBuilder {
    internal var action: SnackAction? = null
        private set

    fun resendMessage(message: Message) {
        action = SnackAction.ResendMessage(message)
    }

    fun openDetails(data: Any) {
        action = SnackAction.OpenDetails(data)
    }

    fun retryCharacter(description: String) {
        action = SnackAction.RetryCharacter(description)
    }

    fun revaluateSaga() {
        action = SnackAction.RevaluateSaga
    }
}

data class SnackBarState(
    val icon: Any? = null,
    val message: String,
    val action: SnackAction? = null,
)

sealed class SnackAction(
    @StringRes val actionRes: Int? = null,
) {
    data class ResendMessage(
        val message: Message,
    ) : SnackAction(R.string.try_again)

    data class OpenDetails(
        val data: Any,
    ) : SnackAction(R.string.see_more)

    data class RetryCharacter(
        val description: String,
    ) : SnackAction(R.string.try_again)

    data object RevaluateSaga : SnackAction(R.string.try_again)
}
