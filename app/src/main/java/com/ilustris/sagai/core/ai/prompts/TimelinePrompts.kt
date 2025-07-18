package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.formatToJsonArray
import com.ilustris.sagai.features.timeline.data.model.Timeline

object TimelinePrompts {
    fun timeLineDetails(events: List<Timeline>) =
        """
         **CURRENT SAGA TIMELINE (Most Recent Events):**
         // This section provides the most recent events from the saga's timeline (max 5 events).
         // Use this to understand the immediate plot progression and current situation.
         [ ${events.formatToJsonArray()} ]
        """
}
