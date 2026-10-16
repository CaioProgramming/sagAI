package com.ilustris.sagai.core.ai.services

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ModelRequirement
import com.ilustris.sagai.core.ai.model.Artwork
import com.ilustris.sagai.core.ai.model.ImageType
import com.ilustris.sagai.core.ai.prompts.ArtworkConcept
import com.ilustris.sagai.core.ai.prompts.ArtworkPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.features.newsaga.data.model.Genre
import javax.inject.Inject

/**
 * Ensures a Saga/Chapter/Character has an `artwork` concept before it is used as image
 * generation context. If the current artwork is already set, it's returned as-is with no AI call.
 */
class ArtworkConceptService
    @Inject
    constructor(
        private val gemmaClient: GemmaClient,
        private val genreConfigService: GenreConfigService,
        private val promptService: PromptService,
    ) {
        /**
         * Artwork rules for [imageType] as an instruction bucket, to merge into any generation
         * that outputs an `artwork` field. Reads from the same blueprint used by [ensureArtwork],
         * so icon rules only need to be maintained in one place.
         */
        suspend fun artworkInstructions(imageType: ImageType): Map<String, Any> =
            promptService
                .buildSplitBlueprint(ArtworkPrompts.conceptBlueprintKey(imageType))
                .renderInstructions()

        /** The text-only portrait concept (characters); covers go through [ensureCoverArtwork]. */
        suspend fun ensureArtwork(
            imageType: ImageType,
            contentType: String,
            genre: Genre,
            context: String,
            currentArtwork: String?,
        ): RequestResult<String> =
            executeRequest {
                currentArtwork?.takeIf { it.isNotBlank() } ?: gemmaClient
                    .generateBlueprint<ArtworkConcept>(
                        remoteConfigKey = ArtworkPrompts.conceptBlueprintKey(imageType),
                        variables =
                            mapOf(
                                "contentType" to contentType,
                                "genreAesthetic" to genreConfigService.aesthetic(genre),
                                "context" to context,
                            ),
                        requirement = ModelRequirement.MEDIUM,
                    )!!
                    .artwork
            }

        /**
         * The cover curator: reads [context] (a whole saga or chapter, never its latest beat) and
         * decides the concept, the characters the cover needs and the story elements it draws on.
         * Its own step, so the choice is made with full attention instead of at the tail of a
         * prose generation.
         */
        suspend fun ensureCoverArtwork(
            contentType: String,
            genre: Genre,
            context: String,
            currentArtwork: Artwork?,
        ): RequestResult<Artwork> =
            executeRequest {
                currentArtwork?.takeUnless { it.isBlank() } ?: gemmaClient
                    .generateBlueprint<Artwork>(
                        remoteConfigKey = ArtworkPrompts.COVER_CURATOR_BLUEPRINT,
                        variables =
                            mapOf(
                                "contentType" to contentType,
                                "genreAesthetic" to genreConfigService.aesthetic(genre),
                                "context" to context,
                            ),
                        requirement = ModelRequirement.MEDIUM,
                    )!!
            }
    }
