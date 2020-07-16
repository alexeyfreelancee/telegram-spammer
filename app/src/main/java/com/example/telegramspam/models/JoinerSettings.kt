package com.example.telegramspam.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "joiner_settings_table")
data class JoinerSettings(
    @PrimaryKey
    val id:String = "settings",
    val groups: String = "",
    val accounts: String = "",
    val delay: Int = 0
)