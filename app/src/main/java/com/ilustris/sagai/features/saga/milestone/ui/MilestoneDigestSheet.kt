package com.ilustris.sagai.features.saga.milestone.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.ui.GlassButton
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone

/**
 * The full text behind a milestone's digest card, with the way on to the list it belongs to. Only
 * the long generated text lives here: the cast, the wiki entries and the emotional note are
 * already on the milestone itself, and repeating them would only make the sheet longer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilestoneDigestSheet(
    milestone: SagaMilestone,
    onSeeMore: () -> Unit,
    onDismiss: () -> Unit,
) {
    val text = milestone.digestText ?: return
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(milestone.title),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = milestone.subtitle,
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    ),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = text,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
            )
            GlassButton(
                text = stringResource(R.string.milestone_see_more),
                onClick = onSeeMore,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            )
        }
    }
}
