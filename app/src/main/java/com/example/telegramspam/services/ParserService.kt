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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi

class ParserService : Service() {
    private val clients = HashMap<String, Client>()
    private val STOP_PARSE = "com.example.telegramspam.services.STOP_PARSE"

    private fun generateIntent(phone: String): Intent {
        val stopIntent = Intent(this@ParserService, ParserService::class.java)
        stopIntent.action = "$STOP_PARSE@$phone"
        return stopIntent
    }

    private fun stop(action: String) {
        val phone = action.split("@")[1]
        clients[phone]?.close()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action
            if (action != null && action.startsWith(STOP_PARSE)) {
                stop(action)
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    val account =
                        Gson().fromJson(intent.getStringExtra(ACCOUNT), Account::class.java)
                    val settings =
                        Gson().fromJson(intent.getStringExtra(SETTINGS), Settings::class.java)
                    val telegram = TelegramClientUtil()

                    when (val result = telegram.createClient(account)) {
                        is ClientCreateResult.Success -> {
                            clients[account.phoneNumber] = result.client
                            startParse(
                                account.phoneNumber,
                                settings,
                                telegram,
                                generateRandomInt()
                            )
                        }
                        is ClientCreateResult.Error -> sendNotification(
                            account.phoneNumber,
                            "Already parsing",
                            PARSER_ID
                        )
                    }
                }
            }


        }
        return START_NOT_STICKY
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    private fun startParse(
        phone: String,
        settings: Settings,
        telegram: TelegramClientUtil,
        notificationId: Int
    ) =
        CoroutineScope(Dispatchers.IO).launch {
            val stopIntent = generateIntent(phone)
            startForeground(
                notificationId,
                createServiceNotification(
                    phone,
                    "Started parsing users. It'll take some time to complete",
                    stopIntent
                )
            )
            val resultList = HashSet<String>()
            val chats = HashSet<TdApi.ChatTypeSupergroup>()

            settings.chats.split(",").forEach { link ->
                if (link.length > 3) {
                    when(val result = telegram.getChat(clients[phone], link)){
                       is GetChatResult.Success -> chats.add(result.chat)
                        is GetChatResult.Error -> log("error parsing chats")
                    }

                }
            }

            val chatMembers = HashSet<TdApi.ChatMember>()

            chats.forEach { chat ->
                when(val result =  telegram.getChatMembers(clients[phone], chat, 0)){
                    is GetChatMembersResult.Success ->{
                        val members = result.chatMembers
                        var offset = 0
                        val count = members.totalCount / 200
                        for (i in 0..count) {
                            when(val res = telegram.getChatMembers(clients[phone], chat, offset)){
                                is GetChatMembersResult.Success ->{
                                    res.chatMembers.members.forEach {
                                        chatMembers.add(it)
                                    }
                                }
                                is GetChatMembersResult.Error ->{
                                    log("error get chat members")
                                }
                            }

                            offset += 200
                        }
                    }
                }

            }


            val users = HashSet<TdApi.User>()
            chatMembers.forEach { member ->
                when(val result = telegram.getUser(clients[phone], member.userId)){
                    is GetUserResult.Success ->  users.add(result.user)
                    is GetUserResult.Error -> log("error get user")
                }

            }

            log("total users in chats ${users.size}")
            users.forEach { user ->
                if (telegram.checkUserBySettings(user, settings)) {
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
                copyToClipboard(result)
                sendNotification(
                    phone,
                    "Parsed ${resultList.size} users. List copied to clipboard",
                    notificationId
                )
                clients[phone]?.close()
            }


        }
}
