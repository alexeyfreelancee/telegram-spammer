package com.example.telegramspam.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.telegramspam.ACCOUNT
import com.example.telegramspam.SETTINGS
import com.example.telegramspam.data.telegram.TelegramClientUtil
import com.example.telegramspam.models.*
import com.example.telegramspam.utils.*
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi

class SpammerService : Service() {
    private val clients = HashSet<String>()
    override fun onBind(intent: Intent?): IBinder? = null
    private val STOP_CURRENT = "com.example.telegramspam.services.STOP_CURRENT_SPAM"
    private val STOP_ALL = "com.example.telegramspam.services.STOP_ALL_SPAM"


    private fun generateStopCurrent(phone: String): Intent {
        val stopIntent = Intent(this@SpammerService, SpammerService::class.java)
        stopIntent.action = "$STOP_CURRENT@$phone"
        return stopIntent
    }

    private fun generateStopAll(): Intent {
        val stopIntent = Intent(this@SpammerService, SpammerService::class.java)
        stopIntent.action = STOP_ALL
        return stopIntent
    }

    private fun stopCurrent(action: String) {
        val phone = action.split("@")[1]
        TelegramClientUtil.stopClient(phone)
    }

    private fun stopAll() {
        stopSelf()
        clients.forEach {
            TelegramClientUtil.stopClient(it)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action
            if (action != null && action.startsWith(STOP_CURRENT)) {
                log("stop spamming")
                stopCurrent(action)
            } else if (action != null && action == STOP_ALL) {
                log("stop all")
                stopAll()
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    val settings =
                        Gson().fromJson(intent.getStringExtra(SETTINGS), AccountSettings::class.java)
                    val account =
                        Gson().fromJson(intent.getStringExtra(ACCOUNT), Account::class.java)

                    when (val result = TelegramClientUtil.provideClient(account)) {
                        is ClientCreateResult.Success -> {
                            startSpam(result.client,account.phoneNumber, settings, generateRandomInt())
                        }
                        is ClientCreateResult.Error -> {
                            sendNotification(
                                account.phoneNumber,
                                "Something went wrong. Try to restart the app",
                                SPAMMER_ID
                            )
                        }
                    }
                }
            }


        }
        return START_NOT_STICKY
    }


    private fun startSpam(
        client:Client,
        phone: String,
        accountSettings: AccountSettings,
        notificationId: Int
    ) = CoroutineScope(Dispatchers.IO).launch {
        clients.add(phone)


        val stopCurrentIntent = generateStopCurrent(phone)

        startForeground(
            SPAMMER_ID,
            createServiceNotification("Spammer is running", generateStopAll())
        )

        val usersChecked = HashSet<TdApi.User>()
        val chats = HashSet<TdApi.ChatTypeSupergroup>()

        accountSettings.chats.split(",").forEach { link ->
            if (link.length > 3) {
                when (val result = TelegramClientUtil.getChat(client, link)) {
                    is GetChatResult.Success -> chats.add(result.chat)
                }

            }
        }

        val chatMembers = HashSet<TdApi.ChatMember>()

        chats.forEach { chat ->
            when (val result = TelegramClientUtil.getChatMembers(client, chat, 0)) {
                is GetChatMembersResult.Success -> {
                    var offset = 0
                    val count = result.chatMembers.totalCount / 200
                    for (i in 0..count) {
                        when (val res = TelegramClientUtil.getChatMembers(client, chat, offset)) {
                            is GetChatMembersResult.Success -> {
                                res.chatMembers.members.forEach {
                                    chatMembers.add(it)
                                }
                            }
                        }

                        offset += 200
                    }
                }
            }

        }

        val usersNotChecked = HashSet<TdApi.User>()
        chatMembers.forEach { member ->
            when (val result = TelegramClientUtil.getUser(client, member.userId)) {
                is GetUserResult.Success -> {
                    usersNotChecked.add(result.user)
                }
            }

        }

        log("total users in chats ${usersNotChecked.size}")
        usersNotChecked.forEach { user ->
            if (TelegramClientUtil.checkUserBySettings(user, accountSettings)) {
                usersChecked.add(user)
            }
        }

        log("users that passed settings test ${usersChecked.size}")
        var successCounter = 0
        var errorsCounter = 0
        for ((i, user) in usersChecked.withIndex()) {
            val name = "${user.firstName} ${user.lastName}".trim()
            val photos = accountSettings.files.split(",").removeEmpty()
            val message = getRandomMessage(accountSettings.message, name)

            val success = TelegramClientUtil.sendMessage(client,photos,message,user.id)
            if (success) {
                successCounter++
                sendNotificationService(
                    phone,
                    "Sent ${i + 1}/${usersChecked.size} messages",
                    notificationId, stopCurrentIntent
                )
                if (accountSettings.block) {
                    TelegramClientUtil.blockUser(client, user.id)
                }
                val delay = accountSettings.delay.toInt() * 1000
                delay(delay.toLong())
            } else {
                errorsCounter++
            }
        }

        sendNotification(
            phone,
            "Spam finished. Success $successCounter, Errors $errorsCounter",
            notificationId
        )

        TelegramClientUtil.stopClient(phone)
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
}