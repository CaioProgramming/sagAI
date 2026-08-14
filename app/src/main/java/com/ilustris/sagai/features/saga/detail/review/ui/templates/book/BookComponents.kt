package com.ilustris.sagai.features.saga.detail.review.ui.templates.book

import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import com.ilustris.sagai.R
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.share.domain.model.ShareType

/** A tappable "Share" line in the book's own idiom — an italic serif link, not a filled button. */
@Composable
fun BookShareLink(
    shareType: ShareType,
    accent: Color,
    onAction: (ReviewAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(R.string.share),
        fontStyle = FontStyle.Italic,
        color = accent,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyLarge,
        modifier = modifier.clickable { onAction(ReviewAction.Share(shareType)) },
    )
}
