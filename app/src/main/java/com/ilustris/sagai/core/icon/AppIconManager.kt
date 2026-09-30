package com.ilustris.sagai.core.icon

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.ilustris.sagai.core.datastore.DataStorePreferences
import com.ilustris.sagai.core.theme.SagaThemeManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Switches the launcher icon by enabling one `<activity-alias>` and disabling the others.
 *
 * The system is the source of truth for which icon is active, so nothing is stored for it. Two things
 * to know: the launcher takes a few seconds to redraw, and a home-screen shortcut that pointed at the
 * alias being disabled may be dropped by some launchers.
 *
 * Disabling the alias an open screen was launched through makes the system finish that screen, so the
 * app would close in the player's face. That is why the icon is never switched while the app is on
 * screen: a choice made in the picker ([choose]) and the "follow the last saga" option
 * ([followsLastSaga]) are both applied once the app goes to the background ([applyOnBackground]).
 */
@Singleton
class AppIconManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val dataStore: DataStorePreferences,
        private val sagaThemeManager: SagaThemeManager,
    ) {
        private val _selected = MutableStateFlow(readCurrent())

        /** The icon the player picked. It can be ahead of what the launcher shows until [applyOnBackground] runs. */
        val selected: StateFlow<LauncherIcon> = _selected.asStateFlow()

        private var pending: LauncherIcon? = null

        val followsLastSaga: Flow<Boolean> = dataStore.getBoolean(FOLLOW_LAST_SAGA_KEY, false)

        suspend fun setFollowsLastSaga(enabled: Boolean) = dataStore.setBoolean(FOLLOW_LAST_SAGA_KEY, enabled)

        /** Remembers the player's pick; the launcher only changes when the app leaves the foreground. */
        fun choose(icon: LauncherIcon) {
            _selected.value = icon
            pending = icon.takeIf { it != readCurrent() }
        }

        /** Applies what is waiting: the player's pick, or else the last saga's icon when that option is on. */
        suspend fun applyOnBackground() {
            pending?.let {
                pending = null
                select(it)
                return
            }
            if (!followsLastSaga.first()) return
            val genre = sagaThemeManager.lastSagaGenre ?: return
            select(LauncherIcon.forGenre(genre))
        }

        private fun select(icon: LauncherIcon) {
            if (icon == readCurrent()) {
                _selected.value = icon
                return
            }
            // Enable the new alias before disabling the old ones, so there is never a moment without a launcher entry.
            setState(icon, PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
            LauncherIcon.entries.filter { it != icon }.forEach {
                setState(it, PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
            }
            _selected.value = icon
            Timber.d("AppIconManager: launcher icon is now $icon")
        }

        private fun setState(
            icon: LauncherIcon,
            state: Int,
        ) {
            try {
                context.packageManager.setComponentEnabledSetting(componentOf(icon), state, PackageManager.DONT_KILL_APP)
            } catch (e: Exception) {
                Timber.e(e, "AppIconManager: could not update ${icon.aliasName}")
            }
        }

        private fun readCurrent(): LauncherIcon =
            LauncherIcon.entries.firstOrNull { isEnabled(it) } ?: LauncherIcon.DEFAULT

        private fun isEnabled(icon: LauncherIcon): Boolean =
            try {
                when (context.packageManager.getComponentEnabledSetting(componentOf(icon))) {
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED -> false
                    // Not overridden yet: the manifest decides, and only the default alias is enabled there.
                    else -> icon == LauncherIcon.DEFAULT
                }
            } catch (e: IllegalArgumentException) {
                Timber.w(e, "AppIconManager: alias ${icon.aliasName} not found")
                false
            }

        private fun componentOf(icon: LauncherIcon) = ComponentName(context.packageName, "$ALIAS_PACKAGE.${icon.aliasName}")

        private companion object {
            const val FOLLOW_LAST_SAGA_KEY = "app_icon_follows_last_saga"
            const val ALIAS_PACKAGE = "com.ilustris.sagai.launcher"
        }
    }
