package com.ilustris.sagai.ui.theme.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.ui.components.AutoResizeText
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.headerFont

@Composable
fun LargeHorizontalHeader(
    title: String,
    subtitle: String,
    titleStyle: TextStyle,
    subtitleStyle: TextStyle,
    modifier: Modifier,
    titleModifier: Modifier = Modifier,
) {
    Row(
        modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AutoResizeText(
            title,
            style = titleStyle,
            maxLines = 1,
            modifier = titleModifier.weight(1f, false),
        )

        Text(
            subtitle,
            style = subtitleStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier =
                Modifier.alpha(.5f),
        )
    }
}
