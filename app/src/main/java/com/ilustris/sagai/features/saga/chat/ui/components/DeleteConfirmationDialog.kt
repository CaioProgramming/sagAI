package com.ilustris.sagai.features.saga.chat.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre

@Composable
fun DeleteConfirmationDialog(
    genre: Genre,
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        AnimatedVisibility(
            visible = true,
            enter = scaleIn(),
        ) {
            Box(
                modifier =
                    modifier
                        .background(
                            MaterialTheme.colorScheme.surface,
                            shape = genre.bubble(isNarrator = true),
                        )
                        .padding(16.dp),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.wrapContentHeight(),
                ) {
                    Text(
                        text = stringResource(R.string.delete_message_title),
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                            ),
                    )

                    Text(
                        text = stringResource(R.string.delete_message_description),
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                            ),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    ) {
                        Button(
                            onClick = onDismiss,
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            shape = genre.bubble(isNarrator = true),
                        ) {
                            Text(
                                text = stringResource(R.string.cancel),
                                style =
                                    MaterialTheme.typography.labelLarge.copy(
                                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                    ),
                            )
                        }

                        Button(
                            onClick = onConfirm,
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError,
                                ),
                            shape = genre.bubble(isNarrator = true),
                        ) {
                            Text(
                                text = stringResource(R.string.delete),
                                style =
                                    MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                    ),
                            )
                        }
                    }
                }
            }
        }
    }
}
