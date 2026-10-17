package com.ilustris.sagai.features.characters.data.usecase

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ModelRequirement
import com.ilustris.sagai.core.ai.prompts.EpiloguePrompts
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.data.model.CharacterKnowledge
import com.ilustris.sagai.features.characters.data.model.GeneratedCharacterKnowledge
import com.ilustris.sagai.features.characters.data.source.CharacterKnowledgeDao
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.datasource.EpilogueMessageDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps each character's [CharacterKnowledge] of the player up to date: every few epilogue turns
 * it folds the new ones into the character's memory, rewriting it whole so it stays small. Runs in
 * its own scope so leaving the screen mid-compaction doesn't lose the pass. Background only — a
 * failure (quota, a missing blueprint) just means the next pass folds in more turns.
 */
@Singleton
class CharacterKnowledgeService
    @Inject
    constructor(
        private val knowledgeDao: CharacterKnowledgeDao,
        private val epilogueMessageDao: EpilogueMessageDao,
        private val gemmaClient: GemmaClient,
        private val promptService: PromptService,
    ) {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private val running = ConcurrentHashMap.newKeySet<Int>()

        suspend fun get(characterId: Int): CharacterKnowledge? = knowledgeDao.get(characterId)

        /**
         * Compacts when enough turns piled up since the last pass. [leaving] lowers the bar, so a
         * short visit still leaves a mark instead of waiting for the next one to fill a batch.
         */
        fun compactIfDue(
            saga: SagaContent,
            character: CharacterContent,
            leaving: Boolean = false,
        ) {
            val characterId = character.data.id
            if (!running.add(characterId)) return
            scope.launch {
                try {
                    val previous = knowledgeDao.get(characterId)
                    val newTurns = epilogueMessageDao.getMessagesAfter(characterId, previous?.compactedThroughMessageId ?: 0)
                    val due = newTurns.size >= if (leaving) MIN_TURNS_ON_LEAVE else TURNS_PER_PASS
                    if (!due) return@launch

                    val generated =
                        runCatching {
                            gemmaClient.generate<GeneratedCharacterKnowledge>(
                                promptSplit =
                                    EpiloguePrompts.knowledgePrompt(
                                        promptService = promptService,
                                        saga = saga,
                                        character = character,
                                        previous = previous,
                                        newTurns = newTurns,
                                    ),
                                requirement = ModelRequirement.LOW,
                            )
                        }.onFailure { Timber.w(it, "Character knowledge compaction failed for $characterId") }
                            .getOrNull()
                            ?.takeIf { it.impression.isNotBlank() }
                            ?: return@launch

                    knowledgeDao.upsert(
                        CharacterKnowledge(
                            characterId = characterId,
                            sagaId = saga.data.id,
                            impression = generated.impression.trim(),
                            sharedMoments = generated.sharedMoments.map { it.trim() }.filter { it.isNotBlank() }.take(MAX_ITEMS),
                            openThreads = generated.openThreads.map { it.trim() }.filter { it.isNotBlank() }.take(MAX_ITEMS),
                            compactedThroughMessageId = newTurns.last().id,
                        ),
                    )
                } finally {
                    running.remove(characterId)
                }
            }
        }

        companion object {
            private const val TURNS_PER_PASS = 10
            private const val MIN_TURNS_ON_LEAVE = 4

            /** Per list: the memory should read like a few vivid recollections, not a log. */
            private const val MAX_ITEMS = 6
        }
    }
