package com.ilustris.sagai.core.network

/** Gemini REST HTTP error with response body for retry parsing. */
class GeminiHttpException(
    val code: Int,
    val errorBody: String,
) : Exception(formatMessage(code, errorBody)) {
    companion object {
        private fun formatMessage(
            code: Int,
            body: String,
        ): String {
            val parsed =
                body
                    .takeIf { it.isNotBlank() }
                    ?.let { runCatching { GeminiApiCodec.decodeErrorResponse(it) }.getOrNull() }
            val apiMessage = parsed?.error?.message?.takeIf { it.isNotBlank() }
            return when {
                apiMessage != null -> "Gemini HTTP $code: $apiMessage"
                body.isNotBlank() -> "Gemini HTTP $code: ${body.trim()}"
                else -> "Gemini HTTP $code"
            }
        }
    }
}
