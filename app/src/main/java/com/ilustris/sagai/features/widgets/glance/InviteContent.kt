package com.ilustris.sagai.features.widgets.glance

import androidx.compose.runtime.Composable
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
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
import com.ilustris.sagai.features.widgets.domain.WidgetCells
import com.ilustris.sagai.features.widgets.domain.WidgetInvite
import com.ilustris.sagai.features.widgets.domain.WidgetLinks
import com.ilustris.sagai.features.widgets.domain.WidgetSizes
import com.ilustris.sagai.features.widgets.domain.WidgetTheme
import com.ilustris.sagai.features.widgets.domain.bestFit
import androidx.compose.ui.unit.dp

/**
 * Shown by Continue and Last message when no saga is running: a nudge to write the next story.
 *
 * It is the one widget that asks for an action, so it is the one with a gradient. The color glow
 * stays in the top-right corner, away from the text, which always sits on the dark base.
 */
@Composable
fun InviteContent(
    invite: WidgetInvite,
    theme: WidgetTheme,
    supported: List<WidgetCells>,
) {
    val context = LocalContext.current
    val palette = theme.palette
    val cells = currentCells().bestFit(supported)
    val size = LocalSize.current
    val width = size.width.value
    val text =
        context.getString(if (invite.hasFinishedSagas) R.string.widget_invite_text else R.string.widget_invite_first)
    val tag = context.getString(R.string.widget_tag_new_story)
    val description = context.getString(R.string.widget_cd_new_story)

    WidgetRoot(
        color = palette.base,
        cornerDp = theme.cornerDp,
        action = deepLinkAction(context, WidgetLinks.NEW_SAGA),
    ) {
        TintedLayer(R.drawable.widget_glow_corner, glanceColor(palette.glow, 0.9f))
        Box(
            modifier = GlanceModifier.fillMaxSize(),
            contentAlignment = Alignment.TopEnd,
        ) {
            Image(
                provider = ImageProvider(invite.genre.icon),
                contentDescription = null,
                modifier = GlanceModifier.size(glyphSize(cells).dp),
                colorFilter = ColorFilter.tint(glanceColor(WHITE, 0.14f)),
            )
        }
        when {
            cells.columns == 1 -> {
                Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircleButton(R.drawable.widget_ic_add, palette.accent, palette.onAccent, 34, description)
                }
            }

            cells.rows == 1 -> {
                Row(
                    modifier = GlanceModifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        WidgetText(tag.uppercase(), palette.textMuted, 9f, FontWeight.Bold, maxLines = 1)
                        WidgetText(
                            text = if (cells.columns >= 4) text else context.getString(R.string.widget_invite_short),
                            color = palette.text,
                            sizeSp = if (cells.columns >= 4) 12.5f else 11.5f,
                            weight = FontWeight.Medium,
                            maxLines = 2,
                            modifier = GlanceModifier.width((width * 0.6f).dp),
                        )
                    }
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    CircleButton(R.drawable.widget_ic_add, palette.accent, palette.onAccent, 30, description)
                }
            }

            cells.rows == 2 && cells.columns == 2 -> {
                Column(
                    modifier = GlanceModifier.fillMaxSize().padding(12.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    WidgetText(tag.uppercase(), palette.textMuted, 9f, FontWeight.Bold, maxLines = 1)
                    Spacer(modifier = GlanceModifier.height(5.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        WidgetText(
                            text = text,
                            color = palette.text,
                            sizeSp = 12.5f,
                            weight = FontWeight.Medium,
                            maxLines = 4,
                            modifier = GlanceModifier.defaultWeight(),
                        )
                        Spacer(modifier = GlanceModifier.width(6.dp))
                        CircleButton(R.drawable.widget_ic_add, palette.accent, palette.onAccent, 34, description)
                    }
                }
            }

            else -> {
                Column(
                    modifier = GlanceModifier.fillMaxSize().padding(16.dp),
                ) {
                    WidgetText(tag.uppercase(), palette.textMuted, 9f, FontWeight.Bold, maxLines = 1)
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    WidgetText(
                        text = text,
                        color = palette.text,
                        sizeSp = 16f,
                        weight = FontWeight.Medium,
                        maxLines = 4,
                        modifier = GlanceModifier.width((width * 0.6f).dp),
                    )
                    Spacer(modifier = GlanceModifier.height(10.dp))
                    CircleButton(R.drawable.widget_ic_add, palette.accent, palette.onAccent, 38, description)
                }
            }
        }
    }
}

private fun glyphSize(cells: WidgetCells): Int =
    when {
        cells.columns == 1 -> 64
        cells.rows == 1 -> 56
        cells.columns == 2 -> 84
        else -> 120
    }

private const val WHITE = 0xFFFFFFFF.toInt()
