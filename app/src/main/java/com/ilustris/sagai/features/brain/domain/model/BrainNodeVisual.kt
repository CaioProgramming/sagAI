package com.ilustris.sagai.features.brain.domain.model

/** Visual hierarchy multipliers — tiny pinpoints, characters larger than wikis. */
fun BrainNodeType.starScale(isCenter: Boolean): Float =
    when (this) {
        BrainNodeType.SAGA -> 1.35f
        BrainNodeType.CHARACTER -> if (isCenter) 1.35f else 1.05f
        BrainNodeType.ACT -> 1.15f
        BrainNodeType.CHAPTER -> 1.05f
        BrainNodeType.EVENT -> 1f
        BrainNodeType.CHARACTER_EVENT -> 0.85f
        BrainNodeType.RELATION -> 0.8f
        BrainNodeType.WIKI -> 0.65f
    }

/** Satellite-only scale — characters read clearly; wikis stay smaller. */
fun BrainNodeType.satelliteScale(): Float =
    when (this) {
        BrainNodeType.CHARACTER -> 1f
        BrainNodeType.CHARACTER_EVENT -> 0.75f
        BrainNodeType.RELATION -> 0.7f
        BrainNodeType.WIKI -> 0.55f
        else -> 0.75f
    }

fun BrainEdgeType.connectionScale(): Float =
    when (this) {
        BrainEdgeType.RELATED_TO -> 0.8f
        else -> 1f
    }
