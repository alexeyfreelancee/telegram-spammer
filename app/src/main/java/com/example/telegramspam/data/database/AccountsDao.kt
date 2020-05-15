package com.example.telegramspam.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.telegramspam.models.Account

@Dao
interface AccountsDao {

    @Query("SELECT * FROM accounts_table")
    fun loadAll() : LiveData<List<Account>>

    @Insert
    fun insert(account:Account)

    @Query("SELECT * FROM accounts_table WHERE id LIKE :id")
    suspend fun loadById(id: Long) : Account
}