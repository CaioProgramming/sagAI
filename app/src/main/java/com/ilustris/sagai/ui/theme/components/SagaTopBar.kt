package com.ilustris.sagai.ui.theme.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.headerFont

@Composable
fun SagaTopBar(
    title: String,
    subtitle: String,
    genre: Genre,
    actionContent: (@Composable () -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier,
) {
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        onBackClick?.let {
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                tint = MaterialTheme.colorScheme.onBackground,
                contentDescription = stringResource(R.string.back_button_description),
                modifier =
                    Modifier.size(24.dp).clickable {
                        onBackClick()
                    },
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 8.dp).weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                title,
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontFamily = genre.headerFont(),
                        color = genre.color,
                        textAlign = TextAlign.Center,
                    ),
            )

            Text(
                subtitle,
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontFamily = genre.bodyFont(),
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f),
                        textAlign = TextAlign.Center,
                    ),
            )
        }

        actionContent?.invoke()
    }
}
