package com.ilustris.sagai.features.saga.chat.presentation

import androidx.annotation.StringRes
import com.ilustris.sagai.R
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.saga.chat.data.model.Message
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
    val icon: Any? = null,
    val message: String,
    val redirectAction: ChatAction? = null,
)

sealed class ChatAction(
    @StringRes val actionRes: Int? = null,
) {
    data class ResendMessage(
        val message: Message,
    ) : ChatAction(R.string.try_again)

    data class OpenDetails(
        val data: Any,
    ) : ChatAction(R.string.see_more)

    data class RetryCharacter(
        val description: String,
    ) : ChatAction(R.string.try_again)

    data object RevaluateSaga : ChatAction(R.string.try_again)
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
