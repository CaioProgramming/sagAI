package com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal

import com.ilustris.sagai.features.home.data.model.SagaContent

/** The `user@host` portion of every terminal prompt — the saga's own title, slugified. */
fun SagaContent.terminalHost(): String {
    val slug =
        data.title
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
    return "admin@${slug.ifBlank { "sagai" }}"
}
