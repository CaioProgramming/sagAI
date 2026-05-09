package com.ilustris.sagai.core.ai

class ModelOutageException(
    val requirement: GemmaClient.ModelRequirement,
    val modelName: String,
) : Exception(
        "The ${requirement.name} model($modelName) tier is currently unavailable due to an outage or maintenance. Please try a different model tier or try again later.",
    )
