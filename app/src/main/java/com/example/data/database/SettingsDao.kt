package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {

    @Query("SELECT * FROM circle_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<SettingsEntity?>

    @Query("SELECT * FROM circle_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsOnce(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(entity: SettingsEntity)

    @Query("UPDATE circle_settings SET isEnabled = :enabled WHERE id = 1")
    suspend fun updateEnabled(enabled: Boolean)

    @Query("UPDATE circle_settings SET opacityActive = :opacityActive, opacityIdle = :opacityIdle WHERE id = 1")
    suspend fun updateOpacity(opacityActive: Float, opacityIdle: Float)

    @Query("UPDATE circle_settings SET sizeDp = :sizeDp WHERE id = 1")
    suspend fun updateSize(sizeDp: Int)

    @Query("UPDATE circle_settings SET colorHex = :colorHex WHERE id = 1")
    suspend fun updateColor(colorHex: Long)

    @Query("UPDATE circle_settings SET circleStyle = :style WHERE id = 1")
    suspend fun updateStyle(style: String)

    @Query("UPDATE circle_settings SET totalHomeClicks = totalHomeClicks + 1 WHERE id = 1")
    suspend fun incrementClickCount()

    @Query("UPDATE circle_settings SET posX = :x, posY = :y WHERE id = 1")
    suspend fun updatePosition(x: Int, y: Int)
}
