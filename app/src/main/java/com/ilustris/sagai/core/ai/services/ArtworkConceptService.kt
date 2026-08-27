package com.ilustris.sagai.core.ai.services

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ModelRequirement
import com.ilustris.sagai.core.ai.prompts.ArtworkConcept
import com.ilustris.sagai.core.ai.prompts.ArtworkPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.features.newsaga.data.model.Genre
import javax.inject.Inject

/**
 * Ensures a Saga/Chapter/Character has an `artwork` concept before it is used as image
 * generation context. If [currentArtwork] is already set, it's returned as-is with no AI call.
 */
class ArtworkConceptService
    @Inject
    constructor(
        private val gemmaClient: GemmaClient,
        private val genreConfigService: GenreConfigService,
        private val promptService: PromptService,
    ) {
        /**
         * Artwork rules as an instruction bucket, to merge into any generation that outputs
         * an `artwork` field.
         */
        suspend fun artworkInstructions(): Map<String, Any> =
            promptService
                .buildSplitBlueprint(ArtworkPrompts.ARTWORK_DIRECTIVES_BLUEPRINT)
                .renderInstructions()

        suspend fun ensureArtwork(
            contentType: String,
            genre: Genre,
            context: String,
            currentArtwork: String?,
        ): RequestResult<String> =
            executeRequest {
                currentArtwork?.takeIf { it.isNotBlank() } ?: gemmaClient
                    .generateBlueprint<ArtworkConcept>(
                        remoteConfigKey = ArtworkPrompts.ARTWORK_CONCEPT_BLUEPRINT,
                        variables =
                            mapOf(
                                "contentType" to contentType,
                                "genreAesthetic" to genreConfigService.aesthetic(genre),
                                "context" to context,
                            ),
                        useCore = true,
                        requirement = ModelRequirement.MEDIUM,
                    )!!
                    .artwork
            }
    }
