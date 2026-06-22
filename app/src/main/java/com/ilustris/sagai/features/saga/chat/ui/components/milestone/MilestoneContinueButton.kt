package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.animation.MilestoneTransitions
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.morphingGradient

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
        val themeBrush = Brush.horizontalGradient(morphingGradient())
        ElevatedButton(
            onClick = onDismiss,
            shape = MaterialTheme.shapes.medium,
            colors =
                ButtonDefaults.buttonColors(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary.gradientFade()),
            modifier =
                Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
        ) {
            Text(
                stringResource(R.string.continue_button),
            )
        }
    }
}
