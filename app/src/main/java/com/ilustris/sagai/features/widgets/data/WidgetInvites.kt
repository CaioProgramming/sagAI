package com.ilustris.sagai.features.widgets.data

import android.content.Context
import com.ilustris.sagai.core.datastore.DataStorePreferences
import com.ilustris.sagai.features.widgets.domain.WidgetInviteEnv
import com.ilustris.sagai.features.widgets.glance.WidgetPinner
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/** Reads the device and the player's choices that decide whether a widget invite may show. */
@Singleton
class WidgetInvites
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val dataStore: DataStorePreferences,
    ) {
        suspend fun env(): WidgetInviteEnv =
            try {
                WidgetInviteEnv(
                    pinSupported = WidgetPinner.isSupported(context),
                    installedWidgets = WidgetPinner.installedCount(context),
                    dismissed = dataStore.getBooleanNow(DISMISSED_KEY),
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.tag(TAG).w(e, "Could not read the widget state")
                WidgetInviteEnv()
            }

        suspend fun dismiss() = dataStore.setBoolean(DISMISSED_KEY, true)

        /** Opens the launcher's dialog for the stories widget. */
        suspend fun pinStories(): Boolean =
            try {
                WidgetPinner.pinStories(context)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Could not request the stories widget")
                false
            }

        private companion object {
            const val TAG = "WidgetInvites"
            const val DISMISSED_KEY = "widget_invite_dismissed"
        }
    }
