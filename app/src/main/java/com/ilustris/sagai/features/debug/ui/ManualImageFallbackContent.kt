package com.ilustris.sagai.features.debug.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.debug.DebugImageFallbackService
import com.ilustris.sagai.core.file.readUriAsBitmap
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.morphingGradient
import com.ilustris.sagai.ui.theme.sagaBrush

private fun openGeminiWeb(context: Context) {
    val intent =
        Intent(Intent.ACTION_MAIN).apply {
            setClassName(
                "com.google.android.apps.bard",
                "com.google.android.apps.bard.shellapp.BardEntryPointActivity",
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    try {
        context.startActivity(Intent.createChooser(intent, null))
    } catch (_: ActivityNotFoundException) {
        Toast
            .makeText(
                context,
                R.string.debug_image_fallback_open_gemini_error,
                Toast.LENGTH_SHORT,
            ).show()
    }
}

@Composable
fun ManualImageFallbackContent(
    prompt: String,
    debugImageFallbackService: DebugImageFallbackService,
    onSubmitted: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    autoCopyPrompt: Boolean = true,
    scrollEnabled: Boolean = true,
    showHeader: Boolean = false,
    /** Leaner layout for the island's constrained expanded body: copy moves into the prompt
     * card's own header (as an icon) instead of a standalone button, and "Abrir Gemini" becomes
     * the sole full-width action in that row. */
    compact: Boolean = false,
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val photoPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
        ) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            val bitmap = context.readUriAsBitmap(uri)
            if (bitmap != null) {
                debugImageFallbackService.submitBitmap(bitmap)
                onSubmitted()
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

    LaunchedEffect(prompt, autoCopyPrompt) {
        if (autoCopyPrompt) copyPrompt()
    }

    val scrollState = rememberScrollState()
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .then(
                    if (scrollEnabled) {
                        Modifier.verticalScroll(scrollState)
                    } else {
                        Modifier
                    },
                ).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (showHeader) {
            Text(
                text = stringResource(R.string.debug_image_fallback_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        Text(
            text = stringResource(R.string.debug_image_fallback_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        ExpandablePromptCard(
            prompt = prompt,
            onCopy = if (compact) ::copyPrompt else null,
        )

        val geminiButton: @Composable (Modifier) -> Unit = { buttonModifier ->
            Button(
                onClick = { openGeminiWeb(context) },
                modifier =
                    buttonModifier
                        .clip(MaterialTheme.shapes.medium),
                shape = MaterialTheme.shapes.medium,
                colors =
                    ButtonDefaults.buttonColors().copy(
                        containerColor = Color.Transparent,
                    ),
            ) {
                Text(
                    text = stringResource(R.string.debug_image_fallback_open_gemini),
                    maxLines = 2,
                )
            }
        }

        val geminiBrush = sagaBrush()

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .dropShadow(MaterialTheme.shapes.medium) {
                        brush = geminiBrush
                        radius = 10f
                        spread = 1f
                    }.border(1.dp, sagaBrush(), MaterialTheme.shapes.medium)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .clickable {
                        openGeminiWeb(context)
                    }.padding(16.dp)
                    .gradientFill(geminiBrush),
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                painterResource(R.drawable.ic_spark),
                null,
                modifier = Modifier.size(24.dp).padding(horizontal = 8.dp),
                tint = MaterialTheme.colorScheme.onBackground,
            )

            Text(
                text = stringResource(R.string.debug_image_fallback_open_gemini),
                maxLines = 1,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        TextButton(
            onClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                text = stringResource(R.string.debug_image_fallback_pick_image),
                maxLines = 2,
            )
        }

        TextButton(
            onClick = {
                debugImageFallbackService.cancel()
                onCancel()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors =
                ButtonDefaults.textButtonColors().copy(
                    contentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = .4f),
                ),
        ) {
            Text(
                text = stringResource(R.string.image_generation_fallback_cancel),
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun ExpandablePromptCard(
    prompt: String,
    modifier: Modifier = Modifier,
    /** When non-null, an icon button that copies the prompt sits in the header — the compact
     * island layout's stand-in for the full "Copiar prompt" button. */
    onCopy: (() -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    val cardShape = RoundedCornerShape(16.dp)
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(cardShape)
                .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.4f))
                .border(width = 1.dp, color = borderColor, shape = cardShape)
                .clickable { expanded = !expanded }
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.debug_image_fallback_prompt_card_title),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onBackground,
            )
            onCopy?.let { copy ->
                IconButton(
                    onClick = copy,
                    modifier = Modifier.size(28.dp).clip(CircleShape),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_copy),
                        contentDescription = stringResource(R.string.debug_image_fallback_copy_prompt),
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
            val rotation by animateFloatAsState(
                targetValue = if (expanded) 90f else 0f,
                label = "promptCardChevron",
            )
            Icon(
                painter = painterResource(R.drawable.round_arrow_forward_ios_24),
                contentDescription = null,
                modifier =
                    Modifier
                        .padding(4.dp)
                        .rotate(rotation),
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }

        AnimatedVisibility(visible = expanded) {
            Text(
                text = prompt,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .verticalScroll(rememberScrollState())
                        .alpha(0.8f),
            )
        }
    }
}
