package com.ilustris.sagai.features.widgets.domain

import com.ilustris.sagai.ui.navigation.CharacterDetailKey
import com.ilustris.sagai.ui.navigation.ChatKey
import com.ilustris.sagai.ui.navigation.HomeKey
import com.ilustris.sagai.ui.navigation.NewSagaKey
import com.ilustris.sagai.ui.navigation.SagaDetailKey
import com.ilustris.sagai.ui.navigation.findNavKey
import org.junit.Assert.assertEquals
import org.junit.Test

/** The widgets reuse the notification deep links, so each one has to land where the widget says it does. */
class WidgetLinksTest {
    @Test
    fun `chat link opens that saga's chat`() {
        assertEquals(ChatKey("12"), WidgetLinks.chat(12).findNavKey())
    }

    @Test
    fun `saga detail link opens that saga`() {
        assertEquals(SagaDetailKey("7"), WidgetLinks.sagaDetail(7).findNavKey())
    }

    @Test
    fun `character link opens that character`() {
        assertEquals(CharacterDetailKey(9), WidgetLinks.character(sagaId = 3, characterId = 9).findNavKey())
    }

    @Test
    fun `new saga and home links open their screens`() {
        assertEquals(NewSagaKey, WidgetLinks.NEW_SAGA.findNavKey())
        assertEquals(HomeKey, WidgetLinks.HOME.findNavKey())
    }
}
