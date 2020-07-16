package com.example.telegramspam.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.telegramspam.models.Account

@Dao
interface AccountsDao {
    @Query("SELECT * FROM accounts_table")
    suspend fun loadAll() : List<Account>
    
    @Query("SELECT * FROM accounts_table WHERE username LIKE :username")
    suspend fun loadByUsername(username:String):Account?

    @Query("SELECT * FROM accounts_table")
    fun loadAllAsync() : LiveData<List<Account>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account:Account)

    @Query("SELECT * FROM accounts_table WHERE databasePath LIKE :dbPath")
    suspend fun loadByPath(dbPath:String): Account

    @Query("DELETE FROM accounts_table WHERE id == :id")
    suspend fun delete(id:Int)

    @Query("SELECT * FROM accounts_table WHERE id == :id")
    suspend fun loadById(id: Int) : Account
}