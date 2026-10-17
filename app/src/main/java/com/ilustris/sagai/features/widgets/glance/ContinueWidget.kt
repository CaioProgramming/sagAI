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
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.background
import androidx.glance.text.FontWeight
import com.ilustris.sagai.R
import com.ilustris.sagai.features.widgets.domain.ContinueData
import com.ilustris.sagai.features.widgets.domain.WidgetCells
import com.ilustris.sagai.features.widgets.domain.WidgetInvite
import com.ilustris.sagai.features.widgets.domain.WidgetLinks
import com.ilustris.sagai.features.widgets.domain.WidgetSaga
import com.ilustris.sagai.features.widgets.domain.WidgetSizes
import com.ilustris.sagai.features.widgets.domain.WidgetTheme
import com.ilustris.sagai.features.widgets.domain.bestFit

/** What [ContinueWidget] draws, with the bitmaps and colors already prepared. */
sealed interface ContinueUi {
    data class Active(
        val saga: WidgetSaga,
        val objective: String?,
        val art: Bitmap?,
        val theme: WidgetTheme,
    ) : ContinueUi

    data class Invite(
        val invite: WidgetInvite,
        val theme: WidgetTheme,
    ) : ContinueUi
}

/**
 * The current objective of the saga the player touched last, over its artwork, with a play button.
 * When no saga is running it becomes an invitation to write the next one.
 */
class ContinueWidget : DataGlanceWidget<ContinueUi>() {
    override val sizeMode = SizeMode.Responsive(WidgetSizes.CONTINUE.toDpSizes())

    override suspend fun load(
        context: Context,
        id: GlanceId,
    ): ContinueUi {
        val source = context.widgetEntryPoint().dataSource()
        return when (val data = source.loadContinue()) {
            is ContinueData.Active -> {
                val art = WidgetBitmaps.decode(data.saga.backdropPath)
                val light = art?.let { WidgetBitmaps.averageLuminance(it) > LIGHT_ART_LUMINANCE } ?: false
                ContinueUi.Active(data.saga, data.objective, art, source.theme(data.saga.genre, light))
            }

            is ContinueData.Invite -> {
                ContinueUi.Invite(data.invite, source.theme(data.invite.genre, false))
            }
        }
    }

    @Composable
    override fun Content(data: ContinueUi) {
        when (data) {
            is ContinueUi.Active -> ActiveContent(data)
            is ContinueUi.Invite -> InviteContent(data.invite, data.theme, WidgetSizes.CONTINUE)
        }
    }

    private companion object {
        /** Above this mean luminance the artwork is ink on paper and the widget switches to dark text. */
        const val LIGHT_ART_LUMINANCE = 0.62
    }
}

class ContinueWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ContinueWidget()
}

