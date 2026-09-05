package com.ilustris.sagai.core.ai

/**
 * The model stopped because it ran out of output tokens, not because it was finished.
 *
 * Worth its own type because of how this used to present. `finishReason` was only ever checked for
 * `SAFETY` and `OTHER`, so `MAX_TOKENS` counted as a normal completion and the half-written JSON
 * went straight to the sanitizer — which either failed to parse (three immediate retries, each one
 * truncating the same way and each costing a request from a 500/day budget) or recovered a partial
 * object and returned quietly short content.
 *
 * That second path is the expensive one: it looks exactly like the model being lazy or
 * oversimplifying, so it gets blamed on model quality and chased with a model swap, when the real
 * cause is a blueprint whose schema asks for more than the response can hold.
 */
class ResponseTruncatedException(
    val model: String,
    val blueprintKey: String?,
    val outputTokens: Int?,
) : Exception(
        "Response from $model was cut off at the output limit " +
            "(${outputTokens ?: "unknown"} tokens). Blueprint: ${blueprintKey ?: "unknown"}",
    )
