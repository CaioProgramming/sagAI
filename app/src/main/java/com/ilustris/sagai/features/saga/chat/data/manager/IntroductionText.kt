package com.ilustris.sagai.features.saga.chat.data.manager

/**
 * The text an Introduction milestone shows. The model's `finalMessage` is optional in practice (and
 * is normalised to "" upstream), so a blank one falls back to the introduction that was saved on the
 * chapter/act instead of rendering an empty body.
 */
internal fun resolveIntroductionText(
    message: String?,
    savedIntroduction: String,
): String = message?.takeIf { it.isNotBlank() } ?: savedIntroduction
