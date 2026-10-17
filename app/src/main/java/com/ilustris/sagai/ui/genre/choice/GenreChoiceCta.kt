package com.ilustris.sagai.ui.genre.choice

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.genre.GenreSurfaceStyle
import com.ilustris.sagai.ui.genre.PhysicalButton
import com.ilustris.sagai.ui.genre.surfaceStyle
import com.ilustris.sagai.ui.genre.terminal.TerminalCommandButton
import com.ilustris.sagai.ui.theme.LocalSagaGenre

/**
 * The confirm action under the cards, in the same material as the rest of the Milestone chain:
 * a terminal's next command, a physical cap for Heroes and Horror, a flat Material button otherwise.
 * Disabled means the caller has nothing to confirm yet.
 */
@Composable
fun GenreChoiceCta(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    genre: Genre? = LocalSagaGenre.current,
) {
    val accent = MaterialTheme.colorScheme.primary
    when {
        (genre?.surfaceStyle() ?: GenreSurfaceStyle.DEFAULT) == GenreSurfaceStyle.TERMINAL -> {
            // The terminal button has no disabled state; "busy" is what stops the caret and the tap.
            TerminalCommandButton(
                label = label,
                onClick = onClick,
                accent = accent,
                busy = !enabled,
                modifier = modifier,
            )
        }

        genre == Genre.HEROES || genre == Genre.HORROR -> {
            PhysicalButton(text = label, onClick = onClick, enabled = enabled, accent = accent, modifier = modifier)
        }

        else -> {
            Button(onClick = onClick, enabled = enabled, shape = MaterialTheme.shapes.medium, modifier = modifier) {
                Text(label)
            }
        }
    }
}
