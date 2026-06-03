package com.ilustris.sagai.core.network

/** OpenRouter REST HTTP error with response body for retry parsing. */
class OpenRouterHttpException(
    val code: Int,
    val errorBody: String,
) : Exception("OpenRouter HTTP $code")
