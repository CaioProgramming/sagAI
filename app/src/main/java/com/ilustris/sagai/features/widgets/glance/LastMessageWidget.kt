package com.ilustris.sagai.features.widgets.glance

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import com.ilustris.sagai.R
import com.ilustris.sagai.features.widgets.domain.LastMessageData
import com.ilustris.sagai.features.widgets.domain.WidgetCells
import com.ilustris.sagai.features.widgets.domain.WidgetInvite
import com.ilustris.sagai.features.widgets.domain.WidgetLinks
import com.ilustris.sagai.features.widgets.domain.WidgetPalette
import com.ilustris.sagai.features.widgets.domain.WidgetSizes
import com.ilustris.sagai.features.widgets.domain.WidgetTheme
import com.ilustris.sagai.features.widgets.domain.bestFit

sealed interface LastMessageUi {
    data class Message(
        val data: LastMessageData.Message,
        val avatar: Bitmap?,
        val theme: WidgetTheme,
    ) : LastMessageUi

    data class Invite(
        val invite: WidgetInvite,
        val theme: WidgetTheme,
    ) : LastMessageUi
}

/** The last line of the saga's chat, as written, with who said it. No AI involved. */
class LastMessageWidget : DataGlanceWidget<LastMessageUi>() {
    override val sizeMode = SizeMode.Responsive(WidgetSizes.LAST_MESSAGE.toDpSizes())

    override suspend fun load(
        context: Context,
        id: GlanceId,
    ): LastMessageUi {
        val source = context.widgetEntryPoint().dataSource()
        return when (val data = source.loadLastMessage()) {
            is LastMessageData.Message -> {
                val portrait = data.portraitPath ?: data.saga.iconPath
                LastMessageUi.Message(
                    data = data,
                    avatar = WidgetBitmaps.decode(portrait, AVATAR_SOURCE_PX),
                    theme = source.theme(data.saga.genre, false),
                )
            }

            is LastMessageData.Invite -> {
                LastMessageUi.Invite(data.invite, source.theme(data.invite.genre, false))
            }
        }
    }

    @Composable
    override fun Content(data: LastMessageUi) {
        when (data) {
            is LastMessageUi.Message -> MessageContent(data)
            is LastMessageUi.Invite -> InviteContent(data.invite, data.theme, WidgetSizes.LAST_MESSAGE)
        }
    }

    private companion object {
        const val AVATAR_SOURCE_PX = 320
    }
}

class LastMessageWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LastMessageWidget()
}

@Composable
private fun MessageContent(ui: LastMessageUi.Message) {
    val context = LocalContext.current
    val palette = ui.theme.palette
    val cells = currentCells().bestFit(WidgetSizes.LAST_MESSAGE)
    val speaker = ui.data.speaker ?: ui.data.saga.title
    val text = ui.data.text

    WidgetRoot(
        color = palette.base,
        cornerDp = ui.theme.cornerDp,
        action = deepLinkAction(context, WidgetLinks.chat(ui.data.saga.id)),
    ) {
        when {
            // 2x1 and 4x1: an avatar and two lines.
            cells.rows == 1 -> {
                val avatarSize = if (cells.columns >= 4) 40 else 34
                val avatar = rememberAvatar(ui.avatar, avatarSize, palette.accent)
                Row(
                    modifier = GlanceModifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Avatar(avatar, avatarSize)
                    Spacer(modifier = GlanceModifier.width(10.dp))
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        WidgetTag(speaker, palette.accent)
                        WidgetText(
                            text = text,
                            color = palette.text,
                            sizeSp = if (cells.columns >= 4) 12.5f else 11.5f,
                            maxLines = 2,
                        )
                    }
                }
            }

            // 2x2: label, who spoke, then the line.
            cells.columns == 2 -> {
                val avatar = rememberAvatar(ui.avatar, 36, palette.accent)
                Column(modifier = GlanceModifier.fillMaxSize().padding(12.dp)) {
                    WidgetTag(context.getString(R.string.widget_tag_last_message), palette.textMuted)
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Avatar(avatar, 36)
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        WidgetTag(speaker, palette.accent)
                    }
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    QuoteLine(text, palette, 12f, maxLines = 4)
                }
            }

            // 4x2 and larger.
            else -> {
                val avatar = rememberAvatar(ui.avatar, 48, palette.accent)
                Column(modifier = GlanceModifier.fillMaxSize().padding(14.dp)) {
                    WidgetTag(context.getString(R.string.widget_tag_last_message), palette.textMuted)
                    Spacer(modifier = GlanceModifier.height(10.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Avatar(avatar, 48)
                        Spacer(modifier = GlanceModifier.width(10.dp))
                        Column(modifier = GlanceModifier.defaultWeight()) {
                            WidgetTag(speaker, palette.accent)
                            Spacer(modifier = GlanceModifier.height(4.dp))
                            QuoteLine(text, palette, 13.5f, maxLines = 3)
                        }
                    }
                }
            }
        }
    }
}

/** The line itself. Kept to its own function so the three layouts share one text style. */
@Composable
private fun QuoteLine(
    text: String,
    palette: WidgetPalette,
    sizeSp: Float,
    maxLines: Int,
) {
    WidgetText(
        text = text,
        color = palette.text,
        sizeSp = sizeSp,
        maxLines = maxLines,
    )
}

@Composable
private fun Avatar(
    bitmap: Bitmap?,
    sizeDp: Int,
) {
    if (bitmap != null) {
        Image(
            provider = ImageProvider(bitmap),
            contentDescription = null,
            modifier = GlanceModifier.size(sizeDp.dp),
        )
    } else {
        Spacer(modifier = GlanceModifier.size(sizeDp.dp))
    }
}
