package com.example.telegramspam.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.telegramspam.models.InviterSettings

@Dao
interface InviterSettingsDao {
    @Query("SELECT * FROM inviter_settings_table WHERE id LIKE :id")
    fun loadSettings(id:String = "settings"):InviterSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveSettings(settings:InviterSettings)
}