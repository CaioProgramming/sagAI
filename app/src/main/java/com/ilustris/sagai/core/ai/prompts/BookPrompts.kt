package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.act.data.model.BookPage
import com.ilustris.sagai.features.act.data.model.WriterNotes
import com.ilustris.sagai.features.characters.data.model.ArcSourceType
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent

data class BookPrologueArgs(
    val sagaContext: String,
    val actContext: String,
    val previousVolume: String,
    val position: String,
)

data class BookChapterArgs(
    val sagaContext: String,
    val actContext: String,
    val chapter: String,
    val scenes: String,
    val characters: String,
    val writerNotes: String,
    val previousTail: String,
    val position: String,
)

data class BookVolumeClosureArgs(
    val sagaContext: String,
    val actSynthesis: String,
    val chapterNotes: String,
    val openingPage: String,
    val closingPage: String,
    val previousVolumes: String,
    val position: String,
)

object BookPrompts {
    const val BOOK_PROLOGUE_BLUEPRINT = "book_prologue_blueprint"
    const val BOOK_CHAPTER_BLUEPRINT = "book_chapter_blueprint"
    const val BOOK_VOLUME_CLOSURE_BLUEPRINT = "book_volume_closure_blueprint"

    /** Upper bound for the raw chat transcript of one chapter; oldest lines are dropped past it. */
    private const val SCENES_CHAR_BUDGET = 60_000
    private const val PREVIOUS_TAIL_CHARS = 1_200

    private const val NONE = "None"

    suspend fun prologuePrompt(
        promptService: PromptService,
        saga: SagaContent,
        act: ActContent,
        rules: NarrativeRules,
    ): SplitPrompt {
        val previousAct = saga.previousActOf(act)
        val previousVolume =
            previousAct?.let { previous ->
                buildMap {
                    put("title", previous.data.title)
                    previous.book?.authorNote?.let { put("authorNote", it) }
                    previous.volumeChapters().lastOrNull()?.chapterId?.let { lastId ->
                        previous.chapters
                            .find { it.data.id == lastId }
                            ?.bookPages
                            ?.writerNotes
                            ?.let { put("lastWriterNotes", it) }
                    }
                }.toJsonFormat()
            } ?: NONE

        return promptService.buildSplitBlueprint(
            BOOK_PROLOGUE_BLUEPRINT,
            BookPrologueArgs(
                sagaContext = SagaPrompts.mainContext(saga),
                actContext = actContext(act),
                previousVolume = previousVolume,
                position = position(saga, act, rules, chapterIndex = null),
            ),
        )
    }

    suspend fun chapterPrompt(
        promptService: PromptService,
        saga: SagaContent,
        act: ActContent,
        chapter: ChapterContent,
        rules: NarrativeRules,
    ): SplitPrompt {
        val orderedChapters = act.bookChapters(rules)
        val index = orderedChapters.indexOfFirst { it.data.id == chapter.data.id }
        val previousChapter = orderedChapters.getOrNull(index - 1)

        val previousNotes: WriterNotes? =
            previousChapter?.bookPages?.writerNotes ?: act.book?.prologueNotes
        val previousPages: List<BookPage>? =
            previousChapter?.bookPages?.pages ?: act.book?.prologue

        val messages = chapter.fetchChapterMessages().sortedBy { it.message.timestamp }
        val presentIds = messages.mapNotNull { it.message.characterId }.toSet()
        val characters =
            saga.characters
                .filter { it.data.id in presentIds }
                .map { character ->
                    buildMap {
                        put("character", character.data.toAINormalize(ChatPrompts.CHARACTER_EXCLUSIONS))
                        character.arcs
                            .filter { it.sourceType == ArcSourceType.CHAPTER && it.sourceId == chapter.data.id }
                            .takeIf { it.isNotEmpty() }
                            ?.let { arcs -> put("arcInThisChapter", arcs.map { "${it.title}: ${it.content}" }) }
                    }
                }.toJsonFormat()

        return promptService.buildSplitBlueprint(
            BOOK_CHAPTER_BLUEPRINT,
            BookChapterArgs(
                sagaContext = SagaPrompts.mainContext(saga, ommitCharacter = true),
                actContext = actContext(act),
                chapter = chapter.data.toAINormalize(ChapterPrompts.CHAPTER_EXCLUSIONS),
                scenes = scenes(chapter),
                characters = characters,
                writerNotes = previousNotes?.toJsonFormat() ?: NONE,
                previousTail =
                    previousPages
                        ?.lastOrNull()
                        ?.content
                        ?.takeLast(PREVIOUS_TAIL_CHARS)
                        ?: NONE,
                position = position(saga, act, rules, chapterIndex = index),
            ),
        )
    }

