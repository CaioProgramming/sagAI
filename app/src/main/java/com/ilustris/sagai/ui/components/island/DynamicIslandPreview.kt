package com.ilustris.sagai.ui.components.island

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.SagAITheme

private val previewGenre = Genre.entries.first()

private class PreviewIslandContent(
    override val compact: CompactIslandData,
    override val onAction: () -> Unit = {},
) : IslandContent {
    @Composable
    override fun Expanded(scope: IslandScope) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Detalhes expandidos",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Aqui vai o conteúdo livre do Expanded — progresso, ações, texto rico.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(name = "Compact — loading", showBackground = true)
@Composable
private fun CompactIslandLoadingPreview() {
    SagAITheme(previewGenre) {
        CompactIslandLayout(
            data =
                CompactIslandData(
                    label = "Gerando imagem…",
                    iconRes = previewGenre.icon,
                    isLoading = true,
                    genre = previewGenre,
                ),
            expanded = false,
        )
    }
}

@Preview(name = "Compact — progress", showBackground = true)
@Composable
private fun CompactIslandProgressPreview() {
    SagAITheme(previewGenre) {
        CompactIslandLayout(
            data =
                CompactIslandData(
                    label = "Escrevendo capítulo…",
                    iconRes = previewGenre.icon,
                    progress = 0.6f,
                    genre = previewGenre,
                ),
            expanded = false,
        )
    }
}

@Preview(
    name = "Island — expanded",
    showBackground = true,
    device = "spec:width=1080px,height=2340px,dpi=440",
)
@Composable
private fun DynamicIslandExpandedPreview() {
    var expanded by remember { mutableStateOf(true) }
    SagAITheme(previewGenre) {
        DynamicIslandOverlay(
            content =
                PreviewIslandContent(
                    compact =
                        CompactIslandData(
                            label = "Gerando resposta…",
                            iconRes = previewGenre.icon,
                            isLoading = true,
                            genre = previewGenre,
                        ),
                ),
            expanded = expanded,
            onExpandedChange = { expanded = it },
        )
    }
}
