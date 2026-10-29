package com.ilustris.sagai.features.widgets.glance

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import com.ilustris.sagai.R
import com.ilustris.sagai.features.widgets.domain.StoriesData
import com.ilustris.sagai.features.widgets.domain.StoryBook
import com.ilustris.sagai.features.widgets.domain.WidgetLinks
import com.ilustris.sagai.features.widgets.domain.WidgetSaga
import com.ilustris.sagai.features.widgets.domain.WidgetSizes
import com.ilustris.sagai.features.widgets.domain.WidgetTheme
import com.ilustris.sagai.features.widgets.domain.bestFit

data class BookUi(
    val book: StoryBook,
    val icon: Bitmap?,
    val theme: WidgetTheme,
)

/** The saga on top: its artwork, its theme and what it is about right now. */
data class HeroUi(
    val saga: WidgetSaga,
    val isEnded: Boolean,
    val objective: String?,
    val art: Bitmap?,
    val theme: WidgetTheme,
)

data class StoriesUi(
    val hero: HeroUi?,
    val books: List<BookUi>,
)

/**
 * The saga the player touched last on top, with its objective and a play button, and a strip with the
 * other recent sagas under it. Tapping the top opens the chat (the details when it is finished), tapping a
 * cover on the strip opens that saga the same way. The corner of the top carries the saga's theme icon.
 */
class StoriesWidget : DataGlanceWidget<StoriesUi>() {
    override val sizeMode = SizeMode.Responsive(WidgetSizes.STORIES.toDpSizes())

    override suspend fun load(
        context: Context,
        id: GlanceId,
    ): StoriesUi {
        val source = context.widgetEntryPoint().dataSource()
        val data = source.loadStories()
        val hero =
            data.hero?.let { saga ->
                val art = WidgetBitmaps.decode(saga.backdropPath)
                val light = art?.let { WidgetBitmaps.averageLuminance(it) > LIGHT_ART_LUMINANCE } ?: false
                HeroUi(saga, data.heroIsEnded, data.objective, art, source.theme(saga.genre, light))
            }
        val books =
            data.others.take(MAX_BOOKS).map {
                BookUi(
                    book = it,
                    icon = WidgetBitmaps.decode(it.iconPath, ICON_SOURCE_PX),
                    theme = source.theme(it.genre, false),
                )
            }
        return StoriesUi(hero, books)
    }

    @Composable
    override fun Content(data: StoriesUi) {
        StoriesContent(data)
    }

    private companion object {
        const val MAX_BOOKS = 5
        const val ICON_SOURCE_PX = 240

        /** Above this mean luminance the artwork is ink on paper and the widget switches to dark text. */
        const val LIGHT_ART_LUMINANCE = 0.62
    }
}

class StoriesWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StoriesWidget()
}

@Composable
private fun StoriesContent(ui: StoriesUi) {
    val context = LocalContext.current
    val hero = ui.hero
    if (hero == null) {
        EmptyStories(context)
        return
    }
    val palette = hero.theme.palette
    val cells = currentCells().bestFit(WidgetSizes.STORIES)
    val size = LocalSize.current
    val width = size.width.value
    val height = size.height.value
    // The strip needs the height of a tall widget; below that the top saga has the whole box.
    val stripDp = if (cells.rows >= 3 && ui.books.isNotEmpty()) STRIP_DP else 0f
    val heroHeight = height - stripDp
    // The objective fills what the top leaves after its padding and tag; the launcher may give a little less
    // than the declared size, so the estimate keeps a margin.
    val objectiveSp = if (cells.rows >= 3) 14f else 13f
    val objectiveLines = (((heroHeight * HERO_MARGIN - HERO_CHROME_DP) / (objectiveSp * LINE_HEIGHT)).toInt()).coerceIn(2, 4)
    val art = rememberArt(hero.art, width / 2f, heroHeight, WidgetFocus.ARTWORK_X, WidgetFocus.ARTWORK_Y, fade = ArtFade.LEFT)
    val objective =
        when {
            hero.isEnded -> context.getString(R.string.saga_detail_ended_tag)
            else -> hero.objective ?: context.getString(R.string.widget_discreet_objective)
        }
    val openHero = if (hero.isEnded) WidgetLinks.sagaDetail(hero.saga.id) else WidgetLinks.chat(hero.saga.id)

    WidgetRoot(
        color = palette.base,
        cornerDp = hero.theme.cornerDp,
        action = deepLinkAction(context, openHero),
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            Box(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                // Art on the right half of whatever size the launcher gives, melting into the base on its left.
                Row(modifier = GlanceModifier.fillMaxSize()) {
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    art?.let { ArtImage(it, GlanceModifier.fillMaxHeight().defaultWeight()) }
                }
                Row(modifier = GlanceModifier.fillMaxSize().padding(14.dp)) {
                    Column(modifier = GlanceModifier.defaultWeight().fillMaxHeight()) {
                        WidgetTag(hero.saga.title, palette.textMuted)
                        Spacer(modifier = GlanceModifier.defaultWeight())
                        WidgetText(
                            text = objective,
                            color = palette.text,
                            sizeSp = objectiveSp,
                            weight = FontWeight.Medium,
                            maxLines = objectiveLines,
                        )
                    }
                    Spacer(modifier = GlanceModifier.defaultWeight())
                }
                // Play sits in the bottom corner over the art, where the eye ends up.
                Box(modifier = GlanceModifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.BottomEnd) {
                    CircleButton(
                        R.drawable.widget_ic_play,
                        palette.accent,
                        palette.onAccent,
                        PLAY_DP,
                        context.getString(R.string.widget_cd_continue),
                    )
                }
                // The theme icon in the corner, where a music widget carries its logo.
                Box(modifier = GlanceModifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.TopEnd) {
                    Image(
                        provider = ImageProvider(hero.saga.genre.icon),
                        contentDescription = null,
                        modifier = GlanceModifier.size(22.dp),
                        colorFilter = ColorFilter.tint(glanceColor(palette.accent)),
                    )
                }
            }
            if (stripDp > 0f) {
                CoverStrip(ui.books, stripDp, slots = MAX_SLOTS)
            }
        }
    }
}

