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
import com.example.telegramspam.utils.*
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi

class ParserService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val account =
                    Gson().fromJson(intent.getStringExtra(ACCOUNT), Account::class.java)
                val settings =
                    Gson().fromJson(intent.getStringExtra(SETTINGS), Settings::class.java)
                val telegram = TelegramClientUtil()

                when (val result = telegram.createClient(account)) {
                    is ClientCreateResult.Success -> loadUsers(result.client, settings, telegram)
                    is ClientCreateResult.Error -> applicationContext.sendNotification(
                        "Telegram Spammer",
                        "Parsing already started",
                        ERROR_ID
                    )
                }
            }
        }
        return START_NOT_STICKY
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun loadUsers(client: Client, settings: Settings, telegram: TelegramClientUtil) =
        CoroutineScope(Dispatchers.IO).launch {
            applicationContext.sendNotification(
                "Telegram Spammer",
                "Started parsing users. It'll take some time to complete",
                PARSER_ID
            )
            val resultList = HashSet<String>()
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

            val users = HashSet<TdApi.User>()
            chatMembers.forEach { member ->
                users.add(telegram.getUser(client, member.userId))
            }

            log("total users in chats ${users.size}")
            users.forEach { user ->
                val fullInfo = telegram.getUserFullInfo(client, user)
                if (telegram.checkUserBySettings(user, settings, fullInfo)) {
                    resultList.add(user.username)
                }
            }
            log("users that passed settings test ${resultList.size}")

            val usersString = StringBuilder()
            resultList.forEach {
                usersString.append("@$it,")
            }

            withContext(Dispatchers.Main) {
                val result = usersString.toString().dropLast(1)
                applicationContext.apply {
                    copyToClipboard(result)
                    sendNotification(
                        "Telegram Spammer",
                        "Parsed ${resultList.size} users. List copied to clipboard",
                        PARSER_ID
                    )
                }
                client.close()
                stopSelf()
            }


        }
}