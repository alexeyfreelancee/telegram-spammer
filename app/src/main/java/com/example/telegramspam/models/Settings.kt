package com.example.telegramspam.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings_table")
data class Settings(
    @PrimaryKey
    var dbPath: String = "",
    var maxOnlineDifference: Long = 0,
    var havePhoto: Boolean = false,
    var hiddenStatus: Boolean = false,
    var chats:String = "",
    var message:String = "",
    var block:Boolean = false,
    var delay:String= "",
    var files:String = ""
)