package com.example.telegramspam.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.telegramspam.models.JoinerSettings

@Dao
interface JoinerSettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings:JoinerSettings)

    @Query("SELECT * FROM joiner_settings_table WHERE id LIKE :id")
    suspend fun loadSettings(id:String):JoinerSettings?
}