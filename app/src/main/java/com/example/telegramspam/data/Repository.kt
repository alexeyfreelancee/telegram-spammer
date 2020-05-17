package com.example.telegramspam.data

import android.os.Environment
import androidx.lifecycle.LiveData
import com.example.telegramspam.data.database.AppDatabase
import com.example.telegramspam.models.Account
import com.example.telegramspam.models.Settings
import com.example.telegramspam.utils.AuthorizationListener
import com.example.telegramspam.utils.UsersLoadingListener
import com.example.telegramspam.utils.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi
import java.util.concurrent.ThreadLocalRandom

//                                    val users = StringBuilder()
//                                    resultList.forEach { username ->
//                                        users.append("$username,")
//                                    }
//
//                                    listener.loaded(
//                                        users.toString().dropLast(1),
//                                        view
//                                    )

class Repository(private val db: AppDatabase, private val telegram: TelegramAccountsHelper) {

    fun loadAccountList(settings: Settings, listener: UsersLoadingListener) {
        log(settings.groups)
        val list = settings.groups.split(",")
        val resultList = ArrayList<String>()
        val client = telegram.getByDbPath(settings.dbPath)
        if (client != null) {
            list.forEach {
                client.send(TdApi.SearchPublicChat(it)) { chat ->
                    if (chat is TdApi.Chat && chat.type is TdApi.ChatTypeSupergroup) {
                        val type = chat.type
                        if (type is TdApi.ChatTypeSupergroup) {
                            parseUsersFromChat(client, type, settings, resultList)
                        }
                    }
                }
            }
        }
    }

    private fun parseUsersFromChat(
        client: Client,
        type: TdApi.ChatTypeSupergroup,
        settings: Settings,
        resultList: ArrayList<String>
    ) {
        client.send(TdApi.GetSupergroupMembers(type.supergroupId, null, 0, 200)) { members ->
            var offset = 0
            val count = (members as TdApi.ChatMembers).totalCount / 200

            for (i in 0..count) {
                client.send(
                    TdApi.GetSupergroupMembers(
                        type.supergroupId,
                        null,
                        offset,
                        200
                    )
                ) { result ->
                    log("iteration $i/$count")
                    log("loaded with offset $offset")
                    if (result is TdApi.ChatMembers) {
                        result.members.forEach {
                            client.send(TdApi.GetUser(it.userId)) { user ->
                                if (user is TdApi.User) {
                                    checkUserBySettings(settings, user, resultList)
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
        settings: Settings,
        user: TdApi.User,
        resultList: ArrayList<String>
    ) {
        val zero: Long = 0
        if (!user.username.isNullOrEmpty()) {
            if ((user.profilePhoto?.id != zero && settings.havePhoto) || (!settings.havePhoto && user.profilePhoto?.id == zero)) {
                val status = user.status
                val minOnline = System.currentTimeMillis() / 1000 - settings.maxOnlineDifference
                if (status.constructor == TdApi.UserStatusOnline.CONSTRUCTOR || (status.constructor == TdApi.UserStatusOffline.CONSTRUCTOR && minOnline <=  (status as TdApi.UserStatusOffline).wasOnline )) {
                    log("@${user.username} added to list")
                    resultList.add("@${user.username}")
                }

            }
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