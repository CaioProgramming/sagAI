package com.ilustris.sagai.features.settings.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.core.icon.LauncherIcon
import com.ilustris.sagai.features.settings.ui.AppIconViewModel

/** Picker for the launcher icon: the default dragon and one per genre, plus the option to follow the last saga. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppIconSheet(
    onDismiss: () -> Unit,
    viewModel: AppIconViewModel = hiltViewModel(),
) {
    val selected by viewModel.selected.collectAsStateWithLifecycle()
    val followsLastSaga by viewModel.followsLastSaga.collectAsStateWithLifecycle()
    // The launcher follows the system theme, not the app's, so that is the variant to preview.
    val dark = isSystemInDarkTheme()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier =
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                stringResource(R.string.app_icon_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
            )
            Text(
                stringResource(R.string.app_icon_description),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.alpha(.7f),
            )

            PreferencesContainer(
                stringResource(R.string.app_icon_follow_title),
                stringResource(R.string.app_icon_follow_description),
                isActivated = followsLastSaga,
                onClickSwitch = { viewModel.setFollowsLastSaga(!it) },
            )

            LauncherIcon.entries.chunked(COLUMNS).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    row.forEach { icon ->
                        IconTile(
                            icon = icon,
                            isSelected = icon == selected && !followsLastSaga,
                            dark = dark,
                            onClick = { viewModel.select(icon) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(COLUMNS - row.size) { Box(Modifier.weight(1f)) }
                }
            }

            Text(
                stringResource(R.string.app_icon_delay_note),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .alpha(.5f),
            )
        }
    }
}

@Composable
private fun IconTile(
    icon: LauncherIcon,
    isSelected: Boolean,
    dark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(percent = 28)
    Column(
        modifier =
            modifier
                .clip(MaterialTheme.shapes.medium)
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // The launcher shows the middle 72dp of the 108dp layers, so zoom in the same way.
        Box(
            modifier =
                Modifier
                    .size(ICON_SIZE)
                    .clip(shape)
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color =
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = .15f)
                            },
                        shape = shape,
                    ),
        ) {
            Image(
                painterResource(if (dark) icon.backgroundDark else icon.backgroundLight),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().scale(LAUNCHER_ZOOM),
            )
            Image(
                painterResource(if (dark) icon.foregroundDark else icon.foregroundLight),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().scale(LAUNCHER_ZOOM),
            )
        }
        Text(
            text = icon.genre?.let { stringResource(it.title) } ?: stringResource(R.string.app_icon_default),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
        )
    }
}

private const val COLUMNS = 3
private val ICON_SIZE = 84.dp
private const val LAUNCHER_ZOOM = 108f / 72f
