package com.ilustris.sagai.core.services

import androidx.compose.runtime.staticCompositionLocalOf
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.ai.services.GenreVisualConfigService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterProfile
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.onboarding.data.OnboardingAsset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CompositionLocal to provide [MultiverseAssetsProvider] to the UI.
 */
val LocalMultiverseAssets =
    staticCompositionLocalOf<MultiverseAssetsProvider> {
        error("MultiverseAssetsProvider not provided")
    }

/**
 * A centralized provider for shared multiversal assets.
 * Manages fetching and caching of personas, story covers, and genre configurations.
 */
@Singleton
class MultiverseAssetsProvider
    @Inject
    constructor(
        private val remoteConfigService: RemoteConfigService,
        private val genreVisualConfigService: GenreVisualConfigService,
    ) {
        private val _personas = MutableStateFlow<List<Character>>(emptyList())
        val personas = _personas.asStateFlow()

        private val _storyAssets = MutableStateFlow<List<OnboardingAsset>>(emptyList())
        val storyAssets = _storyAssets.asStateFlow()

        private val _genreConfigs = MutableStateFlow<Map<Genre, GenreVisualConfig>>(emptyMap())
        val genreConfigs = _genreConfigs.asStateFlow()

        private var isLoadingPersonas = false
        private var isLoadingStories = false
        private var isLoadingGenres = false

        suspend fun fetchPersonas(): RequestResult<List<Character>> =
            executeRequest {
                if (_personas.value.isNotEmpty()) return@executeRequest _personas.value
                if (isLoadingPersonas) return@executeRequest emptyList()

                isLoadingPersonas = true
                try {
                    val assets =
                        remoteConfigService.getJson<List<OnboardingAsset>>("avatar_faces")
                            ?: emptyList()
                    val mappedPersonas =
                        assets.mapIndexed { index, asset ->
                            Character(
                                id = index,
                                name = "Multiverse Persona $index",
                                image = asset.image,
                                details = Details(),
                                profile = CharacterProfile(),
                            )
                        }
                    _personas.emit(mappedPersonas)
                    mappedPersonas
                } finally {
                    isLoadingPersonas = false
                }
            }

        suspend fun fetchStoryAssets(): RequestResult<List<OnboardingAsset>> =
            executeRequest {
                if (_storyAssets.value.isNotEmpty()) return@executeRequest _storyAssets.value
                if (isLoadingStories) return@executeRequest emptyList()

                isLoadingStories = true
                try {
                    val assets =
                        remoteConfigService.getJson<List<OnboardingAsset>>("story_faces") ?: emptyList()
                    _storyAssets.emit(assets)
                    assets
                } finally {
                    isLoadingStories = false
                }
            }

        suspend fun fetchAllGenreConfigs() {
            if (_genreConfigs.value.isNotEmpty() || isLoadingGenres) return

            isLoadingGenres = true
            try {
                val configs = mutableMapOf<Genre, GenreVisualConfig>()
                Genre.entries.forEach { genre ->
                    genreVisualConfigService.getVisualConfig(genre)?.let {
                        configs[genre] = it
                    }
                }
                _genreConfigs.emit(configs)
            } finally {
                isLoadingGenres = false
            }
        }
    }
