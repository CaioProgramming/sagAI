package com.ilustris.sagai.core.services.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class MascotExpressionTest {
    // The table published to Remote Config under default_mascot_expressions.
    private val published =
        "{" +
            "\"neutral\": {\"w\": 1, \"h\": 1, \"rot\": -0.8, \"lid\": 0, \"lidTilt\": 0, \"dy\": 0, \"asym\": 0, \"arc\": 0, \"posture\": 0, \"headTilt\": 0, \"headDip\": 0, \"noseDy\": 0, \"noseS\": 1, \"tempo\": 1}" +
            ",\"calm\": {\"w\": 1.05, \"h\": 1, \"rot\": -0.3, \"lid\": 0, \"lidTilt\": 0, \"dy\": 0.03, \"asym\": 0, \"arc\": -1, \"posture\": -0.25, \"headTilt\": 0.05, \"headDip\": 0.03, \"noseDy\": 0.01, \"noseS\": 0.95, \"gy\": 0.1, \"glance\": 0.35, \"tempo\": 0.6}" +
            ",\"curious\": {\"w\": 1.1, \"h\": 1.2, \"rot\": -0.26, \"lid\": 0.14, \"lidTilt\": 0.1, \"dy\": -0.04, \"asym\": 0.55, \"arc\": 0, \"cheek\": 0.05, \"posture\": 0.5, \"headTilt\": 0.13, \"headDip\": -0.03, \"noseDy\": -0.02, \"noseS\": 1.08, \"gx\": 0.28, \"gy\": -0.25, \"glance\": 1.1, \"tempo\": 1.25}" +
            ",\"hopeful\": {\"w\": 1.08, \"h\": 1.22, \"rot\": -0.15, \"lid\": 0, \"lidTilt\": 0, \"dy\": -0.07, \"asym\": 0, \"arc\": 0, \"cheek\": 0.16, \"posture\": 0.6, \"headTilt\": -0.08, \"headDip\": -0.06, \"noseDy\": -0.02, \"noseS\": 1.06, \"gy\": -0.45, \"glance\": 0.8, \"tempo\": 1.3}" +
            ",\"determined\": {\"w\": 1.06, \"h\": 0.92, \"rot\": -0.85, \"lid\": 0.14, \"lidTilt\": -0.1, \"dy\": 0, \"asym\": 0, \"arc\": 0, \"posture\": 0.45, \"headTilt\": 0, \"headDip\": -0.04, \"noseDy\": -0.01, \"noseS\": 1.05, \"gy\": -0.05, \"glance\": 0.3, \"tempo\": 1.15}" +
            ",\"empathetic\": {\"w\": 1.02, \"h\": 1.1, \"rot\": 0.1, \"lid\": 0.1, \"lidTilt\": 0.42, \"dy\": 0.03, \"asym\": 0, \"arc\": 0, \"cheek\": 0.08, \"posture\": -0.05, \"headTilt\": 0.1, \"headDip\": 0.02, \"noseDy\": 0.01, \"noseS\": 0.98, \"tempo\": 0.88}" +
            ",\"joyful\": {\"w\": 1.1, \"h\": 0.95, \"rot\": -0.1, \"lid\": 0, \"lidTilt\": 0, \"dy\": 0, \"asym\": 0, \"arc\": 1, \"posture\": 0.65, \"headTilt\": -0.04, \"headDip\": -0.05, \"noseDy\": -0.02, \"noseS\": 1.05, \"tempo\": 1.45}" +
            ",\"concerned\": {\"w\": 1, \"h\": 1.12, \"rot\": 0.4, \"lid\": 0.12, \"lidTilt\": 0.8, \"dy\": -0.01, \"asym\": 0, \"arc\": 0, \"posture\": -0.3, \"headTilt\": -0.09, \"headDip\": 0.03, \"noseDy\": 0.01, \"noseS\": 0.95, \"gy\": 0.15, \"glance\": 0.7, \"tempo\": 0.95}" +
            ",\"anxious\": {\"w\": 0.95, \"h\": 1.28, \"rot\": 0.1, \"lid\": 0, \"lidTilt\": 0, \"dy\": -0.02, \"asym\": 0.1, \"arc\": 0, \"posture\": -0.15, \"headTilt\": 0, \"headDip\": 0, \"noseDy\": 0, \"noseS\": 1, \"glance\": 1.5, \"tempo\": 1.7, \"jit\": 0.45}" +
            ",\"frustrated\": {\"w\": 1.06, \"h\": 0.88, \"rot\": -0.72, \"lid\": 0.42, \"lidTilt\": -0.04, \"dy\": 0.01, \"asym\": 0, \"arc\": 0, \"posture\": 0.35, \"headTilt\": -0.03, \"headDip\": 0.04, \"noseDy\": -0.03, \"noseS\": 1.1, \"gx\": 0.4, \"glance\": 0.5, \"tempo\": 1.3, \"jit\": 0.22}" +
            ",\"angry\": {\"w\": 1.1, \"h\": 0.84, \"rot\": -0.9, \"lid\": 0.3, \"lidTilt\": -0.16, \"dy\": 0, \"asym\": 0, \"arc\": 0, \"posture\": 0.9, \"headTilt\": 0, \"headDip\": 0.05, \"noseDy\": -0.04, \"noseS\": 1.15, \"glance\": 0.15, \"tempo\": 1.65, \"jit\": 0.75}" +
            ",\"sad\": {\"w\": 1, \"h\": 0.9, \"rot\": 0.35, \"lid\": 0.24, \"lidTilt\": 0.3, \"dy\": 0.1, \"asym\": 0, \"arc\": 0, \"posture\": -0.8, \"headTilt\": 0.05, \"headDip\": 0.09, \"noseDy\": 0.02, \"noseS\": 0.9, \"gy\": 0.5, \"glance\": 0.35, \"tempo\": 0.62, \"jit\": 0.1}" +
            ",\"melancholic\": {\"w\": 0.94, \"h\": 0.62, \"rot\": 0.26, \"lid\": 0.42, \"lidTilt\": 0.16, \"dy\": 0.11, \"asym\": 0, \"arc\": 0, \"posture\": -0.55, \"headTilt\": 0.05, \"headDip\": 0.07, \"noseDy\": 0.02, \"noseS\": 0.9, \"gx\": -0.45, \"gy\": 0.45, \"glance\": 0.3, \"tempo\": 0.5}" +
            ",\"cynical\": {\"w\": 1.02, \"h\": 0.82, \"rot\": -0.6, \"lid\": 0.45, \"lidTilt\": -0.06, \"dy\": 0.03, \"asym\": 0.32, \"arc\": 0, \"posture\": -0.05, \"headTilt\": 0.08, \"headDip\": 0.01, \"noseDy\": 0, \"noseS\": 0.96, \"gx\": 0.38, \"gy\": 0.04, \"glance\": 0.3, \"tempo\": 0.8}" +
            "}"

    private fun parse(json: String): Map<String, MascotExpression> =
        Gson().fromJson(json, object : TypeToken<Map<String, MascotExpression>>() {}.type)

    @Test
    fun `every emotional tone has a valid published expression`() {
        val table = parse(published)

        assertEquals(EmotionalTone.entries.map { it.name.lowercase() }.toSet(), table.keys)
        table.forEach { (tone, expression) ->
            assertNotNull("$tone should be within the renderer's ranges", expression.sanitized())
        }
    }

    @Test
    fun `unset optional fields resolve to their neutral value`() {
        val neutral = parse(published).getValue("neutral").sanitized()!!

        assertEquals(1f, neutral.glance, 0f)
        assertEquals(1f, neutral.tempo, 0f)
        assertEquals(1f, neutral.noseS, 0f)
        assertEquals(0f, neutral.cheek, 0f)
    }

    @Test
    fun `out of range values drop the tone`() {
        assertNull(MascotExpression(w = 1f, h = 1f, rot = 3f).sanitized())
        assertNull(MascotExpression(w = 0f, h = 1f).sanitized())
        assertNull(MascotExpression(w = 1f, h = 1f, lid = 0.9f).sanitized())
        assertNull(MascotExpression(w = 1f, h = 1f, arc = 2f).sanitized())
    }
}
