package com.ilustris.sagai.ui.navigation

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.navigation3.runtime.entryProvider
import com.ilustris.sagai.features.chapter.ui.ChapterView
import com.ilustris.sagai.features.characters.ui.CharacterDetailsView
import com.ilustris.sagai.features.faq.ui.FAQView
import com.ilustris.sagai.features.home.ui.HomeView
import com.ilustris.sagai.features.newsaga.ui.NewSagaView
import com.ilustris.sagai.features.playthrough.PlaythroughView
import com.ilustris.sagai.features.saga.chat.ui.ChatView
import com.ilustris.sagai.features.saga.detail.ui.SagaDetailView
import com.ilustris.sagai.features.settings.ui.SettingsView
import com.ilustris.sagai.features.settings.ui.audit.AIAuditLogView

@OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)
fun createSagaEntryProvider(
    navigator: Navigator,
    padding: PaddingValues,
    snackbarHostState: SnackbarHostState,
    sharedTransitionScope: SharedTransitionScope,
) = entryProvider {
    entry<HomeKey> {
        HomeView(
            navToProfile = { navigator.navigate(ProfileKey) },
            navToNewSaga = { navigator.navigate(NewSagaKey) },
            navToSaga = { sagaId, isDebug -> navigator.navigate(ChatKey(sagaId, isDebug)) },
            navToFAQ = { navigator.navigate(FAQKey) },
            navToAuditLogs = { navigator.navigate(AuditLogsKey) },
            padding = padding,
        )
    }

    entry<ProfileKey> {
        SettingsView(
            onOpenPremiumOnboarding = { },
            onBack = { navigator.goBack() },
            navToFAQ = { navigator.navigate(FAQKey) },
            navToAuditLogs = { navigator.navigate(AuditLogsKey) },
            navToPlaythrough = { navigator.navigate(PlaythroughKey) },
        )
    }

    entry<PlaythroughKey> {
        PlaythroughView(onBack = { navigator.goBack() })
    }

    entry<FAQKey> {
        FAQView(onBack = { navigator.goBack() })
    }

    entry<NewSagaKey> {
        NewSagaView(
            onBack = { navigator.goBack() },
            onNavigate = { key -> navigator.navigate(key) },
        )
    }

    entry<AuditLogsKey> {
        AIAuditLogView(onBack = { navigator.goBack() })
    }

    entry<ChatKey> { key ->
        ChatView(
            sagaId = key.sagaId,
            isDebug = key.isDebug,
            padding = padding,
            onBack = { navigator.goBack() },
            onCharacterDetails = { characterId ->
                navigator.navigate(CharacterDetailKey(key.sagaId, characterId))
            },
            onSagaDetails = {
                navigator.navigate(SagaDetailKey(key.sagaId))
            },
            sharedTransitionScope = sharedTransitionScope,
        )
    }

    entry<SagaDetailKey> { key ->
        SagaDetailView(
            sagaId = key.sagaId,
            paddingValues = padding,
            onBack = { navigator.goBack() },
            onChapters = { navigator.navigate(SagaChaptersKey(key.sagaId)) },
            onDeleted = { navigator.navigate(HomeKey) },
        )
    }

    entry<CharacterDetailKey> { key ->
        CharacterDetailsView(
            sagaId = key.sagaId,
            characterId = key.characterId,
            onBack = { navigator.goBack() },
        )
    }

    entry<SagaChaptersKey> { key ->
        ChapterView(
            sagaId = key.sagaId,
            onBack = { navigator.goBack() },
        )
    }
}
