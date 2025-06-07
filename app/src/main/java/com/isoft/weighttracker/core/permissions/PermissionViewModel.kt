package com.isoft.weighttracker.core.permissions

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class PermissionState {
    NOT_REQUESTED,
    GRANTED,
    DENIED
}

class PermissionViewModel : ViewModel() {

    private val _notificationPermission = MutableStateFlow(PermissionState.NOT_REQUESTED)
    val notificationPermission: StateFlow<PermissionState> = _notificationPermission

    private val _activityRecognitionPermission = MutableStateFlow(PermissionState.NOT_REQUESTED)
    val activityRecognitionPermission: StateFlow<PermissionState> = _activityRecognitionPermission

    fun updateNotificationPermission(granted: Boolean) {
        _notificationPermission.value = if (granted) PermissionState.GRANTED else PermissionState.DENIED
    }

    fun updateActivityRecognitionPermission(granted: Boolean) {
        _activityRecognitionPermission.value = if (granted) PermissionState.GRANTED else PermissionState.DENIED
    }
}