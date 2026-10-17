package com.ilustris.sagai.features.widgets.data

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ModelRequirement
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.features.characters.data.model.CharacterArc
import com.ilustris.sagai.features.characters.data.model.CharacterDetailData
import com.ilustris.sagai.features.widgets.domain.LinesFacts
import com.ilustris.sagai.features.widgets.domain.LinesType
import com.ilustris.sagai.features.widgets.domain.WidgetLimits
import com.ilustris.sagai.features.widgets.domain.WidgetLinesPrompt
import com.ilustris.sagai.features.widgets.domain.WidgetLinesSanitizer
import javax.inject.Inject
import javax.inject.Singleton

/** What the model returns: the lines, nothing else. */
data class WidgetLinesGen(
    val lines: List<String> = emptyList(),
)

/**
 * Writes a batch of poetic lines for a generated widget, in a character's voice.
 *
 * One call per batch, with the profile, arcs, events and bonds of the character and none of the
 * chat. The result goes through the same safety layer as every other generation in the app.
 */
@Singleton
class WidgetLinesUseCase
    @Inject
    constructor(
        private val gemmaClient: GemmaClient,
        private val genreConfigService: GenreConfigService,
        private val promptService: PromptService,
    ) {
        suspend fun generate(
            detail: CharacterDetailData,
            arcs: List<CharacterArc>,
            type: LinesType,
            objective: String?,
            avoid: List<String> = emptyList(),
            count: Int = WidgetLimits.BATCH_SIZE,
        ): RequestResult<List<String>> =
            executeRequest(reportCrash = false) {
                val genre = detail.sagaInfo.genre
                val character = detail.character
                val facts =
                    LinesFacts(
                        type = type,
                        characterName = character.name,
                        occupation = character.profile.occupation,
                        personality = character.profile.personality,
                        backstory = character.backstory,
                        sagaTitle = detail.sagaInfo.title,
                        genreAesthetic = genreConfigService.aesthetic(genre),
                        arcs = arcs.map { "${it.title}: ${it.content}" },
                        events = detail.events.map { "${it.event.title}: ${it.event.summary}" },
                        relationships = detail.relationships.map { it.summarizeRelation(threshold = 1) },
                        objective = objective,
                        count = count,
                        avoid = avoid,
                    )
                // Instructions come from the Remote Config blueprint only (remote-only, no compiled copy);
                // the task context is data and is built here. A missing blueprint fails the request.
                val prompt =
                    promptService
                        .buildSplitBlueprint(WidgetLinesPrompt.BLUEPRINT_KEY, emptyMap<String, String>())
                        .copy(processedTemplate = WidgetLinesPrompt.build(facts))

                val generated =
                    gemmaClient.generate<WidgetLinesGen>(
                        promptSplit = prompt,
                        requirement = ModelRequirement.LOW,
                        temperatureRandomness = CREATIVE_TEMPERATURE,
                    )
                val lines = WidgetLinesSanitizer.clean(generated?.lines.orEmpty(), type, avoid)
                check(lines.isNotEmpty()) { "The model returned no usable widget lines" }
                lines
            }

        private companion object {
            const val CREATIVE_TEMPERATURE = 0.9f
        }
    }
