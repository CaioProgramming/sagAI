package com.ilustris.sagai.features.premium

import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseInBounce
import androidx.compose.animation.core.RepeatMode.*
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.ui.components.GenreCard
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.core.services.BillingState
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PremiumView(
    premiumViewModel: PremiumViewModel = viewModel()
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val brush = Brush.verticalGradient(holographicGradient)
        val genres = Genre.entries
        var currentIndex by remember { androidx.compose.runtime.mutableIntStateOf(0) }
        val genreOffsets = remember { mutableStateListOf(*Array(genres.size) { 0.dp }) }
        LaunchedEffect(genres) {
            while (true) {
                delay(3.seconds)
                genres.forEachIndexed { i, _ ->
                    genreOffsets[i] = ((Random.nextInt(-16, 16)).dp)
                }
                currentIndex = Random.nextInt(genres.size)
            }
        }

        Box(Modifier.fillMaxWidth().fillMaxHeight(.3f)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.animateContentSize(),
            ) {
                items(genres.size) { index ->
                    val scale by animateFloatAsState(
                        targetValue = if (index == currentIndex) 1f else .85f,
                        animationSpec = tween(durationMillis = 600, easing = EaseInBounce),
                    )
                    val infiniteTransition = rememberInfiniteTransition()
                    val offsetY by infiniteTransition.animateFloat(
                        initialValue = .85f,
                        targetValue = 1f,
                        animationSpec =
                            infiniteRepeatable( tween(
                                 durationMillis = 4000,
                                 easing = EaseInBounce,
                             ),
                                repeatMode = Reverse,
                            ),
                    )
                    val offsetX by infiniteTransition.animateFloat(
                        initialValue = .9f,
                        targetValue = 1f,
                        animationSpec =
                            infiniteRepeatable(tween(
                                durationMillis = 5000,
                                easing = EaseInBounce),
                                repeatMode = Reverse,
                            ),
                    )
                    val levitateY = (kotlin.math.sin(offsetY * 2 * Math.PI) * 12).dp
                    val levitateX = (kotlin.math.cos(offsetX * 2 * Math.PI) * 8).dp
                    GenreCard(
                        genres[index],
                        false,
                        Modifier
                            .reactiveShimmer(true)
                            .aspectRatio(1f)
                            .scale(scale)
                            .offset(x = levitateX, y = levitateY)
                            .padding(8.dp)
                        ,
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
                        .size(64.dp)
                        .gradientFill(brush)
                        .reactiveShimmer(true),
            )
        }

        Text(
            "Vá além com o Sagas Pro",
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            style =
                MaterialTheme.typography.titleLarge.copy(
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    brush = brush,
                ),
        )

        Text(
            "Mergulhe em universos únicos e transforme suas ideias em histórias inesquecíveis. Com Sagas Pro, sua criatividade ganha vida e cada capítulo se torna uma experiência envolvente e visualmente imersiva.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp),
        )

        Text(
            "Crie narrativas profundas, explore novos mundos e dê vida aos seus personagens com recursos que elevam sua saga a outro nível. Sinta a diferença de contar histórias com liberdade e inspiração ilimitada.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp),
        )

        val billingState by premiumViewModel.billingState.collectAsState()
        val activity = LocalActivity.current
        val productDetails = (billingState as? BillingState.SignatureDisabled)?.products?.firstOrNull()
        val offerToken = productDetails?.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: ""

        Button(
            onClick = {
                if (activity != null && productDetails != null && offerToken.isNotEmpty()) {
                    premiumViewModel.purchaseSignature(activity, productDetails, offerToken)
                }
            },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.background,
            ),
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            enabled = productDetails != null && offerToken.isNotEmpty(),
        ) {
            Text(
                if (billingState is BillingState.SignatureEnabled) "Assinatura ativa" else "Continuar",
                modifier = Modifier.gradientFill(brush).padding(8.dp)
            )
        }

        Text(
            "Restaurar compras",
            style =
                MaterialTheme.typography.labelMedium.copy(
                    textAlign = TextAlign.Center,
                ),
            modifier =
                Modifier.alpha(.4f).padding(16.dp).fillMaxWidth().clickable {
                },
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
fun PremiumViewPreview() {
    SagAIScaffold {
        PremiumView()
    }
}
