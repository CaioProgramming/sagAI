package com.ilustris.sagai.features.voicepicker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.ilustris.sagai.features.voicepicker.presentation.NarratorVoiceViewModel
import com.ilustris.sagai.features.voicepicker.presentation.VoicePickerViewModel
import com.ilustris.sagai.ui.theme.SagAITheme

/**
 * Where a new saga picks the narrator it keeps for good, before its chat opens. Nothing narrated
 * exists yet at this point, so the choice can't be pre-empted by an automatic pick. When the
 * catalog has no curated voices (Remote Config without display names) the screen steps aside.
 */
@Composable
fun NarratorVoiceView(
    sagaId: String,
    onDone: () -> Unit,
    viewModel: NarratorVoiceViewModel = hiltViewModel(),
    pickerViewModel: VoicePickerViewModel = hiltViewModel(),
) {
    val id = sagaId.toIntOrNull()
    val genre by viewModel.genre.collectAsStateWithLifecycle()
    val picker by pickerViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(id) { if (id == null) onDone() else viewModel.load(id) }
    LaunchedEffect(picker.loaded, picker.voices.isEmpty()) { if (picker.loaded && picker.voices.isEmpty()) onDone() }

    val sagaGenre = genre ?: return
    SagAITheme(genre = sagaGenre) {
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(vertical = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(Modifier.padding(horizontal = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(R.string.narrator_voice_title),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )
                Text(
                    stringResource(R.string.narrator_voice_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = .7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            VoicePicker(
                genre = sagaGenre,
                onConfirm = { voice -> id?.let { viewModel.confirm(it, voice, onDone) } },
                viewModel = pickerViewModel,
            )
        }
    }
}
