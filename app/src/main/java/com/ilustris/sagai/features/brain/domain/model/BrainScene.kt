package com.ilustris.sagai.features.brain.domain.model

data class BrainScene(
    val focusNodeId: String,
    val structuralNodeIds: Set<String>,
    val satelliteNodeIds: Set<String>,
    val spineEdgeIds: Set<String>,
    val storyPath: List<String>,
    val isCharacterLens: Boolean = false,
) {
    val visibleNodeIds: Set<String> = structuralNodeIds + satelliteNodeIds
}
