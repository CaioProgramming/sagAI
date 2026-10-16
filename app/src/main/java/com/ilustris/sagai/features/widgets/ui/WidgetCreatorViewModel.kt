package com.ilustris.sagai.features.widgets.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.characters.data.model.CharacterArc
import com.ilustris.sagai.features.characters.data.model.CharacterDetailData
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase
import com.ilustris.sagai.features.widgets.data.WidgetDataSource
import com.ilustris.sagai.features.widgets.data.WidgetLinesUseCase
import com.ilustris.sagai.features.widgets.domain.LinesType
import com.ilustris.sagai.features.widgets.domain.RotationRule
import com.ilustris.sagai.features.widgets.domain.WidgetConfig
import com.ilustris.sagai.features.widgets.domain.WidgetLimits
import com.ilustris.sagai.features.widgets.domain.WidgetLinesSanitizer
import com.ilustris.sagai.features.widgets.glance.WidgetPinner
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random

enum class CreatorStep {
    STYLE,
    GENERATING,
    PICK,
    OPTIONS,
}

data class CreatorState(
    val step: CreatorStep = CreatorStep.STYLE,
    val type: LinesType = LinesType.QUOTE,
    val lines: List<String> = emptyList(),
    /** Indexes into [lines] the player kept. */
    val selected: Set<Int> = emptySet(),
    val rotation: RotationRule = RotationRule.EACH_OPEN,
    val discreet: Boolean = false,
    val failed: Boolean = false,
    val generatingMore: Boolean = false,
    val littleHistory: Boolean = false,
    val limitReached: Boolean = false,
    val pinUnsupported: Boolean = false,
    /** True once the launcher showed its confirmation, so the sheet can close. */
    val done: Boolean = false,
)

/**
 * Drives the widget creation flow for one character: pick a style, let the AI write a batch of
 * lines, keep the ones the player likes, choose how they rotate, and ask the launcher to pin it.
 */
@HiltViewModel
class WidgetCreatorViewModel
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val characterUseCase: CharacterUseCase,
        private val linesUseCase: WidgetLinesUseCase,
        private val dataSource: WidgetDataSource,
    ) : ViewModel() {
        private val _state = MutableStateFlow(CreatorState())
        val state: StateFlow<CreatorState> = _state.asStateFlow()

        private var detail: CharacterDetailData? = null
        private var arcs: List<CharacterArc> = emptyList()
        private var objective: String? = null
        private var generationJob: Job? = null

        /** Loads what the prompt needs. Calling it again for the same character keeps the player's progress. */
        fun start(data: CharacterDetailData) {
            if (detail?.character?.id == data.character.id) return
            generationJob?.cancel()
            detail = data
            _state.value = CreatorState()
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    arcs = characterUseCase.getCharacterArcs(data.character.id).first()
                    objective = dataSource.currentObjective(data.sagaInfo.id)
                    val limitReached = WidgetPinner.count(context) >= WidgetLimits.MAX_WIDGETS
                    _state.update { it.copy(limitReached = limitReached) }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Timber.tag(TAG).w(e, "Could not load the widget creator context")
                }
            }
        }

        /** Forgets the flow, so the next time the sheet opens it starts from the first step. */
        fun reset() {
            generationJob?.cancel()
            detail = null
            _state.value = CreatorState()
        }

        fun chooseType(type: LinesType) = _state.update { it.copy(type = type) }

        fun generate() {
            val data = detail ?: return
            generationJob?.cancel()
            _state.update {
                it.copy(
                    step = CreatorStep.GENERATING,
                    failed = false,
                    lines = emptyList(),
                    selected = emptySet(),
                    generatingMore = false,
                )
            }
            generationJob =
                viewModelScope.launch(Dispatchers.IO) {
                    when (val result = linesUseCase.generate(data, arcs, _state.value.type, objective)) {
                        is RequestResult.Success -> {
                            val lines = result.value
                            _state.update {
                                it.copy(
                                    step = CreatorStep.PICK,
                                    lines = lines,
                                    selected = lines.indices.take(DEFAULT_SELECTED).toSet(),
                                    littleHistory =
                                        WidgetLinesSanitizer.hasLittleHistory(data.events.size, arcs.size),
                                )
                            }
                        }

                        is RequestResult.Error -> {
                            Timber.tag(TAG).w(result.value, "Widget lines generation failed")
                            _state.update { it.copy(failed = true) }
                        }
                    }
                }
        }

        /** Asks for another batch that avoids repeating what is already there. */
        fun generateMore() {
            val data = detail ?: return
            val current = _state.value
            if (current.generatingMore) return
            _state.update { it.copy(generatingMore = true) }
            generationJob =
                viewModelScope.launch(Dispatchers.IO) {
                    val result = linesUseCase.generate(data, arcs, current.type, objective, avoid = current.lines)
                    _state.update { latest ->
                        when (result) {
                            is RequestResult.Success -> latest.copy(lines = latest.lines + result.value, generatingMore = false)
                            is RequestResult.Error -> latest.copy(generatingMore = false)
                        }
                    }
                }
        }

        fun toggle(index: Int) =
            _state.update {
                when {
                    index in it.selected -> it.copy(selected = it.selected - index)
                    it.selected.size >= WidgetLimits.MAX_LINES -> it
                    else -> it.copy(selected = it.selected + index)
                }
            }

        fun selectAllOrNone() =
            _state.update {
                if (it.selected.isNotEmpty()) {
                    it.copy(selected = emptySet())
                } else {
                    it.copy(selected = it.lines.indices.take(WidgetLimits.MAX_LINES).toSet())
                }
            }

        /** Replaces a line with the player's own wording, held to the same limits as generated ones. */
        fun editLine(
            index: Int,
            text: String,
        ) = _state.update {
            val cleaned = WidgetLinesSanitizer.clean(listOf(text), it.type).firstOrNull()
            if (cleaned == null || index !in it.lines.indices) {
                it
            } else {
                it.copy(lines = it.lines.toMutableList().also { list -> list[index] = cleaned })
            }
        }

        fun setRotation(rule: RotationRule) = _state.update { it.copy(rotation = rule) }

        fun setDiscreet(enabled: Boolean) = _state.update { it.copy(discreet = enabled) }

        fun goTo(step: CreatorStep) {
            if (step != CreatorStep.GENERATING) generationJob?.cancel()
            _state.update { it.copy(step = step, failed = false) }
        }

        /** Asks the launcher to add the widget. The system shows its own confirmation. */
        fun addToHome() {
            val data = detail ?: return
            val current = _state.value
            val chosen = current.selected.sorted().mapNotNull { current.lines.getOrNull(it) }
            if (chosen.isEmpty()) return
            viewModelScope.launch {
                if (!WidgetPinner.isSupported(context)) {
                    _state.update { it.copy(pinUnsupported = true) }
                    return@launch
                }
                val config =
                    WidgetConfig(
                        type = current.type,
                        sagaId = data.sagaInfo.id,
                        characterId = data.character.id,
                        characterName = data.character.name,
                        lines = chosen,
                        rotation = current.rotation,
                        discreet = current.discreet,
                        seed = Random.nextInt(1, Int.MAX_VALUE),
                    )
                val shown =
                    try {
                        WidgetPinner.pin(context, config)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Timber.tag(TAG).e(e, "Could not request the widget pin")
                        false
                    }
                _state.update { it.copy(pinUnsupported = !shown, done = shown) }
            }
        }

        private companion object {
            const val TAG = "WidgetCreator"
            const val DEFAULT_SELECTED = 6
        }
    }
