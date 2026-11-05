package com.ilustris.sagai.features.characters.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.ui.DesignReviewState

/**
 * The player's design review of a character: a spinner while the audit runs, then either what it would
 * rewrite (applied only on confirm), a pass, or a failure.
 */
@Composable
fun CharacterDesignReviewDialog(
    state: DesignReviewState,
    onApply: (redrawImage: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    when (state) {
        DesignReviewState.Idle -> Unit

        DesignReviewState.Reviewing ->
            AlertDialog(
                onDismissRequest = {},
                title = { Text(stringResource(R.string.character_design_review_title)) },
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                        Text(stringResource(R.string.character_design_review_running))
                    }
                },
                confirmButton = {},
            )

        DesignReviewState.Passed ->
            MessageDialog(R.string.character_design_review_passed, onDismiss)

        DesignReviewState.Failed ->
            MessageDialog(R.string.character_design_review_failed, onDismiss)

        is DesignReviewState.Proposal ->
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.character_design_review_title)) },
                text = {
                    Column(
                        modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.character_design_review_proposal),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        state.changes.forEach { (field, before, after) ->
                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = stringResource(designFieldLabel(field)),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = before.ifBlank { "—" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = after,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Column(horizontalAlignment = Alignment.End) {
                        TextButton(onClick = { onApply(true) }) {
                            Text(stringResource(R.string.character_design_review_apply_redraw))
                        }
                        TextButton(onClick = { onApply(false) }) {
                            Text(stringResource(R.string.character_design_review_apply))
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.character_design_review_discard)) }
                },
            )
    }
}

@Composable
private fun MessageDialog(
    messageRes: Int,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.character_design_review_title)) },
        text = { Text(stringResource(messageRes)) },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.progression_close)) } },
    )
}

/** The sheet field names [com.ilustris.sagai.features.characters.data.model.designFields] reports, as player-facing labels. */
private fun designFieldLabel(field: String): Int =
    when (field) {
        "hair" -> R.string.character_design_field_hair
        "eyes" -> R.string.character_design_field_eyes
        "mouth" -> R.string.character_design_field_mouth
        "distinctiveMarks" -> R.string.character_design_field_marks
        "distinguishFeatures" -> R.string.character_design_field_features
        "buildAndPosture" -> R.string.character_design_field_build
        "outfitDescription" -> R.string.character_design_field_outfit
        "accessories" -> R.string.character_design_field_accessories
        "personality" -> R.string.character_design_field_personality
        else -> R.string.character_design_field_artwork
    }
