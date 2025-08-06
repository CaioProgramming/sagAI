package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.CharacterFraming

object CharacterGuidelines

object FramingGuideLines {
    fun guidelineForFraming(framing: CharacterFraming) =
        when (framing) {
            CharacterFraming.PORTRAIT ->
                """
                ABSOLUTE FRAMING CONSTRAINT: The generated description MUST ONLY include elements that are clearly visible in a close-up portrait, focusing strictly on the head, face, neck, shoulders, and upper chest/torso. Any details from the JSON that fall outside this frame MUST BE OMITTED or rephrased to imply partial visibility within the portrait.
                
                Body Type/Physique: Describe physical attributes solely as they are evident in the upper body and shoulders (e.g., "athletic build subtly evident in her upper body"). Explicitly exclude descriptions of height or overall body shape that require a wider view.
                
                Weapons/Large Items: If weapons or large items are mentioned, describe ONLY the parts that would be visible within an upper body portrait (e.g., "the hilt of a sword visible over her shoulder," "a dagger pommel at her waist"). Crucially, completely omit any mention of parts that would not be seen, such as the full length of a large weapon or weapons carried on the lower body. If a weapon cannot be partially hinted at, it must be omitted.
                
                Clothing: Describe the upper portion of clothing, focusing on the collar, shoulders, chest, arms, and any accessories worn on the upper body. Completely omit any mention of clothing items or footwear that would be below the upper torso (e.g., skirts, pants, boots, full dresses). If a JSON field mentions these, they must be ignored for a portrait description.
                """
            CharacterFraming.MEDIUM_SHOT ->
                """
                * **Focus:** Character from the waist up, or just below the hips, including most of the torso, arms, and hands.
                * **Body Type/Physique:** Describe the physique as evident from the waist up. Terms like "athletic build," "muscular physique" are appropriate.
                * **Weapons/Large Items:** Weapons can be held or slung, showing most of their length if held across the body, or parts of larger items.
                * **Clothing:** Describe the top half of the attire, including belts, visible parts of skirts/trousers, and how items like holsters or pouches are attached to the waist. Footwear is generally not visible.
                """
            CharacterFraming.FULL_BODY ->
                """
                * **Focus:** The entire character from head to toe, occupying a significant portion of the frame.
                * **Body Type/Physique:** Describe the full physique (e.g., "tall and slender build," "broad-shouldered and muscular").
                * **Weapons/Large Items:** All weapons, tools, and items carried by the character should be fully described and depicted in relation to their full body.
                * **Clothing:** Describe the complete outfit from headwear to footwear, including how layers interact and full details of skirts, trousers, boots, etc.
                * **Pose:** The pose can be dynamic, showing full body action or interaction with the ground/environment.
                """
            CharacterFraming.EPIC_WIDE_SHOT ->
                """
                * **Focus:** The character as a prominent but integrated element within a vast and detailed environment/scene.
                The character might be smaller in the frame compared to a FULL_BODY shot, but still the central subject.
                * **Body Type/Physique:** Describe the full physique. The scale might be emphasized in relation to the environment.
                * **Weapons/Large Items:** Fully describe weapons and items. Their use or placement can contribute to the scene's narrative.
                * **Clothing:** Describe the complete outfit, noting how it flows or interacts with movement in a grand setting.
                * **Pose/Interaction:** Describe a pose that integrates the character into the epic scale of the background, showing interaction with the vast environment (e.g., "standing atop a mountain surveying a stormy sky," "striding across a desolate plain," "battling a colossal beast"). The character's action or presence should amplify the sense of scale and epicness of the scene.
                * **Environmental Context:** Briefly mention how the character's appearance enhances or contrasts with the specific epic environment (though the environment itself would be described in a separate part of the *final* image prompt).

                """
        }
}
