package com.ilustris.sagai.core.ai

class ModelOutageException(
    val requirement: GemmaClient.ModelRequirement,
) : Exception(
        "The ${requirement.name} model tier is currently unavailable due to an outage or maintenance. Please try a different model tier or try again later.",
    )
