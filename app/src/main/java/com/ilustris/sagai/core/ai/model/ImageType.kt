package com.ilustris.sagai.core.ai.model

enum class ImageType(
    val description: String,
) {
    ICON(
        """
        ⚠️ CONSTRAINT: UI AVATAR / ICON MODE
        1. FOCUS: SUBJECT PRESENCE. The focus must be absolute—the character's face/expression is the centerpiece.
        2. CANDID PROXIMITY: Encourage natural, candid poses (e.g., leaning into frame, looking away, looking over shoulder). Variety is key as long as the viewer is 'spiritually close' to the face.
        3. COMPOSITION: Iconic and High Contrast. Reads well as a circular or square avatar.
        4. BANNED (STRICT): No text, HUDs, borders, frames, digital overlays, or legs/feet. This is an avatar, not a scene.
        5. PERSPECTIVE: The face/head should be the primary visual anchor of the frame.
        """.trimIndent(),
    ),
    CHARACTER(
        """
        ⚠️ CONSTRAINT: UI PORTRAIT MODE
        1. FOCUS: THE CHARACTER'S SOUL. Proximity is the priority—the focus must stay on the face and upper torso.
        2. NARRATIVE POSING: Encourage dynamic, candid, and organic poses (sitting, leaning, reclining, or incidental action). Think 'high-end photography portrait'.
        3. ATMOSPHERE: The environment should support the character (e.g., blurred palms in a beach portrait) but never overwhelm them.
        4. CONTENT: Head, Neck, Shoulders, Chest, Arms. NO Full Body.
        5. BANNED (STRICT): No text, HUDs, borders, frames, digital overlays, or legs/feet.
        """.trimIndent(),
    ),
    COVER(
        """
        ✨ MODE: CINEMATIC STORY ART
        1. FRAMING: DYNAMIC & DRAMATIC. Use Low Angles (Heroic) or High Angles (Vulnerable).
        2. SCOPE: Epic Scale. Establish the setting and the stakes.
        3. COMPOSITION: Visual Masterpiece. High-fidelity cinematic rendering.
        4. BANNED (STRICT): No text, logos, HUDs, borders, frames, or UI elements. This is raw artwork only.
        5. GOAL: Capture the peak soul of the story in a single frame.
        """.trimIndent(),
    ),
    SCENE(
        """
        ✨ MODE: CINEMATIC STORYTELLING
        1. FRAMING: NARRATIVE-DRIVEN. Match the camera to the emotion (e.g., Dutch Angle for confusion).
        2. ACTION: Capture the key moment of interaction or conflict.
        3. DEPTH: Use foreground/background elements to create depth.
        4. BANNED (STRICT): No text, HUDs, borders, frames, or digital contamination.
        5. GOAL: Immerse the viewer in the scene purely through visual storytelling.
        """.trimIndent(),
    ),
}
