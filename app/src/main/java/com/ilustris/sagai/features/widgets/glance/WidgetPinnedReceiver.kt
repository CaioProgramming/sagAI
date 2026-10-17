package com.ilustris.sagai.features.widgets.glance

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.ilustris.sagai.features.widgets.domain.LinesType
import com.ilustris.sagai.features.widgets.domain.WidgetConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Called by the system once the player confirms the "add widget" dialog. The pin request carried
 * the widget's configuration, and this is where it is attached to the widget that was just created.
 */
class WidgetPinnedReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val config = WidgetConfigState.decode(intent.getStringExtra(EXTRA_CONFIG))
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID || config == null) {
            Timber.tag(TAG).w("Ignoring a pin callback without a widget id or configuration")
            return
        }
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
                WidgetConfigState.write(context, glanceId, config)
                val widget = if (config.type == LinesType.QUOTE) QuoteWidget() else HookWidget()
                widget.update(context, glanceId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Could not attach the configuration to widget $appWidgetId")
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        private const val TAG = "WidgetPinnedReceiver"
        private const val EXTRA_CONFIG = "widget_config"

        /** The callback the system fires on confirmation. It must be mutable so the widget id can be added to it. */
        fun callback(
            context: Context,
            config: WidgetConfig,
        ): PendingIntent {
            val intent =
                Intent(context, WidgetPinnedReceiver::class.java)
                    .putExtra(EXTRA_CONFIG, WidgetConfigState.encode(config))
            val flags =
                PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
            return PendingIntent.getBroadcast(context, config.seed, intent, flags)
        }
    }
}

/** Asks the launcher to add a generated widget to the home screen. */
object WidgetPinner {
    fun isSupported(context: Context): Boolean = AppWidgetManager.getInstance(context).isRequestPinAppWidgetSupported

    /** How many generated widgets the player already has on the home screen. */
    suspend fun count(context: Context): Int {
        val manager = GlanceAppWidgetManager(context)
        return manager.getGlanceIds(QuoteWidget::class.java).size + manager.getGlanceIds(HookWidget::class.java).size
    }

    /** True when the launcher showed its confirmation dialog. */
    suspend fun pin(
        context: Context,
        config: WidgetConfig,
    ): Boolean {
        val manager = GlanceAppWidgetManager(context)
        val callback = WidgetPinnedReceiver.callback(context, config)
        return if (config.type == LinesType.QUOTE) {
            manager.requestPinGlanceAppWidget(receiver = QuoteWidgetReceiver::class.java, successCallback = callback)
        } else {
            manager.requestPinGlanceAppWidget(receiver = HookWidgetReceiver::class.java, successCallback = callback)
        }
    }
}
