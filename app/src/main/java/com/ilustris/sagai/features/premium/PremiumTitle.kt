package com.ilustris.sagai.features.premium

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.ui.theme.SagaTitle
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer

@Composable
fun PremiumTitle(
    titleStyle: TextStyle = MaterialTheme.typography.titleLarge,
    brush: Brush = Brush.horizontalGradient(holographicGradient),
    modifier: Modifier = Modifier.Companion,
    iconModifier: Modifier = Modifier.Companion,
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
            iconModifier = iconModifier,
        )
        Text(
            stringResource(id = R.string.pro_label),
            modifier = Modifier.Companion,
            style =
                MaterialTheme.typography.labelSmall.copy(
                    color = TextFieldDefaults.colors().unfocusedPlaceholderColor,
                ),
        )
    }
}
