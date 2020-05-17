package com.example.telegramspam.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.telegramspam.models.Account
import com.example.telegramspam.models.Settings

@Database(entities = [Account::class, Settings::class], version = 11)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountsDao(): AccountsDao
    abstract fun settingsDao(): SettingsDao
}