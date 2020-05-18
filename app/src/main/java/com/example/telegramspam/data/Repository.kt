package com.example.telegramspam.data

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.os.Handler
import androidx.lifecycle.LiveData
import com.example.telegramspam.data.database.AppDatabase
import com.example.telegramspam.models.Account
import com.example.telegramspam.models.Settings
import com.example.telegramspam.utils.AuthorizationListener
import com.example.telegramspam.utils.UsersLoadingListener
import com.example.telegramspam.utils.getPath
import com.example.telegramspam.utils.log
import kotlinx.coroutines.*
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi
import java.util.concurrent.ThreadLocalRandom


class Repository(private val db: AppDatabase, private val telegram: TelegramAccountsHelper) {

    fun removeFile(position: Int, settings: Settings?): String {
        return if (settings != null) {
            val files = ArrayList<String>()
            settings.files.split(",").forEach {
                if (it.length > 3) files.add(it)
            }
            val result = java.lang.StringBuilder()
            files.removeAt(position)

            files.forEach {
                result.append("$it,")
            }
            result.toString().dropLast(1)
        } else {
            ""
        }
    }

    fun checkSettings(settings: Settings?, beforeSpam: Boolean): Boolean {
        if (settings != null) {
            return if (beforeSpam) {
                val chats = settings.chats.split(",")
                val chatsOk = chats.isNotEmpty() && chats[0].startsWith("@") && chats[0].length > 1
                settings.delay.isNotEmpty() && settings.message.isNotEmpty() && chatsOk
            } else {
                val chats = settings.chats.split(",")
                return chats.isNotEmpty() && chats[0].startsWith("@") && chats[0].length > 1
            }
        }

        return false
    }

    fun loadFilePaths(data: Intent, context: Context): String {
        val pathList = ArrayList<String>()
        if (data.clipData != null) {
            for (i in 0 until data.clipData!!.itemCount) {
                pathList.add(data.clipData!!.getItemAt(i).uri.getPath(context))
            }
        } else if (data.data != null) {
            pathList.add(data.data!!.getPath(context))
        }
        val result = StringBuilder()
        pathList.forEach {
            if (it.isNotEmpty()) result.append("$it,")
        }
        log(result.toString().dropLast(1))
        return result.toString().dropLast(1)
    }


    fun loadAccountList(settings: Settings, listener: UsersLoadingListener) {
        val list = settings.chats.split(",")
        val client = telegram.getByDbPath(settings.dbPath)
        if (client != null) {
            list.forEach {
                if (it.length > 3) {
                    client.send(TdApi.SearchPublicChat(it)) { chat ->
                        if (chat is TdApi.Chat && chat.type is TdApi.ChatTypeSupergroup) {
                            val type = chat.type
                            if (type is TdApi.ChatTypeSupergroup) {
                                parseUsersFromChat(client, type, settings, listener)
                            }
                        } else if (chat is TdApi.Error) {
                            log(chat)
                        }
                    }
                }
            }
        }
    }

    private fun parseUsersFromChat(
        client: Client,
        type: TdApi.ChatTypeSupergroup,
        settings: Settings, listener: UsersLoadingListener
    ) {

        client.send(TdApi.GetSupergroupMembers(type.supergroupId, null, 0, 200)) { members ->
            if (members is TdApi.ChatMembers) {
                var offset = 0
                val count = members.totalCount / 200
                val resultList = HashSet<String>()
                for (i in 0..count) {
                    client.send(
                        TdApi.GetSupergroupMembers(
                            type.supergroupId,
                            null,
                            offset,
                            200
                        )
                    ) { result ->
                        if (result is TdApi.ChatMembers) {
                            result.members.forEach {
                                client.send(TdApi.GetUser(it.userId)) { user ->

                                    if (user is TdApi.User) {
                                        checkUserBySettings(
                                            client,
                                            settings,
                                            user,
                                            resultList,
                                            listener
                                        )
                                    }
                                }
                            }
                        }
                    }
                    offset += 200
                }
            }
        }
    }

    private fun checkUserBySettings(
        client: Client,
        settings: Settings,
        user: TdApi.User,
        resultList: HashSet<String>,
        listener: UsersLoadingListener
    ) {
        client.send(TdApi.GetUserFullInfo(user.id)) { fullInfo ->
            if (checkOnline(user.status, settings.maxOnlineDifference)) {
                resultList.add("@${user.username}")
                log("added ${resultList.size}")
                checkLoaded(resultList, listener)
            }
            if (fullInfo is TdApi.UserFullInfo && !user.username.isNullOrEmpty()) {
                if (user.profilePhoto != null == settings.havePhoto) {
                    if (settings.hiddenStatus == fullInfo.bio.isEmpty()) {

                    }
                }
            }
        }
    }


