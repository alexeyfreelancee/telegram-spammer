package com.example.telegramspam.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.telegramspam.models.Account

@Database(entities = [Account::class], version = 1)
abstract class AppDatabase(): RoomDatabase(){
    abstract fun accountsDao(): AccountsDao
}