    suspend fun volumeClosurePrompt(
        promptService: PromptService,
        saga: SagaContent,
        act: ActContent,
        rules: NarrativeRules,
    ): SplitPrompt {
        val chapters = act.bookChapters(rules)
        val chapterNotes =
            chapters
                .mapNotNull { chapter ->
                    chapter.bookPages?.let {
                        mapOf("chapter" to chapter.data.title, "writerNotes" to it.writerNotes)
                    }
                }.toJsonFormat()
        val openingPage =
            act.book?.prologue?.firstOrNull()?.content
                ?: chapters.firstOrNull()?.bookPages?.pages?.firstOrNull()?.content
                ?: NONE
        val closingPage =
            chapters
                .lastOrNull()
                ?.bookPages
                ?.pages
                ?.lastOrNull()
                ?.content ?: NONE
        val previousVolumes =
            saga.acts
                .takeWhile { it.data.id != act.data.id }
                .mapNotNull { previous ->
                    previous.book?.takeIf { it.isSealed() }?.let {
                        mapOf(
                            "title" to previous.data.title,
                            "coverQuote" to it.coverQuote,
                            "authorNote" to (it.authorNote ?: emptyString()),
                        )
                    }
                }.takeIf { it.isNotEmpty() }
                ?.toJsonFormat() ?: NONE

        return promptService.buildSplitBlueprint(
            BOOK_VOLUME_CLOSURE_BLUEPRINT,
            BookVolumeClosureArgs(
                sagaContext = SagaPrompts.mainContext(saga),
                actSynthesis = act.data.toAINormalize(ActPrompts.ACT_EXCLUSIONS),
                chapterNotes = chapterNotes,
                openingPage = openingPage,
                closingPage = closingPage,
                previousVolumes = previousVolumes,
                position = position(saga, act, rules, chapterIndex = null),
            ),
        )
    }

    private fun actContext(act: ActContent) =
        buildMap {
            put("introduction", act.data.introduction)
            act.data.narrativeGuide?.takeIf { it.isNotBlank() }?.let { put("narrativeGuide", it) }
        }.toJsonFormat()

    private fun position(
        saga: SagaContent,
        act: ActContent,
        rules: NarrativeRules,
        chapterIndex: Int?,
    ) = buildMap {
        put("volume", saga.acts.indexOfFirst { it.data.id == act.data.id } + 1)
        put("totalVolumes", rules.actUpdateLimit)
        chapterIndex?.let {
            put("chapter", it + 1)
            put("chaptersPerVolume", rules.chapterUpdateLimit)
        }
        put("isFinalVolume", saga.acts.size >= rules.actUpdateLimit && saga.acts.lastOrNull()?.data?.id == act.data.id)
    }.toJsonFormat()

    /** Chapter events with their real chat lines, trimmed from the oldest side to fit the budget. */
    private fun scenes(chapter: ChapterContent): String {
        val blocks =
            chapter.events
                .sortedBy { it.data.createdAt }
                .map { event ->
                    buildString {
                        appendLine("## SCENE")
                        appendLine(event.data.toAINormalize(LorePrompts.TIMELINE_EXCLUDED_FIELDS))
                        appendLine("### TRANSCRIPT")
                        event.messages
                            .sortedBy { it.message.timestamp }
                            .forEach { appendLine(it.transcriptLine()) }
                    }
                }
        val kept = ArrayDeque<String>()
        var size = 0
        for (block in blocks.asReversed()) {
            if (size + block.length > SCENES_CHAR_BUDGET && kept.isNotEmpty()) break
            kept.addFirst(block.takeLast(SCENES_CHAR_BUDGET))
            size += block.length
        }
        return kept.joinToString("\n")
    }

    private fun MessageContent.transcriptLine(): String {
        val speaker = character?.name ?: message.speakerName ?: message.senderType.name
        return "$speaker: ${message.text}"
    }

    private fun SagaContent.previousActOf(act: ActContent): ActContent? {
        val index = acts.indexOfFirst { it.data.id == act.data.id }
        return if (index > 0) acts[index - 1] else null
    }
}
