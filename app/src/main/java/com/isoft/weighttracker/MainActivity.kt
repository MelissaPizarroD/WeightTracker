package com.isoft.weighttracker

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.isoft.weighttracker.core.navigation.AppNavigation
import com.isoft.weighttracker.core.permissions.PermissionViewModel

class MainActivity : ComponentActivity() {

    private lateinit var permissionViewModel: PermissionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionViewModel = ViewModelProvider(this)[PermissionViewModel::class.java]

        setContent {
            com.isoft.weighttracker.ui.theme.WeightTrackerTheme {
                AppNavigation(
                    requestPermission = { permission ->
                        when (permission) {
                            Manifest.permission.POST_NOTIFICATIONS -> {
                                notificationPermissionLauncher.launch(permission)
                            }

                            Manifest.permission.ACTIVITY_RECOGNITION -> {
                                activityRecognitionLauncher.launch(permission)
                            }
                        }
                    },
                    permissionViewModel = permissionViewModel
                )
            }
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionViewModel.updateNotificationPermission(granted)
    }

    private val activityRecognitionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionViewModel.updateActivityRecognitionPermission(granted)
    }

    companion object {
        fun hasActivityRecognitionPermission(context: android.content.Context): Boolean {
            return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else true
        }

        fun hasNotificationPermission(context: android.content.Context): Boolean {
            return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else true
        }
    }
}