package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey
import com.ilustris.sagai.R
import com.ilustris.sagai.ui.navigation.BookReaderKey
import com.ilustris.sagai.ui.navigation.CharacterDetailKey
import com.ilustris.sagai.ui.navigation.SagaCharactersKey
import com.ilustris.sagai.ui.navigation.SagaEventsKey
import com.ilustris.sagai.ui.navigation.SagaWikiKey

sealed interface MilestoneDetailAction {
    data class OpenBookReader(
        val sagaId: Int,
        val actId: Int,
    ) : MilestoneDetailAction

    data class OpenCharacter(
        val characterId: Int,
    ) : MilestoneDetailAction

    data class OpenWiki(
        val sagaId: String,
    ) : MilestoneDetailAction

    data class OpenEvents(
        val sagaId: String,
    ) : MilestoneDetailAction

    data class OpenCharacters(
        val sagaId: String,
    ) : MilestoneDetailAction
}

fun MilestoneDetailAction.toNavKey(): NavKey =
    when (this) {
        is MilestoneDetailAction.OpenBookReader -> BookReaderKey(sagaId, actId)
        is MilestoneDetailAction.OpenCharacter -> CharacterDetailKey(characterId)
        is MilestoneDetailAction.OpenWiki -> SagaWikiKey(sagaId)
        is MilestoneDetailAction.OpenEvents -> SagaEventsKey(sagaId)
        is MilestoneDetailAction.OpenCharacters -> SagaCharactersKey(sagaId)
    }

@Composable
fun MilestoneDetailAction.label(): String =
    when (this) {
        is MilestoneDetailAction.OpenBookReader -> stringResource(R.string.milestone_action_open_chronicle)
        is MilestoneDetailAction.OpenCharacter -> stringResource(R.string.milestone_action_open_character)
        is MilestoneDetailAction.OpenWiki -> stringResource(R.string.milestone_action_open_wiki)
        is MilestoneDetailAction.OpenEvents -> stringResource(R.string.milestone_action_open_events)
        is MilestoneDetailAction.OpenCharacters -> stringResource(R.string.milestone_action_open_characters)
    }
