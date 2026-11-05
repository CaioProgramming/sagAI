package com.ilustris.sagai.features.widgets.domain

/** What the device says about widgets, read once and handed to [WidgetInvitePolicy]. */
data class WidgetInviteEnv(
    /** The launcher can show the "add widget" dialog. */
    val pinSupported: Boolean = false,
    /** Widgets of any kind already on the home screen. */
    val installedWidgets: Int = 0,
    /** The player closed the home card, which retires it for good. */
    val dismissed: Boolean = false,
)

/** When the app offers to put a widget on the home screen. Kept apart from the UI so the rules are testable. */
object WidgetInvitePolicy {
    /** The home card shows from the fourth active saga on. */
    const val ACTIVE_SAGAS_THRESHOLD = 3

    fun showHomeCard(
        activeSagas: Int,
        env: WidgetInviteEnv,
        anotherLayerOpen: Boolean,
    ): Boolean =
        activeSagas > ACTIVE_SAGAS_THRESHOLD &&
            env.pinSupported &&
            env.installedWidgets == 0 &&
            !env.dismissed &&
            !anotherLayerOpen

    /** The link on the last rewards page: only for a player who still has no widget at all. */
    fun showRewardsLink(env: WidgetInviteEnv): Boolean = env.pinSupported && env.installedWidgets == 0
}
