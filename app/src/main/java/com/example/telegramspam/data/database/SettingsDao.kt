package com.example.telegramspam.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.telegramspam.models.Settings

@Dao
interface SettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings:Settings)

    @Query("SELECT * FROM settings_table WHERE dbPath LIKE :dbPath")
    suspend fun loadByPath(dbPath:String) : Settings
}