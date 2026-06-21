package com.ilustris.sagai.features.brain.ui.components

import androidx.compose.ui.graphics.Color
import com.ilustris.sagai.features.brain.domain.model.BrainNode
import com.ilustris.sagai.features.brain.domain.model.BrainNodeLayout
import com.ilustris.sagai.features.brain.domain.model.BrainNodeType
import com.ilustris.sagai.features.brain.domain.model.glowColor
import com.ilustris.sagai.features.brain.domain.model.satelliteScale
import com.ilustris.sagai.features.brain.domain.model.starScale
import com.ilustris.sagai.ui.theme.darker

object BrainStarGlow {
    private val CanvasStarWhite = Color.White

    fun color(
        node: BrainNode,
        isSatellite: Boolean,
        isSelected: Boolean,
        primary: Color,
        secondary: Color,
    ): Color {
        val base =
            when (node.type) {
                BrainNodeType.SAGA -> primary
                BrainNodeType.ACT -> primary.darker(0.08f)
                BrainNodeType.CHAPTER -> primary.darker(0.16f)
                BrainNodeType.EVENT -> primary.darker(0.24f)
                BrainNodeType.CHARACTER_EVENT -> primary.darker(0.2f)
                BrainNodeType.RELATION -> primary.darker(0.12f)
                BrainNodeType.CHARACTER -> node.glowColor()
                BrainNodeType.WIKI -> secondary
            }
        return when {
            isSelected -> base
            isSatellite -> base.darker(0.38f)
            else -> base.darker(0.28f)
        }
    }

    /** White core — selection uses twinkle; unselected stays bright, attention shifts to glow. */
    fun starCoreColor(
        isSelected: Boolean,
        twinkle: Float,
    ): Color =
        if (isSelected) {
            CanvasStarWhite.copy(alpha = twinkle)
        } else {
            CanvasStarWhite.copy(alpha = 0.92f)
        }

    fun haloAlpha(
        node: BrainNode,
        isSelected: Boolean,
        twinkle: Float,
    ): Float =
        when {
            isSelected && node.type == BrainNodeType.CHARACTER -> {
                (0.95f * twinkle).coerceIn(
                    0.75f,
                    1f,
                )
            }

            isSelected -> {
                0.9f * twinkle
            }

            else -> {
                0.72f
            }
        }

    fun selectedGlowBlurFactor(node: BrainNode): Float = if (node.type == BrainNodeType.CHARACTER) 2.6f else 2.0f

    fun selectedGlowSpreadFactor(node: BrainNode): Float = if (node.type == BrainNodeType.CHARACTER) 1.85f else 1.5f

    fun starVisualRadius(
        node: BrainNode,
        layout: BrainNodeLayout,
        centerNodeId: String,
        isSatellite: Boolean,
        isSelected: Boolean,
    ): Float {
        val isCenter = node.id == centerNodeId
        val typeScale =
            if (isSatellite && !isSelected) {
                node.type.satelliteScale()
            } else {
                node.type.starScale(isCenter)
            }
        val presenceScale =
            when {
                isSelected -> 1f
                isSatellite -> 0.88f
                else -> 0.9f
            }
        return layout.radius * typeScale * presenceScale
    }
}
