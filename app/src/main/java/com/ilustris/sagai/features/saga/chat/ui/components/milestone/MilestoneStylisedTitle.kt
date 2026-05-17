package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.MilestonePhaseVisibility
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.MilestoneTransitions
import com.ilustris.sagai.ui.components.stylisedText

@Composable
fun MilestoneStylisedTitle(
    genre: Genre,
    text: String,
    visible: Boolean,
    modifier: Modifier = Modifier,
    useHeroEnter: Boolean = false,
) {
    MilestonePhaseVisibility(
        visible = visible,
        enter =
            if (useHeroEnter) {
                MilestoneTransitions.heroEnter
            } else {
                MilestoneTransitions.labelEnter
            },
    ) {
        genre.stylisedText(
            text = text,
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}
