package com.ilustris.sagai.core.permissions

import android.Manifest
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R

data class PermissionInfo(
    @StringRes val title: Int,
    @StringRes val description: Int,
    val emoji: String,
)

@Composable
private fun getPermissionInfo(permission: String): PermissionInfo =
    remember(permission) {
        when (permission) {
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE ->
                PermissionInfo(
                    title = R.string.storage_permission_title,
                    description = R.string.storage_permission_description,
                    emoji = "💾",
                )

            Manifest.permission.POST_NOTIFICATIONS ->
                PermissionInfo(
                    title = R.string.notifications_permission_title,
                    description = R.string.notifications_permission_description,
                    emoji = "🔔",
                )

            else ->
                PermissionInfo(
                    title = R.string.permission_required_title,
                    description = R.string.permission_required_description,
                    emoji = "🔒",
                )
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionComponent(
    requestedPermission: String? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
    ) {
        requestedPermission?.let { permission ->
            val permissionInfo = getPermissionInfo(permission)

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = permissionInfo.emoji,
                    style = MaterialTheme.typography.displayMedium,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(permissionInfo.title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(permissionInfo.description),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(text = stringResource(R.string.confirm_permission_button))
                }
            }
        }
    }
}
