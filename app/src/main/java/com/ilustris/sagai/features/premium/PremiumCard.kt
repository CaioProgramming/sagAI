package com.ilustris.sagai.features.premium

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.solidGradient

@Composable
fun PremiumCard(
    isUserPro: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .dropShadow(
                    RoundedCornerShape(15.dp),
                    Shadow(
                        10.dp,
                        Brush.verticalGradient(holographicGradient),
                    ),
                ).border(
                    1.dp,
                    Brush.verticalGradient(holographicGradient),
                    RoundedCornerShape(15.dp),
                ).background(
                    MaterialTheme.colorScheme.surfaceContainer,
                    RoundedCornerShape(15.dp),
                ).clickable { onClick() }
                .padding(16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val iconShape = RoundedCornerShape(7.dp)
            Image(
                painterResource(R.drawable.ic_spark),
                null,
                colorFilter = ColorFilter.tint(Color.White),
                modifier =
                    Modifier
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.onBackground.copy(alpha = .2f),
                            iconShape,
                        ).background(
                            MaterialTheme.colorScheme.background,
                            iconShape,
                        ).size(24.dp)
                        .padding(4.dp)
                        .gradientFill(Brush.verticalGradient(holographicGradient)),
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PremiumTitle(
                    titleStyle = MaterialTheme.typography.labelLarge,
                    brush = MaterialTheme.colorScheme.onBackground.solidGradient(),
                )

                val premiumText =
                    if (isUserPro) R.string.premium_already else R.string.premium_first_title

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
