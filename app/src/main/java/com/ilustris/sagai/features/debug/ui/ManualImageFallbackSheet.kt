package com.ilustris.sagai.features.debug.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.debug.DebugImageFallbackService
import com.ilustris.sagai.core.file.readUriAsBitmap

private const val GEMINI_PACKAGE = "com.google.android.apps.bard"
private const val GEMINI_WEB_URL = "https://gemini.google.com/app"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualImageFallbackSheet(
    prompt: String,
    debugImageFallbackService: DebugImageFallbackService,
    onDismiss: () -> Unit,
) {
    if (!BuildConfig.DEBUG) return

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var promptExpanded by remember { mutableStateOf(false) }

    val photoPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
        ) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            val bitmap = context.readUriAsBitmap(uri)
            if (bitmap != null) {
                debugImageFallbackService.submitBitmap(bitmap)
                onDismiss()
            } else {
                Toast
                    .makeText(
                        context,
                        R.string.debug_image_fallback_pick_error,
                        Toast.LENGTH_SHORT,
                    ).show()
            }
        }

    fun copyPrompt() {
        clipboardManager.setText(AnnotatedString(prompt))
        Toast
            .makeText(context, R.string.debug_image_fallback_prompt_copied, Toast.LENGTH_SHORT)
            .show()
    }

    LaunchedEffect(prompt) {
        copyPrompt()
    }

    ModalBottomSheet(
        onDismissRequest = {
            debugImageFallbackService.cancel()
            onDismiss()
        },
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
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.debug_image_fallback_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = stringResource(R.string.debug_image_fallback_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = prompt,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (promptExpanded) Int.MAX_VALUE else 6,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = if (promptExpanded) 320.dp else 120.dp),
            )

            OutlinedButton(
                onClick = { promptExpanded = !promptExpanded },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    if (promptExpanded) {
                        stringResource(R.string.debug_image_fallback_collapse_prompt)
                    } else {
                        stringResource(R.string.debug_image_fallback_expand_prompt)
                    },
                )
            }

            OutlinedButton(
                onClick = { copyPrompt() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(stringResource(R.string.debug_image_fallback_copy_prompt))
            }

            OutlinedButton(
                onClick = {
                    val launchIntent =
                        context.packageManager.getLaunchIntentForPackage(GEMINI_PACKAGE)
                    if (launchIntent != null) {
                        context.startActivity(launchIntent)
                    } else {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GEMINI_WEB_URL)))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(stringResource(R.string.debug_image_fallback_open_gemini))
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(stringResource(R.string.debug_image_fallback_pick_image))
            }
        }
    }
}
