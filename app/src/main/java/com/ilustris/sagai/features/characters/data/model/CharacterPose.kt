package com.ilustris.sagai.features.characters.data.model

enum class CharacterPose(
    val description: String,
) {
    STANDING_CONFIDENTLY("Standing confidently, looking directly at the viewer"),
    SITTING_PENSIVELY("Sitting pensively, perhaps looking off to the side"),
    LEANING_CASUALLY("Leaning casually against a wall or object"),
    WALKING_PURPOSEFULLY("Walking purposefully towards or away from the viewer"),
    ACTION_READY("In a dynamic action-ready stance, mid-movement or prepared for action"),
    LOOKING_HORIZON("Looking off towards the horizon, contemplative"),
    CROUCHING_LOW("Crouching low, as if observing or hiding"),
    HANDS_ON_HIPS("Standing with hands on hips, displaying assurance"),
    ARMS_CROSSED("Standing with arms crossed, looking thoughtful or resolute"),
    ;

    companion object {
        fun random(): CharacterPose = entries.random()
    }
}
