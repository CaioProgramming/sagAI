package com.ilustris.sagai.features.widgets.glance

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
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
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.TextAlign
import com.ilustris.sagai.R
import com.ilustris.sagai.features.widgets.domain.StoriesData
import com.ilustris.sagai.features.widgets.domain.StoryBook
import com.ilustris.sagai.features.widgets.domain.WidgetLinks
import com.ilustris.sagai.features.widgets.domain.WidgetSizes
import com.ilustris.sagai.features.widgets.domain.WidgetTheme
import com.ilustris.sagai.features.widgets.domain.bestFit

data class BookUi(
    val book: StoryBook,
    val icon: Bitmap?,
    val theme: WidgetTheme,
)

data class StoriesUi(
    val data: StoriesData,
    val books: List<BookUi>,
)

/**
 * A shelf with the sagas the player finished: one book each, title below.
 *
 * The widget itself is neutral on purpose (one dark surface, one font). Only the books take the
 * colors of their genre, and a saga with its own icon shows that icon instead of the genre glyph.
 */
class StoriesWidget : DataGlanceWidget<StoriesUi>() {
    override val sizeMode = SizeMode.Responsive(WidgetSizes.STORIES.toDpSizes())

    override suspend fun load(
        context: Context,
        id: GlanceId,
    ): StoriesUi {
        val source = context.widgetEntryPoint().dataSource()
        val data = source.loadStories()
        val books =
            data.books.take(MAX_BOOKS).map {
                BookUi(
                    book = it,
                    icon = WidgetBitmaps.decode(it.iconPath, ICON_SOURCE_PX),
                    theme = source.theme(it.genre, false),
                )
            }
        return StoriesUi(data, books)
    }

    @Composable
    override fun Content(data: StoriesUi) {
        StoriesContent(data)
    }

    private companion object {
        const val MAX_BOOKS = 4
        const val ICON_SOURCE_PX = 240
    }
}

class StoriesWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StoriesWidget()
}

@Composable
private fun StoriesContent(ui: StoriesUi) {
    val context = LocalContext.current
    val cells = currentCells().bestFit(WidgetSizes.STORIES)
    val width = LocalSize.current.width.value
    val tall = cells.rows >= 3
    val wide = cells.columns >= 4
    val padding = if (tall) 14 else 10

    WidgetRoot(
        color = WidgetNeutral.BASE,
        cornerDp = WidgetNeutral.RADIUS_DP,
        action = deepLinkAction(context, WidgetLinks.HOME),
    ) {
        if (ui.books.isEmpty()) {
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
        } else {
            val slots = if (wide) 4 else 2
            val shown = ui.books.take(slots)
            val slotWidth = (width - padding * 2) / slots
            val bookWidth = slotWidth - 10f
            val bookHeight = if (tall) 90f else 48f
            val total = ui.data.totalCount

            Column(modifier = GlanceModifier.fillMaxSize().padding(padding.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WidgetTag(context.getString(R.string.widget_tag_stories), WidgetNeutral.MUTED)
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    if (wide) {
                        WidgetText(
                            text = context.resources.getQuantityString(R.plurals.widget_stories_completed, total, total),
                            color = WidgetNeutral.MUTED,
                            sizeSp = 10f,
                            maxLines = 1,
                        )
                    }
                }
                Spacer(modifier = GlanceModifier.height(6.dp))
                Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                    shown.forEach { book ->
                        Column(
                            modifier =
                                GlanceModifier
                                    .defaultWeight()
                                    .padding(horizontal = 5.dp)
                                    .clickable(deepLinkAction(context, WidgetLinks.sagaDetail(book.book.id))),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            val tile = rememberBookTile(book, bookWidth, bookHeight)
                            Image(
                                provider = ImageProvider(tile),
                                contentDescription = book.book.title,
                                modifier = GlanceModifier.size(bookWidth.dp, bookHeight.dp),
                            )
                        }
                    }
                    repeat(slots - shown.size) {
                        Spacer(modifier = GlanceModifier.defaultWeight())
                    }
                }
                Box(
                    modifier =
                        GlanceModifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(glanceColor(WidgetNeutral.TEXT, 0.25f)),
                ) {}
                Spacer(modifier = GlanceModifier.height(4.dp))
                Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    shown.forEach { book ->
                        Box(
                            modifier = GlanceModifier.defaultWeight().padding(horizontal = 3.dp),
                            contentAlignment = Alignment.TopCenter,
                        ) {
                            WidgetText(
                                text = book.book.title,
                                color = WidgetNeutral.TEXT,
                                sizeSp = 9f,
                                weight = FontWeight.Bold,
                                maxLines = 2,
                                align = TextAlign.Center,
                            )
                        }
                    }
                    repeat(slots - shown.size) {
                        Spacer(modifier = GlanceModifier.defaultWeight())
                    }
                }
                if (tall) {
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    WidgetText(
                        text = context.getString(R.string.widget_stories_hours, ui.data.playTimeHours),
                        color = WidgetNeutral.MUTED,
                        sizeSp = 10f,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/** One book of the shelf, drawn for its box. The genre glyph is white on the genre gradient. */
@Composable
private fun rememberBookTile(
    ui: BookUi,
    widthDp: Float,
    heightDp: Float,
): Bitmap {
    val context = LocalContext.current
    val density = context.resources.displayMetrics.density
    return remember(ui, widthDp, heightDp) {
        val glyph =
            ContextCompat
                .getDrawable(context, ui.book.genre.icon)
                ?.mutate()
                ?.also { it.setTint(GLYPH_COLOR) }
        WidgetBitmaps.bookTile(
            widthPx = (widthDp * density).toInt(),
            heightPx = (heightDp * density).toInt(),
            topColor = ui.theme.palette.glow,
            bottomColor = ui.theme.palette.base,
            radiusPx = BOOK_RADIUS_DP * density,
            icon = ui.icon,
            glyph = glyph,
        )
    }
}

private const val GLYPH_COLOR = 0xE0FFFFFF.toInt()
private const val BOOK_RADIUS_DP = 6f
