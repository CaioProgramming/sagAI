package com.ilustris.sagai.features.timeline.data.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.ilustris.sagai.R
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.utils.StringResourceHelper
import com.ilustris.sagai.core.utils.asMap
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.events.data.model.CharacterEvent
import com.ilustris.sagai.features.characters.events.data.model.CharacterEventDetails
import com.ilustris.sagai.features.characters.relations.data.model.RelationshipContent
import com.ilustris.sagai.features.characters.relations.data.model.RelationshipUpdateEvent
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.MilestoneCardKind
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.MilestoneDashboardItem
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.MilestoneDetailAction
import com.ilustris.sagai.features.wiki.data.model.Wiki

data class TimelineContent(
    @Embedded
    val data: Timeline,
    @Relation(
        parentColumn = "id",
        entityColumn = "timelineId",
        entity = Message::class,
    )
    val messages: List<MessageContent> = emptyList(),
    @Relation(
        parentColumn = "id",
        entity = CharacterEvent::class,
        entityColumn = "gameTimelineId",
    )
    val characterEventDetails: List<CharacterEventDetails> = emptyList(),
    @Relation(
        parentColumn = "id",
        entityColumn = "timelineId",
        entity = Wiki::class,
    )
    val updatedWikis: List<Wiki> = emptyList(),
    @Relation(
        parentColumn = "id",
        entity = com.ilustris.sagai.features.characters.relations.data.model.CharacterRelation::class,
        entityColumn = "id",
        associateBy =
            Junction(
                value = RelationshipUpdateEvent::class,
                parentColumn = "timelineId",
                entityColumn = "relationId",
            ),
    )
    val updatedRelationshipDetails: List<RelationshipContent> = emptyList(),
    @Relation(
        parentColumn = "id",
        entityColumn = "firstSceneId",
        entity = Character::class,
    )
    val newlyAppearedCharacters: List<Character> = emptyList(),
) {
    fun isFull(loreLimit: Int): Boolean = messages.size >= loreLimit

    fun isComplete(narrativeRules: NarrativeRules): Boolean =
        isFull(narrativeRules.loreUpdateLimit) &&
            data.title.isNotEmpty() &&
            data.content.isNotEmpty()

    fun numberOfRelationshipUpdates(): Int = updatedRelationshipDetails.size

    fun canBeReviewed() =
        data.emotionalReview.isNullOrEmpty() ||
            characterEventDetails.isEmpty() ||
            updatedRelationshipDetails.isEmpty() ||
            updatedWikis.isEmpty()

    fun emotionalRanking() =
        if (data.emotionalTone != null) {
            listOf(data.emotionalTone to 1)
        } else {
            messages
                .filter { it.message.emotionalTone != null }
                .groupBy { it.message.emotionalTone }
                .mapNotNull {
                    it.key to it.value.size
                }.sortedByDescending {
                    it.second
                }
        }

    fun statsSummary(
        resourceHelper: StringResourceHelper,
        sagaId: String,
    ) = buildList {
        if (data.content.isNotEmpty()) {
            add(
                MilestoneDashboardItem(
                    title = data.title,
                    subtitle = resourceHelper.getString(R.string.milestone_label_timeline_update),
                    content = data.content,
                    iconRes = R.drawable.center_spark,
                    fullWidth = true,
                    kind = MilestoneCardKind.Narrative,
                ),
            )
        }

        data.emotionalTone?.let { tone ->
            add(
                MilestoneDashboardItem(
                    title = data.title,
                    subtitle = resourceHelper.getString(R.string.milestone_label_vibe),
                    value = resourceHelper.getString(tone.getStringRes()),
                    iconRes = R.drawable.ic_spark,
                    content = data.emotionalReview,
                    displayContent = emptyMap(),
                    kind = MilestoneCardKind.Stat,
                ),
            )
        }

        if (updatedWikis.isNotEmpty()) {
            add(
                MilestoneDashboardItem(
                    title = data.title,
                    subtitle = resourceHelper.getString(R.string.milestone_label_knowledge),
                    value = updatedWikis.size.toString(),
                    iconRes = R.drawable.ic_note,
                    displayContent =
                        updatedWikis.associate {
                            "${it.emojiTag} ${it.title}" to it.content
                        },
                    kind = MilestoneCardKind.Stat,
                    detailAction = MilestoneDetailAction.OpenWiki(sagaId),
                ),
            )
        }

        if (newlyAppearedCharacters.isNotEmpty()) {
            val characterAction =
                if (newlyAppearedCharacters.size == 1) {
                    MilestoneDetailAction.OpenCharacter(newlyAppearedCharacters.first().id)
                } else {
                    MilestoneDetailAction.OpenCharacters(sagaId)
                }
            add(
                MilestoneDashboardItem(
                    title = data.title,
                    subtitle = resourceHelper.getString(R.string.new_characters_label),
                    value = newlyAppearedCharacters.size.toString(),
                    iconRes = R.drawable.character_icon,
                    chipCharacters = newlyAppearedCharacters,
                    displayContent =
                        newlyAppearedCharacters.associate {
                            it.name to it.backstory
                        },
                    kind = MilestoneCardKind.Stat,
                    detailAction = characterAction,
                ),
            )
        }

        if (updatedRelationshipDetails.isNotEmpty()) {
            add(
                MilestoneDashboardItem(
                    title = data.title,
                    subtitle = resourceHelper.getString(R.string.saga_detail_relationships_section_title),
                    value = updatedRelationshipDetails.size.toString(),
                    iconRes = R.drawable.ic_relationship,
                    displayContent =
                        updatedRelationshipDetails.associate {
                            it.data.title to it.data.description
                        },
                    kind = MilestoneCardKind.Stat,
                    detailAction = MilestoneDetailAction.OpenCharacters(sagaId),
                ),
            )
        }

        if (characterEventDetails.isNotEmpty()) {
            add(
                MilestoneDashboardItem(
                    title = data.title,
                    subtitle = resourceHelper.getString(R.string.character_events),
                    value = characterEventDetails.size.toString(),
                    iconRes = R.drawable.ic_full_spark,
                    displayContent =
                        characterEventDetails.associate {
                            it.event.title to it.event.summary
                        },
                    kind = MilestoneCardKind.Stat,
                    detailAction = MilestoneDetailAction.OpenEvents(sagaId),
                ),
            )
        }

        data.sceneSummary?.let { scene ->
            val objective = scene.immediateObjective ?: scene.quote
            if (!objective.isNullOrBlank()) {
                add(
                    MilestoneDashboardItem(
                        title = data.title,
                        subtitle = resourceHelper.getString(R.string.current_objective),
                        content = objective,
                        iconRes = R.drawable.ic_globe,
                        displayContent = scene.asMap(),
                        kind = MilestoneCardKind.Narrative,
                        fullWidth = false,
                    ),
                )
            }
        }
    }
}
