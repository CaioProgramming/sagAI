package com.ilustris.sagai.features.debug.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.ai.debug.DebugImageFallbackService
import com.ilustris.sagai.core.ai.model.ImageType
import com.ilustris.sagai.core.ai.services.ArtworkConceptService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.designChangesTo
import com.ilustris.sagai.features.debug.data.CharacterPreviewGenerator
import com.ilustris.sagai.features.imagegeneration.ImageGenerationService
import com.ilustris.sagai.features.imagegeneration.model.ImageGenerationRequest
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.components.island.ChatIslandService
import com.ilustris.sagai.ui.components.island.IslandContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class CharacterPreviewState(
    val isGeneratingCharacter: Boolean = false,
    val isGeneratingImage: Boolean = false,
    val character: Character? = null,
    val bitmap: Bitmap? = null,
    val error: String? = null,
)

@HiltViewModel
class DesignSystemViewModel @Inject constructor(
    private val chatIslandService: ChatIslandService,
    private val characterPreviewGenerator: CharacterPreviewGenerator,
    private val artworkConceptService: ArtworkConceptService,
    private val imageGenerationService: ImageGenerationService,
    val debugImageFallbackService: DebugImageFallbackService,
) : ViewModel() {

    fun testIsland(content: IslandContent?) {
        chatIslandService.setTop(content)
    }

    /** Characters generated so far for [previewCastGenre]: the cast the next preview is generated against and reviewed with. */
    private val previewCast = mutableListOf<Character>()
    private var previewCastGenre: Genre? = null

    private val _characterPreviewState = MutableStateFlow(CharacterPreviewState())
    val characterPreviewState: StateFlow<CharacterPreviewState> = _characterPreviewState.asStateFlow()

    /**
     * Runs the real character-generation prompt/blueprint pipeline for [genre] against an
     * in-memory [DesignSystemMocks.mockSagaContent] — never touches the database, so this is
     * safe to spam while tuning genre appearance/identity blueprints.
     */
    fun generateRandomCharacterPreview(genre: Genre) {
        viewModelScope.launch {
            _characterPreviewState.value = CharacterPreviewState(isGeneratingCharacter = true)
            if (previewCastGenre != genre) previewCast.clear()
            previewCastGenre = genre
            val result =
                executeRequest {
                    val preview = characterPreviewGenerator.generate(genre, previewCast)
                    preview.generated.designChangesTo(preview.reviewed).forEach { (field, before, after) ->
                        Timber.tag("DesignReview").i("[$field]\n  BEFORE: $before\n  AFTER:  $after")
                    }
                    preview.reviewed
                }.onSuccess { previewCast.add(it) }
            _characterPreviewState.value =
                when (result) {
                    is RequestResult.Success -> CharacterPreviewState(character = result.value)
                    is RequestResult.Error -> CharacterPreviewState(error = result.value.message ?: "Failed to generate character")
                }
        }
    }

    /**
     * Runs real image generation for the previewed character — result is held in memory only.
     * Mirrors [com.ilustris.sagai.features.characters.data.usecase.CharacterUseCaseImpl]'s
     * `ensureCharacterArtwork`: the initial generation call already fills `artwork` as part of
     * the full [Character] schema, so this only asks [ArtworkConceptService] for a fresh concept
     * when that came back blank, instead of always overwriting it.
     */
    fun generatePreviewImage(genre: Genre) {
        val character = _characterPreviewState.value.character ?: return
        viewModelScope.launch {
            _characterPreviewState.update { it.copy(isGeneratingImage = true, error = null) }
            val artwork =
                character.artwork?.takeIf { it.isNotBlank() }
                    ?: artworkConceptService
                        .ensureArtwork(
                            imageType = ImageType.ICON,
                            contentType = ImageType.ICON.name,
                            genre = genre,
                            context = character.backstory,
                            currentArtwork = character.artwork,
                        ).getSuccess()
                        ?.takeIf { it.isNotBlank() }
            val characterWithArtwork = artwork?.let { character.copy(artwork = it) } ?: character
            val contextString =
                buildString {
                    artwork?.let {
                        appendLine("### CONCEPT ART DIRECTION")
                        appendLine(it)
                        appendLine()
                    }
                    appendLine("### CHARACTER")
                    appendLine(
                        characterWithArtwork.toAINormalize(
                            listOf("id", "image", "sagaId", "joinedAt", "knowledge", "firstSceneId", "emojified", "hexColor", "artwork"),
                        ),
                    )
                }
            val result =
                imageGenerationService.enqueue(
                    ImageGenerationRequest(
                        genre = genre,
                        imageReference = null,
                        context = contextString,
                        imageType = ImageType.ICON,
                        variationId = null,
                        label = character.name,
                        showReveal = false,
                    ),
                ) { bitmap -> bitmap }
            _characterPreviewState.update {
                it.copy(
                    isGeneratingImage = false,
                    character = characterWithArtwork,
                    bitmap = result.getOrNull(),
                    error = result.exceptionOrNull()?.message,
                )
            }
        }
    }

    fun resetCharacterPreview() {
        _characterPreviewState.value = CharacterPreviewState()
    }
}
