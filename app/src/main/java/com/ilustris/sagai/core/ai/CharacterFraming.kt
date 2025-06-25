package com.ilustris.sagai.core.ai

enum class CharacterFraming(
    val description: String,
) {
    CLOSE_UP("close-up"),
    PORTRAIT(
        """
        head and shoulders portrait
        (e.g., direct engaging gaze,
        subtle head tilt, looking off-camera,
        over-the-shoulder glance, profile with a slight turn, chin raised proudly,
        chin lowered contemplatively)
        """,
    ),
    MEDIUM_SHOT(
        """
            medium shot, showing the character from the waist up,
            focusing on their upper body and immediate actions or expressions,
            with relevant background context 
            visible but not overpowering the subject
            """,
    ),

    FULL_BODY("full body shot"),
    EPIC_WIDE_SHOT(
        """
        epic wide shot, cinematic composition,
        vast landscape,
        dramatic lights
        atmospheric perspective creating depth and scale
        sense of scale and grandeur emphasized by the vastness of the landscape
        masterpiece, highly detailed.
        """,
    ),
}
