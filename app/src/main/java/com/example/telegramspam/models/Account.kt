package com.example.telegramspam.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts_table")
data class Account(
    @PrimaryKey
    val id: Int = 0,
    val username: String = "",
    val phoneNumber: String = "",
    val proxyIp: String? = "",
    val proxyPort: Int? = 0,
    val databasePath:String = ""
)