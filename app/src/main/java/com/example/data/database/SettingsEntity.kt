package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.CircleSettings

@Entity(tableName = "circle_settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val isEnabled: Boolean = false,
    val opacityActive: Float = 0.85f,
    val opacityIdle: Float = 0.35f,
    val autoFade: Boolean = true,
    val sizeDp: Int = 56,
    val colorHex: Long = 0xFFFFFFFF,
    val circleStyle: String = CircleSettings.STYLE_RING,
    val hapticEnabled: Boolean = true,
    val snapToEdge: Boolean = true,
    val singleTapAction: String = CircleSettings.ACTION_HOME,
    val doubleTapAction: String = CircleSettings.ACTION_NONE,
    val longPressAction: String = CircleSettings.ACTION_OPEN_SETTINGS,
    val totalHomeClicks: Long = 0L,
    val posX: Int = 100,
    val posY: Int = 500
) {
    fun toDomainModel(): CircleSettings {
        return CircleSettings(
            isEnabled = isEnabled,
            opacityActive = opacityActive,
            opacityIdle = opacityIdle,
            autoFade = autoFade,
            sizeDp = sizeDp,
            colorHex = colorHex,
            circleStyle = circleStyle,
            hapticEnabled = hapticEnabled,
            snapToEdge = snapToEdge,
            singleTapAction = singleTapAction,
            doubleTapAction = doubleTapAction,
            longPressAction = longPressAction,
            totalHomeClicks = totalHomeClicks,
            posX = posX,
            posY = posY
        )
    }

    companion object {
        fun fromDomain(settings: CircleSettings): SettingsEntity {
            return SettingsEntity(
                id = 1,
                isEnabled = settings.isEnabled,
                opacityActive = settings.opacityActive,
                opacityIdle = settings.opacityIdle,
                autoFade = settings.autoFade,
                sizeDp = settings.sizeDp,
                colorHex = settings.colorHex,
                circleStyle = settings.circleStyle,
                hapticEnabled = settings.hapticEnabled,
                snapToEdge = settings.snapToEdge,
                singleTapAction = settings.singleTapAction,
                doubleTapAction = settings.doubleTapAction,
                longPressAction = settings.longPressAction,
                totalHomeClicks = settings.totalHomeClicks,
                posX = settings.posX,
                posY = settings.posY
            )
        }
    }
}
