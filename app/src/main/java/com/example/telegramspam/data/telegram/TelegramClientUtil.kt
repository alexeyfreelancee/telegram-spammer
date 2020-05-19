package com.example.telegramspam.data.telegram

import com.example.telegramspam.API_HASH
import com.example.telegramspam.API_ID
import com.example.telegramspam.SOCKS5
import com.example.telegramspam.models.ClientCreateResult
import com.example.telegramspam.models.Settings
import com.example.telegramspam.utils.log
import kotlinx.coroutines.suspendCancellableCoroutine
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi

class TelegramClientUtil {

    suspend fun createClient(account: com.example.telegramspam.models.Account): ClientCreateResult {
        return suspendCancellableCoroutine { continuation ->
            var client: Client? = null
            client = Client.create({ state ->
                when (state) {
                    is TdApi.UpdateAuthorizationState -> {
                        when (state.authorizationState.constructor) {
                            TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR -> {
                                val params = generateParams(account.databasePath)
                                client?.send(TdApi.SetTdlibParameters(params)){
                                    if(it is TdApi.Error){
                                        continuation.resume(ClientCreateResult.Error(it.message)){}
                                    }
                                }
                            }
                            TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR -> {
                                client?.send(TdApi.CheckDatabaseEncryptionKey()){
                                    if(it is TdApi.Error){
                                        continuation.resume(ClientCreateResult.Error(it.message)){}
                                    }
                                }
                            }
                            TdApi.AuthorizationStateReady.CONSTRUCTOR -> {
                                if (client != null) {
                                    continuation.resume(ClientCreateResult.Success(client!!)) {}
                                }

                            }
                        }
                    }
                }
            }, null, null)

            if (account.proxyIp.isNotEmpty() && account.proxyPort != 0) {
                client.addProxy(
                    account.proxyIp,
                    account.proxyPort,
                    account.proxyUsername,
                    account.proxyPassword,
                    account.proxyType
                )
            }
        }


    }


    private fun Client.addProxy(
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
        this.send(
            TdApi.AddProxy(
                proxyIp,
                proxyPort,
                true,
                type
            ), null
        )

    }


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

    fun checkUserBySettings(
        user: TdApi.User, settings: Settings, fullInfo: TdApi.UserFullInfo
    ): Boolean {
        if (user.profilePhoto != null == settings.havePhoto) {
            if (settings.hiddenStatus == fullInfo.bio.isEmpty()) {
                if (checkOnline(user.status, settings.maxOnlineDifference)) {
                    return true
                }
            }
        }
        return false
    }

    private fun checkOnline(status: TdApi.UserStatus, maxOnlineDifference: Long): Boolean {
        return when (status) {
            is TdApi.UserStatusOffline -> {
                val minOnline = System.currentTimeMillis() / 1000 - maxOnlineDifference
                return minOnline <= status.wasOnline
            }
            is TdApi.UserStatusLastMonth -> false
            is TdApi.UserStatusRecently -> {
                val zero: Long = 0
                return maxOnlineDifference != zero
            }
            is TdApi.UserStatusOnline -> true
            else -> false
        }
    }

    suspend fun getChatMembers(
        client: Client,
        chat: TdApi.ChatTypeSupergroup,
        offset: Int
    ): TdApi.ChatMembers {
        return suspendCancellableCoroutine { continuation ->
            client.send(
                TdApi.GetSupergroupMembers(
                    chat.supergroupId,
                    null,
                    offset,
                    200
                )
            ) { members ->
                if (members is TdApi.ChatMembers) {
                    continuation.resume(members) {}
                } else {
                    log(members)
                }
            }
        }
    }

    fun sendMessage(client: Client, photos: List<String>, text: String, chatId: Long) {
        val caption = TdApi.FormattedText(text, null)
        if (photos.isEmpty()) {
            val content = TdApi.InputMessageText(caption, false, false)
            client.send(TdApi.SendMessage(chatId, 0, null, null, content)) {}
        } else {
            val contentList = ArrayList<TdApi.InputMessageContent>()
            photos.forEach { path ->
                if (path.length > 3) {
                    val photo = TdApi.InputFileLocal(path)
                    val content = TdApi.InputMessagePhoto(photo, null, null, 300, 300, null, 0)
                    contentList.add(content)
                }
            }
            client.send(
                TdApi.SendMessageAlbum(
                    chatId,
                    0,
                    null,
                    contentList.toArray() as Array<out TdApi.InputMessageContent>
                )
            ) {}
        }
    }

    suspend fun getChat(client: Client, link: String): TdApi.ChatTypeSupergroup {
        return suspendCancellableCoroutine { continuation ->
            client.send(TdApi.SearchPublicChat(link)) { chat ->
                if (chat is TdApi.Chat && chat.type is TdApi.ChatTypeSupergroup) {
                    val type = chat.type
                    if (type is TdApi.ChatTypeSupergroup) {
                        continuation.resume(type) {}
                    } else {
                        log(type)
                    }
                }
            }
        }
    }

    suspend fun getUserFullInfo(client: Client, user: TdApi.User): TdApi.UserFullInfo {
        return suspendCancellableCoroutine { continuation ->
            client.send(TdApi.GetUserFullInfo(user.id)) { fullInfo ->
                if (fullInfo is TdApi.UserFullInfo) {
                    continuation.resume(fullInfo) {}
                } else {
                    log(fullInfo)
                }
            }
        }
    }


    suspend fun getUser(client: Client, userId: Int): TdApi.User {
        return suspendCancellableCoroutine { continuation ->
            client.send(TdApi.GetUser(userId)) { user ->
                if (user is TdApi.User) {
                    continuation.resume(user) {}
                } else {
                    log(user)
                }
            }
        }
    }
}

fun Client.addProxy(
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
    this.send(
        TdApi.AddProxy(
            proxyIp,
            proxyPort,
            true,
            type
        ), null
    )

}