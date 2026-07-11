package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.StringResourceHelper
import com.ilustris.sagai.features.home.data.model.findTimeline
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MilestoneDashboardMapper
    @Inject
    constructor(
        private val stringResourceHelper: StringResourceHelper,
    ) {
        fun toDashboardItems(
            milestone: SagaMilestone,
            sagaId: Int,
        ): List<MilestoneDashboardItem> =
            when (milestone) {
                is SagaMilestone.NewEvent -> {
                    milestone.sagaContent
                        .findTimeline(milestone.timeline.id)
                        ?.statsSummary(
                            stringResourceHelper,
                            sagaId.toString(),
                        ).orEmpty()
                }

                is SagaMilestone.ChapterFinished -> {
                    milestone.sagaContent
                        .flatChapters()
                        .find { it.data.id == milestone.chapter.id }
                        ?.toMilestoneItems(stringResourceHelper, milestone.sagaContent)
                        ?: milestone.chapter.toMilestoneItems(stringResourceHelper)
                }

                is SagaMilestone.ActFinished -> {
                    milestone.act.toMilestoneItems(
                        stringResourceHelper,
                        milestone.act.sagaId ?: sagaId,
                    )
                }

                is SagaMilestone.NewCharacter -> {
                    val characterAction =
                        MilestoneDetailAction.OpenCharacter(milestone.character.id)
                    buildList {
                        add(
                            MilestoneDashboardItem(
                                title = milestone.character.name,
                                subtitle = stringResourceHelper.getString(R.string.milestone_label_backstory),
                                content = milestone.character.backstory,
                                iconRes = R.drawable.ic_note,
                                fullWidth = true,
                                kind = MilestoneCardKind.Narrative,
                                detailAction = characterAction,
                            ),
                        )
                        if (milestone.character.profile.occupation
                                .isNotBlank()
                        ) {
                            add(
                                MilestoneDashboardItem(
                                    title = milestone.character.name,
                                    subtitle = stringResourceHelper.getString(R.string.milestone_label_occupation),
                                    value = milestone.character.profile.occupation,
                                    iconRes = R.drawable.ic_note,
                                    kind = MilestoneCardKind.Stat,
                                ),
                            )
                        }
                        if (milestone.character.profile.personality
                                .isNotBlank()
                        ) {
                            add(
                                MilestoneDashboardItem(
                                    title = milestone.character.name,
                                    subtitle = stringResourceHelper.getString(R.string.milestone_label_personality),
                                    value = milestone.character.profile.personality,
                                    iconRes = R.drawable.ic_idea,
                                    kind = MilestoneCardKind.Stat,
                                ),
                            )
                        }
                    }
                }

                else -> {
                    emptyList()
                }
            }
    }

private fun com.ilustris.sagai.features.chapter.data.model.Chapter.toMilestoneItems(
    resourceHelper: StringResourceHelper,
): List<MilestoneDashboardItem> =
    buildList {
        if (content.isNotBlank()) {
            add(
                MilestoneDashboardItem(
                    title = title,
                    subtitle = resourceHelper.getString(R.string.milestone_label_chapter_summary),
                    content = content,
                    iconRes = R.drawable.center_spark,
                    fullWidth = true,
                    kind = MilestoneCardKind.Narrative,
                ),
            )
        }
        emotionalReview?.takeIf { it.isNotBlank() }?.let { review ->
            add(
                MilestoneDashboardItem(
                    title = title,
                    subtitle = resourceHelper.getString(R.string.milestone_label_emotional_review),
                    content = review,
                    iconRes = R.drawable.ic_review,
                    fullWidth = true,
                    kind = MilestoneCardKind.Narrative,
                ),
            )
        }
        continuitySummary?.takeUnless { it.isBlank() }?.let { continuity ->
            val sections = continuity.toMilestoneSections(resourceHelper)
            add(
                MilestoneDashboardItem(
                    title = title,
                    subtitle = resourceHelper.getString(R.string.milestone_label_continuity),
                    value = continuity.factCount().toString(),
                    content =
                        sections.values
                            .firstOrNull()
                            ?.lineSequence()
                            ?.take(2)
                            ?.joinToString("\n"),
                    iconRes = R.drawable.ic_globe,
                    displayContent = sections,
                    kind = MilestoneCardKind.Continuity,
                ),
            )
        }
    }
