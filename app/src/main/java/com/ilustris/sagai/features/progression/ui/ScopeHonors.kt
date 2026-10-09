package com.ilustris.sagai.features.progression.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.progression.data.model.CharacterFeat
import com.ilustris.sagai.features.progression.data.model.HonorKind
import com.ilustris.sagai.features.progression.data.source.HonorWithCharacter
import com.ilustris.sagai.features.progression.data.source.ProgressionDao
import com.ilustris.sagai.features.settings.domain.SettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScopeHonorsViewModel
    @Inject
    constructor(
        private val dao: ProgressionDao,
        settings: SettingsUseCase,
    ) : ViewModel() {
        private val scope = MutableStateFlow<Pair<HonorKind, Int>?>(null)
        val feats = MutableStateFlow<List<CharacterFeat>>(emptyList())

        val honors =
            combine(scope.flatMapLatest { s -> if (s == null) flowOf(emptyList()) else dao.observeHonorsOfScope(s.first.name, s.second) }, settings.getShowProgression()) { list, show ->
                if (show) list else emptyList()
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        fun load(
            kind: HonorKind,
            scopeId: Int,
        ) {
            scope.value = kind to scopeId
        }

        fun loadFeats(honors: List<HonorWithCharacter>) {
            val ids = honors.flatMap { it.honor.featIds }.distinct()
            if (ids.isEmpty()) return
            viewModelScope.launch { feats.value = dao.getFeatsByIds(ids) }
        }
    }

/**
 * The medals of a chapter or the legacies of an act, as rows on the story screens: the coin or the turning
 * badge, the name and who earned it. Tapping one opens the same sheet the character sheet uses.
 * Draws nothing while there is none, so sagas without progression are untouched.
 */
@Composable
fun ScopeHonors(
    kind: HonorKind,
    scopeId: Int,
    genre: Genre,
    modifier: Modifier = Modifier,
) {
    val viewModel = hiltViewModel<ScopeHonorsViewModel>(key = "honors_${kind.name}_$scopeId")
    LaunchedEffect(kind, scopeId) { viewModel.load(kind, scopeId) }
    val honors by viewModel.honors.collectAsStateWithLifecycle()
    val feats by viewModel.feats.collectAsStateWithLifecycle()
    var open by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(honors) { viewModel.loadFeats(honors) }
    if (honors.isEmpty()) return

    Column(modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            stringResource(if (kind == HonorKind.TITLE) R.string.progression_legacy_title else R.string.progression_medals),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        honors.forEachIndexed { index, entry ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { open = index },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (kind == HonorKind.TITLE) {
                    LegacyBadge3D(genre = genre, size = 48.dp, levitate = false)
                } else {
                    MedalCoin(entry.honor.emoji, genre, size = 44.dp)
                }
                Column(Modifier.weight(1f)) {
                    Text(entry.honor.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(entry.characterName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = .65f))
                }
            }
        }
    }
    open?.let { index ->
        HonorSheet(honors = honors.map { it.honor }, startIndex = index, feats = feats, genre = genre, onDismiss = { open = null })
    }
}
