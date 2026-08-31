package com.ilustris.sagai.features.onboarding.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ilustris.sagai.MainActivity
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import kotlinx.coroutines.launch

@Composable
fun OnboardingPagerContent(
    state: OnboardingUiState.Content,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    genre: Genre? = null,
    isPurchaseInProgress: Boolean = false,
) {
    val viewModel: OnboardingViewModel = hiltViewModel()
    val pagerState = rememberPagerState { state.pages.size }
    val scope = rememberCoroutineScope()

    Box(
        modifier =
            modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize(),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = true,
        ) { pageIndex ->
            val uiPage = state.pages.getOrNull(pageIndex) ?: state.pages.lastOrNull()
            uiPage?.background?.invoke()
        }

        Column(
            modifier =
                Modifier
                    .background(fadeGradientBottom())
                    .fillMaxSize(),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        painterResource(R.drawable.round_close_24),
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    var currentIcon by remember {
                        mutableIntStateOf(genre?.icon ?: Genre.entries.random().icon)
                    }

                    LaunchedEffect(pagerState.currentPage) {
                        currentIcon = genre?.icon ?: Genre.entries.random().icon
                    }

                    repeat(state.pages.size) { iteration ->
                        val isSelected = pagerState.currentPage == iteration
                        val size by animateDpAsState(
                            targetValue = if (isSelected) 24.dp else 6.dp,
                            animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
                            label = "dot_size",
                        )
                        val color by animateColorAsState(
                            targetValue =
                                if (isSelected) {
                                    MaterialTheme.colorScheme.onBackground
                                } else {
                                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                                },
                            label = "dot_color",
                        )

                        AnimatedContent(isSelected, transitionSpec = {
                            fadeIn(tween(100, easing = EaseIn)) + scaleIn(tween(600)) togetherWith
                                fadeOut() + scaleOut(tween(600))
                        }) {
                            if (it) {
                                Icon(
                                    painter = painterResource(currentIcon),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    modifier =
                                        Modifier
                                            .clip(CircleShape)
                                            .size(size),
                                )
                            } else {
                                Box(
                                    modifier =
                                        Modifier
                                            .padding(4.dp)
                                            .size(size, 6.dp)
                                            .clip(CircleShape)
                                            .background(color),
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            val currentPage =
                state.pages.getOrNull(pagerState.currentPage) ?: state.pages.lastOrNull()

            currentPage?.let { uiPage ->
                val context = LocalContext.current

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        AnimatedContent(uiPage, transitionSpec = {
                            slideInVertically { -it } +
                                fadeIn(tween(700, easing = EaseIn)) togetherWith
                                fadeOut()
                        }) {
                            it.content()
                        }
                    }

                    uiPage.primaryButton?.let { button ->
                        val isSubscribeLoading =
                            button.action is OnboardingAction.Subscribe && isPurchaseInProgress
                        Button(
                            onClick = {
                                if (button.action is OnboardingAction.Next) {
                                    if (pagerState.currentPage < state.pages.size - 1) {
                                        scope.launch {
                                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                        }
                                    } else {
                                        onDismiss()
                                    }
                                } else if (button.action is OnboardingAction.Dismiss) {
                                    onDismiss()
                                } else {
                                    viewModel.handleAction(
                                        button.action,
                                        context as? MainActivity,
                                    )
                                }
                            },
                            enabled = !isSubscribeLoading,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                            shape = MaterialTheme.shapes.large,
                        ) {
                            if (isSubscribeLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                AnimatedContent(button) {
                                    Text(
                                        text = it.text.uppercase(),
                                        style =
                                            MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                            ),
                                    )
                                }
                            }
                        }
                    }

                    uiPage.secondaryButton?.let { button ->
                        TextButton(
                            onClick = {
                                when (button.action) {
                                    is OnboardingAction.Skip -> {
                                        scope.launch {
                                            pagerState.animateScrollToPage(state.pages.size - 1)
                                        }
                                    }

                                    is OnboardingAction.Dismiss -> {
                                        onDismiss()
                                    }

                                    is OnboardingAction.OpenUrl -> {
                                        context.startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(button.action.url),
                                            ),
                                        )
                                    }

                                    is OnboardingAction.DeactivateTutorials -> {
                                        viewModel.handleAction(
                                            button.action,
                                            context as? MainActivity,
                                        )
                                        onDismiss()
                                    }

                                    else -> {
                                        viewModel.handleAction(
                                            button.action,
                                            context as? MainActivity,
                                        )
                                    }
                                }
                            },
                            enabled = !isPurchaseInProgress,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            AnimatedContent(button) {
                                Text(
                                    text = it.text.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
