package com.example.telegramspam.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inviter_settings_table")
data class InviterSettings(
    @PrimaryKey
    val id: String = "settings",
    val accounts: String= "",
    val chat: String= "",
    val delay: Int = 0,
    val inviteFromJson:String = "",
    val inviteFrom:String = ""
)