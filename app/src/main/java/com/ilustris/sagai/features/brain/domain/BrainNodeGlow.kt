package com.ilustris.sagai.features.brain.domain

import androidx.compose.ui.graphics.Color
import com.ilustris.sagai.features.brain.domain.model.BrainNodeType
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.wiki.data.model.WikiType
import com.ilustris.sagai.ui.theme.hexToColor
import com.ilustris.sagai.ui.theme.holographicGradient

object BrainNodeGlow {
    fun saga(genre: Genre?): Long = genrePrimaryArgb(genre, boost = 1f)

    fun character(
        hexColor: String,
        isMainCharacter: Boolean,
    ): Long {
        val base = hexColor.hexToColor() ?: Color(0xFF90E0EF)
        val factor = if (isMainCharacter) 1.2f else 1f
        return boostedArgb(base, factor)
    }

    fun event(emotionalAlpha: Float = 0.8f): Long {
        val alpha = (emotionalAlpha * 255).toInt().coerceIn(80, 255)
        return Color(0xFF, 0xE6, 0xFF, alpha).value.toLong()
    }

    fun actOrChapter(genre: Genre?): Long = genrePrimaryArgb(genre, boost = 0.85f)

    fun relation(genre: Genre?): Long = genrePrimaryArgb(genre, boost = 0.72f)

    fun wiki(type: WikiType?): Long {
        val color =
            when (type) {
                WikiType.LOCATION -> Color(0xFF6BCB77)
                WikiType.FACTION, WikiType.ORGANIZATION -> Color(0xFFE8A838)
                WikiType.ITEM -> Color(0xFF4ECDC4)
                WikiType.CREATURE -> Color(0xFF9B59B6)
                WikiType.CONCEPT -> Color(0xFFBB86FC)
                WikiType.TECHNOLOGY -> Color(0xFF64B5F6)
                WikiType.MAGIC -> Color(0xFFE040FB)
                WikiType.OTHER, null -> Color(0xFFB0BEC5)
            }
        return color.value.toLong()
    }

    fun defaultFor(
        type: BrainNodeType,
        genre: Genre? = null,
    ): Long =
        when (type) {
            BrainNodeType.SAGA -> saga(genre)
            BrainNodeType.ACT, BrainNodeType.CHAPTER -> actOrChapter(genre)
            BrainNodeType.EVENT -> event()
            BrainNodeType.RELATION -> relation(genre)
            BrainNodeType.CHARACTER -> Color(0xFF90E0EF).value.toLong()
            BrainNodeType.WIKI -> wiki(null)
        }

    private fun genrePrimaryArgb(
        genre: Genre?,
        boost: Float,
    ): Long {
        val base = genre?.themeColor() ?: holographicGradient.first()
        return boostedArgb(base, boost)
    }

    private fun Genre.themeColor(): Color =
        when (this) {
            Genre.FANTASY -> Color(0xFFB1A7F0)
            Genre.CYBERPUNK -> Color(0xFF90E0EF)
            Genre.HORROR -> Color(0xFF9B59B6)
            Genre.HEROES -> Color(0xFF64B5F6)
            Genre.CRIME -> Color(0xFFE57373)
            Genre.SHINOBI -> Color(0xFF81C784)
            Genre.SPACE_OPERA -> Color(0xFF4FC3F7)
            Genre.COWBOY -> Color(0xFFE8A838)
            Genre.PUNK_ROCK -> Color(0xFFFF7043)
        }

    private fun boostedArgb(
        color: Color,
        factor: Float,
    ): Long {
        val r = (color.red * factor).coerceIn(0f, 1f)
        val g = (color.green * factor).coerceIn(0f, 1f)
        val b = (color.blue * factor).coerceIn(0f, 1f)
        return Color(r, g, b, color.alpha).value.toLong()
    }
}
