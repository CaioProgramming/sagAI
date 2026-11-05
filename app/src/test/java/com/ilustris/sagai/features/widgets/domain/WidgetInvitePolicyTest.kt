package com.ilustris.sagai.features.widgets.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetInvitePolicyTest {
    private val ready = WidgetInviteEnv(pinSupported = true, installedWidgets = 0, dismissed = false)

    private fun card(
        sagas: Int = 4,
        env: WidgetInviteEnv = ready,
        busy: Boolean = false,
    ) = WidgetInvitePolicy.showHomeCard(sagas, env, busy)

    @Test
    fun `the card needs more than three active sagas`() {
        assertFalse(card(sagas = 3))
        assertTrue(card(sagas = 4))
    }

    @Test
    fun `a launcher that cannot pin widgets never sees the card`() {
        assertFalse(card(env = ready.copy(pinSupported = false)))
    }

    @Test
    fun `any widget already on the home screen hides the card`() {
        assertFalse(card(env = ready.copy(installedWidgets = 1)))
    }

    @Test
    fun `a dismissed card stays gone`() {
        assertFalse(card(env = ready.copy(dismissed = true)))
    }

    @Test
    fun `the card waits while a sheet or the onboarding is open`() {
        assertFalse(card(busy = true))
    }

    @Test
    fun `the rewards link needs pin support and no widget yet, and ignores the dismissed card`() {
        assertTrue(WidgetInvitePolicy.showRewardsLink(ready.copy(dismissed = true)))
        assertFalse(WidgetInvitePolicy.showRewardsLink(ready.copy(pinSupported = false)))
        assertFalse(WidgetInvitePolicy.showRewardsLink(ready.copy(installedWidgets = 2)))
    }
}
