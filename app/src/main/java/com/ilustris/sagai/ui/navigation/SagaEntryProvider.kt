package com.ilustris.sagai.ui.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.navigation3.runtime.entryProvider
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.features.act.ui.BookReaderView
import com.ilustris.sagai.features.act.ui.SagaActsView
import com.ilustris.sagai.features.chapter.ui.ChapterView
import com.ilustris.sagai.features.characters.ui.CharacterDetailsView
import com.ilustris.sagai.features.characters.ui.SagaCharactersView
import com.ilustris.sagai.features.debug.ui.LoreDebugView
import com.ilustris.sagai.features.faq.ui.FAQView
import com.ilustris.sagai.features.home.ui.HomeView
import com.ilustris.sagai.features.newsaga.ui.NewSagaView
import com.ilustris.sagai.features.playthrough.PlaythroughView
import com.ilustris.sagai.features.saga.chat.ui.ChatView
import com.ilustris.sagai.features.saga.detail.ui.SagaDetailView
import com.ilustris.sagai.features.saga.detail.ui.SagaWikiView
import com.ilustris.sagai.features.settings.ui.SettingsView
import com.ilustris.sagai.features.settings.ui.audit.AIAuditLogView
import com.ilustris.sagai.features.timeline.ui.SagaEventsView

@OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)
fun createSagaEntryProvider(
    navigator: Navigator,
    padding: PaddingValues,
    snackbarHostState: SnackbarHostState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
) = entryProvider {
    entry<HomeKey> {
        HomeView(
            navToProfile = { navigator.navigate(ProfileKey) },
            navToNewSaga = { navigator.navigate(NewSagaKey) },
            navToSaga = { sagaId, isDebug -> navigator.navigate(ChatKey(sagaId, isDebug)) },
            navToFAQ = { navigator.navigate(FAQKey) },
            navToAuditLogs = { navigator.navigate(AuditLogsKey) },
            padding = padding,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }

    entry<ProfileKey> {
        SettingsView(
            onOpenPremiumOnboarding = { },
            onBack = { navigator.goBack() },
            navToFAQ = { navigator.navigate(FAQKey) },
            navToAuditLogs = { navigator.navigate(AuditLogsKey) },
            navToPlaythrough = { navigator.navigate(PlaythroughKey) },
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }

    entry<PlaythroughKey> {
        PlaythroughView(
            onBack = { navigator.goBack() },
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }

    entry<FAQKey> {
        FAQView(
            onBack = { navigator.goBack() },
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }

    entry<NewSagaKey> {
        NewSagaView(
            onBack = { navigator.goBack() },
            onNavigate = { key -> navigator.navigate(key) },
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }

    entry<AuditLogsKey> {
        AIAuditLogView(
            onBack = { navigator.goBack() },
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }

    entry<ChatKey> { key ->
        ChatView(
            sagaId = key.sagaId,
            isDebug = key.isDebug,
            padding = padding,
            onBack = { navigator.goBack() },
            onCharacterDetails = { characterId ->
                navigator.navigate(CharacterDetailKey(characterId))
            },
            onSagaDetails = {
                navigator.navigate(SagaDetailKey(key.sagaId))
            },
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }

    entry<SagaDetailKey> { key ->
        SagaDetailView(
            sagaId = key.sagaId,
            paddingValues = padding,
            onBack = { navigator.goBack() },
            onChapters = { navigator.navigate(SagaChaptersKey(key.sagaId)) },
            onCharacters = { navigator.navigate(SagaCharactersKey(key.sagaId)) },
            onWiki = { navigator.navigate(SagaWikiKey(key.sagaId)) },
            onEvents = { navigator.navigate(SagaEventsKey(key.sagaId)) },
            onActs = { navigator.navigate(SagaActsKey(key.sagaId)) },
            onDeleted = { navigator.navigate(HomeKey) },
            onCharacterDetails = { characterId ->
                navigator.navigate(CharacterDetailKey(characterId))
            },
            onLoreDebug = { navigator.navigate(LoreDebugKey(key.sagaId)) },
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }

    entry<SagaCharactersKey> { key ->
        SagaCharactersView(
            sagaId = key.sagaId,
            onBack = { navigator.goBack() },
            onCharacterDetails = { characterId ->
                navigator.navigate(CharacterDetailKey(characterId))
            },
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }

    entry<SagaWikiKey> { key ->
        SagaWikiView(
            sagaId = key.sagaId,
            onBack = { navigator.goBack() },
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }

    entry<SagaEventsKey> { key ->
        SagaEventsView(
            sagaId = key.sagaId,
            onBack = { navigator.goBack() },
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }

    entry<SagaActsKey> { key ->
        SagaActsView(
            sagaId = key.sagaId,
            onBack = { navigator.goBack() },
            onOpenBookReader = { bookReaderKey -> navigator.navigate(bookReaderKey) },
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }

    entry<CharacterDetailKey> { key ->
        CharacterDetailsView(
            characterId = key.characterId,
            onBack = { navigator.goBack() },
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }

    entry<SagaChaptersKey> { key ->
        ChapterView(
            sagaId = key.sagaId,
            onBack = { navigator.goBack() },
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }

    if (BuildConfig.DEBUG) {
        entry<LoreDebugKey> { key ->
            LoreDebugView(
                sagaId = key.sagaId,
                onBack = { navigator.goBack() },
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
            )
        }
    }

    entry<BookReaderKey> { key ->
        BookReaderView(
            sagaId = key.sagaId,
            initialActId = key.initialActId,
            onBack = { navigator.goBack() },
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }

    entry<SagaStoryReaderKey> { key ->
        com.ilustris.sagai.features.act.ui.SagaStoryReaderView(
            sagaId = key.sagaId,
            onBack = { navigator.goBack() },
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }
}
