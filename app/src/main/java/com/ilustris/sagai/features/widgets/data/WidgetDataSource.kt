package com.ilustris.sagai.features.widgets.data

import androidx.compose.ui.graphics.toArgb
import com.ilustris.sagai.core.ai.services.GenreVisualConfigService
import com.ilustris.sagai.features.characters.data.model.fullName
import com.ilustris.sagai.features.characters.data.source.CharacterDao
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaSummary
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.datasource.SagaDao
import com.ilustris.sagai.features.timeline.data.source.TimelineDao
import com.ilustris.sagai.features.widgets.domain.ContinueData
import com.ilustris.sagai.features.widgets.domain.LastMessageData
import com.ilustris.sagai.features.widgets.domain.StoriesData
import com.ilustris.sagai.features.widgets.domain.StoryBook
import com.ilustris.sagai.features.widgets.domain.WidgetCharacter
import com.ilustris.sagai.features.widgets.domain.WidgetInvite
import com.ilustris.sagai.features.widgets.domain.WidgetPaletteFactory
import com.ilustris.sagai.features.widgets.domain.WidgetSaga
import com.ilustris.sagai.features.widgets.domain.WidgetText
import com.ilustris.sagai.features.widgets.domain.WidgetTheme
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads what the automatic widgets show, straight from the database.
 *
 * Nothing here calls an AI: every value already exists because the app produced it while the player
 * was playing (scene objective, last message, finished sagas).
 */
