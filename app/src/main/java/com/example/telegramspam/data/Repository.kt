package com.example.telegramspam.data

import android.os.Environment
import androidx.lifecycle.LiveData
import com.example.telegramspam.data.database.AppDatabase
import com.example.telegramspam.models.Account
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.drinkless.td.libcore.telegram.TdApi

class Repository(private val db: AppDatabase) {

    fun loadAccounts(): LiveData<List<Account>> = db.accountsDao().loadAllAsync()


    fun createDatabasePath(): String {
        return "${Environment.getExternalStorageDirectory()}/telegram-spammer/user${Math.random()
            .toInt()}"
    }

    fun saveUser(
        user: TdApi.User,
        proxyIp: String? = null,
        proxyPort: Int? = null,
        databasePath: String
    ) {
        val account = if (proxyIp.isNullOrEmpty() && proxyPort == null) {
            Account(
                id = user.id,
                username = user.username,
                phoneNumber = user.phoneNumber,
                databasePath = databasePath
            )
        } else {
            Account(
                id = user.id,
                username = user.username,
                phoneNumber = user.phoneNumber,
                proxyIp = proxyIp,
                proxyPort = proxyPort,
                databasePath = databasePath
            )
        }
        CoroutineScope(Dispatchers.IO).launch {
            db.accountsDao().insert(account)
        }
    }
}