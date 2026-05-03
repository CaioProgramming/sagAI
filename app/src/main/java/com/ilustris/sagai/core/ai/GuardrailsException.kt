package com.ilustris.sagai.core.ai

import com.ilustris.sagai.core.ai.model.SafeGuard

class GuardrailsException(
    val status: SafeGuard,
) : Exception("AI Guardrail triggered: ${status.name}")
