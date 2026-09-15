package com.ilustris.sagai.features.audiobook.data.usecase

import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.AudioGenClient
import com.ilustris.sagai.core.ai.TranscribeClient
import com.ilustris.sagai.core.ai.model.AudioConfig
import com.ilustris.sagai.core.ai.model.PromptBlueprint
import com.ilustris.sagai.core.ai.model.Voice
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.file.FileHelper
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.utils.StringResourceHelper
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.act.data.model.Book
import com.ilustris.sagai.features.audiobook.data.model.AlignmentStatus
import com.ilustris.sagai.features.audiobook.data.model.AudioSection
import com.ilustris.sagai.features.audiobook.data.model.BookAudioConfig
import com.ilustris.sagai.features.audiobook.data.model.BookAudioSegment
import com.ilustris.sagai.features.audiobook.data.model.WordTiming
import com.ilustris.sagai.features.audiobook.data.source.BookAudioDao
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class BookAudioUseCaseImpl
    @Inject
    constructor(
        private val sagaRepository: SagaRepository,
        private val bookAudioDao: BookAudioDao,
        private val audioGenClient: AudioGenClient,
        private val transcribeClient: TranscribeClient,
        private val remoteConfigService: RemoteConfigService,
        private val fileHelper: FileHelper,
        private val stringResourceHelper: StringResourceHelper,
    ) : BookAudioUseCase {
        override suspend fun config(): BookAudioConfig? {
            val settings = remoteConfigService.getJson<BookAudioConfig>(CONFIG_KEY)?.takeIf { it.isValid() } ?: return null
            return settings.takeIf { narrationInstruction() != null }
        }

        /**
         * The narrator persona lives in its own blueprint ([NARRATION_BLUEPRINT_KEY]), like every
         * other AI prompt in the app — not inlined into [BookAudioConfig]. It never generates JSON,
         * so this renders `directives`/`rules` straight into the plain instruction string the TTS
         * request wants, instead of going through [com.ilustris.sagai.core.ai.GemmaClient]'s
         * system-instruction split.
         */
        private suspend fun narrationInstruction(): String? {
            val blueprint = remoteConfigService.getJson<PromptBlueprint>(NARRATION_BLUEPRINT_KEY) ?: return null
            val lines = (blueprint.directives + blueprint.rules).entries.map { (key, value) -> "$key: $value" }
            return lines.joinToString("\n").takeIf { it.isNotBlank() }
        }

        override fun sections(act: ActContent): List<AudioSection> {
            val book = act.book?.takeIf { it.isSealed() } ?: return emptyList()
            return buildList {
                book.prologue?.takeIf { it.isNotEmpty() }?.let { pages ->
                    add(
                        AudioSection(
                            AudioSection.PROLOGUE,
                            stringResourceHelper.getString(R.string.book_prologue_title),
                            pages.map { it.content },
                        ),
                    )
                }
                if (book.isLegacy()) {
                    book.chapters.forEachIndexed { index, chapter ->
                        add(AudioSection(AudioSection.legacyKey(index), chapter.title, chapter.pages.map { it.content }))
                    }
                } else {
                    act.volumeChapters().forEach { chapter ->
                        val chapterId = chapter.chapterId ?: return@forEach
                        add(AudioSection(AudioSection.chapterKey(chapterId), chapter.title, chapter.pages.map { it.content }))
                    }
                }
                book.epilogue?.takeIf { it.isNotEmpty() }?.let { pages ->
                    add(
                        AudioSection(
                            AudioSection.EPILOGUE,
                            stringResourceHelper.getString(R.string.book_epilogue_title),
                            pages.map { it.content },
                        ),
                    )
                }
            }
        }

        override fun observeSegments(bookId: Long) = bookAudioDao.observeSegments(bookId)

        override fun narrateSection(
            sagaId: Int,
            actId: Int,
            sectionKey: String,
        ): Flow<NarrationProgress> =
            flow {
                val context = resolve(sagaId, actId, sectionKey) ?: return@flow
                val (book, section, config) = context
                val voice = narratorVoice(book, config)
                val plans = BookAudioSegmenter.plan(section.pages, config.maxSegmentChars)

                val existing = bookAudioDao.getSectionSegments(book.id, section.key)
                val stale = existing.any { segment -> !segment.matches(plans.getOrNull(segment.segmentIndex), voice) }
                if (stale) {
                    Timber.tag(TAG).i("Section ${section.key} changed since narration, discarding ${existing.size} segments")
                    discard(book.id, section.key, existing)
                }
                val done = if (stale) emptySet() else existing.map { it.segmentIndex }.toSet()

                var firstCall = true
                for (plan in plans) {
                    if (plan.index in done) continue
                    emit(NarrationProgress.Narrating(plan.index + 1, plans.size))

                    val result =
                        executeRequest(reportCrash = false) {
                            var attempt = 0
                            var segment: BookAudioSegment
                            do {
                                if (!firstCall && config.requestIntervalMs > 0) delay(config.requestIntervalMs)
                                firstCall = false
                                segment = narrate(sagaId, book, section, plan, voice, config)
                                attempt++
                                val diverged = segment.alignmentStatus == AlignmentStatus.DIVERGED
                                if (diverged && attempt <= config.maxRegenerations) {
                                    Timber.tag(TAG).w("Segment ${plan.index} diverged (${segment.alignmentScore}), narrating again")
                                    File(segment.audioPath).delete()
                                }
                            } while (diverged && attempt <= config.maxRegenerations)
                            bookAudioDao.saveSegment(segment)
                        }

                    if (result is RequestResult.Error) {
                        return@flow emit(NarrationProgress.Failed(result.value.message.orEmpty()))
                    }
                }

                emit(NarrationProgress.Done(bookAudioDao.getSectionSegments(book.id, section.key)))
            }

        override fun realignSection(
            sagaId: Int,
            actId: Int,
            sectionKey: String,
        ): Flow<NarrationProgress> =
            flow {
                val (book, section, config) = resolve(sagaId, actId, sectionKey) ?: return@flow
                val segments = bookAudioDao.getSectionSegments(book.id, section.key)
                segments.forEachIndexed { position, segment ->
                    emit(NarrationProgress.Aligning(position + 1, segments.size))
                    val wav = fileHelper.readAudioFile(segment.audioPath) ?: return@forEachIndexed
                    val plan = segment.plan(section.pages) ?: return@forEachIndexed
                    bookAudioDao.saveSegment(align(section, plan, wav, segment.durationMs, config).let { segment.withAlignment(it) })
                }
                emit(NarrationProgress.Done(bookAudioDao.getSectionSegments(book.id, section.key)))
            }

        override suspend fun deleteSection(
            bookId: Long,
            sectionKey: String,
        ) {
            discard(bookId, sectionKey, bookAudioDao.getSectionSegments(bookId, sectionKey))
        }

        private suspend fun narrate(
            sagaId: Int,
            book: Book,
            section: AudioSection,
            plan: SegmentPlan,
            voice: Voice,
            config: BookAudioConfig,
        ): BookAudioSegment {
            val wav =
                audioGenClient.generateAudio(
                    AudioConfig(
                        voice = voice,
                        prompt = plan.text,
                        instruction = narrationInstruction() ?: error("$NARRATION_BLUEPRINT_KEY is not configured"),
                    ),
                ) ?: error("Narration returned no audio")

            val file =
                fileHelper.saveBinaryFile(
                    data = wav,
                    path = "sagas/$sagaId/audios/book/${book.id}",
                    fileName = "${section.key}_${plan.index}_",
                    extension = "wav",
                ) ?: error("Couldn't save narration audio")

            val durationMs = (wav.size - WAV_HEADER_BYTES).coerceAtLeast(0) / PCM_BYTES_PER_MS
            val alignment = align(section, plan, wav, durationMs, config)
            return BookAudioSegment(
                bookId = book.id,
                sectionKey = section.key,
                segmentIndex = plan.index,
                startPageIndex = plan.startPageIndex,
                startChar = plan.startChar,
                endPageIndex = plan.endPageIndex,
                endChar = plan.endChar,
                textHash = plan.textHash,
                audioPath = file.absolutePath,
                durationMs = durationMs,
                voice = voice.id,
                alignmentStatus = alignment.status,
                alignmentScore = alignment.score,
                timings = alignment.timings,
            )
        }

        private data class SegmentAlignment(
            val status: String,
            val score: Float?,
            val timings: List<WordTiming>,
        )

        /** Transcription failures never fail narration: the clip keeps character-weighted timings instead. */
        private suspend fun align(
            section: AudioSection,
            plan: SegmentPlan,
            wav: ByteArray,
            durationMs: Long,
            config: BookAudioConfig,
        ): SegmentAlignment {
            if (!transcribeClient.isAvailable(config.transcribeModel)) {
                return SegmentAlignment(AlignmentStatus.ESTIMATED, null, TimingAligner.estimate(section.pages, plan, durationMs))
            }
            return try {
                val transcription = transcribeClient.transcribeWords(wav, legacyModel = config.transcribeModel)
                val alignment = TimingAligner.align(section.pages, plan, transcription.words, durationMs)
                Timber.tag(TAG).d(
                    "Segment ${section.key}#${plan.index}: score=${alignment.score} spoken=${transcription.words.size}\n${transcription.rawJson}",
                )
                val status = if (alignment.score < config.minAlignmentScore) AlignmentStatus.DIVERGED else AlignmentStatus.ALIGNED
                SegmentAlignment(status, alignment.score, alignment.timings)
            } catch (e: Exception) {
                Timber.tag(TAG).w(e, "Transcription failed for ${section.key}#${plan.index}, estimating timings")
                SegmentAlignment(AlignmentStatus.ESTIMATED, null, TimingAligner.estimate(section.pages, plan, durationMs))
            }
        }

        private data class Resolved(
            val book: Book,
            val section: AudioSection,
            val config: BookAudioConfig,
        )

        private suspend fun FlowCollector<NarrationProgress>.resolve(
            sagaId: Int,
            actId: Int,
            sectionKey: String,
        ): Resolved? {
            val config = config() ?: return null.also { emit(NarrationProgress.Failed("Audiobook is not configured")) }
            val saga = sagaRepository.getSagaById(sagaId).first()
            val act = saga?.acts?.find { it.data.id == actId }
            val book = act?.book?.takeIf { it.isSealed() }
            val section = act?.let { sections(it) }?.find { it.key == sectionKey }
            if (book == null || section == null) {
                emit(NarrationProgress.Failed("Section $sectionKey not found for act $actId"))
                return null
            }
            return Resolved(book, section, config)
        }

        private suspend fun narratorVoice(
            book: Book,
            config: BookAudioConfig,
        ): Voice {
            Voice.findByName(book.narrationVoice)?.let { return it }
            val voice =
                config.voices.mapNotNull { Voice.findByName(it) }.randomOrNull()
                    ?: error("book_audio_config has no known voices")
            bookAudioDao.setNarrationVoice(book.id, voice.id)
            return voice
        }

        private suspend fun discard(
            bookId: Long,
            sectionKey: String,
            segments: List<BookAudioSegment>,
        ) {
            segments.forEach { File(it.audioPath).delete() }
            bookAudioDao.deleteSection(bookId, sectionKey)
        }

        private fun BookAudioSegment.matches(
            plan: SegmentPlan?,
            voice: Voice,
        ) = plan != null &&
            textHash == plan.textHash &&
            this.voice == voice.id &&
            File(audioPath).exists()

        /** Null when the section text changed since this clip was narrated. */
        private fun BookAudioSegment.plan(pages: List<String>): SegmentPlan? {
            if (endPageIndex > pages.lastIndex) return null
            return runCatching { BookAudioSegmenter.between(pages, segmentIndex, startPageIndex, startChar, endPageIndex, endChar) }
                .getOrNull()
                ?.takeIf { it.textHash == textHash }
        }

        private fun BookAudioSegment.withAlignment(alignment: SegmentAlignment) =
            copy(alignmentStatus = alignment.status, alignmentScore = alignment.score, timings = alignment.timings)

        companion object {
            private const val TAG = "🎧 Audiobook"
            const val CONFIG_KEY = "book_audio_config"
            const val NARRATION_BLUEPRINT_KEY = "book_audio_narration_blueprint"
            private const val WAV_HEADER_BYTES = 44

            /** 24 kHz, 16-bit mono PCM. */
            private const val PCM_BYTES_PER_MS = 48L
        }
    }
