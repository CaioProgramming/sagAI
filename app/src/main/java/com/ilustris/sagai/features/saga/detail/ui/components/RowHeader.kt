package com.ilustris.sagai.features.saga.detail.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R

@Composable
fun RowHeader(
    title: String,
    textStyle: TextStyle = MaterialTheme.typography.headlineMedium,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            style = textStyle,
            modifier =
                Modifier
                    .padding(8.dp)
                    .weight(1f),
        )

        IconButton(onClick = onClick, modifier = Modifier.size(24.dp)) {
            Icon(
                painterResource(R.drawable.round_arrow_forward_ios_24),
                contentDescription = null,
            )
        }
    }
}
