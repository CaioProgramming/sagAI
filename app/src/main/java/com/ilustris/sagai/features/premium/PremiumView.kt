@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.premium

import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ilustris.sagai.R
import com.ilustris.sagai.core.services.BillingState
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.ui.components.GenreCard
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.SagaTitle
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.solidGradient
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

@Composable
fun PremiumView(
    isVisible: Boolean = false,
    onDismiss: () -> Unit = {},
    premiumViewModel: PremiumViewModel = hiltViewModel(),
) {
    val brush = Brush.verticalGradient(holographicGradient)
    val genres = Genre.entries
    var currentIndex by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    val genreOffsets = remember { mutableStateListOf(*Array(genres.size) { 0.dp }) }
    val billingState by premiumViewModel.billingState.collectAsState()
    val activity = LocalActivity.current
    val productDetails = (billingState as? BillingState.SignatureDisabled)?.products?.firstOrNull()
    val offerToken = productDetails?.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: emptyString()
    val isPremium = billingState is BillingState.SignatureEnabled

    if (isVisible) {
        LaunchedEffect(genres) {
            while (true) {
                delay(1.seconds)
                genres.forEachIndexed { i, _ ->
                    genreOffsets[i] = ((Random.nextInt(-16, 16)).dp)
                }
                currentIndex = Random.nextInt(genres.size)
            }
        }
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize(),
            ) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .fillParentMaxHeight(.4f)
                            .padding(8.dp),
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            userScrollEnabled = false,
                        ) {
                            items(genres.size) { index ->
                                val scale by animateFloatAsState(
                                    targetValue = if (index == currentIndex) 1f else .8f,
                                    animationSpec = tween(durationMillis = 500, easing = EaseIn),
                                )

                                GenreCard(
                                    genres[index],
                                    index == currentIndex,
                                    Modifier
                                        .fillMaxHeight()
                                        .aspectRatio(1f)
                                        .scale(scale),
                                    false,
                                ) { }
                            }
                        }

                        Image(
                            painterResource(R.drawable.ic_spark),
                            null,
                            modifier =
                                Modifier
                                    .align(Alignment.Center)
                                    .reactiveShimmer(true)
                                    .size(64.dp)
                                    .gradientFill(brush),
                        )
                    }
                }

                item {
                    val text =
                        if (isPremium.not()) {
                            stringResource(R.string.not_premium_title_label)
                        } else {
                            stringResource(R.string.premium_label_title)
                        }
                    Text(
                        text,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.alpha(.4f),
                    )
                }

                item { PremiumTitle(titleStyle = MaterialTheme.typography.headlineMedium) }

                item {
                    Text(
                        stringResource(R.string.premium_first_title),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                    )
                }

                item {
                    Text(
                        stringResource(R.string.premium_description),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                    )
                }

                item {
                    AnimatedVisibility(isPremium.not()) {
                        Button(
                            onClick = {
                                if (activity != null && productDetails != null && offerToken.isNotEmpty()) {
                                    premiumViewModel.purchaseSignature(
                                        activity,
                                        productDetails,
                                        offerToken,
                                    )
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.background,
                                    contentColor = MaterialTheme.colorScheme.onBackground,
                                ),
                            modifier =
                                Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                            enabled = productDetails != null && offerToken.isNotEmpty(),
                        ) {
                            Text(
                                stringResource(R.string.subscribe),
                                style =
                                    MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        brush = Brush.horizontalGradient(holographicGradient),
                                    ),
                            )
                        }
                    }
                }
                item {
                    val text =
                        if (isPremium.not()) {
                            stringResource(R.string.restore_purchases)
                        } else {
                            stringResource(
                                R.string.premium_cancel_sign_up,
                            )
                        }
                    TextButton(
                        {
                            if (isPremium.not()) {
                                premiumViewModel.restorePurchases()
                            } else {
                                premiumViewModel.cancelSubscription()
                            }
                        },
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.primary,
                            ),
                        modifier =
                            Modifier
                                .alpha(.8f)
                                .padding(16.dp)
                                .fillMaxWidth(),
                    ) {
                        Text(
                            text,
                            style =
                                MaterialTheme.typography.labelMedium.copy(
                                    textAlign = TextAlign.Center,
                                ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumTitle(
    titleStyle: TextStyle = MaterialTheme.typography.titleLarge,
    brush: Brush = Brush.horizontalGradient(holographicGradient),
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier =
            modifier
                .reactiveShimmer(true)
                .gradientFill(brush),
    ) {
        SagaTitle(
            textStyle = titleStyle,
        )
        Text(
            stringResource(id = R.string.pro_label),
            modifier = Modifier.alpha(.6f),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
fun PremiumCard(
    isUserPro: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .clickable { onClick() }
                .background(
                    MaterialTheme.colorScheme.surfaceContainer,
                    RoundedCornerShape(10.dp),
                ).padding(16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Image(
                painterResource(R.drawable.ic_spark),
                null,
                colorFilter = ColorFilter.tint(Color.White),
                modifier =
                    Modifier
                        .border(1.dp, Color.White.gradientFade(), RoundedCornerShape(5.dp))
                        .background(
                            Brush.verticalGradient(holographicGradient, endY = 150f),
                            RoundedCornerShape(5.dp),
                        ).clip(RoundedCornerShape(10.dp))
                        .size(24.dp)
                        .padding(4.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PremiumTitle(
                    titleStyle = MaterialTheme.typography.labelLarge,
                    brush = MaterialTheme.colorScheme.onBackground.solidGradient(),
                )

                val premiumText = if (isUserPro) R.string.premium_already else R.string.premium_first_title

                Text(
                    stringResource(premiumText),
                    modifier = Modifier.alpha(.4f),
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Light,
                        ),
                )
            }
        }

        TextButton(onClick = {
            onClick()
        }, modifier = Modifier.fillMaxWidth()) {
            val text =
                if (isUserPro) {
                    stringResource(R.string.premium_cancel_sign_up)
                } else {
                    stringResource(R.string.premium_sign_up)
                }
            Text(
                text,
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Light,
                    ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
fun PremiumViewPreview() {
    SagAIScaffold {
        PremiumView()
    }
}

@Preview(name = "Premium Card Light", showBackground = true)
@Preview(name = "Premium Card Night", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PremiumCardPreview() {
    SagAIScaffold {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            PremiumCard(isUserPro = false)
            PremiumCard(isUserPro = true)
        }
    }
}
