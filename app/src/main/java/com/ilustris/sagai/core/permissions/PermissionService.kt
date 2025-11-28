package com.ilustris.sagai.core.permissions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class PermissionStatus {
    GRANTED,
    DENIED,
}

@Singleton
class PermissionService
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val permissionStates = mutableMapOf<String, MutableStateFlow<PermissionStatus>>()
        private val lifecycleObserver =
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    super.onStart(owner)
                    // When app starts/resumes, re-check all observed permissions
                    permissionStates.keys.forEach { permission ->
                        checkPermission(permission)
                    }
                }
            }

        init {
            ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
        }

        /**
         * Observe the status of a given permission.
         * The flow will automatically update when the app comes to the foreground.
         * @param permission The permission to observe (e.g., android.Manifest.permission.CAMERA)
         * @return A StateFlow emitting the current [PermissionStatus]
         */
        fun observePermission(permission: String): StateFlow<PermissionStatus> =
            permissionStates.getOrPut(permission) {
                MutableStateFlow(getPermissionStatus(permission))
            }

        /**
         * Manually forces a re-check of a permission's status.
         * This is useful to call after you've requested a permission and received the result.
         * @param permission The permission to check.
         */
        fun checkPermission(permission: String) {
            getPermissionStatus(permission).let { newStatus ->
                permissionStates[permission]?.value = newStatus
            }
        }

        private fun getPermissionStatus(permission: String): PermissionStatus =
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                PermissionStatus.GRANTED
            } else {
                PermissionStatus.DENIED
            }

        companion object {
            /**
             * A helper function to orchestrate the permission request flow from the UI.
             * It checks if a rationale should be shown and calls the appropriate lambda,
             * otherwise it launches the system permission dialog.
             *
             * This should be called from your Composable or Activity.
             *
             * @param activity The current Activity, needed to check for rationale.
             * @param permission The permission string to request.
             * @param permissionLauncher The ActivityResultLauncher to trigger the system dialog.
             * @param onShowRationale A lambda to be executed to show a rationale dialog. This dialog
             *                        should ideally guide the user to app settings.
             */
            fun requestPermission(
                activity: Activity?,
                permission: String?,
                permissionLauncher: ActivityResultLauncher<String>,
                onShowRationale: () -> Unit,
            ) {
                activity ?: return
                permission ?: return

                when {
                    ActivityCompat.shouldShowRequestPermissionRationale(activity, permission) -> {
                        onShowRationale()
                    }
                    else -> {
                        permissionLauncher.launch(permission)
                    }
                }
            }

            fun requestMultiplePermissions(
                activity: Activity,
                permissions: List<String>,
                permissionLauncher: ActivityResultLauncher<Array<String>>,
                onShowRationale: () -> Unit,
            ) {
                val showRationale =
                    permissions.any { permission ->
                        ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
                    }

                if (showRationale) {
                    onShowRationale()
                } else {
                    permissionLauncher.launch(permissions.toTypedArray())
                }
            }

            @Composable
            fun rememberBackupLauncher(onresult: (Uri?) -> Unit = {}) =
                rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree()) { uri ->
                    onresult(uri)
                }

            /**
             * Opens the application's settings screen for the user to manually manage permissions.
             * This is typically called from a rationale dialog.
             */
            fun openAppSettings(context: Context?) {
                context ?: return
                val intent =
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null),
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                context.startActivity(intent)
            }

            @Composable
            fun rememberPermissionLauncher(onResult: (isGranted: Boolean) -> Unit = {}): ActivityResultLauncher<String> =
                rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = onResult,
                )

            @Composable
            fun rememberMultiplePermissionLauncher(onResult: (Map<String, Boolean>) -> Unit = {}): ActivityResultLauncher<Array<String>> =
                rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions(),
                    onResult = onResult,
                )
        }
    }
