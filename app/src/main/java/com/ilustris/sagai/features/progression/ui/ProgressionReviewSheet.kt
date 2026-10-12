package com.ilustris.sagai.features.progression.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R

/**
 * Shown when a saga is opened and some evaluation of its experience (levels, medals, legacy) failed or never ran:
 * says so plainly and offers to redo it now or later. Draws nothing when nothing is pending.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressionReviewSheet(sagaId: Int) {
    val viewModel = hiltViewModel<ProgressionReviewViewModel>()
    val pending by viewModel.pending.collectAsStateWithLifecycle()
    val running by viewModel.running.collectAsStateWithLifecycle()
    LaunchedEffect(sagaId) { viewModel.check(sagaId) }
    if (pending <= 0 && !running) return

    ModalBottomSheet(onDismissRequest = { if (!running) viewModel.dismiss() }) {
        Column(
            modifier = Modifier.navigationBarsPadding().padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(stringResource(R.string.progression_review_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(stringResource(R.string.progression_review_body), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            if (running) {
                CircularProgressIndicator()
                Text(stringResource(R.string.progression_retrying), style = MaterialTheme.typography.labelMedium)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = viewModel::dismiss, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.progression_review_later)) }
                    Button(onClick = { viewModel.reviewNow(sagaId) }, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.progression_review_now)) }
                }
            }
        }
    }
}
