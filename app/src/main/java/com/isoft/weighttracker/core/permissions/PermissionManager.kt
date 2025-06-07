package com.isoft.weighttracker.core.permissions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

class PermissionManager(private val activity: ComponentActivity) {

    private var currentCallback: ((Boolean) -> Unit)? = null

    private val notificationLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        currentCallback?.invoke(granted)
    }

    private val activityRecognitionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        currentCallback?.invoke(granted)
    }

    fun requestNotificationPermission(onResult: (Boolean) -> Unit) {
        currentCallback = onResult
        val permission = Manifest.permission.POST_NOTIFICATIONS

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
        ) {
            onResult(true)
        } else {
            notificationLauncher.launch(permission)
        }
    }

    fun requestActivityRecognitionPermission(onResult: (Boolean) -> Unit) {
        val permission = Manifest.permission.ACTIVITY_RECOGNITION

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
        ) {
            onResult(true)
        } else {
            // No uses AlertDialog.Builder, solo lanza el permiso directamente
            activityRecognitionLauncher.launch(permission)
        }
    }

}