package com.ilustris.sagai.core.ai

enum class CharacterFraming(
    val description: String,
) {
    CLOSE_UP("close-up"),
    PORTRAIT("Portrait of character looking directly at the camera with the neck slightly turned. in a 3/4 angle"),
    MEDIUM_SHOT("medium shot"),
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
