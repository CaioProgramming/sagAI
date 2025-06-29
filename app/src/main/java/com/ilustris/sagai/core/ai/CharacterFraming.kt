package com.ilustris.sagai.core.ai

enum class CharacterFraming(
    val description: String,
) {
    PORTRAIT(
        """
        head and shoulders portrait,
        clear facial features, engaging expression,
        well-lit face,
        (e.g., direct engaging gaze, subtle head tilt, looking off-camera,
        over-the-shoulder glance, profile with a slight turn, chin raised proudly,
        chin lowered contemplatively),
        simple or softly blurred background to keep focus on the character
        """,
    ),
    MEDIUM_SHOT(
        """
        medium shot, showing the character from the waist up,
        dynamic pose, emphasizing character's primary action or emotion,
        showcasing key costume details or a held prop,
        relevant background context that enhances character's role but is not overwhelming,
        clear and expressive facial features
        """,
    ),
    FULL_BODY(
        """
        full body shot, character in a compelling and representative stance,
        emphasizing their complete design and silhouette,
        minimal or studio-like background for maximum character visibility,
        heroic pose or iconic action if applicable
        """,
    ),
    EPIC_WIDE_SHOT(
        """
        epic wide shot, cinematic composition,
        character as the prominent focal point within a vast landscape,
        dramatic lighting highlighting the character's form,
        atmospheric perspective creating depth and scale around the subject,
        sense of scale and grandeur emphasizing character's importance in the vastness,
        masterpiece, highly detailed, character clearly distinguishable despite the wide shot
        """,
    ),
}