@Singleton
class WidgetDataSource
    @Inject
    constructor(
        private val sagaDao: SagaDao,
        private val timelineDao: TimelineDao,
        private val characterDao: CharacterDao,
        private val visualConfigService: GenreVisualConfigService,
    ) {
        /** The saga a widget follows: the unfinished one the player touched most recently. */
        private suspend fun activeSummary(): SagaSummary? =
            realSummaries()
                .filter { !it.data.isEnded }
                .maxByOrNull { it.lastMessageTime ?: it.data.createdAt }

        private suspend fun realSummaries(): List<SagaSummary> = sagaDao.getSagaSummaries().first().filter { !it.data.isDebug }

        suspend fun loadContinue(): ContinueData {
            val active = activeSummary()
            if (active != null) {
                return ContinueData.Active(toWidgetSaga(active.data), currentObjective(active.data.id))
            }
            return ContinueData.Invite(inviteState())
        }

        /** The objective of the saga's newest scene, or null when it has none. */
        suspend fun currentObjective(sagaId: Int): String? =
            timelineDao
                .getLatestTimelineBySagaOnce(sagaId)
                ?.displayObjective()
                ?.let { WidgetText.plain(it) }
                ?.takeIf { it.isNotBlank() }

        suspend fun loadLastMessage(): LastMessageData {
            val active = activeSummary() ?: return LastMessageData.Invite(inviteState())
            val saga = toWidgetSaga(active.data)
            val text =
                WidgetText
                    .plain(active.lastMessageText)
                    .ifBlank { WidgetText.plain(active.data.description) }
            val speaker = active.lastMessageSpeaker?.takeIf { it.isNotBlank() }
            return LastMessageData.Message(
                saga = saga,
                text = text,
                speaker = speaker,
                portraitPath = speakerPortrait(active, speaker),
            )
        }

        /** The saga played last on top, then the other sagas, running and finished, newest first. */
        suspend fun loadStories(): StoriesData {
            val recent =
                realSummaries().sortedByDescending {
                    if (it.data.isEnded) maxOf(it.data.endedAt, it.lastMessageTime ?: 0L) else it.lastMessageTime ?: it.data.createdAt
                }
            // A running saga wins the main slot; when everything is finished, the newest finished one takes it.
            val hero = activeSummary() ?: recent.firstOrNull()
            val others =
                recent
                    .filter { it.data.id != hero?.data?.id }
                    .map {
                        StoryBook(
                            id = it.data.id,
                            title = it.data.title,
                            genre = it.data.genre,
                            iconPath = it.data.icon.takeIf { path -> path.isNotBlank() },
                            isEnded = it.data.isEnded,
                        )
                    }
            return StoriesData(
                hero = hero?.let { toWidgetSaga(it.data) },
                heroIsEnded = hero?.data?.isEnded ?: false,
                objective = hero?.takeIf { !it.data.isEnded }?.let { currentObjective(it.data.id) },
                others = others,
            )
        }

        /** The character behind a generated widget, or null when it (or its saga) no longer exists. */
        suspend fun loadCharacter(
            sagaId: Int,
            characterId: Int,
        ): WidgetCharacter? {
            val character = characterDao.getCharacterById(characterId) ?: return null
            val saga = sagaDao.getSaga(sagaId).first() ?: return null
            return WidgetCharacter(
                id = character.id,
                sagaId = saga.id,
                name = character.name,
                portraitPath = character.image.takeIf { it.isNotBlank() },
                genre = saga.genre,
                sagaTitle = saga.title,
            )
        }

        /** Colors and corner radius for [genre]. [light] is true when the artwork is ink on paper. */
        suspend fun theme(
            genre: Genre,
            light: Boolean,
        ): WidgetTheme {
            val config =
                try {
                    visualConfigService.getVisualConfig(genre)
                } catch (e: Exception) {
                    Timber.tag(TAG).w(e, "No visual config for $genre, using compiled colors")
                    null
                }
            val primary = parseColor(config?.primaryColor) ?: genre.color.toArgb()
            val colors =
                config
                    ?.colorPalette
                    ?.mapNotNull { parseColor(it) }
                    .orEmpty()
                    .ifEmpty {
                        listOf(
                            WidgetPaletteFactory.blend(primary, DARK, 0.6f),
                            primary,
                        )
                    }
            val corner =
                (BASE_CORNER_DP + (config?.cornerSizeDp ?: DEFAULT_CORNER) * CORNER_FACTOR)
                    .coerceIn(MIN_CORNER_DP, MAX_CORNER_DP)
            return WidgetTheme(
                palette = WidgetPaletteFactory.create(colors, primary, light),
                cornerDp = corner,
            )
        }

        private suspend fun inviteState(): WidgetInvite {
            val lastFinished =
                realSummaries()
                    .map { it.data }
                    .filter { it.isEnded }
                    .maxByOrNull { it.endedAt }
            return WidgetInvite(
                genre = lastFinished?.genre ?: Genre.entries.first(),
                hasFinishedSagas = lastFinished != null,
            )
        }

        private suspend fun toWidgetSaga(saga: Saga): WidgetSaga {
            val portrait =
                saga.mainCharacterId?.let { id ->
                    try {
                        characterDao.getCharacterById(id)?.image
                    } catch (e: Exception) {
                        Timber.tag(TAG).w(e, "Could not read the main character of saga ${saga.id}")
                        null
                    }
                }
            return WidgetSaga(
                id = saga.id,
                title = saga.title,
                genre = saga.genre,
                iconPath = saga.icon.takeIf { it.isNotBlank() },
                portraitPath = portrait?.takeIf { it.isNotBlank() },
            )
        }

        private suspend fun speakerPortrait(
            summary: SagaSummary,
            speaker: String?,
        ): String? {
            val characters =
                try {
                    characterDao.getCharactersBySaga(summary.data.id).first().map { it.data }
                } catch (e: Exception) {
                    Timber.tag(TAG).w(e, "Could not read the characters of saga ${summary.data.id}")
                    emptyList()
                }
            val character =
                when {
                    summary.lastMessageSender == SenderType.USER -> {
                        characters.firstOrNull { it.id == summary.data.mainCharacterId }
                    }

                    speaker != null -> {
                        characters.firstOrNull {
                            it.fullName().equals(speaker, ignoreCase = true) || it.name.equals(speaker, ignoreCase = true)
                        }
                    }

                    else -> {
                        null
                    }
                }
            return character?.image?.takeIf { it.isNotBlank() }
        }

        private fun parseColor(value: String?): Int? =
            if (value.isNullOrBlank()) {
                null
            } else {
                try {
                    android.graphics.Color.parseColor(value)
                } catch (_: IllegalArgumentException) {
                    null
                }
            }

        private companion object {
            const val TAG = "WidgetDataSource"
            const val DARK = 0xFF000000.toInt()
            const val BASE_CORNER_DP = 12f
            const val CORNER_FACTOR = 1.2f
            const val DEFAULT_CORNER = 8
            const val MIN_CORNER_DP = 12f
            const val MAX_CORNER_DP = 30f
        }
    }
