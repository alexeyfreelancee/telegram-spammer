package com.example.telegramspam.data

import com.example.telegramspam.data.database.AppDatabase
import com.example.telegramspam.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi

class TelegramAccountsHelper(private val db: AppDatabase) {
    private val clients = HashMap<String, Client>()

    fun init() {
        CoroutineScope(Dispatchers.IO).launch {
            val accounts = db.accountsDao().loadAll()
            accounts.forEach { account ->

                log("loaded account ${account.phoneNumber}")
                val client = Client.create(null, null, null)
                val params = generateParams(account.databasePath)
                client.send(TdApi.SetTdlibParameters(params), null)
                client.send(TdApi.CheckDatabaseEncryptionKey(), null)
                if (account.proxyIp.isNotEmpty() && account.proxyPort != 0) {
                    addProxy(
                        account.databasePath,
                        account.proxyIp,
                        account.proxyPort,
                        account.proxyUsername,
                        account.proxyPassword,
                        account.proxyType
                    )
                }
                clients[account.databasePath] = client
            }
        }
    }

    fun addProxy(
        databasePath: String,
        proxyIp: String,
        proxyPort: Int,
        proxyUsername: String,
        proxyPass: String,
        proxyType: String
    ) {
        val type = if (proxyType == SOCKS5) {
            log("socks 5 proxy added")
            TdApi.ProxyTypeSocks5(proxyUsername, proxyPass)
        } else {
            log("http proxy added")
            TdApi.ProxyTypeHttp(proxyUsername, proxyPass, false)
        }
        clients[databasePath]?.send(
            TdApi.AddProxy(
                proxyIp,
                proxyPort,
                true,
                type
            ), null
        )
    }

    fun finishAuthentication(
        smsCode: String,
        dbPath: String,
        listener: AuthorizationListener
    ) {
        clients[dbPath]?.send(TdApi.CheckAuthenticationCode(smsCode)) {
            if (it is TdApi.Error) {
                listener.error(it.message)
            }
        }
    }

    fun enterPhoneNumber(dbPath: String, phoneNumber: String, listener: AuthorizationListener) {
        clients[dbPath]?.send(TdApi.SetAuthenticationPhoneNumber(phoneNumber, null)) {
            if (it is TdApi.Error) {
                listener.error(it.message)
            } else {
                log("sms code send")
                listener.codeSend()
            }
        }
    }

    fun startAuthentication(
        dbPath: String,
        listener: AuthorizationListener
    ) {
        log("auth started")
        var client: Client? = null
        client = Client.create({ state ->
            when (state) {
                is TdApi.Error -> {
                    listener.error(state.message)
                    log(state.message)
                }
                is TdApi.UpdateAuthorizationState -> {
                    when (state.authorizationState.constructor) {
                        TdApi.AuthorizationStateReady.CONSTRUCTOR -> {
                            log("success auth")
                            client?.send(TdApi.GetMe()) { user ->
                                listener.success(user as TdApi.User)
                            }

                        }
                        TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR -> {
                            log("waiting params")
                            val params = generateParams(dbPath)
                            client?.send(TdApi.SetTdlibParameters(params)) {
                                if (it is TdApi.Error) {
                                    listener.error(it.message)
                                }
                            }
                        }
                        TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR -> {
                            log("waiting encryption key")
                            client?.send(TdApi.CheckDatabaseEncryptionKey()) {
                                if (it is TdApi.Error) {
                                    listener.error(it.message)
                                }
                            }
                        }
                    }
                }
            }
        }, null, null)
        clients[dbPath] = client


    }


    private fun generateParams(dbPath: String): TdApi.TdlibParameters {
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