package com.ilustris.sagai.features.saga.chat

import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.features.saga.chat.data.model.InputMode
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.PlayerInputFeedback
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.data.usecase.PlayerInputCorrection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerInputCorrectionTest {
    private fun typed(text: String) = Message(text = text, senderType = SenderType.USER, timelineId = 1, inputMode = InputMode.TYPED)

    private fun voice() = Message(text = "", senderType = SenderType.USER, timelineId = 1, inputMode = InputMode.VOICE)

    @Test
    fun `typed typo fix is applied and the original is kept`() {
        val original = typed("nao vou deixar vc sozinha <action>seguro a mao dela</action>")
        val fixed = "Não vou deixar você sozinha. <action>Seguro a mão dela.</action>"

        val result = PlayerInputCorrection.apply(original, PlayerInputFeedback(correctedText = fixed))

        assertEquals(fixed, result.message.text)
        assertEquals(original.text, result.message.originalText)
        assertFalse(result.needsTranscription)
    }

    @Test
    fun `typed correction that writes new content is rejected`() {
        val original = typed("Eu confio nele.")
        val rewritten = "<think>No fundo, eu não confio e tenho medo do que ele esconde.</think> Eu confio nele."

        val result = PlayerInputCorrection.apply(original, PlayerInputFeedback(correctedText = rewritten))

        assertEquals(original.text, result.message.text)
        assertNull(result.message.originalText)
    }

    @Test
    fun `typed correction that drops a tag the player wrote is rejected`() {
        val original = typed("<action>seguro a mao dela</action> fica aqui")

        val result = PlayerInputCorrection.apply(original, PlayerInputFeedback(correctedText = "Seguro a mão dela. Fica aqui."))

        assertEquals(original.text, result.message.text)
    }

    @Test
    fun `tone is applied even when the text is left alone`() {
        val original = typed("Fica aqui.")

        val result =
            PlayerInputCorrection.apply(
                original,
                PlayerInputFeedback(correctedText = null, emotionalTone = EmotionalTone.entries.first()),
            )

        assertEquals(original.text, result.message.text)
        assertEquals(EmotionalTone.entries.first(), result.message.emotionalTone)
    }

    @Test
    fun `voice correction fills the empty message`() {
        val text = "<action>Puxo a espada.</action> Ninguém passa daqui. <think>Por dentro, tô morrendo de medo.</think>"

        val result = PlayerInputCorrection.apply(voice(), PlayerInputFeedback(correctedText = text), audioDurationMs = 6_000)

        assertEquals(text, result.message.text)
        assertFalse(result.needsTranscription)
    }

    @Test
    fun `voice correction too long for the recording needs transcription`() {
        val text = List(40) { "palavra" }.joinToString(" ")

        val result = PlayerInputCorrection.apply(voice(), PlayerInputFeedback(correctedText = text), audioDurationMs = 2_000)

        assertEquals("", result.message.text)
        assertTrue(result.needsTranscription)
    }

    @Test
    fun `missing voice correction needs transcription`() {
        val result = PlayerInputCorrection.apply(voice(), null, audioDurationMs = 3_000)

        assertTrue(result.needsTranscription)
    }

    @Test
    fun `tags must be flat closed and known`() {
        assertTrue(PlayerInputCorrection.isWellFormed("Oi. <action>Aceno.</action> <think>Hm.</think>"))
        assertFalse(PlayerInputCorrection.isWellFormed("<action>Aceno.<think>Hm.</think></action>"))
        assertFalse(PlayerInputCorrection.isWellFormed("<action>Aceno."))
        assertFalse(PlayerInputCorrection.isWellFormed("<shout>Ei!</shout>"))
    }
}
