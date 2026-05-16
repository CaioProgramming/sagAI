package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.ui.components.bubble
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.MilestoneTransitions

@Composable
fun MilestoneContinueButton(
    genre: Genre,
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = MilestoneTransitions.labelEnter + slideInVertically { it / 2 },
        exit = MilestoneTransitions.fadeExit,
        modifier = modifier,
    ) {
        Button(
            onClick = onDismiss,
            shape = genre.bubble(isNarrator = true),
            colors = ButtonDefaults.elevatedButtonColors(),
            modifier = Modifier.padding(top = 24.dp),
        ) {
            Text(stringResource(R.string.continue_button))
        }
    }
}
