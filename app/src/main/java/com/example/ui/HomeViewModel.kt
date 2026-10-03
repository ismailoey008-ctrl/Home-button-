package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.HomeCircleApplication
import com.example.data.model.CircleSettings
import com.example.service.FloatingHomeService
import com.example.service.HomeAccessibilityService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val settings: CircleSettings = CircleSettings(),
    val isOverlayPermissionGranted: Boolean = false,
    val isAccessibilityPermissionGranted: Boolean = false,
    val isServiceRunning: Boolean = false,
    val testTapCount: Int = 0
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as HomeCircleApplication).repository

    private val _permissionsState = MutableStateFlow(
        Pair(checkOverlayPermission(), checkAccessibilityPermission())
    )
    private val _testTapCount = MutableStateFlow(0)
    private val _serviceRunningState = MutableStateFlow(FloatingHomeService.isRunning)

    val uiState: StateFlow<HomeUiState> = combine(
        repository.settingsFlow,
        _permissionsState,
        _serviceRunningState,
        _testTapCount
    ) { settings, permissions, isRunning, testTaps ->
        HomeUiState(
            settings = settings,
            isOverlayPermissionGranted = permissions.first,
            isAccessibilityPermissionGranted = permissions.second,
            isServiceRunning = isRunning || FloatingHomeService.isRunning,
            testTapCount = testTaps
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(
            isOverlayPermissionGranted = checkOverlayPermission(),
            isAccessibilityPermissionGranted = checkAccessibilityPermission(),
            isServiceRunning = FloatingHomeService.isRunning
        )
    )

    fun refreshState() {
        val overlay = checkOverlayPermission()
        val accessibility = checkAccessibilityPermission()
        _permissionsState.value = Pair(overlay, accessibility)
        _serviceRunningState.value = FloatingHomeService.isRunning
    }

    private fun checkOverlayPermission(): Boolean {
        val context = getApplication<Application>()
        return Settings.canDrawOverlays(context)
    }

    private fun checkAccessibilityPermission(): Boolean {
        return HomeAccessibilityService.isServiceRunning()
    }

    fun toggleService(context: Context, shouldEnable: Boolean) {
        viewModelScope.launch {
            if (shouldEnable) {
                if (checkOverlayPermission()) {
                    repository.setEnabled(true)
                    FloatingHomeService.start(context)
                    _serviceRunningState.value = true
                }
            } else {
                repository.setEnabled(false)
                FloatingHomeService.stop(context)
                _serviceRunningState.value = false
            }
        }
    }

    fun updateOpacityActive(value: Float) {
        viewModelScope.launch {
            val current = uiState.value.settings
            repository.updateSettings(current.copy(opacityActive = value))
        }
    }

    fun updateOpacityIdle(value: Float) {
        viewModelScope.launch {
            val current = uiState.value.settings
            repository.updateSettings(current.copy(opacityIdle = value))
        }
    }

    fun updateAutoFade(enabled: Boolean) {
        viewModelScope.launch {
            val current = uiState.value.settings
            repository.updateSettings(current.copy(autoFade = enabled))
        }
    }

    fun updateSize(sizeDp: Int) {
        viewModelScope.launch {
            val current = uiState.value.settings
            repository.updateSettings(current.copy(sizeDp = sizeDp))
        }
    }

    fun updateColor(colorHex: Long) {
        viewModelScope.launch {
            val current = uiState.value.settings
            repository.updateSettings(current.copy(colorHex = colorHex))
        }
    }

    fun updateStyle(style: String) {
        viewModelScope.launch {
            val current = uiState.value.settings
            repository.updateSettings(current.copy(circleStyle = style))
        }
    }

    fun updateSnapToEdge(snap: Boolean) {
        viewModelScope.launch {
            val current = uiState.value.settings
            repository.updateSettings(current.copy(snapToEdge = snap))
        }
    }

    fun updateHaptic(haptic: Boolean) {
        viewModelScope.launch {
            val current = uiState.value.settings
            repository.updateSettings(current.copy(hapticEnabled = haptic))
        }
    }

    fun updateSingleTapAction(action: String) {
        viewModelScope.launch {
            val current = uiState.value.settings
            repository.updateSettings(current.copy(singleTapAction = action))
        }
    }

    fun updateDoubleTapAction(action: String) {
        viewModelScope.launch {
            val current = uiState.value.settings
            repository.updateSettings(current.copy(doubleTapAction = action))
        }
    }

    fun updateLongPressAction(action: String) {
        viewModelScope.launch {
            val current = uiState.value.settings
            repository.updateSettings(current.copy(longPressAction = action))
        }
    }

    fun triggerTestHome(context: Context) {
        _testTapCount.value += 1
        val handled = HomeAccessibilityService.performHome()
        if (!handled) {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            }
            context.startActivity(homeIntent)
        }
        viewModelScope.launch {
            repository.recordHomeClick()
        }
    }
}
