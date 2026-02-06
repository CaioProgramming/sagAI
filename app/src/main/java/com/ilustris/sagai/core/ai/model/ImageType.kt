package com.ilustris.sagai.core.ai.model

enum class ImageType(
    val description: String,
) {
    ICON(
        """
        ⚠️ CONSTRAINT: UI AVATAR / ICON MODE
        1. FOCUS: INCIDENTAL PERSONALITY. The character should NOT look like they are posing. Capture a natural, incidental moment (e.g., mid-thought, reacting to something off-screen, a subtle smirk, or leaning into their work).
        2. PROXIMITY: Absolute 'Spirit' Proximity. We must be close enough to feel their presence, but the framing should feel like a 'stolen' or candid moment, not a portrait session.
        3. COMPOSITION: High Contrast & Compelling. Reads well as a circular or square avatar.
        4. BANNED (STRICT): No text, HUDs, borders, frames, digital overlays, or legs/feet. This is an avatar, not a scene.
        5. PERSPECTIVE: The face/head is the visual anchor, but the angle should be natural and dynamic.
        """.trimIndent(),
    ),
    CHARACTER(
        """
        ⚠️ CONSTRAINT: UI PORTRAIT MODE
        1. FOCUS: THE CHARACTER'S LIVED HISTORY. Capture them in their element (leaning on a weapon, sitting in a cockpit, mid-action, or in quiet contemplation).
        2. CANDID DEPTH: Proximity is key—stay close to the face and torso, but favor organic, non-posed utility (incidental actions). Avoid the 'staring at camera' photoshoot look.
        3. ATMOSPHERE: The environment should bleed into the subject, hinting at their current reality (e.g., sparks from a forge, the dust of a road).
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
