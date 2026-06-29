package com.ilustris.sagai.features.brain.domain

import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findTimeline
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.domain.model.filterMention
import javax.inject.Inject

data class BrainScopePresence(
    val characterIds: Set<Int> = emptySet(),
    val wikiIds: Set<Int> = emptySet(),
)

data class BrainPresenceIndex(
    val byAct: Map<Int, BrainScopePresence>,
    val byChapter: Map<Int, BrainScopePresence>,
    val byEvent: Map<Int, BrainScopePresence>,
    val actIdsInOrder: List<Int>,
    val chapterIdsByAct: Map<Int, List<Int>>,
    val eventIdsByChapter: Map<Int, List<Int>>,
    val currentActId: Int?,
) {
    fun actWindow(maxActs: Int = 3): List<Int> {
        if (actIdsInOrder.isEmpty()) return emptyList()
        if (actIdsInOrder.size <= maxActs) return actIdsInOrder
        val anchor =
            currentActId?.let { id ->
                actIdsInOrder.indexOf(id).takeIf { it >= 0 }
            } ?: 0
        val half = maxActs / 2
        val start = (anchor - half).coerceIn(0, actIdsInOrder.size - maxActs)
        return actIdsInOrder.subList(start, start + maxActs)
    }
}

class BrainPresenceIndexBuilder
    @Inject
    constructor() {
        fun build(sagaContent: SagaContent): BrainPresenceIndex {
            val byAct = mutableMapOf<Int, MutableSet<Int>>()
            val byChapter = mutableMapOf<Int, MutableSet<Int>>()
            val byEvent = mutableMapOf<Int, MutableSet<Int>>()
            val wikiByAct = mutableMapOf<Int, MutableSet<Int>>()
            val wikiByChapter = mutableMapOf<Int, MutableSet<Int>>()
            val wikiByEvent = mutableMapOf<Int, MutableSet<Int>>()

            val actIdsInOrder = sagaContent.acts.map { it.data.id }
            val chapterIdsByAct = mutableMapOf<Int, List<Int>>()
            val eventIdsByChapter = mutableMapOf<Int, List<Int>>()
            val characters = sagaContent.characters.map { it.data }

            sagaContent.acts.forEach { act ->
                val actId = act.data.id
                val chapterIds = mutableListOf<Int>()
                val actMessages = mutableListOf<MessageContent>()

                act.chapters.forEach { chapter ->
                    val chapterId = chapter.data.id
                    chapterIds.add(chapterId)
                    val eventIds = mutableListOf<Int>()

                    chapter.data.featuredCharacters.forEach { charId ->
                        byAct.addChar(actId, charId)
                        byChapter.addChar(chapterId, charId)
                    }

                    chapter.events.forEach { event ->
                        val eventId = event.data.id
                        eventIds.add(eventId)

                        event.newlyAppearedCharacters.forEach { char ->
                            byAct.addChar(actId, char.id)
                            byChapter.addChar(chapterId, char.id)
                            byEvent.addChar(eventId, char.id)
                        }

                        event.characterEventDetails.forEach { detail ->
                            val charId = detail.character.id
                            byAct.addChar(actId, charId)
                            byChapter.addChar(chapterId, charId)
                            byEvent.addChar(eventId, charId)
                        }

                        event.messages.forEach { msg ->
                            actMessages.add(msg)
                            msg.message.characterId?.let { charId ->
                                byAct.addChar(actId, charId)
                                byChapter.addChar(chapterId, charId)
                                byEvent.addChar(eventId, charId)
                            }
                        }

                        event.updatedWikis.forEach { wiki ->
                            wikiByAct.addWiki(actId, wiki.id)
                            wikiByChapter.addWiki(chapterId, wiki.id)
                            wikiByEvent.addWiki(eventId, wiki.id)
                        }
                    }

                    eventIdsByChapter[chapterId] = eventIds
                }

                chapterIdsByAct[actId] = chapterIds

                characters.forEach { character ->
                    val debutEventId = character.firstSceneId ?: return@forEach
                    sagaContent.findTimeline(debutEventId)?.let { timeline ->
                        if (act.chapters.any { it.data.id == timeline.data.chapterId }) {
                            byAct.addChar(actId, character.id)
                        }
                    }
                }

                characters.forEach { character ->
                    if (actMessages.any { it.message.characterId == character.id }) return@forEach
                    if (actMessages.filterMention(character.name).isNotEmpty()) {
                        byAct.addChar(actId, character.id)
                    }
                }

                actMessages.forEach { msg ->
                    sagaContent.wikis.forEach { wiki ->
                        if (wiki.title.isNotBlank() &&
                            msg.message.text.contains(wiki.title, ignoreCase = true)
                        ) {
                            wikiByAct.addWiki(actId, wiki.id)
                        }
                    }
                }
            }

            sagaContent.wikis.forEach { wiki ->
                wiki.chapterId?.let { chapterId ->
                    sagaContent.acts.forEach { act ->
                        if (act.chapters.any { it.data.id == chapterId }) {
                            wikiByAct.addWiki(act.data.id, wiki.id)
                            wikiByChapter.addWiki(chapterId, wiki.id)
                        }
                    }
                }
                wiki.timelineId?.let { timelineId ->
                    sagaContent.flatEvents().find { it.data.id == timelineId }?.let { event ->
                        val chapterId = event.data.chapterId
                        sagaContent.acts.forEach { act ->
                            act.chapters.find { it.data.id == chapterId }?.let { chapter ->
                                wikiByAct.addWiki(act.data.id, wiki.id)
                                wikiByChapter.addWiki(chapter.data.id, wiki.id)
                                wikiByEvent.addWiki(event.data.id, wiki.id)
                            }
                        }
                    }
                }
            }

            return BrainPresenceIndex(
                byAct = mergePresence(byAct, wikiByAct),
                byChapter = mergePresence(byChapter, wikiByChapter),
                byEvent = mergePresence(byEvent, wikiByEvent),
                actIdsInOrder = actIdsInOrder,
                chapterIdsByAct = chapterIdsByAct,
                eventIdsByChapter = eventIdsByChapter,
                currentActId = sagaContent.currentActInfo?.data?.id,
            )
        }

        private fun mergePresence(
            charMap: Map<Int, MutableSet<Int>>,
            wikiMap: Map<Int, MutableSet<Int>>,
        ): Map<Int, BrainScopePresence> {
            val keys = charMap.keys + wikiMap.keys
            return keys.associateWith { key ->
                BrainScopePresence(
                    characterIds = charMap[key].orEmpty(),
                    wikiIds = wikiMap[key].orEmpty(),
                )
            }
        }
    }

private fun MutableMap<Int, MutableSet<Int>>.addChar(
    scopeId: Int,
    charId: Int,
) {
    getOrPut(scopeId) { mutableSetOf() }.add(charId)
}

private fun MutableMap<Int, MutableSet<Int>>.addWiki(
    scopeId: Int,
    wikiId: Int,
) {
    getOrPut(scopeId) { mutableSetOf() }.add(wikiId)
}
