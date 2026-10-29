package com.ilustris.sagai.features.widgets.domain

/** Text helpers shared by the widgets. */
object WidgetText {
    private val TAGS = Regex("</?(action|think|narrator)>", RegexOption.IGNORE_CASE)
    private val SPACES = Regex("\\s+")

    /**
     * Chat messages carry inline tags (`<action>`, `<think>`, `<narrator>`). A widget has no way to
     * style them, so the markup goes and the words stay, which keeps narrator-only messages readable.
     */
    fun plain(raw: String?): String =
        raw
            .orEmpty()
            .replace(TAGS, " ")
            .replace(SPACES, " ")
            .trim()
}
