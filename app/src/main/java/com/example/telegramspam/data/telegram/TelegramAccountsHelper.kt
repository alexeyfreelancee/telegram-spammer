package com.example.telegramspam.data.telegram

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
            Client.execute(TdApi.SetLogVerbosityLevel(0))
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


    suspend fun sendMessage(client: Client) {
        val path1 = "/storage/emulated/0/VK/Downloads/OeQ9POJ_Vlk.jpg"
        val path2 = "/storage/emulated/0/VK/Photos/1589675415479.jpg"

        val photo = TdApi.InputFileLocal(path1)
        val photo1 = TdApi.InputFileLocal(path2)

        val text = TdApi.FormattedText("plivite sosiski", null)
        val text1 = TdApi.FormattedText("sosiski pliviski", null)

        val content = TdApi.InputMessagePhoto(photo, null, null, 300, 300, null, 0)
        val content1 = TdApi.InputMessagePhoto(photo1, null,null, 300,300,text1,0)

        client.send(TdApi.SendMessageAlbum(698697860, 0, null,  arrayOf(content, content1))){

        }

    }

    fun getByDbPath(dbPath: String): Client? {
        return clients[dbPath]
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
            TdApi.ProxyTypeSocks5(proxyUsername, proxyPass)
        } else {
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