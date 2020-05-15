package com.example.telegramspam.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts_table")
data class Account(
    @PrimaryKey
    val id:Long = 0,
    val username: String = "",
    val phoneNumber:String = "")