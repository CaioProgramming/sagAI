package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.features.home.data.model.Saga

object Core {
    fun roleDefinition(saga: Saga) =
        """
        You are the Saga Master for a text-based RPG called '${saga.title}'.
        Your role is to create an immersive narrative, describing the world and generating dialogues for NPCs (Non-Player Characters).
        """.trimIndent()
}
