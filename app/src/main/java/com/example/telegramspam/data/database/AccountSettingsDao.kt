package com.example.telegramspam.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.telegramspam.models.AccountSettings

@Dao
interface AccountSettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(accountSettings:AccountSettings)

    @Query("SELECT * FROM settings_table WHERE dbPath LIKE :dbPath")
    suspend fun loadByPath(dbPath:String) : AccountSettings
}