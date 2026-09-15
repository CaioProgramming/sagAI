package com.ilustris.sagai.features.audiobook

import com.ilustris.sagai.features.audiobook.data.usecase.BookAudioUseCase
import com.ilustris.sagai.features.audiobook.data.usecase.NarrationProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class NarrationJob(
    val bookId: Long,
    val sectionKey: String,
    val progress: NarrationProgress?,
)

/**
 * Runs section narration in a singleton scope so it survives leaving the reader. One job at a
 * time: TTS quotas are per minute and per day, so parallel sections would only fail faster.
 */
@Singleton
class BookAudioService
    @Inject
    constructor(
        private val bookAudioUseCase: BookAudioUseCase,
    ) {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        private val _job = MutableStateFlow<NarrationJob?>(null)

        /** The running job, or the last failed one until [dismissFailure] or a new job starts. */
        val job: StateFlow<NarrationJob?> = _job.asStateFlow()

        fun narrate(
            sagaId: Int,
            actId: Int,
            bookId: Long,
            sectionKey: String,
        ) = run(bookId, sectionKey) { bookAudioUseCase.narrateSection(sagaId, actId, sectionKey) }

        fun realign(
            sagaId: Int,
            actId: Int,
            bookId: Long,
            sectionKey: String,
        ) = run(bookId, sectionKey) { bookAudioUseCase.realignSection(sagaId, actId, sectionKey) }

        fun dismissFailure() {
            if (_job.value?.progress is NarrationProgress.Failed) _job.value = null
        }

        private fun run(
            bookId: Long,
            sectionKey: String,
            work: () -> Flow<NarrationProgress>,
        ) {
            val current = _job.value
            if (current != null && current.progress !is NarrationProgress.Failed) return
            _job.value = NarrationJob(bookId, sectionKey, null)
            scope.launch {
                work()
                    .catch { emit(NarrationProgress.Failed(it.message.orEmpty())) }
                    .collect { progress ->
                        _job.value =
                            when (progress) {
                                is NarrationProgress.Done -> null
                                else -> NarrationJob(bookId, sectionKey, progress)
                            }
                    }
                if (_job.value?.progress !is NarrationProgress.Failed) _job.value = null
            }
        }
    }
