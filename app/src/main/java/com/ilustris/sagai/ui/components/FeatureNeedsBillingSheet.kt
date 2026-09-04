package com.ilustris.sagai.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.ui.theme.SagAITheme

/**
 * Explains that the user's Google project tier, not their key, is what blocked this.
 *
 * Worth its own sheet because the alternative is actively misleading: the API answers a free-tier
 * image request with a bare 403 or a zero quota, which the generic handling would report as "your
 * key was rejected" or "daily limit reached". The first would send the user off to mint a
 * replacement key that fails identically; the second promises a reset that never comes. Neither is
 * true, and the real remedy — enabling billing on their Google Cloud project — appears in neither.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureNeedsBillingSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current

    SagAITheme(null) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                )
            },
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.feature_needs_billing_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.size(8.dp))

                Text(
                    text = stringResource(R.string.feature_needs_billing_message),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Thin,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.size(8.dp))

                TextButton(
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(AI_STUDIO_RATE_LIMIT_URL)),
                        )
                    },
                ) {
                    Text(stringResource(R.string.api_key_learn_more))
                }

                Spacer(Modifier.size(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(stringResource(R.string.guardrail_dismiss))
                }
            }
        }
    }
}
