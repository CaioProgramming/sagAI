package com.ilustris.sagai.features.saga.detail.review.ui.templates.comic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.share.domain.model.ShareType
import com.ilustris.sagai.ui.theme.themeFilter

/**
 * Container for [com.ilustris.sagai.features.saga.detail.review.ui.ReviewNavigationStyle.ComicBoard].
 *
 * Unlike the other containers there is no per-page navigation to drive — [ComicBoard] owns the
 * camera, so this mostly supplies chrome and routes panel actions. Generation is ensured up front
 * rather than as the reader nears the end, because the whole board is laid out at once and there is
 * no "approaching the last page" moment to hook.
 */
@Composable
fun ComicBoardReviewContainer(
    pages: List<ReviewPage>,
    genre: Genre,
    onEnsureGeneration: () -> Unit,
    onDismiss: () -> Unit,
    onShare: (ShareType) -> Unit,
    onRegenerate: () -> Unit,
) {
    var finished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { onEnsureGeneration() }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .themeFilter(),
    ) {
        ComicBoard(
            pages = pages,
            modifier = Modifier.fillMaxSize(),
            panelBorderColor = MaterialTheme.colorScheme.onBackground,
            panelBackground = MaterialTheme.colorScheme.surfaceContainer,
            onFinished = { finished = true },
            onPanelAction = { action ->
                when (action) {
                    is ReviewAction.Share -> onShare(action.shareType)
                    ReviewAction.Finish -> onDismiss()
                    ReviewAction.Regenerate -> onRegenerate()
                    // Continue/Restart/Navigate are page-sequence concepts; the board has no
                    // sequence to move through, so they're inert here.
                    else -> Unit
                }
            },
        )

        IconButton(
            onClick = onDismiss,
            modifier =
                Modifier
                    .size(24.dp)
                    .alpha(if (finished) 0.9f else 0.6f)
                    .align(Alignment.TopCenter)
                    .statusBarsPadding(),
        ) {
            Icon(
                painter = painterResource(genre.icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}