    private fun checkLoaded(resultList: HashSet<String>, listener: UsersLoadingListener) = CoroutineScope(Dispatchers.IO).launch{
        val beforeSize = resultList.size
        delay(10000)
        if (beforeSize == resultList.size) {
            val users = StringBuilder()
            resultList.forEach { username ->
                users.append("$username,")
            }
            if (resultList.isEmpty()) {
                listener.loaded("Ничего не найдено", false)
            } else {
                listener.loaded(users.toString().dropLast(1), true)
            }
        }
    }

    private fun checkOnline(status: TdApi.UserStatus, maxOnlineDifference: Long): Boolean {
        return when (status) {
            is TdApi.UserStatusOffline -> {
                val minOnline = System.currentTimeMillis() / 1000 - maxOnlineDifference - (60 * 5)
                return minOnline <= status.wasOnline
            }
            is TdApi.UserStatusLastMonth -> false
            else -> true
        }
    }

    suspend fun loadSettings(dbPath: String): Settings? {
        return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            db.settingsDao().loadByPath(dbPath)
        }
    }

    suspend fun saveSettings(settings: Settings) {
        CoroutineScope(Dispatchers.IO).launch {
            log("settings saved $settings")
            db.settingsDao().insert(settings)
        }
    }

    fun deleteAccount(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            db.accountsDao().delete(id)
        }
    }

    suspend fun loadAccount(accountId: Int): Account {
        return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            db.accountsDao().loadById(accountId)
        }
    }

    fun loadAccounts(): LiveData<List<Account>> = db.accountsDao().loadAllAsync()

    fun updateProxy(
        proxyIp: String,
        proxyPort: Int,
        username: String,
        pass: String,
        proxyType: String,
        databasePath: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val account = db.accountsDao().loadByPath(databasePath).apply {
                this.proxyIp = proxyIp
                this.proxyPort = proxyPort
                this.proxyUsername = username
                this.proxyPassword = pass
                this.proxyType = proxyType
            }
            log("updated proxy $proxyIp:$proxyPort  type = $proxyType")
            telegram.addProxy(databasePath, proxyIp, proxyPort, username, pass, proxyType)
            db.accountsDao().insert(account)
        }
    }

    fun createDatabasePath(): String {
        val id = ThreadLocalRandom.current().nextInt(Int.MIN_VALUE, Int.MAX_VALUE - 1)
        return "${Environment.getExternalStorageDirectory()}/telegram-spammer/user$id"
    }

    suspend fun checkNotExists(phoneNumber: String): Boolean {
        return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            db.accountsDao().loadAll().forEach {
                if (it.phoneNumber == phoneNumber) {
                    return@withContext true
                }
            }
            return@withContext false
        }

    }

    fun finishAuthentication(
        smsCode: String,
        dbPath: String,
        listener: AuthorizationListener
    ) {
        telegram.finishAuthentication(smsCode, dbPath, listener)
    }

    fun startAuthentication(
        dbPath: String,
        listener: AuthorizationListener
    ) {
        telegram.startAuthentication(dbPath, listener)
    }

    fun enterPhoneNumber(
        dbPath: String,
        phoneNumber: String,
        listener: AuthorizationListener
    ) {
        telegram.enterPhoneNumber(dbPath, phoneNumber, listener)
    }

    fun saveAccount(
        user: TdApi.User,
        proxyIp: String? = null,
        proxyPort: Int? = null,
        proxyUsername: String = "",
        proxyPassword: String = "",
        proxyType: String = "",
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
            telegram.addProxy(
                databasePath,
                proxyIp!!,
                proxyPort!!.toInt(),
                proxyUsername,
                proxyPassword,
                proxyType
            )
            Account(
                id = user.id,
                username = user.username,
                phoneNumber = user.phoneNumber,
                proxyIp = proxyIp,
                proxyPort = proxyPort,
                proxyType = proxyType,
                proxyUsername = proxyUsername,
                proxyPassword = proxyPassword,
                databasePath = databasePath
            )
        }
        CoroutineScope(Dispatchers.IO).launch {
            log("account saved", account)
            db.accountsDao().insert(account)
        }
    }
}