/** The other sagas as covers, same slot width each, the newest on the left. */
@Composable
private fun CoverStrip(
    books: List<BookUi>,
    stripDp: Float,
    slots: Int,
) {
    val context = LocalContext.current
    val shown = books.take(slots)
    val tileHeight = stripDp - STRIP_PADDING_DP * 2
    Row(
        modifier =
            GlanceModifier
                .fillMaxWidth()
                .height(stripDp.dp)
                .background(glanceColor(STRIP_SHADE, STRIP_SHADE_ALPHA))
                .padding(horizontal = 10.dp, vertical = STRIP_PADDING_DP.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        shown.forEach { book ->
            val link = if (book.book.isEnded) WidgetLinks.sagaDetail(book.book.id) else WidgetLinks.chat(book.book.id)
            Box(
                modifier =
                    GlanceModifier
                        .defaultWeight()
                        .fillMaxHeight()
                        .padding(horizontal = 3.dp)
                        .clickable(deepLinkAction(context, link)),
                contentAlignment = Alignment.Center,
            ) {
                // A square, drawn for its box and fitted, never stretched.
                val tile = rememberCoverTile(book, tileHeight)
                Image(
                    provider = ImageProvider(tile),
                    contentDescription = book.book.title,
                    modifier = GlanceModifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
        }
        repeat(slots - shown.size) {
            Spacer(modifier = GlanceModifier.defaultWeight())
        }
    }
}

@Composable
private fun EmptyStories(context: Context) {
    WidgetRoot(
        color = WidgetNeutral.BASE,
        cornerDp = WidgetNeutral.RADIUS_DP,
        action = deepLinkAction(context, WidgetLinks.HOME),
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WidgetTag(context.getString(R.string.widget_tag_stories), WidgetNeutral.MUTED)
            Spacer(modifier = GlanceModifier.height(6.dp))
            WidgetText(
                text = context.getString(R.string.widget_stories_empty),
                color = WidgetNeutral.TEXT,
                sizeSp = 12f,
                weight = FontWeight.Medium,
                maxLines = 4,
            )
        }
    }
}

/** One book of the shelf, drawn for its box. The genre glyph is white on the genre gradient. */
@Composable
private fun rememberCoverTile(
    ui: BookUi,
    sizeDp: Float,
): Bitmap {
    val context = LocalContext.current
    val density = context.resources.displayMetrics.density
    return remember(ui, sizeDp) {
        val glyph =
            ContextCompat
                .getDrawable(context, ui.book.genre.icon)
                ?.mutate()
                ?.also { it.setTint(GLYPH_COLOR) }
        WidgetBitmaps.coverTile(
            sizePx = (sizeDp * density).toInt(),
            topColor = ui.theme.palette.glow,
            bottomColor = ui.theme.palette.base,
            radiusPx = COVER_RADIUS_DP * density,
            icon = ui.icon,
            glyph = glyph,
        )
    }
}

private const val GLYPH_COLOR = 0xE0FFFFFF.toInt()
private const val COVER_RADIUS_DP = 8f
private const val STRIP_DP = 66f
private const val STRIP_PADDING_DP = 8f
private const val MAX_SLOTS = 5
private const val PLAY_DP = 34
private const val HERO_MARGIN = 0.88f
private const val HERO_CHROME_DP = 28f + 14f + 6f
private const val LINE_HEIGHT = 1.3f
private const val STRIP_SHADE = 0xFF000000.toInt()
private const val STRIP_SHADE_ALPHA = 0.28f
