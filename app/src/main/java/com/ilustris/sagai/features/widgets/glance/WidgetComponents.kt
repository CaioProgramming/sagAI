package com.ilustris.sagai.features.widgets.glance

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.TextAlign
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ilustris.sagai.MainActivity
import com.ilustris.sagai.R
import com.ilustris.sagai.features.widgets.domain.WidgetCells
import com.ilustris.sagai.features.widgets.domain.WidgetLinks
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/** Neutral colors for the states that have no saga to take a genre from (loading, error, shelf). */
object WidgetNeutral {
    const val BASE = 0xFF1A1A20.toInt()
    const val BASE_ALT = 0xFF101014.toInt()
    const val TEXT = 0xFFF2F0F6.toInt()
    const val MUTED = 0xFF9A97A6.toInt()
    const val RADIUS_DP = 22f
}

fun glanceColor(argb: Int): ColorProvider = ColorProvider(Color(argb))

fun glanceColor(
    argb: Int,
    alpha: Float,
): ColorProvider = ColorProvider(Color(argb).copy(alpha = alpha))

/** The Responsive size set for a widget: one [DpSize] per design, at the minimum dp of its cells. */
fun List<WidgetCells>.toDpSizes(): Set<DpSize> =
    map { DpSize(WidgetCells.minDp(it.columns).dp, WidgetCells.minDp(it.rows).dp) }.toSet()

/** The cells the launcher gave the layout being composed. */
@Composable
fun currentCells(): WidgetCells {
    val size = LocalSize.current
    return WidgetCells.fromDp(size.width.value, size.height.value)
}

/** Opens the app on [deepLink]. The data URI keeps each widget's PendingIntent distinct. */
fun deepLinkAction(
    context: Context,
    deepLink: String,
): Action =
    actionStartActivity(
        Intent(context, MainActivity::class.java).apply {
            putExtra("deepLink", deepLink)
            data = Uri.parse(deepLink)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        },
    )

@Composable
fun WidgetRoot(
    color: Int,
    cornerDp: Float,
    action: Action,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .background(glanceColor(color))
                .cornerRadius(cornerDp.dp)
                .clickable(action),
    ) {
        content()
    }
}

@Composable
fun WidgetText(
    text: String,
    color: Int,
    sizeSp: Float,
    weight: FontWeight = FontWeight.Normal,
    maxLines: Int = Int.MAX_VALUE,
    align: TextAlign? = null,
    modifier: GlanceModifier = GlanceModifier,
) {
    Text(
        text = text,
        modifier = modifier,
        maxLines = maxLines,
        style =
            TextStyle(
                color = glanceColor(color),
                fontSize = sizeSp.sp,
                fontWeight = weight,
                textAlign = align,
            ),
    )
}

/** The small uppercase label above a widget's content. */
@Composable
fun WidgetTag(
    text: String,
    color: Int,
) {
    WidgetText(text = text.uppercase(), color = color, sizeSp = 9f, weight = FontWeight.Bold, maxLines = 1)
}

/** A round button with an icon, drawn as two layers so it stays round on every Android version. */
@Composable
fun CircleButton(
    @DrawableRes icon: Int,
    color: Int,
    iconColor: Int,
    sizeDp: Int,
    description: String?,
) {
    Box(
        modifier = GlanceModifier.size(sizeDp.dp),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            provider = ImageProvider(R.drawable.widget_circle),
            contentDescription = description,
            modifier = GlanceModifier.fillMaxSize(),
            colorFilter = ColorFilter.tint(glanceColor(color)),
        )
        Image(
            provider = ImageProvider(icon),
            contentDescription = null,
            modifier = GlanceModifier.size((sizeDp * 0.5f).dp),
            colorFilter = ColorFilter.tint(glanceColor(iconColor)),
        )
    }
}

/** A drawable tinted with [color] and stretched over its box: the scrims and glows are drawn this way. */
@Composable
fun TintedLayer(
    @DrawableRes drawable: Int,
    color: ColorProvider,
    modifier: GlanceModifier = GlanceModifier.fillMaxSize(),
) {
    Image(
        provider = ImageProvider(drawable),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.FillBounds,
        colorFilter = ColorFilter.tint(color),
    )
}

sealed interface WidgetLoad<out T> {
    data object Loading : WidgetLoad<Nothing>

    data object Failed : WidgetLoad<Nothing>

