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
    private val clients = HashSet<String>()
    private val STOP_CURRENT = "com.example.telegramspam.services.STOP_CURRENT_PARSE"
    private val STOP_ALL = "com.example.telegramspam.services.STOP_ALL_PARSE"

    private fun generateStopCurrent(phone: String, notificationId: Int): Intent {
        val stopIntent = Intent(this@ParserService, ParserService::class.java)
        stopIntent.action = "$STOP_CURRENT@$phone@$notificationId"
        return stopIntent
    }

    private fun generateStopAll(): Intent {
        val stopIntent = Intent(this@ParserService, ParserService::class.java)
        stopIntent.action =STOP_ALL
        return stopIntent
    }

    private fun stop(action: String) {
        val phone = action.split("@")[1]
        TelegramClientUtil.stopClient(phone)
    }

    private fun stopAll(){
        stopSelf()
        clients.forEach {
            TelegramClientUtil.stopClient(it)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action
            if (action != null && action.startsWith(STOP_CURRENT)) {
                log("stop current parsing")
                stop(action)
            }else if(action!=null && action == STOP_ALL){
                log("stop all parsing")
                stopAll()
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    val account =
                        Gson().fromJson(intent.getStringExtra(ACCOUNT), Account::class.java)
                    val settings =
                        Gson().fromJson(intent.getStringExtra(SETTINGS), AccountSettings::class.java)


                    when (val result = TelegramClientUtil.provideClient(account)) {
                        is ClientCreateResult.Success -> {
                            startParse(
                                account.phoneNumber,
                                settings,
                                generateRandomInt(),
                                result.client
                            )
                        }
                        is ClientCreateResult.Error -> sendNotification(
                            account.phoneNumber,
                            "Something went wrong. Try to restart the app",
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
        accountSettings: AccountSettings,
        notificationId: Int,
        client:Client
    ) = CoroutineScope(Dispatchers.IO).launch {

            clients.add(phone)
            val stopCurrent = generateStopCurrent(phone, notificationId)
            startForeground(
                PARSER_ID,
                createServiceNotification(
                    "Parser is running",
                    generateStopAll()
                )
            )

            sendNotificationService(
                phone,
                "Started parsing users. It'll take some time to complete",
                notificationId, stopCurrent
            )
            val resultList = HashSet<String>()
            val chats = HashSet<TdApi.ChatTypeSupergroup>()


            accountSettings.chats.split(",").forEach { link ->
                if (link.length > 3) {
                    when(val result = TelegramClientUtil.getChat(client, link)){
                        is GetChatResult.Success -> chats.add(result.chat)
                        is GetChatResult.Error -> log("error parsing chats")
                    }

                }
            }


            val chatMembers = HashSet<TdApi.ChatMember>()

            chats.forEach { chat ->
                when(val result =  TelegramClientUtil.getChatMembers(client, chat, 0)){
                    is GetChatMembersResult.Success ->{
                        val members = result.chatMembers
                        var offset = 0
                        val count = members.totalCount / 200
                        for (i in 0..count) {
                            when(val res = TelegramClientUtil.getChatMembers(client, chat, offset)){
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
                when(val result = TelegramClientUtil.getUser(client, member.userId)){
                    is GetUserResult.Success ->  users.add(result.user)
                    is GetUserResult.Error -> log("error get user")
                }

            }

            log("total users in chats ${users.size}")
            users.forEach { user ->
                if (TelegramClientUtil.checkUserBySettings(user, accountSettings)) {
                    log(user.username)
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

            }

        TelegramClientUtil.stopClient(phone)
        }
}
