package com.ilustris.sagai.ui.genre.surface

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.ui.glassSurface
import com.ilustris.sagai.ui.theme.sagaShape

private const val DIGEST_PREVIEW_LINES = 4

/**
 * The same glass card on every genre's beat: a few lines of what was generated and the way to read
 * the rest. Deliberately not themed per genre — the glass takes the genre's own primary color, and
 * the surface around it supplies the material.
 */
@Composable
fun StoryDigestCard(
    digest: StoryDigest,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    Column(
        modifier
            .fillMaxWidth()
            .glassSurface(sagaShape(), primary)
            .clickable(onClick = digest.onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = digest.text,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                ),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
            maxLines = DIGEST_PREVIEW_LINES,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            Modifier.align(Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = digest.expandLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
            )
            Icon(
                painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                modifier = Modifier.size(12.dp),
            )
        }
    }
}
