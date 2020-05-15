package com.example.telegramspam.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings_table")
data class Settings(
    var maxOnlineDifference: Long = 0,
    @PrimaryKey
    var dbPath: String = "",
    var havePhoto: Boolean = false,
    var hiddenStatus: Boolean = false,
    var groups:String = ""
)