package com.example.telegramspam.data

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Environment
import androidx.lifecycle.LiveData
import com.example.telegramspam.data.database.AppDatabase
import com.example.telegramspam.data.telegram.AuthorizationListener
import com.example.telegramspam.data.telegram.TelegramAuthUtil
import com.example.telegramspam.data.telegram.TelegramClientUtil
import com.example.telegramspam.models.*
import com.example.telegramspam.utils.*
import com.example.telegramspam.utils.email_sender.GMailSender
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi
import java.io.File
import java.lang.Exception
import kotlin.coroutines.CoroutineContext


class Repository(
    private val db: AppDatabase,
    private val authUtil: TelegramAuthUtil,
    private val mediaPlayer: MediaPlayer,
    private val prefs: SharedPrefsHelper
) {
    suspend fun checkInviteFrom(inviteFrom: String): Boolean {
        val account = loadAccountByUsername(inviteFrom)
        return account != null
    }

    suspend fun loadInviterSettings(): InviterSettings? {
        return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            db.inviterSettingsDao().loadSettings()
        }
    }

    suspend fun provideAccountJson(username:String):String{
        return withContext(CoroutineScope(Dispatchers.IO).coroutineContext){
            val account = loadAccountByUsername(username)
             if(account == null) "" else Gson().toJson(account)
        }

    }
    fun saveInviterSettings(accounts: String, groups: String, delay: String, inviteFrom: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val delayInt = try {
                delay.toInt()
            } catch (ex: Exception) {
                0
            }
            val settings = InviterSettings(
                accounts = accounts,
                chat = groups,
                delay = delayInt,
                inviteFromJson = provideAccountJson(inviteFrom),
                inviteFrom = inviteFrom
            )
            db.inviterSettingsDao().saveSettings(settings)
        }
    }

    fun accountDeselectedJoiner(account: Account) {
        CoroutineScope(Dispatchers.IO).launch {
            account.joinerSelected = false
            db.accountsDao().insert(account)
        }

    }

    fun accountSelectedJoiner(account: Account) {
        CoroutineScope(Dispatchers.IO).launch {
            account.joinerSelected = true
            db.accountsDao().insert(account)
        }

    }

    suspend fun loadJoinerSettings(): JoinerSettings? {
        return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            log("${db.joinerSettingsDao().loadSettings("settings")}")
            db.joinerSettingsDao().loadSettings("settings")

        }

    }

    fun saveJoinerSettings(groups: String, accounts: String, delay: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val delayInt = try {
                delay.toInt()
            } catch (ex: Exception) {
                0
            }
            val settings =
                JoinerSettings(groups = groups, accounts = accounts, delay = delayInt)
            db.joinerSettingsDao().saveSettings(settings)
            log(settings)
        }

    }

    fun checkRegisterData(login: String, password: String): Boolean {
        val correctLogin = prefs.getLogin()
        val correctPassword = prefs.getPassword()

        return if (correctLogin == login && correctPassword == password) {
            prefs.setRegistered()
            true
        } else {
            false
        }
    }

    fun sendPasswordEmail(login: String) {
        val password = PasswordGenerator.PasswordGeneratorBuilder()
            .useDigits(true)
            .useLower(true)
            .useUpper(false)
            .usePunctuation(false)
            .build()
            .generate(8)
        prefs.savePassword(password)
        prefs.saveLogin(login)

        CoroutineScope(Dispatchers.IO).launch {
            GMailSender().sendMail(login, password)
        }

    }

    fun checkRegistered(): Boolean {
        return prefs.checkRegistered()
    }


    fun playVoice(file: File) {
        if (file.exists()) {
            mediaPlayer.apply {
                setDataSource(file.path)
                prepare()
                start()
            }
        }
    }

    suspend fun getVoice(voiceNote: TdApi.MessageVoiceNote): File? {
        return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            val fileId = voiceNote.voiceNote.voice.id
            TelegramClientUtil.downloadFile(fileId)
        }

    }

    suspend fun getChat(chatId: Long, accountId: Int): TdApi.Chat? {
        return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            val client = provideClient(accountId)
            val result = TelegramClientUtil.getChat(client, chatId)
            if (result is GetChat.Success) result.chat else null
        }
    }

    suspend fun getUser(userId: Long, accountId: Int): TdApi.User? {
        return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            val client = provideClient(accountId)
            val result = TelegramClientUtil.getUser(client, userId.toInt())
            if (result is GetUserResult.Success) result.user else null
        }
    }

    suspend fun provideClient(accountId: Int): Client? {
        return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            val account = loadAccount(accountId)
            val result = TelegramClientUtil.provideClient(account)
            if (result is ClientCreateResult.Success) result.client else null
        }

    }

    suspend fun sendMessage(
        text: String,
        chatId: Int,
        accountId: Int,
        photos: List<String>
    ): Boolean {
        return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            val client = provideClient(accountId)
            return@withContext TelegramClientUtil.sendMessage(client, photos, text, chatId)
        }
    }

    suspend fun loadMessages(
        chatId: Long,
        accountId: Int
    ): List<TdApi.Message> {
        return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            val client = provideClient(accountId)
            val chat = TelegramClientUtil.loadChat(client, chatId)
            if (chat is GetChatInfoResult.Success) {
                var fromMsgId: Long = 0
                val resultList = ArrayList<TdApi.Message>()
                while (resultList.size < 400) {
                    val getMessagesResult =
                        TelegramClientUtil.loadMessages(client, chatId, fromMsgId)
                    if (getMessagesResult is GetMessagesResult.Success) {
                        val messages = getMessagesResult.messages.messages
                        if (messages.isEmpty()) break
                        resultList.addAll(messages)
                        fromMsgId = messages.last().id
                    } else {
                        break
                    }
                }
                return@withContext resultList
            }
            emptyList<TdApi.Message>()
        }
    }

    suspend fun loadChats(accountId: Int): List<TdApi.Chat> {
        return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            val account = loadAccount(accountId)
            val client = TelegramClientUtil.provideClient(account)
            if (client is ClientCreateResult.Success) {
                val chats = TelegramClientUtil.loadChats(client.client)
                if (chats is GetChatsResult.Success) {
                    val resultList = ArrayList<TdApi.Chat>()
                    chats.chats.chatIds.forEach { chatId ->
                        val chat = TelegramClientUtil.loadChat(client.client, chatId)
                        if (chat is GetChatInfoResult.Success) {
                            resultList.add(chat.chat)
                        }
                    }
                    return@withContext resultList
                }
            }
            emptyList<TdApi.Chat>()

        }

    }

    fun removeFile(position: Int, files: String?): String {
        return if (files != null) {
            val first = files.toArrayList()
            first.removeAt(position)
            val result = java.lang.StringBuilder()
            first.forEach { result.append("$it,") }
            return result.toString().dropLast(1)
        } else ""

    }

    fun removeFile(position: Int, accountSettings: AccountSettings?): String {
        return if (accountSettings != null) {
            val files = ArrayList<String>()
            accountSettings.files.split(",").forEach {
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

    fun checkSettings(accountSettings: AccountSettings?, beforeSpam: Boolean): Boolean {
        if (accountSettings != null) {
            return if (beforeSpam) {
                val chats = accountSettings.chats.split(",").removeEmpty()
                accountSettings.delay.isNotEmpty() && accountSettings.message.isNotEmpty() && chats.isNotEmpty()
            } else {
                val chats = accountSettings.chats.split(",")
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

    suspend fun loadSettings(dbPath: String): AccountSettings? {
        return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            db.accountSettingsDao().loadByPath(dbPath)
        }
    }

    suspend fun saveSettings(accountSettings: AccountSettings) {
        CoroutineScope(Dispatchers.IO).launch {
            log("settings saved $accountSettings")
            db.accountSettingsDao().insert(accountSettings)
        }
    }

    fun deleteAccount(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            db.accountsDao().delete(id)
        }
    }

    suspend fun loadAccount(databasePath: String): Account {
        return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            db.accountsDao().loadByPath(databasePath)
        }
    }

    private suspend fun loadAccountByUsername(username: String): Account? {
        return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            db.accountsDao().loadByUsername(username.replace("@", ""))
        }
    }

    suspend fun loadAccount(accountId: Int): Account {
        return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            db.accountsDao().loadById(accountId)
        }
    }

    suspend fun loadAccounts(): List<Account> {
        return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
            db.accountsDao().loadAll()
        }
    }

    fun loadAccountsAsync(): LiveData<List<Account>> = db.accountsDao().loadAllAsync()

    fun updateProxy(
        proxyIp: String,
        proxyPort: Int,
        username: String,
        pass: String,
        proxyType: String,
        databasePath: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val account = db.accountsDao().loadByPath(databasePath)
            val sameProxy =
                account.proxyIp == proxyIp && account.proxyPort == proxyPort && account.proxyUsername == username && account.proxyPassword == pass && account.proxyType == proxyType
            val noProxy = proxyIp.isEmpty() || proxyPort == 0
            if (!sameProxy) {
                account.apply {
                    this.proxyIp = proxyIp
                    this.proxyPort = proxyPort
                    this.proxyUsername = username
                    this.proxyPassword = pass
                    this.proxyType = proxyType
                }


                val result = TelegramClientUtil.provideClient(account)
                if (result is ClientCreateResult.Success) {
                    TelegramClientUtil.disableProxy(result.client)

                    if (!noProxy) {
                        val proxyId = TelegramClientUtil.addProxy(result.client, account)
                        if (proxyId != null) {
                            account.proxyId = proxyId
                        }
                        log("updated proxy $proxyIp:$proxyPort  type = $proxyType")
                    } else {
                        log("removed proxy ${account.proxyId}")
                    }
                    result.client.close()
                    db.accountsDao().insert(account)
                }
            } else {
                log("tried to add same proxy")
            }

        }
    }

    fun createDatabasePath(): String {
        return "${Environment.getExternalStorageDirectory()}/telegram-spammer/account${generateRandomInt()}"
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
        proxyIp: String? = null,
        proxyPort: Int? = null,
        proxyUsername: String = "",
        proxyPassword: String = "",
        proxyType: String = "",
        listener: AuthorizationListener
    ) {
        authUtil.finishAuthentication(
            smsCode,
            proxyIp,
            proxyPort,
            proxyUsername,
            proxyPassword,
            proxyType,
            listener
        )
    }

    fun startAuthentication(
        dbPath: String,
        listener: AuthorizationListener
    ) {
        authUtil.startAuthentication(dbPath, listener)
    }

    fun enterPhoneNumber(
        phoneNumber: String,
        listener: AuthorizationListener
    ) {
        authUtil.enterPhoneNumber(phoneNumber, listener)
    }

    fun saveAccount(
        user: TdApi.User,
        proxyId: Int = 0,
        proxyIp: String? = null,
        proxyPort: Int? = null,
        proxyUsername: String = "",
        proxyPassword: String = "",
        proxyType: String = "",
        databasePath: String
    ) {

        val noProxy =
            proxyIp.isNullOrEmpty() || (proxyPort == null || proxyPort == 0) && proxyId == 0

        val account = if (noProxy) {
            log("no proxy")
            Account(
                id = user.id,
                username = user.username,
                phoneNumber = user.phoneNumber,
                databasePath = databasePath
            )

        } else {
            log("have proxy")
            Account(
                id = user.id,
                username = user.username,
                phoneNumber = user.phoneNumber,
                proxyId = proxyId,
                proxyIp = proxyIp!!,
                proxyPort = proxyPort!!,
                proxyType = proxyType,
                proxyUsername = proxyUsername,
                proxyPassword = proxyPassword,
                databasePath = databasePath
            )
        }


        CoroutineScope(Dispatchers.IO).launch {

            db.accountsDao().insert(account)

        }
    }
}