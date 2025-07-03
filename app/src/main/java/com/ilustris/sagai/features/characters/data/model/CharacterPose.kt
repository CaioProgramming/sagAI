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

enum class PortraitPose(
    val description: String,
) {
    FRONT_VIEW("Front view, looking directly at the viewer"),
    THREE_QUARTER_VIEW("Three-quarter view, face turned slightly to one side"),
    PROFILE_VIEW("Profile view, side of the face visible"),
    OVER_THE_SHOULDER("Over the shoulder, looking back at the viewer"),
    CLOSE_UP("Close-up, focusing on facial features"),
    DUTCH_ANGLE("Dutch angle, tilted camera for a dynamic feel"),
    HIGH_ANGLE("High angle, looking down at the subject"),
    LOW_ANGLE("Low angle, looking up at the subject"),
    CANDID_SHOT("Candid shot, unposed and natural"),
    LOOKING_AWAY("Looking away, creating a sense of mystery or contemplation"),
    ;

    companion object {
        fun random(): PortraitPose = entries.random()
    }
}
