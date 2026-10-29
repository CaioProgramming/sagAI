package com.ilustris.sagai.features.widgets.glance

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.google.gson.Gson
import com.ilustris.sagai.features.widgets.domain.WidgetConfig

/**
 * Keeps a generated widget's configuration in that widget's own Glance state. It lives and dies
 * with the widget, so there is no registry to clean up when the player removes it.
 */
object WidgetConfigState {
    private val KEY = stringPreferencesKey("widget_config")
    private val gson = Gson()

    fun encode(config: WidgetConfig): String = gson.toJson(config)

    fun decode(json: String?): WidgetConfig? =
        if (json.isNullOrBlank()) {
            null
        } else {
            try {
                // Gson leaves an unknown enum (a downgrade) as null in a non-null field.
                gson.fromJson(json, WidgetConfig::class.java)?.takeIf { it.isUsable() }
            } catch (_: Exception) {
                null
            }
        }

    suspend fun read(
        context: Context,
        id: GlanceId,
    ): WidgetConfig? = decode(getAppWidgetState(context, PreferencesGlanceStateDefinition, id)[KEY])

    suspend fun write(
        context: Context,
        id: GlanceId,
        config: WidgetConfig,
    ) {
        updateAppWidgetState(context, id) { prefs -> prefs[KEY] = encode(config) }
    }
}
