package com.ilustris.sagai.features.saga.chat.presentation

import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.timeline.data.model.TimelineContent

sealed class ChatState {
    data object Loading : ChatState()

    data object Success : ChatState()

    data class Error(
        val message: String,
    ) : ChatState()
}

data class SnackBarState(
    val title: String,
    val text: String,
    val redirectAction: Triple<ChatAction, String, Any?>? = null,
)

enum class ChatAction {
    RESEND,
    OPEN_TIMELINE,
    OPEN_CHARACTER,
    RETRY_AI_RESPONSE,
}

data class ActDisplayData(
    val content: ActContent,
    val isComplete: Boolean,
    val chapters: List<ChapterDisplayData>,
)

data class ChapterDisplayData(
    val chapter: ChapterContent,
    val isComplete: Boolean,
    val timelineSummaries: List<TimelineContent>,
)

data class TimelineSummaryData(
    val id: Int,
    val title: String,
    val content: String,
    val isComplete: Boolean,
    val messages: List<MessageContent>,
)
