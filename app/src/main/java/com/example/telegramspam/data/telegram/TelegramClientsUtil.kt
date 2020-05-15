package com.example.telegramspam.data.telegram

import androidx.lifecycle.MutableLiveData
import com.example.telegramspam.data.database.AppDatabase
import com.example.telegramspam.utils.API_HASH
import com.example.telegramspam.utils.API_ID
import com.example.telegramspam.utils.Event
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi

class TelegramClientsUtil(private val db: AppDatabase) {
    private val clients = HashMap<String, Client>()

    fun init(){
        val accounts = db.accountsDao().loadAll()
        accounts.forEach { account ->
            val client = Client.create(null, null, null)
            val params = generateParams(account.databasePath)
            client.send(TdApi.SetTdlibParameters(params), null)
            client.send(TdApi.CheckDatabaseEncryptionKey(), null)
            clients[account.databasePath] = client
        }
    }


    fun sendSms(phoneNumber: String, dbPath: String) {
        clients[dbPath]?.send(TdApi.SetAuthenticationPhoneNumber(phoneNumber, null), null)
    }

    fun confirm(smsCode: String, dbPath: String) {
        clients[dbPath]?.send(TdApi.CheckAuthenticationCode(smsCode), null)
    }

    fun startAuthentication(
        dbPath: String,
        success: MutableLiveData<Event<TdApi.User>>,
        error: MutableLiveData<Event<String>>
    ) {
        var resultHandler: Client.ResultHandler? = null
        val client = Client.create(resultHandler, null, null)
        resultHandler = Client.ResultHandler { state ->
            when (state) {
                is TdApi.User -> success.value = Event(state)
                is TdApi.Error -> error.value = Event(state.message)
                is TdApi.UpdateAuthorizationState -> {
                    when (state.authorizationState.constructor) {
                        TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR -> {
                            val params = generateParams(dbPath)
                            client.send(TdApi.SetTdlibParameters(params), resultHandler)
                        }
                        TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR -> {
                            client.send(TdApi.CheckDatabaseEncryptionKey(), resultHandler)
                        }
                    }
                }
            }
        }
        clients[dbPath] = client
    }



    companion object {
        fun generateParams(dbPath: String): TdApi.TdlibParameters {
            return TdApi.TdlibParameters().apply {
                databaseDirectory = dbPath
                useMessageDatabase = true
                systemLanguageCode = "ru"
                useSecretChats = false
                apiId = API_ID
                apiHash = API_HASH
                deviceModel = "Desktop"
                systemVersion = "Unknown"
                applicationVersion = "1.0"
                enableStorageOptimizer = true
            }
        }
    }


}