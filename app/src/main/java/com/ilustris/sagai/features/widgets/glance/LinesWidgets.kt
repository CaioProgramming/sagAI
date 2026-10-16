package com.ilustris.sagai.features.widgets.glance

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import com.ilustris.sagai.R
import com.ilustris.sagai.features.widgets.WidgetUpdater
import com.ilustris.sagai.features.widgets.domain.LinesType
import com.ilustris.sagai.features.widgets.domain.WidgetCells
import com.ilustris.sagai.features.widgets.domain.WidgetCharacter
import com.ilustris.sagai.features.widgets.domain.WidgetConfig
import com.ilustris.sagai.features.widgets.domain.WidgetLinks
import com.ilustris.sagai.features.widgets.domain.WidgetRotation
import com.ilustris.sagai.features.widgets.domain.WidgetSizes
import com.ilustris.sagai.features.widgets.domain.WidgetTheme
import com.ilustris.sagai.features.widgets.domain.bestFit
import java.time.LocalDate

sealed interface LinesUi {
    data class Ready(
        val config: WidgetConfig,
        val character: WidgetCharacter,
        /** The line to show right now, already picked by the rotation rule. */
        val line: String,
        val art: Bitmap?,
        val theme: WidgetTheme,
    ) : LinesUi

    /** Added from the launcher's widget list, so there are no lines yet. */
    data object Unconfigured : LinesUi
}

/**
 * A widget the player built from a character: a few lines written by the AI, one of them on screen.
 * [QuoteWidget] and [HookWidget] share everything but their layouts.
 */
abstract class LinesGlanceWidget(
    private val supported: List<WidgetCells>,
) : DataGlanceWidget<LinesUi>() {
    override val sizeMode = SizeMode.Responsive(supported.toDpSizes())

    override suspend fun load(
        context: Context,
        id: GlanceId,
    ): LinesUi {
        val config = WidgetConfigState.read(context, id)
        if (config == null || config.lines.isEmpty()) return LinesUi.Unconfigured
        val entry = context.widgetEntryPoint()
        val source = entry.dataSource()
        val character = source.loadCharacter(config.sagaId, config.characterId) ?: return LinesUi.Unconfigured
        val openCounter = entry.dataStore().getIntNow(WidgetUpdater.OPEN_COUNTER_KEY)
        val index =
            WidgetRotation.indexFor(
                rule = config.rotation,
                lineCount = config.lines.size,
                openCounter = openCounter,
                epochDay = LocalDate.now().toEpochDay(),
                seed = config.seed,
            )
        val portrait = WidgetBitmaps.decode(character.portraitPath)
        val light = portrait?.let { WidgetBitmaps.averageLuminance(it) > LIGHT_ART_LUMINANCE } ?: false
        return LinesUi.Ready(
            config = config,
            character = character,
            line = config.lines[index],
            // Discreet mode keeps the widget's look but hides who is in the picture.
            art = if (config.discreet) portrait?.let { WidgetBitmaps.blur(it) } else portrait,
            theme = source.theme(character.genre, light),
        )
    }

    @Composable
    override fun Content(data: LinesUi) {
        when (data) {
            is LinesUi.Ready -> Layout(data, currentCells().bestFit(supported))
            LinesUi.Unconfigured -> UnconfiguredContent()
        }
    }

    @Composable
    protected abstract fun Layout(
        ui: LinesUi.Ready,
        cells: WidgetCells,
    )

    private companion object {
        const val LIGHT_ART_LUMINANCE = 0.62
    }
}

class QuoteWidget : LinesGlanceWidget(WidgetSizes.QUOTE) {
    @Composable
    override fun Layout(
        ui: LinesUi.Ready,
        cells: WidgetCells,
    ) {
        QuoteLayout(ui, cells)
    }
}

class QuoteWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuoteWidget()
}

class HookWidget : LinesGlanceWidget(WidgetSizes.HOOK) {
    @Composable
    override fun Layout(
        ui: LinesUi.Ready,
        cells: WidgetCells,
    ) {
        HookLayout(ui, cells)
    }
}

class HookWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HookWidget()
}

private fun LinesUi.Ready.shownLine(context: Context): String =
    if (config.discreet) {
        context.getString(
            if (config.type == LinesType.QUOTE) R.string.widget_discreet_quote else R.string.widget_discreet_hook,
        )
    } else {
        line
    }

