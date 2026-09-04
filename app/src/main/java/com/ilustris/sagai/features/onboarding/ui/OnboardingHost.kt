package com.ilustris.sagai.features.onboarding.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.onboarding.data.OnboardingType
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.levitate
import com.ilustris.sagai.ui.theme.reactiveShimmer

enum class OnboardingPresentation {
    Sheet,
    Fullscreen,
    Embedded,
}

@Composable
fun OnboardingHost(
    type: OnboardingType,
    presentation: OnboardingPresentation,
    modifier: Modifier = Modifier,
    genre: Genre? = null,
    saga: Saga? = null,
    force: Boolean = false,
    /**
     * False while the app is gated on this onboarding — there is nowhere to dismiss to. The
     * sheet then refuses the drag too, rather than sliding halfway down and springing back,
     * which reads as the app struggling with the user.
     */
    dismissible: Boolean = true,
    onDismiss: () -> Unit = {},
) {
    val viewModel: OnboardingViewModel = hiltViewModel()
    val uiState by viewModel.onboardingState.collectAsStateWithLifecycle()
    val purchaseFlowResult by viewModel.purchaseFlowResult.collectAsStateWithLifecycle()
    val isPurchaseInProgress by viewModel.isPurchaseInProgress.collectAsStateWithLifecycle()

    LaunchedEffect(type, force, genre, saga) {
        viewModel.checkOnboarding(type, genre, saga, force)
    }

    val dismissOnboarding = {
        viewModel.markAsSeen(type)
        viewModel.dismissPurchaseResult()
        onDismiss()
        viewModel.clearState()
    }

    val hostModifier =
        when (presentation) {
            OnboardingPresentation.Sheet -> modifier
            OnboardingPresentation.Fullscreen ->
                modifier
                    .fillMaxSize()
                    .zIndex(20f)
            OnboardingPresentation.Embedded ->
                modifier.fillMaxSize()
        }

    Box(hostModifier) {
        when {
            uiState is OnboardingUiState.Content && uiState.type == type -> {
                when (presentation) {
                    OnboardingPresentation.Sheet -> {
                        OnboardingSheetPresentation(
                            state = uiState as OnboardingUiState.Content,
                            genre = genre,
                            isPurchaseInProgress = isPurchaseInProgress,
                            dismissible = dismissible,
                            onDismiss = dismissOnboarding,
                        )
                    }

                    OnboardingPresentation.Fullscreen -> {
                        OnboardingPagerContent(
                            state = uiState as OnboardingUiState.Content,
                            genre = genre,
                            isPurchaseInProgress = isPurchaseInProgress,
                            onDismiss = dismissOnboarding,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    OnboardingPresentation.Embedded -> {
                        OnboardingPagerContent(
                            state = uiState as OnboardingUiState.Content,
                            genre = genre,
                            isPurchaseInProgress = isPurchaseInProgress,
                            onDismiss = dismissOnboarding,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }

            uiState is OnboardingUiState.Error && uiState.type == type -> {
                SideEffect {
                    onDismiss()
                }
            }
        }

        AnimatedVisibility(
            visible = uiState is OnboardingUiState.Loading,
            enter = fadeIn(tween(durationMillis = 800, delayMillis = 500)),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .background(
                            fadeGradientBottom(
                                MaterialTheme.colorScheme.primary,
                            ),
                        ).fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painterResource(genre?.icon ?: R.drawable.ic_spark),
                        null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier =
                            Modifier
                                .align(Alignment.BottomCenter)
                                .size(50.dp)
                                .levitate()
                                .reactiveShimmer(
                                    true,
                                    targetValue = 100f,
                                ),
                    )
                }
            }
        }
    }

    if (type == OnboardingType.PREMIUM_GUIDE) {
        OnboardingBillingOverlays(
            purchaseFlowResult = purchaseFlowResult,
            isPurchaseInProgress = isPurchaseInProgress,
            onDismiss = dismissOnboarding,
            onConfirmDebugPurchase = viewModel::confirmDebugPurchase,
            onCancelDebugPurchase = viewModel::cancelDebugPurchase,
            onSyncSubscription = viewModel::syncSubscription,
            onDismissPurchaseResult = viewModel::dismissPurchaseResult,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OnboardingSheetPresentation(
    state: OnboardingUiState.Content,
    genre: Genre?,
    isPurchaseInProgress: Boolean,
    onDismiss: () -> Unit,
    dismissible: Boolean = true,
) {
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { dismissible || it != SheetValue.Hidden },
        )
    val shape =
        RoundedCornerShape(
            topStart = CornerSize(15.dp),
            topEnd = CornerSize(15.dp),
            bottomStart = CornerSize(0.dp),
            bottomEnd = CornerSize(0.dp),
        )
    ModalBottomSheet(
        onDismissRequest = { if (dismissible) onDismiss() },
        sheetState = sheetState,
        shape = shape,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        OnboardingPagerContent(
            state = state,
            genre = genre,
            isPurchaseInProgress = isPurchaseInProgress,
            onDismiss = onDismiss,
            modifier =
                Modifier
                    .padding(top = 16.dp)
                    .fillMaxSize(),
        )
    }
}

@Composable
private fun OnboardingBillingOverlays(
    purchaseFlowResult: BillingService.PurchaseFlowResult,
    isPurchaseInProgress: Boolean,
    onDismiss: () -> Unit,
    onConfirmDebugPurchase: () -> Unit,
    onCancelDebugPurchase: () -> Unit,
    onSyncSubscription: () -> Unit,
    onDismissPurchaseResult: () -> Unit,
) {
    when (val result = purchaseFlowResult) {
        is BillingService.PurchaseFlowResult.DebugFallback -> {
            DebugBillingSimulationSheet(
                reason = result.reason,
                isLoading = isPurchaseInProgress,
                onConfirm = onConfirmDebugPurchase,
                onCancel = onCancelDebugPurchase,
                onSyncSubscription = onSyncSubscription,
            )
        }

        is BillingService.PurchaseFlowResult.Success -> {
            BillingResultSheet(
                title = stringResource(R.string.billing_result_success_title),
                message = stringResource(R.string.billing_result_success_message),
                onDismiss = onDismiss,
            )
        }

        BillingService.PurchaseFlowResult.DebugSimulationSuccess -> {
            BillingResultSheet(
                title = stringResource(R.string.billing_debug_simulation_success_title),
                message = stringResource(R.string.billing_debug_simulation_success_message),
                onDismiss = onDismissPurchaseResult,
            )
        }

        is BillingService.PurchaseFlowResult.Cancelled -> {
            BillingResultSheet(
                title = stringResource(R.string.billing_result_cancelled_title),
                message = stringResource(R.string.billing_result_cancelled_message),
                onDismiss = onDismissPurchaseResult,
            )
        }

        is BillingService.PurchaseFlowResult.Error -> {
            BillingResultSheet(
                title = stringResource(R.string.billing_error_generic),
                message = result.message,
                isLoading = isPurchaseInProgress,
                onSyncSubscription = onSyncSubscription,
                onDismiss = onDismissPurchaseResult,
            )
        }

        BillingService.PurchaseFlowResult.Pending -> {
            // Paid for with a method that clears later, a boleto or a bank transfer. Access is not
            // granted yet and there is nothing for the buyer to do, so the one thing that matters
            // is saying so: silence here reads as a payment that failed.
            BillingResultSheet(
                title = stringResource(R.string.billing_result_pending_title),
                message = stringResource(R.string.billing_result_pending_message),
                onDismiss = onDismissPurchaseResult,
            )
        }

        BillingService.PurchaseFlowResult.Idle -> {
            Unit
        }
    }
}
