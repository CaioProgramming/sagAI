package com.ilustris.sagai.core.narrative

object UpdateRules {
    const val MAX_ACTS_LIMIT = 3

    const val ACT_UPDATE_LIMIT = 3

    const val LORE_UPDATE_LIMIT = 15
    const val CHAPTER_UPDATE_LIMIT = 5
}

object ActPurpose {
    const val FIRST_ACT_PURPOSE = """
    Focus on establishing the world building,
    introducing key characters,
    major factions,
    and the central conflict that will drive the story.
    The description should be engaging,
    hinting at the challenges ahead and building anticipation for the next act.
    This is the beginning of a grand journey.
    """

    const val SECOND_ACT_PURPOSE =
        """
    Focus on escalating the conflicts,
    introducing major twists,
    character transformations,
    and raising the stakes for the player and the characters.
    Create situations that test the limits of the characters and reveal new layers of the conspiracy.
    The narrative should become more intense and challenging, leading towards a thrilling climax.
    """
    const val THIRD_ACT_PURPOSE =
        """
    Focus on the climax, the resolution of the main conflict, and the ultimate fate of the characters and the world.
    The description MUST provide a definitive conclusion to the saga, summarizing its ending.
    All character arcs and major plotlines should converge to a satisfying and conclusive closure.
    This is the final act of the saga."
    """
}

object ActDirectives {
    const val FIRST_ACT_DIRECTIVES =
        """
        As the Saga Master, your primary focus is to introduce the player to the saga's world, its crucial characters, main factions, and the central conflict that will drive the story.
        **Crucially, in the initial stages (especially Act 1), ensure the player is given clear, actionable objectives or immediate motivations. These objectives should naturally guide exploration of environments and introduce foundational plot points in an engaging and understandable way.**
        **Avoid excessive initial ambiguity or overly cryptic dialogue from new characters.
        New characters should provide relevant information or a clear call to action (even if small) to propel the player forward.
        Mystery should invite exploration, not hinder it.**
        """

    const val SECOND_ACT_DIRECTIVES =
        """
        As the Saga Master, your objective now is to significantly escalate the core conflict.
        Introduce unexpected complications, deepen the ongoing mysteries, and raise the stakes for the player and other characters.
        Create challenging situations that test their limits and reveal new, more complex layers of the conspiracy.
        The narrative should become more intense, urgent, and morally ambiguous, pushing towards a critical turning point."
        """

    const val THIRD_ACT_DIRECTIVES =
        """
        // As the Saga Master, your objective in Act 3 is to guide the narrative towards its definitive climax and ultimate resolution.
        // This is the final stage where all major plotlines converge, and the saga's ultimate goal is to be achieved or definitively concluded.
        
        1.  **Intensify and Converge:** Continue to significantly escalate the core conflict, introducing the final, most challenging complications. All mysteries should deepen towards their ultimate unveiling, and the stakes must be at their absolute highest for the player and all involved characters.
        2.  **Focus on Ultimate Goal & Organic Urgency:** Every development and challenge in this Act MUST now directly push the player towards confronting the main antagonist, unraveling the final layers of the conspiracy, and achieving the **Saga's Ultimate Goal** as defined in the `SAGA CONCLUSION DIRECTIVE`. The narrative, through the **dialogue and demeanor of NPCs, environmental cues, and the unfolding consequences of events**, should organically convey the growing urgency and the dwindling opportunities for diversion. If the player attempts to explore unrelated avenues, the context should subtly, yet persistently, remind them of the paramount, pressing objective. NPCs should express heightened concern, new threats should naturally emerge from the main plotline, or vital information should become accessible only by pursuing the core path. No entirely new, major side plots should be initiated; existing opportunities should naturally lead back to the ultimate objective.
        3.  **Definitive Choices:** Player actions and choices in this Act will have irreversible and conclusive consequences, leading directly to the saga's final outcome. Emphasize the weight and finality of these decisions.
        4.  **Clear Trajectory to End:** Maintain a clear narrative momentum that builds towards a resolution. The story should feel like it's naturally reaching its concluding moments, presenting the player with the ultimate challenges needed to bring the saga to its definitive end, while preserving their sense of agency.
        5.  **Preparation for Conclusion Trigger:** Be highly attuned to the fulfillment of the ultimate goal. Once the player's actions decisively meet the conditions for conclusion, the narrative should transition immediately and definitively into the `SAGA CONCLUSION DIRECTIVE`'s final message.
        
        
        """
}