private fun LinesUi.Ready.shownName(context: Context): String =
    if (config.discreet) context.getString(R.string.widget_discreet_character) else character.name

@Composable
private fun QuoteLayout(
    ui: LinesUi.Ready,
    cells: WidgetCells,
) {
    val context = LocalContext.current
    val palette = ui.theme.palette
    val size = LocalSize.current
    val width = size.width.value
    val height = size.height.value
    val text = ui.shownLine(context)
    val name = ui.shownName(context)

    WidgetRoot(
        color = palette.base,
        cornerDp = ui.theme.cornerDp,
        action = deepLinkAction(context, WidgetLinks.character(ui.character.sagaId, ui.character.id)),
    ) {
        when {
            // 4x1: a thin slice of the portrait on the right, the line on the left.
            cells == WidgetCells(4, 1) -> {
                val strip = width * 0.38f
                val art = rememberArt(ui.art, strip, height, WidgetFocus.PORTRAIT_X, WidgetFocus.PORTRAIT_Y, 0.38f)
                Row(modifier = GlanceModifier.fillMaxSize()) {
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    art?.let { ArtImage(it, GlanceModifier.fillMaxHeight().width(strip.dp)) }
                }
                TintedLayer(R.drawable.widget_scrim_left, glanceColor(palette.base))
                Column(
                    modifier = GlanceModifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    WidgetText(
                        text = text,
                        color = palette.text,
                        sizeSp = 13f,
                        weight = FontWeight.Medium,
                        maxLines = 2,
                        modifier = GlanceModifier.width((width * 0.56f).dp),
                    )
                    Spacer(modifier = GlanceModifier.height(3.dp))
                    WidgetText(name, palette.accent, 10f, FontWeight.Bold, maxLines = 1)
                }
            }

            // 2x2: portrait on top, the line in the solid part below.
            cells == WidgetCells(2, 2) -> {
                val art = rememberArt(ui.art, width, height, WidgetFocus.PORTRAIT_X, WidgetFocus.PORTRAIT_Y, 0.3f)
                art?.let { ArtImage(it, GlanceModifier.fillMaxSize()) }
                TintedLayer(R.drawable.widget_scrim_bottom, glanceColor(palette.base))
                Column(
                    modifier = GlanceModifier.fillMaxSize().padding(12.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    WidgetText(text, palette.text, 12.5f, FontWeight.Medium, maxLines = 4)
                    Spacer(modifier = GlanceModifier.height(6.dp))
                    WidgetText(name, palette.accent, 10.5f, FontWeight.Bold, maxLines = 1)
                }
            }

            // 4x3: the tall version, with a big opening mark.
            cells.rows >= 3 -> {
                val strip = width * 0.48f
                val art = rememberArt(ui.art, strip, height, WidgetFocus.PORTRAIT_X, WidgetFocus.PORTRAIT_Y, 0.36f)
                Row(modifier = GlanceModifier.fillMaxSize()) {
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    art?.let { ArtImage(it, GlanceModifier.fillMaxHeight().width(strip.dp)) }
                }
                TintedLayer(R.drawable.widget_scrim_left, glanceColor(palette.base))
                Column(modifier = GlanceModifier.fillMaxSize().padding(16.dp)) {
                    WidgetText("“", palette.accent, 44f, FontWeight.Bold, maxLines = 1)
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    WidgetText(
                        text = text,
                        color = palette.text,
                        sizeSp = 19f,
                        weight = FontWeight.Medium,
                        maxLines = 7,
                        modifier = GlanceModifier.width((width * 0.5f).dp),
                    )
                    Spacer(modifier = GlanceModifier.height(12.dp))
                    WidgetText("$name · ${ui.character.sagaTitle}", palette.accent, 12f, FontWeight.Bold, maxLines = 1)
                }
            }

            // 4x2.
            else -> {
                val strip = width * 0.46f
                val art = rememberArt(ui.art, strip, height, WidgetFocus.PORTRAIT_X, WidgetFocus.PORTRAIT_Y, 0.38f)
                Row(modifier = GlanceModifier.fillMaxSize()) {
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    art?.let { ArtImage(it, GlanceModifier.fillMaxHeight().width(strip.dp)) }
                }
                TintedLayer(R.drawable.widget_scrim_left, glanceColor(palette.base))
                Column(modifier = GlanceModifier.fillMaxSize().padding(14.dp)) {
                    WidgetTag(context.getString(R.string.widget_tag_from_saga), palette.textMuted)
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    WidgetText(
                        text = text,
                        color = palette.text,
                        sizeSp = 15f,
                        weight = FontWeight.Medium,
                        maxLines = 4,
                        modifier = GlanceModifier.width((width * 0.5f).dp),
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    WidgetText("$name · ${ui.character.sagaTitle}", palette.accent, 11f, FontWeight.Bold, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun HookLayout(
    ui: LinesUi.Ready,
    cells: WidgetCells,
) {
    val context = LocalContext.current
    val palette = ui.theme.palette
    val text = ui.shownLine(context)
    val name = ui.shownName(context)
    val play = context.getString(R.string.widget_cd_continue)

    WidgetRoot(
        color = palette.base,
        cornerDp = ui.theme.cornerDp,
        action = deepLinkAction(context, WidgetLinks.chat(ui.character.sagaId)),
    ) {
        when {
            // 1x1: just the face, with a dot that says someone is waiting.
            cells == WidgetCells(1, 1) -> {
                val avatar = rememberAvatar(ui.art, 52, palette.accent)
                Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    HookAvatar(avatar, 52)
                }
                Box(
                    modifier = GlanceModifier.fillMaxSize().padding(8.dp),
                    contentAlignment = Alignment.TopEnd,
                ) {
                    Box(
                        modifier = GlanceModifier.size(10.dp).background(glanceColor(palette.accent)).cornerRadius(5.dp),
                    ) {}
                }
            }

            // 4x1: avatar, the line, and a play button.
            cells == WidgetCells(4, 1) -> {
                val avatar = rememberAvatar(ui.art, 40, palette.accent)
                Row(
                    modifier = GlanceModifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HookAvatar(avatar, 40)
                    Spacer(modifier = GlanceModifier.width(10.dp))
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        WidgetTag(name, palette.accent)
                        WidgetText(text, palette.text, 12f, maxLines = 2)
                    }
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    CircleButton(R.drawable.widget_ic_play, palette.accent, palette.onAccent, 30, play)
                }
            }

            // 2x2.
            cells.columns == 2 -> {
                val avatar = rememberAvatar(ui.art, 40, palette.accent)
                Column(modifier = GlanceModifier.fillMaxSize().padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        HookAvatar(avatar, 40)
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        WidgetTag(name, palette.accent)
                    }
                    Spacer(modifier = GlanceModifier.height(9.dp))
                    WidgetText(text, palette.text, 12f, maxLines = 4)
                }
            }

            // 4x2 and larger.
            else -> {
                val avatar = rememberAvatar(ui.art, 52, palette.accent)
                Column(modifier = GlanceModifier.fillMaxSize().padding(14.dp)) {
                    WidgetTag(ui.character.sagaTitle, palette.textMuted)
                    Spacer(modifier = GlanceModifier.height(10.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        HookAvatar(avatar, 52)
                        Spacer(modifier = GlanceModifier.width(11.dp))
                        Column(modifier = GlanceModifier.defaultWeight()) {
                            WidgetTag(name, palette.accent)
                            Spacer(modifier = GlanceModifier.height(4.dp))
                            WidgetText(text, palette.text, 13.5f, maxLines = 3)
                        }
                    }
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        Spacer(modifier = GlanceModifier.defaultWeight())
                        CircleButton(R.drawable.widget_ic_play, palette.accent, palette.onAccent, 38, play)
                    }
                }
            }
        }
    }
}

@Composable
private fun HookAvatar(
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

/** A generated widget added from the launcher's list has no lines until the player builds one. */
@Composable
private fun UnconfiguredContent() {
    val context = LocalContext.current
    val compact = LocalSize.current.height.value < 90f
    WidgetRoot(
        color = WidgetNeutral.BASE,
        cornerDp = WidgetNeutral.RADIUS_DP,
        action = deepLinkAction(context, WidgetLinks.HOME),
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            WidgetText(
                text = context.getString(R.string.widget_unconfigured_title),
                color = WidgetNeutral.TEXT,
                sizeSp = 12f,
                weight = FontWeight.Medium,
                maxLines = 2,
            )
            if (!compact) {
                Spacer(modifier = GlanceModifier.height(4.dp))
                WidgetText(
                    text = context.getString(R.string.widget_unconfigured_subtitle),
                    color = WidgetNeutral.MUTED,
                    sizeSp = 10f,
                    maxLines = 3,
                )
            }
        }
    }
}
