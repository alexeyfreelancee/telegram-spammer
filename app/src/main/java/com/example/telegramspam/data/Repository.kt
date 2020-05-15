package com.example.telegramspam.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.telegramspam.data.database.AppDatabase
import com.example.telegramspam.models.Account
import com.example.telegramspam.utils.Event

class Repository(private val db: AppDatabase) {

    fun loadAccounts(): LiveData<List<Account>> = db.accountsDao().loadAll()


    fun addAccount(
        phone: String?,
        code: String?,
        proxyPort: String? = null,
        proxyIp: String? = null,
        success: MutableLiveData<Event<Boolean>>
    ) {
        //call set code method and save params to db
    }

    fun startAuth(phone: String?) {
        //create new client
    }
}