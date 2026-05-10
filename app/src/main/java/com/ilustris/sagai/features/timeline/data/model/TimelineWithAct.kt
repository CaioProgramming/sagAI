package com.ilustris.sagai.features.timeline.data.model

import androidx.room.Embedded

data class TimelineWithAct(
    @Embedded val timelineContent: TimelineContent,
    val actTitle: String,
)
