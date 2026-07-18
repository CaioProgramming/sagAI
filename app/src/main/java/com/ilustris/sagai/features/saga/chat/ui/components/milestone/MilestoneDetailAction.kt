package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ilustris.sagai.R

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

/**
 * Deep-link string for this action — resolved into a real [androidx.navigation3.runtime.NavKey]
 * by `MainActivity.navigateDeepLink`, same mechanism used by [com.ilustris.sagai.core.globalshell.GlobalShellEffect].
 * A plain string (not a NavKey) so this stays constructible from the data layer, which can't hold
 * a reference to the screen-scoped `Navigator`.
 */
fun MilestoneDetailAction.toDeepLink(): String =
    when (this) {
        is MilestoneDetailAction.OpenBookReader -> "saga://book_reader/$sagaId/$actId"
        is MilestoneDetailAction.OpenCharacter -> "saga://character_detail/$characterId"
        is MilestoneDetailAction.OpenWiki -> "saga://saga_wiki/$sagaId"
        is MilestoneDetailAction.OpenEvents -> "saga://saga_events/$sagaId"
        is MilestoneDetailAction.OpenCharacters -> "saga://saga_characters/$sagaId"
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
