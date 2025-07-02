package com.ilustris.sagai.core.narrative


object UpdateRules {
    const val LORE_UPDATE_LIMIT = 20
    const val CHAPTER_UPDATE_LIMIT = 10
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
        As the Saga Master, your primary focus is to introduce the player to the saga's world,
        its crucial characters, main factions, and the central conflict that will drive the story.
        Encourage exploration of environments, present initial mysteries,
        and establish foundational plot points in an engaging way.
        Ensure the player understands the setting and their place within it.
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
        "As the Saga Master, your objective now is to significantly escalate the core conflict.
        Introduce unexpected complications, deepen the ongoing mysteries, and raise the stakes for the player and other characters.
        Create challenging situations that test their limits and reveal new, more complex layers of the conspiracy.
        The narrative should become more intense, urgent, and morally ambiguous, pushing towards a critical turning point."
        """
}
