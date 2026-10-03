package com.example.data.model

data class CircleSettings(
    val isEnabled: Boolean = false,
    val opacityActive: Float = 0.85f,
    val opacityIdle: Float = 0.35f,
    val autoFade: Boolean = true,
    val sizeDp: Int = 56,
    val colorHex: Long = 0xFFFFFFFF, // Default clean white
    val circleStyle: String = STYLE_RING, // solid, ring, dot, double_ring
    val hapticEnabled: Boolean = true,
    val snapToEdge: Boolean = true,
    val singleTapAction: String = ACTION_HOME,
    val doubleTapAction: String = ACTION_NONE,
    val longPressAction: String = ACTION_OPEN_SETTINGS,
    val totalHomeClicks: Long = 0L,
    val posX: Int = 100,
    val posY: Int = 500
) {
    companion object {
        const val STYLE_SOLID = "solid"
        const val STYLE_RING = "ring"
        const val STYLE_DOT = "dot"
        const val STYLE_DOUBLE_RING = "double_ring"

        const val ACTION_NONE = "none"
        const val ACTION_HOME = "home"
        const val ACTION_RECENTS = "recents"
        const val ACTION_LOCK = "lock"
        const val ACTION_NOTIFICATIONS = "notifications"
        const val ACTION_OPEN_SETTINGS = "open_settings"
    }
}
