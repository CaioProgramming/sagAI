package com.ilustris.sagai.features.widgets

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.ilustris.sagai.core.datastore.DataStorePreferences
import com.ilustris.sagai.features.widgets.glance.ContinueWidget
import com.ilustris.sagai.features.widgets.glance.HookWidget
import com.ilustris.sagai.features.widgets.glance.LastMessageWidget
import com.ilustris.sagai.features.widgets.glance.QuoteWidget
import com.ilustris.sagai.features.widgets.glance.StoriesWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Asks every home screen widget to redraw.
 *
 * Widgets only change when the player plays, so this runs when the app comes forward (to rotate
 * generated lines) and when it goes away (to show what just happened), and never on a timer.
 */
@Singleton
class WidgetUpdater
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val dataStore: DataStorePreferences,
    ) {
        /** Counts an app open, which is what the "each app open" rotation advances on, then redraws. */
        suspend fun onAppOpened() {
            try {
                dataStore.setInt(OPEN_COUNTER_KEY, dataStore.getIntNow(OPEN_COUNTER_KEY) + 1)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.tag(TAG).w(e, "Could not count the app open")
            }
            updateAll()
        }

        suspend fun updateAll() {
            updateOne("continue") { ContinueWidget().updateAll(context) }
            updateOne("last message") { LastMessageWidget().updateAll(context) }
            updateOne("stories") { StoriesWidget().updateAll(context) }
            updateOne("quote") { QuoteWidget().updateAll(context) }
            updateOne("hook") { HookWidget().updateAll(context) }
        }

        /** One broken widget must not stop the others from updating. */
        private suspend fun updateOne(
            name: String,
            block: suspend () -> Unit,
        ) {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.tag(TAG).w(e, "Could not update the $name widget")
            }
        }

        companion object {
            const val OPEN_COUNTER_KEY = "widgets_open_counter"
            private const val TAG = "WidgetUpdater"
        }
    }
