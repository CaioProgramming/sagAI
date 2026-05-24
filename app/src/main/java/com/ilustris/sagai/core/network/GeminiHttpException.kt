package com.ilustris.sagai.core.network

/** Gemini REST HTTP error with response body for retry parsing. */
class GeminiHttpException(
    val code: Int,
    val errorBody: String,
) : Exception("Gemini HTTP $code")
