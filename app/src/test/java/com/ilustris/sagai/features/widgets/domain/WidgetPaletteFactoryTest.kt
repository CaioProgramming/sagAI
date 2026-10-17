package com.ilustris.sagai.features.widgets.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetPaletteFactoryTest {
    // The colorPalette arrays from the Remote Config genre visual configs (version 1121).
    private val genres =
        mapOf(
            "fantasy" to (0xFF8B2635.toInt() to listOf("#050204", "#1A0810", "#5E1A24", "#8B2635", "#D44D63", "#FFB4C2", "#FFD166")),
            "cyberpunk" to (0xFF6E48AA.toInt() to listOf("#03020A", "#0A0420", "#1A0A3D", "#6E48AA", "#9D50BB", "#00F5FF", "#FF2EFF", "#E8DCFF")),
            "horror" to (0xFF748B97.toInt() to listOf("#020408", "#0B132B", "#1C2541", "#3A506B", "#748B97", "#4ADEFF", "#6FFFE9", "#E8FFFE")),
            "heroes" to (0xFF2845D6.toInt() to listOf("#020412", "#0D1A63", "#1A2CA3", "#2845D6", "#4169F5", "#00D9FF", "#FDC500", "#E6E9FF")),
            "crime" to (0xFFE91E63.toInt() to listOf("#1A0F2E", "#3D1B5C", "#C2185B", "#E91E63", "#FF6B9D", "#7FFFEE")),
            "shinobi" to (0xFF8B0000.toInt() to listOf("#5C2751", "#431C3A", "#7A336B", "#F39237")),
            "space" to (0xFF0081A7.toInt() to listOf("#000510", "#001219", "#004E63", "#0081A7", "#00AFB9", "#48CAE4", "#90E0EF", "#C9B1FF")),
            "cowboy" to (0xFF8B4513.toInt() to listOf("#1a0a00", "#a84520", "#fce8a8", "#ff99bb", "#d4612f", "#8b3a0f")),
            "punk" to (0xFF729656.toInt() to listOf("#0A0C0F", "#1A1F2A", "#00E676", "#FF7E00", "#FAFF00")),
        ).mapValues { (_, value) -> value.first to value.second.map { argb(it) } }

    private fun argb(hex: String): Int = (0xFF000000 or hex.removePrefix("#").toLong(16)).toInt()

    @Test
    fun `text reads on the base in every genre, dark and light`() {
        genres.forEach { (name, config) ->
            listOf(false, true).forEach { light ->
                val palette = WidgetPaletteFactory.create(config.second, config.first, light)
                assertTrue(
                    "$name light=$light text on base",
                    WidgetPaletteFactory.contrast(palette.text, palette.base) >= 4.5,
                )
                assertTrue(
                    "$name light=$light muted on base",
                    WidgetPaletteFactory.contrast(palette.textMuted, palette.base) >= 4.5,
                )
            }
        }
    }

    @Test
    fun `the icon on the accent button reads in every genre`() {
        genres.forEach { (name, config) ->
            listOf(false, true).forEach { light ->
                val palette = WidgetPaletteFactory.create(config.second, config.first, light)
                assertTrue(
                    "$name light=$light icon on accent",
                    WidgetPaletteFactory.contrast(palette.onAccent, palette.accent) >= 4.5,
                )
            }
        }
    }

    @Test
    fun `the accent reads on a light base`() {
        genres.forEach { (name, config) ->
            val palette = WidgetPaletteFactory.create(config.second, config.first, true)
            assertTrue(
                "$name accent on paper",
                WidgetPaletteFactory.contrast(palette.accent, palette.base) >= 4.5,
            )
        }
    }

    @Test
    fun `a dark base is dark and a light base is light`() {
        genres.forEach { (name, config) ->
            val dark = WidgetPaletteFactory.create(config.second, config.first, false)
            val light = WidgetPaletteFactory.create(config.second, config.first, true)
            assertTrue("$name dark base", WidgetPaletteFactory.luminance(dark.base) <= 0.08)
            assertTrue("$name light base", WidgetPaletteFactory.luminance(light.base) > 0.5)
            assertFalse(dark.isLight)
            assertTrue(light.isLight)
        }
    }

    @Test
    fun `an empty palette still produces a usable one from the primary color`() {
        val palette = WidgetPaletteFactory.create(emptyList(), 0xFF8B2635.toInt(), false)
        assertTrue(WidgetPaletteFactory.contrast(palette.text, palette.base) >= 4.5)
    }

    @Test
    fun `the glow of a dark palette is a color from the palette and not a near black`() {
        val config = genres.getValue("cyberpunk")
        val palette = WidgetPaletteFactory.create(config.second, config.first, false)
        assertTrue(WidgetPaletteFactory.luminance(palette.glow) > 0.05)
    }

    @Test
    fun `contrast of black on white is the maximum`() {
        assertEquals(21.0, WidgetPaletteFactory.contrast(0xFF000000.toInt(), 0xFFFFFFFF.toInt()), 0.01)
    }

    @Test
    fun `ensure contrast darkens until the color reads`() {
        val pale = 0xFFFFD166.toInt()
        val fixed = WidgetPaletteFactory.ensureContrast(pale, 0xFFF1EDE3.toInt(), 4.5)
        assertTrue(WidgetPaletteFactory.contrast(fixed, 0xFFF1EDE3.toInt()) >= 4.5)
    }
}
