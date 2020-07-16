package com.example.telegramspam.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts_table")
data class Account(
    @PrimaryKey
    val id: Int = 0,
    val username: String = "",
    val phoneNumber: String = "",
    var proxyIp: String = "",
    var proxyPort: Int = 0,
    var proxyUsername:String = "",
    var proxyPassword:String = "",
    var proxyType:String ="",
    var databasePath:String = "",
    var proxyId: Int = 0,
    var joinerSelected:Boolean= false,
    var inviterSelected:Boolean= false
)