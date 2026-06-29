package com.ilustris.sagai.features.brain.ui.components

import androidx.compose.ui.graphics.Color
import com.ilustris.sagai.features.brain.domain.model.BrainNode
import com.ilustris.sagai.features.brain.domain.model.BrainNodeLayout
import com.ilustris.sagai.features.brain.domain.model.BrainNodeType
import com.ilustris.sagai.features.brain.domain.model.glowColor
import com.ilustris.sagai.features.brain.domain.model.satelliteScale
import com.ilustris.sagai.features.brain.domain.model.starScale

object BrainStarGlow {
    fun color(
        node: BrainNode,
        isSatellite: Boolean,
        isSelected: Boolean,
        primary: Color,
        secondary: Color,
    ): Color {
        val raw =
            when (node.type) {
                BrainNodeType.SAGA -> primary
                BrainNodeType.ACT -> primary
                BrainNodeType.CHAPTER -> primary
                BrainNodeType.EVENT -> primary
                BrainNodeType.CHARACTER_EVENT -> primary
                BrainNodeType.RELATION -> primary
                BrainNodeType.CHARACTER -> node.glowColor()
                BrainNodeType.WIKI -> secondary
            }
        val base = raw.visibleOnCosmicCanvas(primary)
        return when {
            isSelected -> base
            isSatellite -> base.withMinAlpha(0.52f)
            else -> base.withMinAlpha(0.62f)
        }
    }

    /** Star core matches the glow hue — brighter when selected. */
    fun starCoreColor(
        glowColor: Color,
        isSelected: Boolean,
        twinkle: Float,
    ): Color {
        val brightCore =
            glowColor.brightenToward(Color.White, targetLuminance = 0.82f)
        return if (isSelected) {
            brightCore.copy(alpha = twinkle.coerceIn(0.88f, 1f))
        } else {
            brightCore.copy(alpha = 0.94f)
        }
    }

    fun selectedAccentColor(
        glowColor: Color,
        twinkle: Float,
    ): Color =
        glowColor
            .brightenToward(Color.White, targetLuminance = 0.92f)
            .copy(alpha = 0.95f * twinkle)

    fun haloAlpha(
        node: BrainNode,
        isSelected: Boolean,
        twinkle: Float,
    ): Float =
        when {
            isSelected && node.type == BrainNodeType.CHARACTER -> {
                (0.95f * twinkle).coerceIn(0.78f, 1f)
            }

            isSelected -> {
                0.9f * twinkle
            }

            else -> {
                0.74f
            }
        }

    fun selectedGlowBlurFactor(node: BrainNode): Float = if (node.type == BrainNodeType.CHARACTER) 2.6f else 2.0f

    fun selectedGlowSpreadFactor(node: BrainNode): Float =
        if (node.type == BrainNodeType.CHARACTER) 1.85f else 1.5f

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

    fun themeAccentForCanvas(themePrimary: Color): Color = themePrimary.visibleOnCosmicCanvas(themePrimary)

    /** Dark theme primaries and character hex must stay visible on the black canvas. */
    private fun Color.visibleOnCosmicCanvas(themePrimary: Color): Color {
        val luminance = relativeLuminance()
        if (luminance >= 0.38f) return this
        val liftedPrimary = themePrimary.brightenToward(Color.White, targetLuminance = 0.55f)
        return Color(
            red = red * 0.25f + liftedPrimary.red * 0.75f,
            green = green * 0.25f + liftedPrimary.green * 0.75f,
            blue = blue * 0.25f + liftedPrimary.blue * 0.75f,
            alpha = alpha.coerceAtLeast(0.88f),
        )
    }

    private fun Color.brightenToward(
        target: Color,
        targetLuminance: Float,
    ): Color {
        if (relativeLuminance() >= targetLuminance) return this
        val mix = ((targetLuminance - relativeLuminance()) / targetLuminance).coerceIn(0f, 1f)
        return Color(
            red = red + (target.red - red) * mix * 0.9f,
            green = green + (target.green - green) * mix * 0.9f,
            blue = blue + (target.blue - blue) * mix * 0.9f,
            alpha = alpha.coerceAtLeast(0.92f),
        )
    }

    private fun Color.relativeLuminance(): Float = 0.2126f * red + 0.7152f * green + 0.0722f * blue

    private fun Color.withMinAlpha(floor: Float): Color =
        copy(alpha = alpha.coerceAtLeast(floor))
}
