package com.example.telegramspam.data.database

import androidx.room.Dao
import androidx.room.Insert
import com.example.telegramspam.models.User

@Dao
interface UsersDao {

    @Insert
    fun insert(user:User)
}