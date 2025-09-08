package com.ilustris.sagai.features.saga.detail.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.formatDate
import com.ilustris.sagai.features.home.data.model.SagaContent

enum class DetailAction {
    CHARACTERS,
    TIMELINE,
    CHAPTERS,
    WIKI,
    ACTS,
    BACK,
    DELETE,
    REGENERATE,
}

fun DetailAction.sharedElementItemKey(
    id: Int,
    itemId: Int,
) = "saga-$id-${this.name}-item=$itemId"

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.sharedTransitionActionItemModifier(
    action: DetailAction,
    animationScope: AnimatedVisibilityScope,
    id: Int,
    itemId: Int,
) = Modifier
    .sharedElement(
        rememberSharedContentState(
            action.sharedElementItemKey(id, itemId),
        ),
        animationScope,
    )

fun DetailAction.sharedElementTitleKey(id: Int) =
    when (this) {
        DetailAction.CHARACTERS -> "saga-$id-characters-section-title"
        DetailAction.TIMELINE -> "saga-$id-timeline-section-title"
        DetailAction.CHAPTERS -> "saga-$id-chapters-section-title"
        DetailAction.WIKI -> "saga-$id-wiki-section-title"
        else -> "saga-$id-main-page"
    }

@Composable
fun DetailAction.titleAndSubtitle(content: SagaContent): Pair<String, String> =
    when (this) {
        DetailAction.CHARACTERS ->
            stringResource(R.string.saga_detail_section_title_characters) to
                stringResource(
                    R.string.saga_detail_section_subtitle_characters,
                    content.characters.size,
                )

        DetailAction.TIMELINE ->
            stringResource(R.string.saga_detail_section_title_timeline) to
                stringResource(
                    R.string.saga_detail_section_subtitle_timeline,
                    content.eventsSize(),
                )

        DetailAction.CHAPTERS ->
            stringResource(R.string.saga_detail_section_title_chapters) to
                stringResource(
                    R.string.saga_detail_section_subtitle_chapters,
                    content.chaptersSize(),
                )

        DetailAction.WIKI ->
            stringResource(R.string.saga_detail_section_title_wiki) to
                stringResource(R.string.saga_detail_section_subtitle_wiki, content.wikis.size)

        DetailAction.ACTS ->
            stringResource(R.string.saga_detail_section_title_acts) to
                stringResource(R.string.saga_detail_section_subtitle_acts, content.acts.size)

        else -> {
            if (content.data.isEnded) {
                content.data.title to
                    stringResource(
                        R.string.saga_detail_status_ended,
                        content.data.endedAt.formatDate(),
                    )
            } else {
                content.data.title to
                    stringResource(
                        R.string.saga_detail_status_created,
                        content.data.createdAt.formatDate(),
                    )
            }
        }
    }
