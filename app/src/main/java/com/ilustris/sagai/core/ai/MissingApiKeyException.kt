package com.ilustris.sagai.core.ai

/**
 * No usable API key was available when a generation was attempted.
 *
 * Should be rare in practice — `MainActivity` gates the whole app on [
 * com.ilustris.sagai.core.ai.key.ApiKeyState.Missing], so reaching a generation without a key means
 * background work (a notification worker, a queued retry) outran that gate. Distinct from a generic
 * `error(...)` so those paths can be told apart from a real API failure in Crashlytics.
 */
class MissingApiKeyException :
    Exception("No Gemini API key is configured. The user must provide one before generating.")
