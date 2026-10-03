package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.database.SettingsDao
import com.example.data.database.SettingsEntity
import com.example.data.model.CircleSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SettingsRepository(
    private val settingsDao: SettingsDao,
    context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("home_circle_quick_prefs", Context.MODE_PRIVATE)

    val settingsFlow: Flow<CircleSettings> = settingsDao.getSettingsFlow().map { entity ->
        entity?.toDomainModel() ?: CircleSettings().also { defaultSettings ->
            // Save initial defaults if database was empty
            CoroutineScope(Dispatchers.IO).launch {
                settingsDao.saveSettings(SettingsEntity.fromDomain(defaultSettings))
            }
        }
    }

    suspend fun getSettingsOnce(): CircleSettings {
        return settingsDao.getSettingsOnce()?.toDomainModel() ?: CircleSettings().also {
            settingsDao.saveSettings(SettingsEntity.fromDomain(it))
        }
    }

    suspend fun updateSettings(settings: CircleSettings) {
        settingsDao.saveSettings(SettingsEntity.fromDomain(settings))
        saveQuickPrefs(settings)
    }

    suspend fun setEnabled(enabled: Boolean) {
        settingsDao.updateEnabled(enabled)
        prefs.edit().putBoolean("is_enabled", enabled).apply()
    }

    suspend fun recordHomeClick() {
        settingsDao.incrementClickCount()
    }

    suspend fun updatePosition(x: Int, y: Int) {
        settingsDao.updatePosition(x, y)
        prefs.edit().putInt("pos_x", x).putInt("pos_y", y).apply()
    }

    private fun saveQuickPrefs(settings: CircleSettings) {
        prefs.edit()
            .putBoolean("is_enabled", settings.isEnabled)
            .putFloat("opacity_active", settings.opacityActive)
            .putFloat("opacity_idle", settings.opacityIdle)
            .putBoolean("auto_fade", settings.autoFade)
            .putInt("size_dp", settings.sizeDp)
            .putLong("color_hex", settings.colorHex)
            .putString("circle_style", settings.circleStyle)
            .putBoolean("haptic_enabled", settings.hapticEnabled)
            .putBoolean("snap_to_edge", settings.snapToEdge)
            .putString("single_tap_action", settings.singleTapAction)
            .putString("double_tap_action", settings.doubleTapAction)
            .putString("long_press_action", settings.longPressAction)
            .putInt("pos_x", settings.posX)
            .putInt("pos_y", settings.posY)
            .apply()
    }

    fun getQuickSettings(): CircleSettings {
        return CircleSettings(
            isEnabled = prefs.getBoolean("is_enabled", false),
            opacityActive = prefs.getFloat("opacity_active", 0.85f),
            opacityIdle = prefs.getFloat("opacity_idle", 0.35f),
            autoFade = prefs.getBoolean("auto_fade", true),
            sizeDp = prefs.getInt("size_dp", 56),
            colorHex = prefs.getLong("color_hex", 0xFFFFFFFF),
            circleStyle = prefs.getString("circle_style", CircleSettings.STYLE_RING)
                ?: CircleSettings.STYLE_RING,
            hapticEnabled = prefs.getBoolean("haptic_enabled", true),
            snapToEdge = prefs.getBoolean("snap_to_edge", true),
            singleTapAction = prefs.getString("single_tap_action", CircleSettings.ACTION_HOME)
                ?: CircleSettings.ACTION_HOME,
            doubleTapAction = prefs.getString("double_tap_action", CircleSettings.ACTION_NONE)
                ?: CircleSettings.ACTION_NONE,
            longPressAction = prefs.getString("long_press_action", CircleSettings.ACTION_OPEN_SETTINGS)
                ?: CircleSettings.ACTION_OPEN_SETTINGS,
            posX = prefs.getInt("pos_x", 100),
            posY = prefs.getInt("pos_y", 500)
        )
    }
}
