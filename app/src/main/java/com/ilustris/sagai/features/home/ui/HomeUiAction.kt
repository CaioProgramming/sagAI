package com.ilustris.sagai.features.home.ui

import com.ilustris.sagai.features.home.data.model.Saga

sealed class HomeUiAction {
    data object CreateNewSaga : HomeUiAction()

    data class SelectSaga(
        val saga: Saga,
    ) : HomeUiAction()

    data object OpenPremium : HomeUiAction()

    data object RecoverSagas : HomeUiAction()

    data object DismissPremiumOnboarding : HomeUiAction()

    data object DismissBackupSheet : HomeUiAction()

    data object CreateFakeSaga : HomeUiAction()
}

sealed class HomeNavigationEvent {
    data object NewSaga : HomeNavigationEvent()

    data class Saga(
        val sagaId: String,
        val isDebug: Boolean,
    ) : HomeNavigationEvent()
}