    data class Ready<T>(
        val data: T,
    ) : WidgetLoad<T>
}

/**
 * Base for widgets that read data before they can draw. It paints a loading state at once, loads
 * in the background, and swaps in the content (or an error state) when the load ends.
 */
abstract class DataGlanceWidget<T : Any> : GlanceAppWidget() {
    protected abstract suspend fun load(
        context: Context,
        id: GlanceId,
    ): T

    @Composable
    protected abstract fun Content(data: T)

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        coroutineScope {
            val state = MutableStateFlow<WidgetLoad<T>>(WidgetLoad.Loading)
            launch {
                state.value =
                    try {
                        WidgetLoad.Ready(load(context, id))
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Timber.tag("DataGlanceWidget").e(e, "${this@DataGlanceWidget::class.simpleName} failed to load")
                        WidgetLoad.Failed
                    }
            }
            provideContent {
                val current by state.collectAsState()
                when (val loaded = current) {
                    WidgetLoad.Loading -> LoadingContent()
                    WidgetLoad.Failed -> ErrorContent()
                    is WidgetLoad.Ready -> Content(loaded.data)
                }
            }
        }
    }
}

/** Shown while a widget loads. The genre is not known yet, so it is neutral on purpose. */
@Composable
fun LoadingContent() {
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
            CircularProgressIndicator(
                modifier = GlanceModifier.size(if (compact) 22.dp else 28.dp),
                color = glanceColor(WidgetNeutral.TEXT),
            )
            if (!compact) {
                Spacer(modifier = GlanceModifier.height(8.dp))
                WidgetText(
                    text = context.getString(R.string.widget_loading_title),
                    color = WidgetNeutral.TEXT,
                    sizeSp = 12f,
                    weight = FontWeight.Medium,
                    maxLines = 2,
                )
            }
        }
    }
}

/** Shown when the load failed. It never stays blank or broken: a tap opens the app. */
@Composable
fun ErrorContent() {
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
            Image(
                provider = ImageProvider(R.drawable.ic_warning),
                contentDescription = null,
                modifier = GlanceModifier.size(if (compact) 20.dp else 28.dp),
                colorFilter = ColorFilter.tint(glanceColor(WidgetNeutral.MUTED)),
            )
            if (!compact) {
                Spacer(modifier = GlanceModifier.height(8.dp))
                WidgetText(
                    text = context.getString(R.string.widget_error_title),
                    color = WidgetNeutral.TEXT,
                    sizeSp = 12f,
                    weight = FontWeight.Medium,
                    maxLines = 2,
                )
                WidgetText(
                    text = context.getString(R.string.widget_error_subtitle),
                    color = WidgetNeutral.MUTED,
                    sizeSp = 10f,
                    maxLines = 2,
                )
            }
        }
    }
}

/**
 * [source] cropped to a box of [widthDp] x [heightDp], with the point ([focusX], [focusY]) placed at
 * [targetY] of the box's height. Cropped once per size and kept while the inputs stay the same.
 */
@Composable
fun rememberArt(
    source: Bitmap?,
    widthDp: Float,
    heightDp: Float,
    focusX: Float,
    focusY: Float,
    targetY: Float = 0.5f,
    fade: ArtFade = ArtFade.NONE,
): Bitmap? {
    val density = LocalContext.current.resources.displayMetrics.density
    return remember(source, widthDp, heightDp, focusX, focusY, targetY, fade) {
        source?.let {
            WidgetBitmaps.fade(
                WidgetBitmaps.crop(
                    source = it,
                    widthPx = (widthDp * density).toInt(),
                    heightPx = (heightDp * density).toInt(),
                    focusX = focusX,
                    focusY = focusY,
                    targetY = targetY,
                ),
                fade,
            )
        }
    }
}

/** A round face crop of [source], ringed with [ringColor]. Drawn once per size and kept. */
@Composable
fun rememberAvatar(
    source: Bitmap?,
    sizeDp: Int,
    ringColor: Int,
): Bitmap? {
    val density = LocalContext.current.resources.displayMetrics.density
    return remember(source, sizeDp, ringColor) {
        source?.let {
            WidgetBitmaps.circle(
                source = it,
                sizePx = (sizeDp * density).toInt(),
                ringColor = ringColor,
                ringWidthPx = 2f * density,
            )
        }
    }
}