@Composable
private fun ActiveContent(ui: ContinueUi.Active) {
    val context = LocalContext.current
    val palette = ui.theme.palette
    val cells = currentCells().bestFit(WidgetSizes.CONTINUE)
    val size = LocalSize.current
    val width = size.width.value
    val height = size.height.value
    val objective = ui.objective ?: context.getString(R.string.widget_discreet_objective)
    val play = context.getString(R.string.widget_cd_continue)

    WidgetRoot(
        color = palette.base,
        cornerDp = ui.theme.cornerDp,
        action = deepLinkAction(context, WidgetLinks.chat(ui.saga.id)),
    ) {
        when {
            // 1x1: the artwork with a play button on top.
            cells == WidgetCells(1, 1) -> {
                val art = rememberArt(ui.art, width, height, WidgetFocus.ARTWORK_X, WidgetFocus.ARTWORK_Y)
                Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    art?.let { ArtImage(it, GlanceModifier.fillMaxSize()) }
                    CircleButton(R.drawable.widget_ic_play, palette.accent, palette.onAccent, 34, play)
                }
            }

            // 2x1: not enough room for art beside text, so it is only a faint tint under a strong scrim.
            cells == WidgetCells(2, 1) -> {
                val art = rememberArt(ui.art, width, height, WidgetFocus.ARTWORK_X, WidgetFocus.ARTWORK_Y)
                art?.let { ArtImage(it, GlanceModifier.fillMaxSize()) }
                Box(
                    modifier = GlanceModifier.fillMaxSize().background(glanceColor(palette.base, 0.88f)),
                ) {}
                Row(
                    modifier = GlanceModifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        WidgetTag(context.getString(R.string.widget_tag_objective_short), palette.accent)
                        WidgetText(objective, palette.text, 11.5f, FontWeight.Medium, maxLines = 2)
                    }
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    CircleButton(R.drawable.widget_ic_play, palette.accent, palette.onAccent, 30, play)
                }
            }

            // 4x1: text on the left half over a solid base, art fading in on the right.
            cells == WidgetCells(4, 1) -> {
                val stripWidth = width * STRIP_WIDE
                val art = rememberArt(ui.art, stripWidth, height, WidgetFocus.ARTWORK_X, WidgetFocus.ARTWORK_Y, fade = ArtFade.LEFT)
                Row(modifier = GlanceModifier.fillMaxSize()) {
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    art?.let { ArtImage(it, GlanceModifier.fillMaxHeight().defaultWeight()) }
                }
                Row(
                    modifier = GlanceModifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = GlanceModifier.width((width * TEXT_WIDE).dp)) {
                        WidgetTag(context.getString(R.string.widget_tag_current_objective), palette.accent)
                        WidgetText(objective, palette.text, 12.5f, FontWeight.Medium, maxLines = 2)
                    }
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    CircleButton(R.drawable.widget_ic_play, palette.accent, palette.onAccent, 30, play)
                }
            }

            // 2x2: art on top, solid scrim below, every piece of text in the solid part.
            cells == WidgetCells(2, 2) -> {
                val art = rememberArt(ui.art, width, height, WidgetFocus.ARTWORK_X, WidgetFocus.ARTWORK_Y, 0.3f, fade = ArtFade.BOTTOM)
                art?.let { ArtImage(it, GlanceModifier.fillMaxSize()) }
                Column(
                    modifier = GlanceModifier.fillMaxSize().padding(12.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    WidgetTag(context.getString(R.string.widget_tag_current_objective), palette.accent)
                    Spacer(modifier = GlanceModifier.height(5.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        WidgetText(
                            text = objective,
                            color = palette.text,
                            sizeSp = 12f,
                            weight = FontWeight.Medium,
                            maxLines = 3,
                            modifier = GlanceModifier.defaultWeight(),
                        )
                        Spacer(modifier = GlanceModifier.width(6.dp))
                        CircleButton(R.drawable.widget_ic_play, palette.accent, palette.onAccent, 34, play)
                    }
                }
            }

            // 4x2 and larger: saga title and a big objective on the left, art on the right.
            else -> {
                val stripWidth = width * STRIP_TALL
                val art = rememberArt(ui.art, stripWidth, height, WidgetFocus.ARTWORK_X, WidgetFocus.ARTWORK_Y, fade = ArtFade.LEFT)
                Row(modifier = GlanceModifier.fillMaxSize()) {
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    art?.let { ArtImage(it, GlanceModifier.fillMaxHeight().defaultWeight()) }
                }
                Column(modifier = GlanceModifier.fillMaxSize().padding(14.dp)) {
                    WidgetTag(ui.saga.title, palette.textMuted)
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    WidgetText(
                        text = objective,
                        color = palette.text,
                        sizeSp = 15f,
                        weight = FontWeight.Medium,
                        maxLines = 4,
                        modifier = GlanceModifier.width((width * TEXT_TALL).dp),
                    )
                    Spacer(modifier = GlanceModifier.height(10.dp))
                    CircleButton(R.drawable.widget_ic_play, palette.accent, palette.onAccent, 38, play)
                }
            }
        }
    }
}

@Composable
fun ArtImage(
    bitmap: Bitmap,
    modifier: GlanceModifier,
) {
    Image(
        provider = ImageProvider(bitmap),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop,
    )
}

// Art and text never overlap: the text column is narrower than the space the scrim leaves clear.
private const val STRIP_WIDE = 0.40f
private const val TEXT_WIDE = 0.56f
private const val STRIP_TALL = 0.46f
private const val TEXT_TALL = 0.50f
