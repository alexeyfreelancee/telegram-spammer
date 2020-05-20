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
    private val clients = HashMap<String, Client>()
    override fun onBind(intent: Intent?): IBinder? = null
    private val STOP_SPAM = "com.example.telegramspam.services.STOP_SPAM"

    private fun generateIntent(phone: String): Intent {
        val stopIntent = Intent(this@SpammerService, SpammerService::class.java)
        stopIntent.action = "$STOP_SPAM@$phone"
        return stopIntent
    }

    private fun stop(action: String) {
        val phone = action.split("@")[1]
        clients[phone]?.close()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log("spammer service started")
        if (intent != null) {
            val action = intent.action
            if (action != null && action.startsWith(STOP_SPAM)) {
                stop(action)
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    val settings =
                        Gson().fromJson(intent.getStringExtra(SETTINGS), Settings::class.java)
                    val account =
                        Gson().fromJson(intent.getStringExtra(ACCOUNT), Account::class.java)
                    val telegram = TelegramClientUtil()
                    when (val result = telegram.createClient(account)) {
                        is ClientCreateResult.Success -> {
                            clients[account.phoneNumber] = result.client
                            startSpam(account.phoneNumber, settings, telegram, generateRandomInt())
                        }
                        is ClientCreateResult.Error -> {
                            sendNotification(
                                account.phoneNumber,
                                "Already spamming",
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
        phone: String,
        settings: Settings,
        telegram: TelegramClientUtil,
        notificationId: Int
    ) = CoroutineScope(Dispatchers.IO).launch {
        log(notificationId)
        val stopIntent = generateIntent(phone)

        startForeground(
            notificationId,
            createServiceNotification(phone, "Parsing users before spam...", stopIntent)
        )

        val usersChecked = HashSet<TdApi.User>()
        val chats = HashSet<TdApi.ChatTypeSupergroup>()

        settings.chats.split(",").forEach { link ->
            if (link.length > 3) {
                when (val result = telegram.getChat(clients[phone], link)) {
                    is GetChatResult.Success -> chats.add(result.chat)
                }

            }
        }

        val chatMembers = HashSet<TdApi.ChatMember>()

        chats.forEach { chat ->
            when (val result = telegram.getChatMembers(clients[phone], chat, 0)) {
                is GetChatMembersResult.Success -> {
                    var offset = 0
                    val count = result.chatMembers.totalCount / 200
                    for (i in 0..count) {
                        when (val res = telegram.getChatMembers(clients[phone], chat, offset)) {
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
            when(val result = telegram.getUser(clients[phone], member.userId)){
                is GetUserResult.Success ->{
                    usersNotChecked.add(result.user)
                }
            }

        }

        log("total users in chats ${usersNotChecked.size}")
        usersNotChecked.forEach { user ->
            if (telegram.checkUserBySettings(user, settings)) {
                usersChecked.add(user)
            }
        }

        log("users that passed settings test ${usersChecked.size}")
        var successCounter = 0
        var errorsCounter = 0
        for ((i, user) in usersChecked.withIndex()) {
            val success = telegram.prepareMessage(clients[phone], settings, user)
            if (success) {
                successCounter++
                sendNotificationService(
                    phone,
                    "Sent ${i + 1}/${usersChecked.size} messages",
                    notificationId, stopIntent
                )
                if (settings.block) {
                    telegram.blockUser(clients[phone], user.id)
                }
                val delay = settings.delay.toInt() * 1000
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

        clients[phone]?.close()
    }


}