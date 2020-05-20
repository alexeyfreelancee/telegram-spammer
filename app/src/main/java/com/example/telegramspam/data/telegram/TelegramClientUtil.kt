package com.example.telegramspam.data.telegram

import com.example.telegramspam.API_HASH
import com.example.telegramspam.API_ID
import com.example.telegramspam.SOCKS5
import com.example.telegramspam.models.*
import com.example.telegramspam.utils.getRandom
import com.example.telegramspam.utils.log
import com.example.telegramspam.utils.removeEmpty
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi

class TelegramClientUtil {

    suspend fun createClient(account: Account): ClientCreateResult {
        return suspendCancellableCoroutine { continuation ->
            var client: Client? = null
            client = Client.create({ state ->
                when (state) {
                    is TdApi.UpdateAuthorizationState -> {
                        when (state.authorizationState.constructor) {
                            TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR -> {
                                val params = generateParams(account.databasePath)
                                client?.send(TdApi.SetTdlibParameters(params)) {
                                    if (it is TdApi.Error) {
                                        continuation.resume(ClientCreateResult.Error(it.message)) {}
                                    }
                                }
                            }
                            TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR -> {
                                client?.send(TdApi.CheckDatabaseEncryptionKey()) {
                                    if (it is TdApi.Error) {
                                        continuation.resume(ClientCreateResult.Error(it.message)) {}
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
            TdApi.ProxyTypeHttp(proxyUsername, proxyPass, true)
        }
        val function =  TdApi.AddProxy(proxyIp, proxyPort, true, type)
        this.send(function){
            log(it)
        }

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
        user: TdApi.User, settings: Settings
    ): Boolean {
        if (user.profilePhoto != null == settings.havePhoto) {
            if (checkOnline(user.status, settings.maxOnlineDifference)) {
                val emptyStatus = user.status.constructor == TdApi.UserStatusEmpty.CONSTRUCTOR
                if (emptyStatus == settings.hiddenStatus) {
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
            is TdApi.UserStatusRecently -> true
            is TdApi.UserStatusEmpty -> true
            is TdApi.UserStatusOnline -> true
            else -> false
        }
    }

    suspend fun getChatMembers(
        client: Client?,
        chat: TdApi.ChatTypeSupergroup,
        offset: Int
    ): GetChatMembersResult{
        return suspendCancellableCoroutine { continuation ->
            client?.send(
                TdApi.GetSupergroupMembers(
                    chat.supergroupId,
                    null,
                    offset,
                    200
                )
            ) { members ->
                if (members is TdApi.ChatMembers) {
                    continuation.resume(GetChatMembersResult.Success(members)) {}
                } else {
                    continuation.resume(GetChatMembersResult.Error()) {}
                    log(members)
                }
            }
        }
    }

    fun blockUser(client: Client?, userId: Int) {
        client?.send(TdApi.BlockUser(userId)){
            if(it is TdApi.Error){
                log(it)
            }
        }
    }

    suspend fun prepareMessage(
        client: Client?,
        settings: Settings,
        user: TdApi.User
    ) = suspendCancellableCoroutine<Boolean> { continuation ->
        if(client!=null){
            val photos = settings.files.split(",").removeEmpty()
            val name = "${user.firstName} ${user.lastName}".trim()
            val message = getRandomMessage(settings.message, name)
            val caption = TdApi.FormattedText(message, null)

            if (photos.isEmpty()) {
                val content = TdApi.InputMessageText(caption, false, false)
                val function =TdApi.SendMessage(user.id.toLong(), 0, null, null, content)
                sendMessage(client,continuation,function,user.id)
            } else {
                val function = TdApi.SendMessageAlbum(
                    user.id.toLong(),
                    0,
                    null,
                    createPhotos(caption,photos)
                )
                sendMessage(client, continuation, function, user.id)
            }
        }

    }

    private fun createPhotos(caption: TdApi.FormattedText, photos: List<String>) : Array<TdApi.InputMessageContent>{
        val paths = photos.removeEmpty()
        var contentList = emptyArray<TdApi.InputMessageContent>()

        for ((index, path) in paths.withIndex()) {
            val photo = TdApi.InputFileLocal(path)
            val content = if (index == 0) {
                TdApi.InputMessagePhoto(photo, null, null, 300, 300, caption, 0)
            } else {
                TdApi.InputMessagePhoto(photo, null, null, 300, 300, null, 0)
            }
            contentList += content

        }
        return contentList
    }

    private fun sendMessage(client: Client, continuation: CancellableContinuation<Boolean>, function: TdApi.Function, userId:Int){
        client.send(function) {
            if (it is TdApi.Error) {
                if(it.code == 5){
                    client.send(TdApi.CreatePrivateChat(userId, false)){result->
                        if(result is TdApi.Error){
                            continuation.resume(false){}
                        }else{
                            client.send(function){res ->
                                if(res is TdApi.Error){
                                    continuation.resume(false){}
                                } else{
                                    continuation.resume(true){}
                                }
                            }
                        }
                    }
                } else{
                    continuation.resume(false) {}
                }
            } else {
                continuation.resume(true) {}
            }

        }
    }

    private fun getRandomMessage(message: String, name: String): String {
        var result = message.replace("@name", name)
        val pattern =
            Regex("<\\w*\\|?\\w*\\|?\\w*\\|?\\w*\\|?\\w*\\|?\\w*\\|?\\w*\\|?\\w*\\|?\\w*\\|?\\w*\\|?>")
        pattern.findAll(message).forEach { resultMatch ->
            val words = resultMatch.value.split("[|<>]".toRegex()).removeEmpty()
            result = result.replace(resultMatch.value, words.getRandom())
        }
        return result
    }

    suspend fun getChat(client: Client?, link: String): GetChatResult {
        return suspendCancellableCoroutine { continuation ->
            client?.send(TdApi.SearchPublicChat(link)) { chat ->
                if (chat is TdApi.Chat && chat.type is TdApi.ChatTypeSupergroup) {
                    val type = chat.type
                    if (type is TdApi.ChatTypeSupergroup) {
                        continuation.resume(GetChatResult.Success(type)) {}
                    }
                } else {
                    continuation.resume(GetChatResult.Error()) {}
                    log(chat)
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


    suspend fun getUser(client: Client?, userId: Int): GetUserResult {
        return suspendCancellableCoroutine { continuation ->
            client?.send(TdApi.GetUser(userId)) { user ->
                if (user is TdApi.User) {
                    continuation.resume(GetUserResult.Success(user)) {}
                } else {
                    continuation.resume(GetUserResult.Error()) {}
                    log(user)
                }
            }
        }
    }
}

