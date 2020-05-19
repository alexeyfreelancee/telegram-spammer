package com.example.telegramspam.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.telegramspam.ACCOUNT
import com.example.telegramspam.SETTINGS
import com.example.telegramspam.data.telegram.TelegramClientUtil
import com.example.telegramspam.models.Account
import com.example.telegramspam.models.ClientCreateResult
import com.example.telegramspam.models.Settings
import com.example.telegramspam.utils.SPAMMER_ID
import com.example.telegramspam.utils.generateRandomInt
import com.example.telegramspam.utils.log
import com.example.telegramspam.utils.sendNotification
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi

class SpammerService : Service() {
    private var client: Client? = null
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log("spammer service started")
        if (intent != null) {

            CoroutineScope(Dispatchers.IO).launch {
                val settings =
                    Gson().fromJson(intent.getStringExtra(SETTINGS), Settings::class.java)
                val account = Gson().fromJson(intent.getStringExtra(ACCOUNT), Account::class.java)
                val telegram = TelegramClientUtil()
                when (val result = telegram.createClient(account)) {
                    is ClientCreateResult.Success -> {
                        startSpam(settings, result.client, telegram, generateRandomInt())
                    }
                    is ClientCreateResult.Error -> {
                        applicationContext.sendNotification(
                            "Account +${account.phoneNumber} is already spamming",
                            SPAMMER_ID
                        )
                    }
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        log("destroy")
        client?.close()
    }

    private fun startSpam(
        settings: Settings,
        client: Client,
        telegram: TelegramClientUtil,
        id: Int
    ) = CoroutineScope(Dispatchers.IO).launch {
        this@SpammerService.client = client
        applicationContext.sendNotification(
            "Started parsing users...",
            id
        )
        val usersChecked = HashSet<TdApi.User>()
        val chats = HashSet<TdApi.ChatTypeSupergroup>()

        settings.chats.split(",").forEach { link ->
            if (link.length > 3) {
                chats.add(telegram.getChat(client, link))
            }
        }

        val chatMembers = HashSet<TdApi.ChatMember>()

        chats.forEach { chat ->
            val info = telegram.getChatMembers(client, chat, 0)
            var offset = 0
            val count = info.totalCount / 200
            for (i in 0..count) {
                telegram.getChatMembers(client, chat, offset).members.forEach {
                    chatMembers.add(it)
                }
                offset += 200
            }
        }

        val usersNotChecked = HashSet<TdApi.User>()
        chatMembers.forEach { member ->
            usersNotChecked.add(telegram.getUser(client, member.userId))
        }

        log("total users in chats ${usersNotChecked.size}")
        usersNotChecked.forEach { user ->
            if (telegram.checkUserBySettings(user, settings)) {
                usersChecked.add(user)
            }
        }
        log("users that passed settings test ${usersChecked.size}")

        applicationContext.sendNotification(
            "Parsed ${usersChecked.size} users",
            id
        )
        var successCounter = 0
        var errorsCounter = 0
        for ((i, user) in usersChecked.withIndex()) {
            val success = telegram.prepareMessage(client, settings, user)
            if (success) {
                successCounter++
                applicationContext.sendNotification(
                    "Sent ${i + 1}/${usersChecked.size} messages",
                    id
                )
                if (settings.block) {
                    telegram.blockUser(client, user.id)
                }
                val delay = settings.delay.toInt() * 1000
                delay(delay.toLong())
            } else {
                errorsCounter++
            }
        }

        applicationContext.sendNotification(
            "Spam finished. Success $successCounter Errors $errorsCounter",
            id
        )
        client.close()
        stopSelf()
    }



}